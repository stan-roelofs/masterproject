import core.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class EquationSystemTests {

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorEqNull() {
        Set<Equation> eq = new HashSet<>();
        Set<Function> Sigma = new HashSet<>();
        Set<Function> C = new HashSet<>();

        Sort sort = new Sort("nat");
        Variable var = new Variable(sort, "x");
        Variable var2 = new Variable(sort, "y");
        Equation goal = new Equation(var, var2);

        EquationSystem system = new EquationSystem(eq, Sigma, C, goal);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorSigmaNull() {
        Set<Equation> eq = new HashSet<>();
        Set<Function> Sigma = null;
        Set<Function> C = new HashSet<>();

        Sort sort = new Sort("nat");
        Variable var = new Variable(sort, "x");
        Variable var2 = new Variable(sort, "y");
        Equation goal = new Equation(var, var2);

        EquationSystem system = new EquationSystem(eq, Sigma, C, goal);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorCNull() {
        Set<Equation> eq = new HashSet<>();
        Set<Function> Sigma = new HashSet<>();
        Set<Function> C = null;

        Sort sort = new Sort("nat");
        Variable var = new Variable(sort, "x");
        Variable var2 = new Variable(sort, "y");
        Equation goal = new Equation(var, var2);

        EquationSystem system = new EquationSystem(eq, Sigma, C, goal);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorGoalNull() {
        Set<Equation> eq = new HashSet<>();
        Set<Function> Sigma = new HashSet<>();
        Set<Function> C = new HashSet<>();
        Equation goal = null;

        EquationSystem system = new EquationSystem(eq, Sigma, C, goal);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorCEmpty() {
        Set<Equation> eq = new HashSet<>();
        Set<Function> Sigma = new HashSet<>();
        Set<Function> C = new HashSet<>();
        Sort sort = new Sort("nat");
        Variable var = new Variable(sort, "x");
        Variable var2 = new Variable(sort, "y");
        Equation goal = new Equation(var, var2);

        EquationSystem system = new EquationSystem(eq, Sigma, C, goal);
    }

    /**
     * If the sort of all functions in C is not the same
     * an exception should be thrown
     */
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorCMultipleSorts() {
        Set<Equation> eq = new HashSet<>();
        Set<Function> Sigma = new HashSet<>();
        Set<Function> C = new HashSet<>();
        Sort sort = new Sort("nat");
        Variable var = new Variable(sort, "x");
        Variable var2 = new Variable(sort, "y");
        Equation goal = new Equation(var, var2);

        Sort sort2 = new Sort("nat2");
        Function f = new Function(sort2, "0");
        Function f2 = new Function(sort, "1");
        C.add(f);
        C.add(f2);

        Sigma.add(f);
        Sigma.add(f2);

        EquationSystem system = new EquationSystem(eq, Sigma, C, goal);
    }

    /**
     * If C is not a subset of Sigma, an exception should be thrown
     */
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorCNotSubsetSigma() {
        Set<Equation> eq = new HashSet<>();
        Set<Function> Sigma = new HashSet<>();
        Set<Function> C = new HashSet<>();
        Sort sort = new Sort("nat");
        Variable var = new Variable(sort, "x");
        Variable var2 = new Variable(sort, "y");
        Equation goal = new Equation(var, var2);

        Sort sort2 = new Sort("nat2");
        Function f = new Function(sort2, "0");
        C.add(f);

        EquationSystem system = new EquationSystem(eq, Sigma, C, goal);
    }

    /**
     * If functions occur in goal that are not in Sigma,
     * an exception should be thrown
     */
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorSigmaIncompleteGoal() {
        Set<Equation> eq = new HashSet<>();
        Set<Function> Sigma = new HashSet<>();
        Set<Function> C = new HashSet<>();
        Sort sort = new Sort("nat");
        Variable var = new Variable(sort, "x");
        Function f = new Function(sort, "0");
        Function f2 = new Function(sort, "1");
        C.add(f); // C = {0}
        Sigma.add(f); // Sigma = {0}

        FunctionTerm term = new FunctionTerm(f2); // 1

        Equation goal = new Equation(var, term); // x = 1

        EquationSystem system = new EquationSystem(eq, Sigma, C, goal);
    }

    /**
     * If functions occur in goal that are not in Sigma,
     * an exception should be thrown
     */
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorSigmaIncompleteEq() {
        Set<Equation> eq = new HashSet<>();
        Set<Function> Sigma = new HashSet<>();
        Set<Function> C = new HashSet<>();
        Sort sort = new Sort("nat");
        Variable var = new Variable(sort, "x");
        Variable var2 = new Variable(sort, "y");
        Function f = new Function(sort, "0");
        Function f2 = new Function(sort, "1");
        C.add(f); // C = {0}
        Sigma.add(f); // Sigma = {0}

        FunctionTerm term = new FunctionTerm(f2); // 1

        Equation equation = new Equation(var, term); // x = 1
        eq.add(equation);

        Equation goal = new Equation(var, var2);

        EquationSystem system = new EquationSystem(eq, Sigma, C, goal);
    }

    @Test
    public void testGetEquations() {
        Set<Equation> eq = new HashSet<>();
        Set<Function> Sigma = new HashSet<>();
        Set<Function> C = new HashSet<>();
        Sort sort = new Sort("nat");
        Variable var = new Variable(sort, "x");
        Function f = new Function(sort, "0");
        C.add(f); // C = {0}
        Sigma.add(f); // Sigma = {0}

        FunctionTerm term = new FunctionTerm(f); // 0

        Equation goal = new Equation(var, term); // x = 0
        eq.add(goal);

        EquationSystem system = new EquationSystem(eq, Sigma, C, goal);

        Assert.assertTrue(collectionEquals(eq, system.getEquations()));
    }

    @Test
    public void testGetSigma() {
        Set<Equation> eq = new HashSet<>();
        Set<Function> Sigma = new HashSet<>();
        Set<Function> C = new HashSet<>();
        Sort sort = new Sort("nat");
        Variable var = new Variable(sort, "x");
        Function f = new Function(sort, "0");
        C.add(f); // C = {0}
        Sigma.add(f); // Sigma = {0}

        FunctionTerm term = new FunctionTerm(f); // 0

        Equation goal = new Equation(var, term); // x = 0
        eq.add(goal);

        EquationSystem system = new EquationSystem(eq, Sigma, C, goal);

        Assert.assertTrue(collectionEquals(Sigma, system.getSigma()));
    }

    @Test
    public void testGetC() {
        Set<Equation> eq = new HashSet<>();
        Set<Function> Sigma = new HashSet<>();
        Set<Function> C = new HashSet<>();
        Sort sort = new Sort("nat");
        Variable var = new Variable(sort, "x");
        Function f = new Function(sort, "0");
        C.add(f); // C = {0}
        Sigma.add(f); // Sigma = {0}

        FunctionTerm term = new FunctionTerm(f); // 0

        Equation goal = new Equation(var, term); // x = 0
        eq.add(goal);

        EquationSystem system = new EquationSystem(eq, Sigma, C, goal);

        Assert.assertTrue(collectionEquals(C, system.getC()));
    }

    @Test
    public void testGetGoal() {
        Set<Equation> eq = new HashSet<>();
        Set<Function> Sigma = new HashSet<>();
        Set<Function> C = new HashSet<>();
        Sort sort = new Sort("nat");
        Variable var = new Variable(sort, "x");
        Function f = new Function(sort, "0");
        C.add(f); // C = {0}
        Sigma.add(f); // Sigma = {0}

        FunctionTerm term = new FunctionTerm(f); // 0

        Equation goal = new Equation(var, term); // x = 0
        eq.add(goal);

        EquationSystem system = new EquationSystem(eq, Sigma, C, goal);

        Assert.assertEquals(goal, system.getGoal());
    }

    @Test
    public void testGetCSort() {
        Set<Equation> eq = new HashSet<>();
        Set<Function> Sigma = new HashSet<>();
        Set<Function> C = new HashSet<>();
        Sort sort = new Sort("nat");
        Variable var = new Variable(sort, "x");
        Function f = new Function(sort, "0");
        C.add(f); // C = {0}
        Sigma.add(f); // Sigma = {0}

        FunctionTerm term = new FunctionTerm(f); // 0

        Equation goal = new Equation(var, term); // x = 0
        eq.add(goal);

        EquationSystem system = new EquationSystem(eq, Sigma, C, goal);

        Assert.assertEquals(sort, system.getCSort());
    }

    private <T> boolean collectionEquals(Collection<T> c, Collection<T> c2) {
        return c.containsAll(c2) && c2.containsAll(c);
    }
}
