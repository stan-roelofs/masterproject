package core;

import core.io.OutputWriter;
import core.logging.Logger;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class with static functions that can prove an EquationSystem
 *
 *
 * @author Stan Roelofs
 * @version 1.2
 */
public class Prover {
    // TODO: This class is a bit of a mess, especially lemma search code

    private static String constantName = "a";
    private static int maxDepth = 2;

    private static boolean convertible(EquationSystem system, OutputWriter outputWriter, int searchSteps, boolean rewriteLeft) throws IOException {
        Map<Term, Term> leftSteps = new ConcurrentHashMap<>();
        Map<Term, Term> rightSteps = new ConcurrentHashMap<>();


        outputWriter.writeLine("Seeking conversion between " + system.getGoal().getLeft().toString() + " and " + system.getGoal().getRight().toString());

        Term conversion = findConversion(system, outputWriter, searchSteps, rewriteLeft, leftSteps, rightSteps);

        outputWriter.writeLine("Conversion not found");

        return conversion != null;
    }

    private static Term findConversion(EquationSystem system, OutputWriter outputWriter, int searchSteps, boolean rewriteLeft, Map<Term, Term> leftSteps, Map<Term, Term> rightSteps) throws IOException {
        // Do BFS for convertibility
        Set<Term> leftTerms = new HashSet<>();
        Set<Term> rightTerms = new HashSet<>();

        Equation goal = system.getGoal();

        leftTerms.add(goal.getLeft());
        rightTerms.add(goal.getRight());

        // Maximum of 4 threads
        int numThreads = Math.min(4, system.getEquations().size());

        // Calculate the number of equations each thread should get
        int[] numEquations = new int[numThreads];
        for (int i = 0; i < system.getEquations().size(); i++) {
            numEquations[i % numThreads]++;
        }

        // Create the set of equations for each thread
        ArrayList<Set<Equation>> threadEquations = new ArrayList<>(numThreads);
        Iterator<Equation> it = system.getEquations().iterator();
        for (int i = 0; i < numThreads; i++) {
            Set<Equation> tmp = new HashSet<>();
            threadEquations.add(tmp);

            for (int j = 0; j < numEquations[i]; j++) {
                tmp.add(it.next());
            }
        }

        int searchDepth = 0;
        Term convergence = null;

        Set<Term> leftAdded = new HashSet<>(leftTerms);
        Set<Term> rightAdded = new HashSet<>(rightTerms);
        while (convergence == null && searchDepth < searchSteps) {
            Logger.d("Rewriting step " + searchDepth + ", using " + numThreads + " threads");

            RewriteThread[] rewriteThreads = new RewriteThread[numThreads];
            Thread[] threads = new Thread[numThreads];
            for (int i = 0; i < numThreads; i++) {
                rewriteThreads[i] = new RewriteThread(i, leftAdded, rightAdded, threadEquations.get(i), leftSteps, rightSteps, true, rewriteLeft);
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

            leftAdded.clear();
            rightAdded.clear();
            for (int i = 0; i < numThreads; i++) {
                leftAdded.addAll(rewriteThreads[i].getResultLeft());
                rightAdded.addAll(rewriteThreads[i].getResultRight());
            }

            leftTerms.addAll(leftAdded);
            rightTerms.addAll(rightAdded);

            searchDepth++;
            convergence = checkConvergence(leftTerms, rightTerms);
        }

        return convergence;
    }

    public static boolean induction(EquationSystem system, OutputWriter outputWriter, int searchSteps, boolean rewriteLeft, int recursionDepth, Variable inductionVar) throws IOException {
        EquationSystem sys = new EquationSystem(system);

        if (recursionDepth >= maxDepth) {
            Logger.i("Reached maximum recursion depth of " + maxDepth + ", returning false");
            outputWriter.writeLine("Maximum recursion depth " + maxDepth + " reached, induction on " + inductionVar.toString() + " failed.");
            return false;
        }

        Equation goal = sys.getGoal();

        if (recursionDepth == 0 && inductionVar == null) {
            Logger.i("Proving " + goal.toString() + " with induction");
            outputWriter.writeLine("Goal to prove with induction: " + goal.toString());
        }

        // Try induction for each variable in function
        Set<Variable> allVariables = new HashSet<>(goal.getLeft().getVariables());

        allVariables.addAll(goal.getRight().getVariables());

        if (inductionVar == null) {
            for (Variable variable : allVariables) {
                Logger.i("Induction on " + variable.toString());
                outputWriter.writeLine("Trying induction variable: " + variable.toString());
                if (induction(sys, outputWriter, searchSteps, rewriteLeft, recursionDepth, variable)) {
                    return true;
                }
            }

            Logger.i("Induction on all variables failed, returning false");
            outputWriter.writeLine("Induction on all variables failed.");
            return false;
        }

        // Check whether induction variable sort is the same as the sort of C
        if (!inductionVar.getSort().equals(sys.getCSort())) {
            Logger.i("Skipping induction variable " + inductionVar.toString() + ", invalid sort");
            outputWriter.writeLine("Skipping variable " + inductionVar.toString() + ", sort does not match sort of C");
            return false;
        }

        // For each function in C
        for (Function function : sys.getC()) {

            List<Term> newConstants = new ArrayList<>();
            List<Equation> hypotheses = new ArrayList<>();

            generateHypotheses(function, inductionVar, sys, newConstants, hypotheses);

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
                outputWriter.writeLine("Added hypotheses: " + sb.toString());
            }

            Map<Term, Term> leftSteps = new ConcurrentHashMap<>();
            Map<Term, Term> rightSteps = new ConcurrentHashMap<>();

            Set<Equation> allEquations = new HashSet<>(sys.getEquations());
            allEquations.addAll(hypotheses);

            EquationSystem temp = new EquationSystem(allEquations, sys.getSigma(), sys.getC(), newGoal);

            Term convergence = findConversion(temp, outputWriter, searchSteps, rewriteLeft, leftSteps, rightSteps);

            if (convergence == null) {
                Logger.d("Convergence null");

                Function successor = null;
                for (Function f : sys.getC()) {
                    if (f.getName().equals("s") && f.getInputSorts().size() == 1) {
                        successor = f;
                    }
                }
                if (successor == null || !successor.getOutputSort().equals(inductionVar.getSort())) {
                    Logger.d("Skipping double induction");
                    outputWriter.writeLine("Failed to prove " + newGoal.toString() + " induction on " + inductionVar.toString() + " failed.");
                    return false;
                }

                outputWriter.writeLine("Trying double induction");

                // Create term f
                List<Term> subterms = new ArrayList<>();
                subterms.add(inductionVar);
                Term newInductionTerm = new FunctionTerm(successor, subterms);

                Equation newGoalll = sys.getGoal().substitute(inductionVar, newInductionTerm);
                EquationSystem newSystem = new EquationSystem(allEquations, sys.getSigma(), sys.getC(), newGoalll);
                return induction(newSystem, outputWriter, searchSteps, rewriteLeft, recursionDepth + 1, inductionVar);
            } else {
                List<Term> conversion = getConversionSequence(newGoal.getLeft(), newGoal.getRight(), convergence, leftSteps, rightSteps);
                String conversionString = getConversionString(conversion);
                outputWriter.writeLine(conversionString);
            }
        }
        return true;
    }

