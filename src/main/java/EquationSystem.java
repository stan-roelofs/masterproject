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

    public void print() {
        System.out.println("Sigma:");
        for (Function f : sigma) {
            System.out.println(f.toString());
        }
        System.out.println("E:");
        for (Equation eq : equations) {
            System.out.println(eq.toString());
        }
        System.out.println("C:");
        for (Function f : C) {
            System.out.println(f.toString());
        }
        System.out.println("Goal:");
        System.out.println(goal.toString());
    }
}
