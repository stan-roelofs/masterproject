import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;

/**
 * Class with static functions that can prove an EquationSystem
 *
 * @author Stan Roelofs
 * @version 1.0
 */
public class Prover {

    public static void induction(EquationSystem system, BufferedWriter outputWriter) throws IOException {
        Equation goal = system.getGoal();
        Util.writeLine(outputWriter, "Goal: " + goal.toString());

        // Try induction for each variable in function
        Set<Variable> allVariables = new HashSet<>(goal.getLeft().getVariables());

        allVariables.addAll(goal.getRight().getVariables());

        for (Variable inductionVar : allVariables) {
            if (!(inductionVar.getName().equals("x"))) { // TODO: check sort of variable
                continue;//TODO: remove
            }
            Util.writeLine(outputWriter, "Trying induction variable: " + inductionVar.toString());

            // For each function in C
            for (Function function : system.getC()) {

                List<Term> newConstants = new ArrayList<>();
                List<Equation> hypotheses = new ArrayList<>();

                String constant = "a"; // TODO: check if this already exists as a function
                int count = 1;

                // Add hypotheses
                for (Sort s : function.getInputSorts()) {
                    Function cnst = new Function(s, constant + count);
                    FunctionTerm a = new FunctionTerm(cnst, new ArrayList<>());
                    newConstants.add(a);

                    if (s == function.getOutputSort()) {
                        Equation hypothesis = goal.substitute(inductionVar, a);
                        hypotheses.add(hypothesis);
                    }
                }

                // Create term f(a1, ..., an)
                FunctionTerm inductionTerm = new FunctionTerm(function, newConstants);

                // Create left and right terms of goal
                Term left = goal.getLeft().substitute(inductionVar, inductionTerm);
                Term right = goal.getRight().substitute(inductionVar, inductionTerm);

                // Prove left = right using system.equations and hypotheses
                Equation newGoal = new Equation(left, right);
                Util.writeLine(outputWriter, "To prove: " + newGoal.toString());

                if (hypotheses.size() > 0) {
                    StringBuilder sb = new StringBuilder();
                    for (Equation hypothesis : hypotheses) {
                        sb.append(hypothesis.toString());
                        sb.append(" ");
                    }
                    Util.writeLine(outputWriter, "Hypotheses: " + sb.toString());
                }

                // Do BFS for convertibility
                Set<Term> leftTerms = new HashSet<>();
                Set<Term> rightTerms = new HashSet<>();

                leftTerms.add(left);
                rightTerms.add(right);

                Set<Equation> allEquations = new HashSet<>(system.getEquations());
                allEquations.addAll(hypotheses);

                Map<Term, Term> leftSteps = new HashMap<>();
                Map<Term, Term> rightSteps = new HashMap<>();

                int searchDepth = 0;
                while (checkConvergence(leftTerms, rightTerms) == null && searchDepth < 8) {
                    leftTerms.addAll(rewriteAll(leftTerms, allEquations, leftSteps));
                    rightTerms.addAll(rewriteAll(rightTerms, allEquations, rightSteps));
                    searchDepth++;
                }

                Term convergence = checkConvergence(leftTerms, rightTerms);
                if (convergence == null) {
                    Logger.w("Convergence null");

                    Util.writeLine(outputWriter, "Trying double induction");

                    List<Term> lpap = new ArrayList<>();
                    lpap.add(inductionVar);

                    Equation newGoalll = system.getGoal().substitute(inductionVar, new FunctionTerm(function, lpap));
                    EquationSystem newSystem = new EquationSystem(allEquations, system.getSigma(), system.getC(), newGoalll);
                    induction(newSystem, outputWriter);
                    return;
                }

                List<Term> conversion = getConversionSequence(left, right, convergence, leftSteps, rightSteps);
                String conversionString = getConversionString(conversion);
                Util.writeLine(outputWriter, conversionString);
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
    private static Set<Term> rewriteAll(Set<Term> terms, Set<Equation> allEquations, Map<Term, Term> steps) {
        Set<Term> toAdd = new HashSet<>();

        for (Term term : terms) {
            for (Equation eq : allEquations) {
                for (Term subterm : term.getAllSubTerms()) {
                    // Try left
                    Map<Variable, Term> sub = eq.getLeft().getSubstitution(subterm, new HashMap<>());

                    if (sub != null) {
                        Term rewriteGoal = eq.getRight();
                        toAdd.add(rewriteTerm(steps, term, subterm, sub, rewriteGoal));
                    }

                    // Try right
                    Map<Variable, Term> sub2 = eq.getRight().getSubstitution(subterm, new HashMap<>());

                    if (sub2 != null) {
                        Term newt = eq.getLeft();
                        toAdd.add(rewriteTerm(steps, term, subterm, sub2, newt));
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
            Logger.d(t.toString());
        }
        Logger.d("Right:");
        for (Term t : right) {
            Logger.d(t.toString());
        }

        Logger.d("Intersection:");
        for (Term t : intersection) {
            Logger.d(t.toString());
        }

        if (!intersection.isEmpty()) {
            Logger.i("Found intersection");
        }

        if (intersection.size() > 1) {
            Logger.w("Intersection size > 1");
        }

        return intersection.isEmpty() ? null : intersection.iterator().next();
    }
}
