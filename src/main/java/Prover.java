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
        outputWriter.write("Goal: " + goal.toString());
        outputWriter.newLine();

        // Try induction for each variable in function
        Set<Variable> allVariables = new HashSet<>(goal.getLeft().getVariables());

        allVariables.addAll(goal.getRight().getVariables());

        for (Variable inductionVar : allVariables) {
            if (!(inductionVar.getName().equals("x"))) { // TODO: check sort of variable
                continue;//TODO: remove
            }
            outputWriter.write("Trying induction variable: " + inductionVar.toString());
            outputWriter.newLine();

            // For each function in C
            for (Function function : system.getC()) {

                List<Term> newConstants = new ArrayList<>();
                List<Equation> hypotheses = new ArrayList<>();

                String constant = "a";
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
                outputWriter.write("To prove: " + newGoal.toString());
                outputWriter.newLine();

                if (hypotheses.size() > 0) {
                    StringBuilder sb = new StringBuilder();
                    for (Equation hypothesis : hypotheses) {
                        sb.append(hypothesis.toString());
                        sb.append(" ");
                    }
                    outputWriter.write("Hypotheses: " + sb.toString());
                    outputWriter.newLine();
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

                while (checkConvergence(leftTerms, rightTerms) == null) {
                    leftTerms.addAll(rewriteAll(leftTerms, allEquations, leftSteps));
                    rightTerms.addAll(rewriteAll(rightTerms, allEquations, rightSteps));
                }

                Term convergence = checkConvergence(leftTerms, rightTerms);
                if (convergence == null) {
                    Logger.w("Convergence null");
                    return;
                }

                String conversion = getConversionString(left, right, convergence, leftSteps, rightSteps);
                outputWriter.write(conversion);
                outputWriter.newLine();
            }
        }
    }

    private static String getConversionString(Term initial, Term goal, Term convergence, Map<Term,Term> leftSteps, Map<Term,Term> rightSteps) {
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
            }
            revSequence.add(temp);
        }

        List<Term> sequence = new ArrayList<>();
        // Reverse sequence
        for (int i = revSequence.size() - 1; i >= 0; i--) {
            sequence.add(revSequence.get(i));
        }

        temp =  null;
        // Trace back the steps until we reach the start term
        while (!(goal.equals(temp))) {
            if (temp == null) {
                if (!rightSteps.containsKey(convergence)) {
                    break;
                }
                temp = rightSteps.get(convergence);
            } else {
                temp = rightSteps.get(temp);
            }
            sequence.add(temp);
        }

        StringBuilder stepsString = new StringBuilder();
        stepsString.append("Conversion: ");
        for (int i = 0; i < sequence.size(); i++) {
            stepsString.append(sequence.get(i));

            if (i < sequence.size() - 1) {
                stepsString.append(" -> ");
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
                        toAdd.addAll(rewriteTerm(steps, term, subterm, sub, rewriteGoal));
                    }

                    // Try right
                    Map<Variable, Term> sub2 = eq.getRight().getSubstitution(subterm, new HashMap<>());

                    if (sub2 != null) {
                        Term newt = eq.getLeft();
                        toAdd.addAll(rewriteTerm(steps, term, subterm, sub2, newt));
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
    private static Set<Term> rewriteTerm(Map<Term, Term> steps, Term term, Term subterm, Map<Variable, Term> sub, Term subtermSubstitute) {
        Set<Term> toAdd = new HashSet<>();

        /*
         * subterm can be rewritten to subtermSubstitute using the substitutions in sub
         * So perform all these substitutions on subtermSubstitute
         */
        for (Map.Entry<Variable, Term> entry : sub.entrySet()) {
            subtermSubstitute = subtermSubstitute.substitute(entry.getKey(), entry.getValue());
        }

        // Create the new term by substituting subterm in term by subtermSubstitute
        Term newTerm = term.substitute(subterm, subtermSubstitute);
        toAdd.add(newTerm);

        // Add this step to steps
        if (!steps.containsKey(newTerm)) {
            steps.put(newTerm, term);
        }

        return toAdd;
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
