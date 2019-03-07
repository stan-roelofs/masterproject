import java.util.ArrayList;
import java.util.Collection;

public class Prover {

    public static void induction(EquationSystem system, Equation goal) {
        // For each function in C
        for (Function function : system.C) {
            // Try induction for each variable in function
            for (Variable inductionVar : function.variables) {

                Collection<Term> newConstants = new ArrayList<>();
                // Add hypotheses
                Collection<Equation> hypotheses = new ArrayList<>();

                for (Sort s : function.inputSorts) {
                    Term a = new Term(s);
                    newConstants.add(a);

                    if (s == function.outputSort) {
                        Equation hypothesis = goal.substitute(inductionVar, a);
                        hypotheses.add(hypothesis);
                    }
                }

                // Create term f(a1, ..., an)
                Term inductionTerm = function.apply(newConstants);

                // Create left and right terms of goal
                Term left = goal.getLeft().substitute(inductionVar, inductionTerm);
                Term right = goal.getRight().substitute(inductionVar, inductionTerm);

                // Prove left = right using system.equations and hypotheses
            }
        }
    }
}
