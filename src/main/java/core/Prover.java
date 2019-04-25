package core;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class with static functions that can prove an EquationSystem
 *
 * @author Stan Roelofs
 * @version 1.0
 */
public class Prover {

    public static String constantName = "a";
    public static int maxDepth = 2;
    private static boolean rewriteRight = true;
    private static boolean rewriteLeft = false;

    public static boolean induction(EquationSystem system, OutputWriter outputWriter, int searchSteps, int recursionDepth, Variable inductionVar) throws IOException {
        if (recursionDepth >= maxDepth) {
            Logger.i("Reached maximum recursion depth of " + maxDepth + ", returning false");
            outputWriter.writeLine("Maximum recursion depth " + maxDepth + " reached, induction on " + inductionVar.toString() + " failed.");
            return false;
        }

        Equation goal = system.getGoal();
        //core.Util.writeLine(outputWriter, "Goal: " + goal.toString());

        // Try induction for each variable in function
        Set<Variable> allVariables = new HashSet<>(goal.getLeft().getVariables());

        allVariables.addAll(goal.getRight().getVariables());

        if (inductionVar == null) {
            for (Variable variable : allVariables) {
                Logger.i("Induction on " + variable.toString());
                outputWriter.writeLine("Trying induction variable: " + variable.toString());
                if (induction(system, outputWriter, searchSteps, recursionDepth, variable)) {
                    return true;
                }
            }

            Logger.i("Induction on all variables failed, returning false");
            outputWriter.writeLine("Induction on all variables failed.");
            return false;
        }

        // Check whether induction variable sort is the same as the sort of C
        if (!inductionVar.getSort().equals(system.getCSort())) {
            Logger.i("Skipping induction variable " + inductionVar.toString() + ", invalid sort");
            outputWriter.writeLine("Skipping variable " + inductionVar.toString() + ", sort does not match sort of C");
            return false;
        }

        // For each function in C
        for (Function function : system.getC()) {

            List<Term> newConstants = new ArrayList<>();
            List<Equation> hypotheses = new ArrayList<>();

            generateHypotheses(function, inductionVar, system, newConstants, hypotheses);

            // Create term f(a1, ..., an)
            FunctionTerm inductionTerm = new FunctionTerm(function, newConstants);

            Equation newGoal = goal.substitute(inductionVar, inductionTerm);

            outputWriter.writeLine("To prove: " + newGoal.toString());

            if (hypotheses.size() > 0) {
                StringBuilder sb = new StringBuilder();
                for (Equation hypothesis : hypotheses) {
                    sb.append(hypothesis.toString());
                    sb.append(" ");
                }
                outputWriter.writeLine("Hypotheses: " + sb.toString());
            }

            // Do BFS for convertibility
            Set<Term> leftTerms = new HashSet<>();
            Set<Term> rightTerms = new HashSet<>();

            leftTerms.add(newGoal.getLeft());
            rightTerms.add(newGoal.getRight());

            Set<Equation> allEquations = new HashSet<>(system.getEquations());
            allEquations.addAll(hypotheses);

            Map<Term, Term> leftSteps = new ConcurrentHashMap<>();
            Map<Term, Term> rightSteps = new ConcurrentHashMap<>();

            // Maximum of 4 threads
            int numThreads = Math.min(4, allEquations.size());

            // Calculate the number of equations each thread should get
            int[] numEquations = new int[numThreads];
            for (int i = 0; i < allEquations.size(); i++) {
                numEquations[i % numThreads]++;
            }

            // Create the set of equations for each thread
            ArrayList<Set<Equation>> threadEquations = new ArrayList<>(numThreads);
            Iterator<Equation> it = allEquations.iterator();
            for (int i = 0; i < numThreads; i++) {
                Set<Equation> tmp = new HashSet<>();
                threadEquations.add(tmp);

                for (int j = 0; j < numEquations[i]; j++) {
                    tmp.add(it.next());
                }
            }

            int searchDepth = 0;
            Term convergence = null;
            while (convergence == null && searchDepth < searchSteps) {
                Logger.i("Rewriting step " + searchDepth + ", using " + numThreads + " threads");

                RewriteThread[] rewriteThreads = new RewriteThread[numThreads];
                Thread[] threads = new Thread[numThreads];
                for (int i = 0; i < numThreads; i++) {
                    rewriteThreads[i] = new RewriteThread(i, leftTerms, rightTerms, threadEquations.get(i), leftSteps, rightSteps);
                    threads[i] = new Thread(rewriteThreads[i]);
                    threads[i].start();
                }

                for (int i = 0; i < numThreads; i++) {
                    try {
                        threads[i].join();
                    } catch (InterruptedException e) {
                        Logger.e("Thread " + i + " interrupted " + e.getMessage());
                    }
                }

                for (int i = 0; i < numThreads; i++) {
                    leftTerms.addAll(rewriteThreads[i].getResultLeft());
                    rightTerms.addAll(rewriteThreads[i].getResultRight());
                }
                searchDepth++;
                convergence = checkConvergence(leftTerms, rightTerms);
            }

            //core.Term convergence = checkConvergence(leftTerms, rightTerms);
            if (convergence == null) {
                Logger.w("Convergence null");

                // TODO: for now only double induction for s
                if (!function.getName().equals("s") || function.getInputSorts().size() != 1) {
                    Logger.w("Skipping double induction");
                    outputWriter.writeLine("Failed to prove " + newGoal.toString() + " induction on " + inductionVar.toString() + " failed.");
                    return false;
                }

                outputWriter.writeLine("Trying double induction");

                // Create term f
                List<Term> subterms = new ArrayList<>();
                subterms.add(inductionVar);
                Term newInductionTerm = new FunctionTerm(function, subterms);

                Equation newGoalll = system.getGoal().substitute(inductionVar, newInductionTerm);
                EquationSystem newSystem = new EquationSystem(allEquations, system.getSigma(), system.getC(), newGoalll);
                return induction(newSystem, outputWriter, searchSteps, recursionDepth + 1, inductionVar);
            } else {
                List<Term> conversion = getConversionSequence(newGoal.getLeft(), newGoal.getRight(), convergence, leftSteps, rightSteps);
                String conversionString = getConversionString(conversion);
                outputWriter.writeLine(conversionString);
            }
        }
        return true;
    }