    /*
     * modifies newconstants, hypotheses
     */
    private static void generateHypotheses(Function function, Variable inductionVar, EquationSystem system, List<Term> newConstants, List<Equation> hypotheses) {
        int count = 1;

        // Add hypotheses
        for (Sort s : function.getInputSorts()) {
            Function cnst = new Function(s, constantName + count);
            system.getSigma().add(cnst);

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
            count++;
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
    static Set<Term> rewriteAll(Set<Term> terms, Set<Equation> allEquations, Map<Term, Term> steps, boolean rewriteRight, boolean rewriteLeft) {
        Set<Term> toAdd = new HashSet<>();

        for (Term term : terms) {
            for (Equation eq : allEquations) {
                for (Term subterm : term.getUniqueSubterms()) {
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
        if (steps != null && !steps.containsKey(newTerm)) {
            steps.put(newTerm, term);
        }

        return newTerm;
    }

    /**
     * Checks convergence between two terms by checking whether the set of terms created from a term
     * has a term in common by the set of terms created from a second term
     * @param left The first set of terms
     * @param right The second set of terms
     * @return A term that is common between {@code left} and {@code right}, or null if no such term exists
     */
    private static Term checkConvergence(Set<Term> left, Set<Term> right) {
        Set<Term> intersection = new HashSet<>(left);
        intersection.retainAll(right);

        Logger.d("Checking convergence");
        Logger.d("Intersection:");
        for (Term t : intersection) {
            Logger.d(t.toString());
        }

        if (!intersection.isEmpty()) {
            Logger.i("Found conversion");
        }

        return intersection.isEmpty() ? null : intersection.iterator().next();
    }

    /**
     * Generates all possible terms that can be obtained by substituting the variables in {@code term}
     * by the terms in {@code terms}.
     *
     * @param terms A set of terms
     * @param term A term with 0 or more variables
     * @param subs A map that contains the current substitutions
     * @param maxTermDepth The maximum depth a term is allowed, terms with higher depth will not be in the set that is
     *                     returned
     * @return A set of terms that can be created by substituting free variables (i.e. those not occuring
     * in {@code subs}) in {@code term} by terms in the set {@code terms}
     * @see Term
     * @see Variable
     */
    private static Set<Term> generateTerms(Set<Term> terms, Term term, Map<Variable, Term> subs, int maxTermDepth) {
        Set<Term> result = new HashSet<>();

        // We can skip this case, if {@code term} is a variable it will not yield any terms that are not yet in {@code terms}
        if (term instanceof Variable) {
            return result;
        }

        Set<Variable> vars = term.getVariables();
        for (Variable var : vars) {
            if (!subs.containsKey(var)) {
                for (Term t : terms) {
                    if (var.getSort().equals(t.getSort())) {

                        Map<Variable, Term> newSubs = new HashMap<>(subs);
                        newSubs.put(var, t);

                        int depth = term.applySubstitution(newSubs).subtermsAmount();
                        if (depth > maxTermDepth) {
                            continue;
                        }

                        result.addAll(generateTerms(terms, term, newSubs, maxTermDepth));
                    }
                }
            }
        }

        result.add(term.applySubstitution(subs));
        return result;
    }

    /**
     * Generates a set of (small) terms that can be used to generate lemmas.
     * This is done by creating the term f(x1, ..., xn) for every f : s1, ..., sn to s in Sigma in {@code system}
     * the variables x_i are fresh variables of sort s_i for i = 1, ..., n
     * The term and its variables are added to a set and combined {@code k} times by substituting variables by different
     * terms of the same sort.
     *
     * @param system A system of equations over a set of operators sigma, a subset of constructors C, and a
     *               goal equation that should be proven
     * @param maxTermDepth The maximum depth a term is allowed to have
     * @param k The amount of times terms are combined to generate new terms
     * @return A set of (small) terms of all functions in system.Sigma
     * @see EquationSystem
     */
    private static List<Term> getTerms(EquationSystem system, int maxTermDepth, int k) {
        Set<Term> terms = new HashSet<>();

        /*
         Create the term f(x1, ..., xn) for every f : s1, ..., sn -> s in system.Sigma
         the variables x_i are fresh variables of sort s_i for i = 1, ..., n
         The term and its variables are added to the set terms
        */
        int varCount = 0;
        for (Function f : system.getSigma()) {
            List<Term> inputs = new ArrayList<>();

            for (int j = 0; j < f.getInputSorts().size(); j++) {
                Sort s = f.getInputSorts().get(j);
                Variable var = new Variable(s, "x" + varCount);
                varCount++;

                inputs.add(var);
                terms.add(var);
            }

            FunctionTerm term = new FunctionTerm(f, inputs);
            terms.add(term);
            Logger.d("TERM: " + term.toString());
        }

        /*
         Combine terms k times to create terms with greater depth
         */
        for (int i = 0; i < k; i++) {
            List<Term> temp = new ArrayList<>();
            for (Term term : terms) {
                temp.addAll(generateTerms(terms, term, new HashMap<>(), maxTermDepth));
            }
            terms.addAll(temp);
        }

        /*
         Sort the items by the weight function
         */
        List<Term> temp = new ArrayList<>(terms);
        temp.sort((o1, o2) -> {
            double w1 = getWeight(o1, 0.1);
            double w2 = getWeight(o2, 0.1);
            return Double.compare(w1, w2);
        });

        Logger.d("Generated terms:");
        for (Term term : temp) {
            Logger.d(term.toString());
        }

        // Return the sorted list of terms
        return temp;
    }

    public static boolean inductionLemmaSearch(EquationSystem system, OutputWriter outputWriter, int searchSteps, boolean rewriteLeft, int maxTermDepth, int combineTerms, int maxLemmas) throws IOException {
        // Try induction first
        if (induction(system, outputWriter, searchSteps, rewriteLeft, 0, null)) {
            return true;
        }

        outputWriter.setEnabled(false);

        List<Term> terms = getTerms(system, maxTermDepth, combineTerms);

        Map<Sort, List<Term>> smallTerms = new HashMap<>();

        Queue<Equation> addedLemmas = new LinkedList<>();
        for (int i = 0; i < terms.size(); i++) {
            Term t1 = terms.get(i);

            for (int j = i; j < terms.size(); j++) {
                Term t2 = terms.get(j);

                if (t1 instanceof Variable || t2 instanceof Variable) {
                    continue;
                }

                if (!(t1.getSort().equals(t2.getSort()))) {
                    continue;
                }


                Set<Variable> variables1 = t1.getVariables();
                Set<Variable> variables2 = t2.getVariables();
                if (variables1.size() != variables2.size()) {
                    continue;
                }

                if (!(variables1.containsAll(variables2) && variables2.containsAll(variables1))) {
                    continue;
                }

                if (proveLemma(t1, t2, system, outputWriter, searchSteps, rewriteLeft, smallTerms, addedLemmas, maxLemmas)) {
                    outputWriter.setEnabled(true);
                    if (induction(system, outputWriter, searchSteps, rewriteLeft, 0, null)) {
                        return true;
                    }
                    outputWriter.setEnabled(false);
                }

                if (!rewriteLeft) {
                    if (proveLemma(t2, t1, system, outputWriter, searchSteps, rewriteLeft, smallTerms, addedLemmas, maxLemmas)) {
                        outputWriter.setEnabled(true);
                        if (induction(system, outputWriter, searchSteps, rewriteLeft, 0, null)) {
                            return true;
                        }
                        outputWriter.setEnabled(false);
                    }
                }
            }
        }

        outputWriter.setEnabled(true);
        return false;
    }

    private static boolean proveLemma(Term t1, Term t2, EquationSystem system, OutputWriter outputWriter, int searchSteps, boolean rewriteLeft, Map<Sort, List<Term>> smallTerms, Queue<Equation> addedLemmas, int maxLemmas) throws IOException {
        Equation eq = new Equation(t1, t2);

        Sort si = new Sort("i");
        Sort snat = new Sort("nat");
        if (eq.getSort().equals(si)) {
            Function take = null;
            for (Function fn : system.getSigma()) {
                if (fn.toString().contains("take") && fn.getInputSorts().size() == 2) {
                    take = fn;
                    break;
                }
            }
            if (take != null) {
                Variable y = new Variable(snat, "y");
                List<Term> input = new ArrayList<>();
                input.add(y);
                input.add(t1);
                Term tt1 = new FunctionTerm(take, input);
                input.clear();
                input.add(y);
                input.add(t2);
                Term tt2 = new FunctionTerm(take, input);
                eq = new Equation(tt1, tt2);
            }
        }

        boolean addLemma = true;
        for (Equation equation : system.getEquations()) {
            if (equation.equivalent(eq, rewriteLeft)) {
                addLemma = false;
                break;
            }
        }

        if (!addLemma) {
            Logger.d("Skipping lemma: " + eq.toString() + ", equation already exists");
            return false;
        }

        EquationSystem newSystem = new EquationSystem(system.getEquations(), system.getSigma(), system.getC(), eq);

        if (convertible(newSystem, outputWriter, searchSteps, rewriteLeft)) {
            return false;
        }

        // Check whether the equation holds on small terms
        if (checkLikelyEqual(newSystem, smallTerms)) {

            Logger.d("Trying lemma: " + eq.toString());
            if (induction(newSystem, outputWriter, searchSteps, rewriteLeft, 0, null)) {
                system.getEquations().add(new Equation(t1, t2));

                if (addedLemmas.size() > maxLemmas) {
                    Equation toRemove = addedLemmas.poll();
                    system.getEquations().remove(toRemove);

                    if (toRemove != null) {
                        Logger.d("Removing lemma" + toRemove.toString());
                    }

                }
                addedLemmas.offer(eq);

                Logger.i("Found lemma " + eq.toString());
                return true;
            }
        }
        return false;
    }

    /*
     * This function should check whether a lemma is likely to be equal by inserting some small terms
     * and checking that the lemma holds for those terms
     */
    private static boolean checkLikelyEqual(EquationSystem system, Map<Sort, List<Term>> smallTerms) {
        //int searchDepth = 0;
        //Term convergence = null;

        return true;
        /*

        for (int i = 0; i < 2; i++) {
            Set<Term> leftTerms = new HashSet<>();
            Set<Term> rightTerms = new HashSet<>();

            Term left = system.getGoal().getLeft();
            Term right = system.getGoal().getRight();

            for (Variable var : left.getVariables()) {
                left = left.substitute(var, smallTerms.get(var.getSort()).get(i));
                right = right.substitute(var, smallTerms.get(var.getSort()).get(i));
            }

            leftTerms.add(left);
            rightTerms.add(right);

            Logger.d("Checking convergence of " + left.toString() + " to " + right.toString());

            while (convergence == null && searchDepth < 5) {
                leftTerms.addAll(Prover.rewriteAll(leftTerms, system.getEquations(), null, true, true));
                rightTerms.addAll(Prover.rewriteAll(rightTerms, system.getEquations(), null, true, true));

                searchDepth++;
                convergence = checkConvergence(leftTerms, rightTerms);
            }

            if (convergence == null) {
                return false;
            }
        }

        return true;*/
    }

    private static double getWeight(Term term, double epsilon) {
        int numFunctions = term.functionsAmount();
        int numVariables = term.getVariables().size();

        return numFunctions - epsilon * numVariables;
    }
}