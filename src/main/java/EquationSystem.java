import java.util.Collection;

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
}
