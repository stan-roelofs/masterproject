import java.util.Collection;

class EquationSystem {
    public Collection<Equation> equations;
    public Collection<Function> sigma;
    public Collection<Function> C;

    EquationSystem(Collection<Equation> eq, Collection<Function> sigma, Collection<Function> C) {
        this.equations = eq;
        this.sigma = sigma;
        this.C = C;
    }
}
