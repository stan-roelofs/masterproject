package core;

import core.io.OutputWriter;
import core.logging.Logger;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class with static functions that can prove an EquationSystem
 *
 * @author Stan Roelofs
 * @version 1.2
 */
public class Prover {

    public static String constantName = "a";
    public static int maxDepth = 2;

    public static boolean convertible(EquationSystem system, OutputWriter outputWriter, int searchSteps, boolean rewriteLeft) throws IOException {
        Map<Term, Term> leftSteps = new ConcurrentHashMap<>();
        Map<Term, Term> rightSteps = new ConcurrentHashMap<>();


        outputWriter.writeLine("Seeking conversion between " + system.getGoal().getLeft().toString() + " and " + system.getGoal().getRight().toString());

        Term conversion = findConversion(system, outputWriter, searchSteps, rewriteLeft, leftSteps, rightSteps);

        outputWriter.writeLine("Conversion not found");

        return conversion != null;
    }

    public static Term findConversion(EquationSystem system, OutputWriter outputWriter, int searchSteps, boolean rewriteLeft, Map<Term, Term> leftSteps, Map<Term, Term> rightSteps) throws IOException {
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
        while (convergence == null && searchDepth < searchSteps) {
            Logger.d("Rewriting step " + searchDepth + ", using " + numThreads + " threads");

            RewriteThread[] rewriteThreads = new RewriteThread[numThreads];
            Thread[] threads = new Thread[numThreads];
            for (int i = 0; i < numThreads; i++) {
                rewriteThreads[i] = new RewriteThread(i, leftTerms, rightTerms, threadEquations.get(i), leftSteps, rightSteps, true, rewriteLeft);
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

        return convergence;
    }

    public static boolean induction(EquationSystem system, OutputWriter outputWriter, int searchSteps, boolean rewriteLeft, int recursionDepth, Variable inductionVar) throws IOException {
        if (recursionDepth >= maxDepth) {
            Logger.i("Reached maximum recursion depth of " + maxDepth + ", returning false");
            outputWriter.writeLine("Maximum recursion depth " + maxDepth + " reached, induction on " + inductionVar.toString() + " failed.");
            return false;
        }

        Equation goal = system.getGoal();

        if (recursionDepth == 0 && inductionVar == null) {
            outputWriter.writeLine("Goal to prove with induction: " + goal.toString());
        }

        // Try induction for each variable in function
        Set<Variable> allVariables = new HashSet<>(goal.getLeft().getVariables());

        allVariables.addAll(goal.getRight().getVariables());

        if (inductionVar == null) {
            for (Variable variable : allVariables) {
                Logger.i("Induction on " + variable.toString());
                outputWriter.writeLine("Trying induction variable: " + variable.toString());
                if (induction(system, outputWriter, searchSteps, rewriteLeft, recursionDepth, variable)) {
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
                outputWriter.writeLine("Added hypotheses: " + sb.toString());
            }

            Map<Term, Term> leftSteps = new ConcurrentHashMap<>();
            Map<Term, Term> rightSteps = new ConcurrentHashMap<>();

            Set<Equation> allEquations = new HashSet<>(system.getEquations());
            allEquations.addAll(hypotheses);

            EquationSystem temp = new EquationSystem(allEquations, system.getSigma(), system.getC(), newGoal);

            Term convergence = findConversion(temp, outputWriter, searchSteps, rewriteLeft, leftSteps, rightSteps);

            if (convergence == null) {
                Logger.d("Convergence null");

                Function successor = null;
                for (Function f : system.getC()) {
                    if (f.getName().equals("s") && f.getInputSorts().size() == 1) {
                        successor = f;
                    }
                }
                if (successor == null || successor.getOutputSort() != inductionVar.getSort()) {
                    Logger.d("Skipping double induction");
                    outputWriter.writeLine("Failed to prove " + newGoal.toString() + " induction on " + inductionVar.toString() + " failed.");
                    return false;
                }

                outputWriter.writeLine("Trying double induction");

                // Create term f
                List<Term> subterms = new ArrayList<>();
                subterms.add(inductionVar);
                Term newInductionTerm = new FunctionTerm(successor, subterms);

                Equation newGoalll = system.getGoal().substitute(inductionVar, newInductionTerm);
                EquationSystem newSystem = new EquationSystem(allEquations, system.getSigma(), system.getC(), newGoalll);
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
    public static Set<Term> rewriteAll(Set<Term> terms, Set<Equation> allEquations, Map<Term, Term> steps, boolean rewriteRight, boolean rewriteLeft) {
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


        /*
        Logger.d("Left:");
        for (Term t : left) {
            if (t.toString().contains("and(0, head(tail(morse)))")) {
                Logger.e(t.toString());
            }
        }*/
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
            //Logger.w("Intersection size > 1, choosing the first term");
        }

        return intersection.isEmpty() ? null : intersection.iterator().next();
    }

    /**
     * Generates all possible terms that can be obtained by substituting the variables in {@code term}
     * by the terms in {@code terms}.
     *
     * @param terms A set of terms
     * @param term A term with 0 or more variables
     * @return The set of terms that can be created by substituting variables in {@code term}
     *         by any of the terms in {@code terms}, including the term itself
     * @see Term
     */
    private static Set<Term> generateTerms(Set<Term> terms, Term term) {
        Set<Term> result = new HashSet<>();

        if (term.getVariables().isEmpty()) {
            result.add(term);
            return result;
        }

        Set<Variable> vars = term.getVariables();
        for (Variable var : vars) {
            for (Term t : terms) {
                if (var.getSort().equals(t.getSort())) {
                    Term temp = term.substitute(var, t);
                    result.add(temp);
                }
            }
        }

        return result;
    }

    public static EquationSystem generateLemmas(EquationSystem system, OutputWriter outputWriter, int maxLemmas, int maxAttempts, int searchSteps, boolean rewriteLeft, int recursionDepth, Variable inductionVar) throws IOException {
        Set<Equation> result = new HashSet<>(system.getEquations());

        outputWriter.setEnabled(false);

        Logger.i("Searching for a maximum of " + maxLemmas + " lemmas, terminating after " + maxAttempts + " attempts...");

        Map<Sort, Integer> numVariables = new HashMap<>();
        for (Function f : system.getSigma()) {

            Map<Sort, Integer> numSorts = new HashMap<>(f.getInputSorts().size());
            for (Sort s : f.getInputSorts()) {
                int current = numSorts.get(s) == null ? 0 : numSorts.get(s);
                numSorts.put(s, current + 1);
            }

            for (Sort s : f.getInputSorts()) {
                int max = numVariables.get(s) == null ? 0 : numVariables.get(s);
                if (numSorts.get(s) > max) {
                    numVariables.put(s, numSorts.get(s));
                }
            }
        }

        Map<Sort, List<Term>> variables = new HashMap<>();
        int variableCounter = 0;
        for (Map.Entry<Sort, Integer> entry : numVariables.entrySet()) {
            Logger.d(entry.getKey().toString() + " : " + entry.getValue());
            variables.put(entry.getKey(), new ArrayList<>());

            for (int i = 0; i < entry.getValue(); i++) {
                Variable v = new Variable(entry.getKey(), "x" + variableCounter);
                variables.get(entry.getKey()).add(v);

                Logger.d("Generated variable: " + v.toString() + " : " + v.getSort().toString());

                variableCounter++;
            }
        }

        Set<Term> terms = new HashSet<>();
        for (Sort s : variables.keySet()) {
            terms.addAll(variables.get(s));
        }

        for (Function f : system.getSigma()) {
            List<Term> inputs = new ArrayList<>();

            Set<Sort> uniqueInputSorts = new HashSet<>(f.getInputSorts());

            Map<Sort, Integer> indexes = new HashMap<>();
            for (Sort s : uniqueInputSorts) {
                indexes.put(s, 0);
            }

            for (int j = 0; j < f.getInputSorts().size(); j++) {
                Sort s = f.getInputSorts().get(j);
                inputs.add(variables.get(s).get(indexes.get(s)));
                indexes.put(s, indexes.get(s) + 1);
            }

            FunctionTerm term = new FunctionTerm(f, inputs);
            terms.add(term);
            Logger.d("TERM: " + term.toString());
        }


        for (int i = 0; i < 1; i++) {
            Set<Term> temp = new HashSet<>();
            for (Term term : terms) {
                temp.addAll(generateTerms(terms, term));
            }
            terms.addAll(temp);
        }

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

        int lemmasFound = 0;
        searchLemmas:
        {
            for (Term t1 : temp) {
                for (Term t2 : temp) {
                    if (lemmasFound == maxLemmas) {
                        break searchLemmas;
                    }
                    if (t1.equals(t2)) {
                        continue;
                    }

                    if (!t1.getSort().equals(t2.getSort())) {
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

                    Equation eq = new Equation(t1, t2);

                    boolean addLemma = true;
                    for (Equation equation : system.getEquations()) {
                        if (equation.equivalent(eq, rewriteLeft)) {
                            addLemma = false;
                            break;
                        }
                    }

                    if (!addLemma) {
                        Logger.i("Skipping lemma: " + eq.toString() + ", equation already exists");
                        continue;
                    }
                    Logger.i("Trying lemma: " + eq.toString());

                    EquationSystem newSystem = new EquationSystem(result, system.getSigma(), system.getC(), eq);
                    // Check whether the equation holds on small terms
                    if (checkLikelyEqual(newSystem, 1)) {
                        // TODO: if a conversion exists without induction we should skip the lemma since it is not useful
                        if (induction(newSystem, outputWriter, 3, rewriteLeft, recursionDepth, inductionVar)) {
                            result.add(eq);
                            Logger.i("Found lemma " + eq.toString());
                            lemmasFound++;
                        }
                    }
                }
            }
        }

        outputWriter.setEnabled(true);

        return new EquationSystem(result, system.getSigma(), system.getC(), system.getGoal());
    }

    private static boolean checkLikelyEqual(EquationSystem system, int attempts) {
        int searchDepth = 0;
        Term convergence = null;

        Sort nat = new Sort("nat");
        Sort inf = new Sort("i");

        Map<Sort, List<Term>> smallTerms = new HashMap<>();
        smallTerms.put(nat, new ArrayList<>());

        // Nat
        Function zero = new Function(nat, "0");
        smallTerms.get(nat).add(new FunctionTerm(zero));

        for (int i = 0; i < attempts; i++) {
            Set<Term> leftTerms = new HashSet<>();

            Term left = system.getGoal().getLeft();
            for (Variable var : left.getVariables()) {
                left = left.substitute(var, smallTerms.get(var.getSort()).get(i));
            }

            leftTerms.add(left);

            Set<Term> rightTerms = new HashSet<>();

            Term right = system.getGoal().getRight();
            for (Variable var : right.getVariables()) {
                right = right.substitute(var, smallTerms.get(var.getSort()).get(i));
            }

            rightTerms.add(right);

            Logger.d("Checking convergence of " + left.toString() + " to " + right.toString());

            while (convergence == null && searchDepth < 3) {
                leftTerms.addAll(Prover.rewriteAll(leftTerms, system.getEquations(), null, true, true));
                rightTerms.addAll(Prover.rewriteAll(rightTerms, system.getEquations(), null, true, true));

                searchDepth++;
                convergence = checkConvergence(leftTerms, rightTerms);
            }

            if (convergence == null) {
                return false;
            }
        }

        return true;
    }

    private static double getWeight(Term term, double epsilon) {
        int numFunctions = term.functionsAmount();
        int numVariables = term.getVariables().size();

        return numFunctions - epsilon * numVariables;
    }
}