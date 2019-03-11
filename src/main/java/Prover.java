import java.util.ArrayList;
import java.util.Collection;

public class Prover {

    public static void induction(EquationSystem system, Equation goal) {
        // For each function in C
        for (Function function : system.C) {
            // Try induction for each variable in function
            Collection<Variable> allVariables = goal.getLeft().getVariables();
            allVariables.addAll(goal.getRight().getVariables());

            for (Variable inductionVar : allVariables) {
                Collection<FunctionTerm> newConstants = new ArrayList<>();
                Collection<Equation> hypotheses = new ArrayList<>();

                // Add hypotheses
                for (Sort s : function.inputSorts) {
                    Function constant = new Function(s);
                    FunctionTerm a = new FunctionTerm(s, constant, null);
                    newConstants.add(a);

                    if (s == function.outputSort) {
                        Equation hypothesis = goal.substitute(inductionVar, a);
                        hypotheses.add(hypothesis);
                    }
                }

                // Create term f(a1, ..., an)
                FunctionTerm inductionTerm = function.apply(newConstants);

                // Create left and right terms of goal
                Term left = goal.getLeft().substitute(inductionVar, inductionTerm);
                Term right = goal.getRight().substitute(inductionVar, inductionTerm);

                // Prove left = right using system.equations and hypotheses
            }
        }
    }
}
