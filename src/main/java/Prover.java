import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;

public class Prover {

    private final static Logger LOGGER = Logger.getLogger(Prover.class.getName());

    public static void induction(EquationSystem system) {
        Equation goal = system.goal;
        LOGGER.info("Goal: " + goal.toString());

        // Try induction for each variable in function
        HashSet<Variable> allVariables = new HashSet<>(goal.getLeft().getVariables());

        allVariables.addAll(goal.getRight().getVariables());

        for (Variable inductionVar : allVariables) {
            LOGGER.info("Trying induction variable: " + inductionVar.toString());

            // For each function in C
            for (Function function : system.C) {

                List<Term> newConstants = new ArrayList<>();
                List<Equation> hypotheses = new ArrayList<>();

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
                LOGGER.info("To prove: " + newGoal.toString());

                if (hypotheses.size() > 0) {
                    StringBuilder sb = new StringBuilder();
                    for (Equation hypothesis : hypotheses) {
                        sb.append(hypothesis.toString());
                        sb.append(" ");
                    }
                    LOGGER.info("Hypotheses: " + sb.toString());
                }
            }
        }
    }
}
