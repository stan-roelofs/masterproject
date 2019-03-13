import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;

public class ProverTests {

    @Test
    public void testInduction() {
        Sort nat = new Sort("nat");
        Function zero = new Function("0", nat);

        Collection<Sort> inputs = new ArrayList<>();
        inputs.add(nat);
        Function successor = new Function("s", inputs, nat);

        Collection<Function> C = new ArrayList<>();
        C.add(zero);
        C.add(successor);

        Collection<Function> Sigma = new ArrayList<>(C);
        inputs.add(nat);
        Function plus = new Function("+", inputs, nat);
        Sigma.add(plus);

        Collection<Term> subterms = new ArrayList<>();
        subterms.add(new FunctionTerm(zero));

        Variable var = new Variable(nat, "x");
        subterms.add(var);
        FunctionTerm left = new FunctionTerm(plus, subterms);
        Equation eq1 = new Equation(left, var);

        subterms.clear();
        subterms.add(var);
        Term t = new FunctionTerm(successor, subterms);

        subterms.clear();
        subterms.add(t);
        Variable var2 = new Variable(nat, "y");
        subterms.add(var2);
        FunctionTerm l = new FunctionTerm(plus, subterms);

        subterms.clear();
        subterms.add(var);
        subterms.add(var2);
        Term tt = new FunctionTerm(plus, subterms);
        subterms.clear();
        subterms.add(tt);
        FunctionTerm r = new FunctionTerm(successor, subterms);

        Equation eq2 = new Equation(l, r);

        Collection<Equation> eqs = new ArrayList<>();
        eqs.add(eq1);
        eqs.add(eq2);

        subterms.clear();
        subterms.add(var);
        subterms.add(new FunctionTerm(zero));
        FunctionTerm hoi = new FunctionTerm(plus, subterms);
        Equation goal = new Equation(hoi, var);

        EquationSystem lol = new EquationSystem(eqs, Sigma, C, goal);
        Prover.induction(lol);
    }
}
