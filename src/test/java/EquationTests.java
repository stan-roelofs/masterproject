import org.junit.Assert;
import org.junit.Test;

public class EquationTests {

    @Test(expected = IllegalArgumentException.class)
    public void testEquationConstructorLeftNull() {
        Equation eq = new Equation(null, new Variable(new Sort("nat"), "x"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEquationConstructorRightNull() {
        Equation eq = new Equation(new Variable(new Sort("nat"), "x"), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEquationConstructorNull() {
        Equation eq = new Equation(null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEquationConstructorSortsNotEqual() {
        Equation eq = new Equation(new Variable(new Sort("ada"), "x"), new Variable(new Sort("nat"), "x"));
    }

    @Test
    public void testGetLeft() {
        Variable var = new Variable(new Sort("nat"), "x");
        Variable var2 = new Variable(new Sort("nat"), "y");
        Equation eq = new Equation(var, var2);
        Assert.assertEquals(var, eq.getLeft());
    }

    @Test
    public void testGetRight() {
        Variable var = new Variable(new Sort("nat"), "x");
        Variable var2 = new Variable(new Sort("nat"), "y");
        Equation eq = new Equation(var, var2);
        Assert.assertEquals(var2, eq.getRight());
    }

    @Test
    public void testGetSort() {
        Sort sort = new Sort("nat");
        Variable var = new Variable(sort, "x");
        Variable var2 = new Variable(sort, "y");
        Equation eq = new Equation(var, var2);
        Assert.assertEquals(sort, eq.getSort());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSubstituteNullFirst() {
        Sort sort = new Sort("nat");
        Variable var = new Variable(sort, "x");
        Variable var2 = new Variable(sort, "y");
        Equation eq = new Equation(var, var2);
        eq.substitute(null, var2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSubstituteNullSecond() {
        Sort sort = new Sort("nat");
        Variable var = new Variable(sort, "x");
        Variable var2 = new Variable(sort, "y");
        Equation eq = new Equation(var, var2);
        eq.substitute(var, null);
    }

    @Test
    public void testSubstitute() {
        Sort sort = new Sort("nat");
        Variable var = new Variable(sort, "x");
        Variable var2 = new Variable(sort, "y");
        Equation eq = new Equation(var, var2);

        Function zero = new Function("0", sort);
        FunctionTerm z = new FunctionTerm(zero);

        Equation newEq = eq.substitute(var, z);

        Assert.assertEquals(z, newEq.getLeft());
        Assert.assertEquals(var2, newEq.getRight());

        Equation nnewEq = newEq.substitute(var2, z);

        Assert.assertEquals(z, nnewEq.getLeft());
        Assert.assertEquals(z, nnewEq.getRight());
    }
}