    /**
     * modifies newconstants, hypotheses
     */
    private static void generateHypotheses(Function function, Variable inductionVar, EquationSystem system, List<Term> newConstants, List<Equation> hypotheses) {
        int count = 1;

        // Add hypotheses
        for (Sort s : function.getInputSorts()) {
            Function cnst = new Function(s, constantName + count);

            for (Function f : system.getSigma()) {
                if (f.equals(cnst)) {
                    Logger.e("Fresh constant " + cnst.toString() + " already exists, possibly introduces errors");
                }
            }

            FunctionTerm a = new FunctionTerm(cnst, new ArrayList<>());
            newConstants.add(a);

            if (s == function.getOutputSort()) {
                Equation hypothesis = system.getGoal().substitute(inductionVar, a);
                hypotheses.add(hypothesis);
            }
        }
    }

    /**
     * Returns a list of terms that represents the conversion steps that were used in the conversion from the term
     * {@code initial} to the term {@code goal}.
     * This conversion is found using a Breadth-First Search starting from both {@code initial} and {@code goal}
     * and a set of equations, which therefore creates two sets of terms, terms that can be found by rewriting
     * {@code initial} and terms that can be found by rewriting {@code goal}.
     *
     * The map {@code leftSteps} stores for each term which term was used to create it, for all terms found by
     * rewriting {@code initial}. This allows us to trace back the steps that were used for the conversion.
     * {@code rightSteps} does the same for terms created by rewriting {@code goal}.
     *
     * When a term is found that is common to both sets of terms, there exists a conversion from {@code initial} to
     * {@code goal}. There can be multiple common terms, {@code convergence} is one of those common terms.
     *
     * @param initial The initial term where the conversion starts
     * @param goal The final term where the conversion ends
     * @param convergence A term that both {@code initial} and {@code goal} can be rewritten to
     * @param leftSteps A map that links each term to the term that was used to create it, from {@code convergence} to {@code initial}
     * @param rightSteps A map that links each term to the term that was used to create it, from {@code convergence} to {@code goal}
     * @return A list of terms which represents the conversion steps that were used to rewrite {@code initial} to {@code goal}
     * @see Term
     */
    private static List<Term> getConversionSequence(Term initial, Term goal, Term convergence, Map<Term,Term> leftSteps, Map<Term,Term> rightSteps) {
        if (initial == null || goal == null || convergence == null || leftSteps == null || rightSteps == null) {
            throw new IllegalArgumentException("Arguments cannot be null");
        }
        List<Term> revSequence = new ArrayList<>();

        Term temp =  null;
        // Trace back the steps until we reach the start term
        revSequence.add(convergence);
        while (!(initial.equals(temp))) {
            if (temp == null) {
                if (!leftSteps.containsKey(convergence)) {
                    break;
                }
                temp = leftSteps.get(convergence);
            } else {
                temp = leftSteps.get(temp);
                if (temp == null) {
                    throw new IllegalStateException("Missing term in leftSteps");
                }
            }
            revSequence.add(temp);
        }

        // Reverse this sequence, as it starts from convergence and ends in initial
        List<Term> sequence = new ArrayList<>();
        // Reverse sequence
        for (int i = revSequence.size() - 1; i >= 0; i--) {
            sequence.add(revSequence.get(i));
        }

        temp =  null;
        // Trace back the steps until we reach the final term
        while (!(goal.equals(temp))) {
            if (temp == null) {
                if (!rightSteps.containsKey(convergence)) {
                    break;
                }
                temp = rightSteps.get(convergence);
            } else {
                temp = rightSteps.get(temp);
                if (temp == null) {
                    throw new IllegalStateException("Missing term in rightSteps");
                }
            }
            sequence.add(temp);
        }

        return sequence;
    }

