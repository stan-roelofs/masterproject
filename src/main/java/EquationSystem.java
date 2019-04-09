import java.util.Collection;

/**
 * Class that represents an instance of the inductive theorem, which consists of a set equations over a signature
 * Sigma, a subset of Sigma called C (constructors) and an equation that should be proven
 *
 * @author Stan Roelofs
 * @version 1.0
 */
class EquationSystem {
    private Collection<Equation> equations;
    private Collection<Function> sigma;
    private Collection<Function> C;
    private Equation goal;

    EquationSystem(Collection<Equation> eq, Collection<Function> sigma, Collection<Function> C, Equation goal) {
        if (eq == null || sigma == null || C == null || goal == null) {
            throw new IllegalArgumentException("Eq, Sigma, C, Goal must not be null");
        }

        if (C.isEmpty()) {
            throw new IllegalArgumentException("C must not be empty");
        }

        Sort sort = null;
        for (Function f : C) {
            // Take sort of f if it is currently null
            if (sort == null) {
                sort = f.getOutputSort();
                continue;
            }
            // Check sorts are the same
            if (!f.getOutputSort().equals(sort)) {
                throw new IllegalArgumentException("Sorts of functions in C should be the same: " +
                        f.toString() + " of sort " + f.getOutputSort().toString() + " expected " + sort.toString());
            }
            // Check C subset of Sigma
            if (!sigma.contains(f)) {
                throw new IllegalArgumentException("C must be a subset of Sigma, symbol " + f.toString() + " not in Sigma");
            }
        }

        this.equations = eq;
        this.sigma = sigma;
        this.C = C;
        this.goal = goal;
    }

    public Collection<Equation> getEquations() {
        return equations;
    }

    public Collection<Function> getSigma() {
        return sigma;
    }

    public Collection<Function> getC() {
        return C;
    }

    public Equation getGoal() {
        return goal;
    }

    public Sort getCSort() {
        // Get the first function using iterator and return the sort of that function
        return this.C.iterator().next().getOutputSort();
    }

    void print() {
        Logger.i("Sigma:");
        for (Function f : sigma) {
            Logger.i(f.toString());
        }
        Logger.i("E:");
        for (Equation eq : equations) {
            Logger.i(eq.toString());
        }
        Logger.i("C:");
        for (Function f : C) {
            Logger.i(f.toString());
        }
        Logger.i("Goal:");
        Logger.i(goal.toString());
    }
}
