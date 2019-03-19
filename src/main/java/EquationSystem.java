import java.util.Collection;

/**
 * Class that represents an instance of the inductive theorem, which consists of a set equations over a signature
 * Sigma, a subset of Sigma called C (constructors) and an equation that should be proven
 *
 * @author Stan Roelofs
 * @version 1.0
 */
class EquationSystem {
    public Collection<Equation> equations;
    public Collection<Function> sigma;
    public Collection<Function> C;
    public Equation goal;

    EquationSystem(Collection<Equation> eq, Collection<Function> sigma, Collection<Function> C, Equation goal) {
        this.equations = eq;
        this.sigma = sigma;
        this.C = C;
        this.goal = goal;
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
