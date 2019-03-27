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
        Equation goal = system.goal;
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
            for (Function function : system.C) {

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

                Set<Equation> allEquations = new HashSet<>(system.equations);
                allEquations.addAll(hypotheses);

                Map<Term, Term> steps = new HashMap<>();

                while (checkConvergence(leftTerms, rightTerms) == null) {
                    Set<Term> toAdd = new HashSet<>();

                    for (Term term : leftTerms) {
                        for (Equation eq : allEquations) {
                            for (Term term2 : term.getAllSubTerms()) {
                                // TODO: Right
                                Map<Variable, Term> sub = eq.getLeft().getSubstitution(term2, new HashMap<>());

                                if (sub != null) {
                                    Term newt = eq.getRight();
                                    for (Map.Entry<Variable, Term> entry : sub.entrySet()) {
                                        newt = newt.substitute(entry.getKey(), entry.getValue());
                                    }

                                    toAdd.add(term.substitute(term2, newt));
                                    if (!steps.containsKey(term.substitute(term2, newt))) {
                                        steps.put(term.substitute(term2, newt), term);
                                    }
                                }

                                // TODO: Right
                                Map<Variable, Term> sub2 = eq.getRight().getSubstitution(term2, new HashMap<>());

                                if (sub2 != null) {
                                    Term newt = eq.getLeft();
                                    for (Map.Entry<Variable, Term> entry : sub2.entrySet()) {
                                        newt = newt.substitute(entry.getKey(), entry.getValue());
                                    }

                                    toAdd.add(term.substitute(term2, newt));
                                    if (!steps.containsKey(term.substitute(term2, newt))) {
                                        steps.put(term.substitute(term2, newt), term);
                                    }
                                }
                            }
                        }
                    }

                    leftTerms.addAll(toAdd);
                    toAdd.clear();

                    // tODO: steps right
                    for (Term term : rightTerms) {
                        for (Equation eq : allEquations) {
                            for (Term term2 : term.getAllSubTerms()) {
                                // TODO: Right
                                Map<Variable, Term> sub = eq.getLeft().getSubstitution(term2, new HashMap<>());

                                if (sub != null) {
                                    Term newt = eq.getRight();
                                    for (Map.Entry<Variable, Term> entry : sub.entrySet()) {
                                        newt = newt.substitute(entry.getKey(), entry.getValue());
                                    }

                                    toAdd.add(term.substitute(term2, newt));
                                    if (!steps.containsKey(term.substitute(term2, newt))) {
                                        steps.put(term.substitute(term2, newt), term);
                                    }
                                }

                                // TODO: Right
                                Map<Variable, Term> sub2 = eq.getRight().getSubstitution(term2, new HashMap<>());

                                if (sub2 != null) {
                                    Term newt = eq.getLeft();
                                    for (Map.Entry<Variable, Term> entry : sub2.entrySet()) {
                                        newt = newt.substitute(entry.getKey(), entry.getValue());
                                    }

                                    toAdd.add(term.substitute(term2, newt));
                                    if (!steps.containsKey(term.substitute(term2, newt))) {
                                        steps.put(term.substitute(term2, newt), term);
                                    }
                                }
                            }
                        }
                    }

                    rightTerms.addAll(toAdd);
                }

                Term convergence = checkConvergence(leftTerms, rightTerms);
                List<Term> revSequence = new ArrayList<>();

                // Trace back the steps until we reach the start term
                while (!(convergence.equals(left))) {
                    revSequence.add(convergence);
                    convergence = steps.get(convergence);
                }
                revSequence.add(convergence);

                StringBuilder stepsString = new StringBuilder();
                stepsString.append("Conversion: ");
                for (int i = revSequence.size() - 1; i >= 0; i--) {
                    stepsString.append(revSequence.get(i));

                    if (i > 0) {
                        stepsString.append(" -> ");
                    }
                }

                outputWriter.write(stepsString.toString());
                outputWriter.newLine();
            }
        }
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
