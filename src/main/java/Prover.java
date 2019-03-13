import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

public class Prover {

    public static void induction(EquationSystem system, Equation goal) {
        System.out.println("Goal: " + goal.toString());

        // Try induction for each variable in function
        HashSet<Variable> allVariables = new HashSet<>(goal.getLeft().getVariables());

        allVariables.addAll(goal.getRight().getVariables());

        for (Variable inductionVar : allVariables) {
            System.out.println("Induction variable: " + inductionVar.toString());

            // For each function in C
            for (Function function : system.C) {

                Collection<Term> newConstants = new ArrayList<>();
                Collection<Equation> hypotheses = new ArrayList<>();

                String constant = "a";
                int count = 1;

                // Add hypotheses
                for (Sort s : function.getInputSorts()) {
                    Function cnst = new Function(constant + count, s);
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
                System.out.println("To prove: " + newGoal.toString());
                for (Equation hypothesis : hypotheses) {
                    System.out.println("Hypothesis: " + hypothesis.toString());
                }
            }
        }
    }
}