    private static String getConversionString(List<Term> conversion) {
        // Turn the sequence into a string using a stringbuilder for efficiency
        StringBuilder stepsString = new StringBuilder();
        stepsString.append("Conversion: \n");
        for (int i = 0; i < conversion.size(); i++) {
            stepsString.append(conversion.get(i));

            if (i < conversion.size() - 1) {
                stepsString.append(" = \n");
            }
        }

        return stepsString.toString();
    }

    /*
     * BFS on terms with allEquations
     *
     * Rewrite all terms and subterms in terms using the equations in allEquations
     *
     * Returns a set of terms created as a result of this procedure
     */
    public static Set<Term> rewriteAll(Set<Term> terms, Set<Equation> allEquations, Map<Term, Term> steps) {
        Set<Term> toAdd = new HashSet<>();

        for (Term term : terms) {
            for (Equation eq : allEquations) {
                for (Term subterm : term.getAllSubTerms()) {
                    // Try left
                    if (rewriteRight) {
                        // Try left
                        Map<Variable, Term> sub = eq.getLeft().getSubstitution(subterm, new HashMap<>());

                        if (sub != null) {
                            Term rewriteGoal = eq.getRight();
                            Term newT = rewriteTerm(steps, term, subterm, sub, rewriteGoal);
                            toAdd.add(newT);
                        }
                    }

                    if (rewriteLeft) {
                        // Try right
                        Map<Variable, Term> sub2 = eq.getRight().getSubstitution(subterm, new HashMap<>());

                        if (sub2 != null) {
                            Term newt = eq.getLeft();
                            toAdd.add(rewriteTerm(steps, term, subterm, sub2, newt));
                        }
                    }
                }
            }
        }

        return toAdd;
    }

    /*
     * Substitutes all variables in newt by the term sub.variable
     *
     * term: whole term
     * subterm: a subterm of term that can be rewritten to subtermSubstitute
     * sub: the substitutions required to do this
     * newTerm: the term obtained
     */
    private static Term rewriteTerm(Map<Term, Term> steps, Term term, Term subterm, Map<Variable, Term> sub, Term subtermSubstitute) {
        subtermSubstitute = subtermSubstitute.applySubstitution(sub);

        // Create the new term by substituting subterm in term by subtermSubstitute
        Term newTerm = term.substitute(subterm, subtermSubstitute);

        // Add this step to steps
        if (!steps.containsKey(newTerm)) {
            steps.put(newTerm, term);
        }

        return newTerm;
    }

    private static Term checkConvergence(Set<Term> left, Set<Term> right) {
        Set<Term> intersection = new HashSet<>(left);
        intersection.retainAll(right);

        Logger.d("Checking convergence");


        Logger.d("Left:");
        for (Term t : left) {
            if (t.toString().contains("head(zip(inv(B), inv(B)))")) {
                Logger.e(t.toString());
            }
        }
        /*
        Logger.d("Right:");
        for (Term t : right) {
            //  core.Logger.d(t.toString());
        }*/

        Logger.d("Intersection:");
        for (Term t : intersection) {
            Logger.d(t.toString());
        }

        if (!intersection.isEmpty()) {
            Logger.i("Found conversion");
        }

        if (intersection.size() > 1) {
            Logger.w("Intersection size > 1, choosing the first term");
        }

        return intersection.isEmpty() ? null : intersection.iterator().next();
    }
}