import core.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

        Function zero = new Function(sort, "0");
        FunctionTerm z = new FunctionTerm(zero);

        Equation newEq = eq.substitute(var, z);

        Assert.assertEquals(z, newEq.getLeft());
        Assert.assertEquals(var2, newEq.getRight());

        Equation nnewEq = newEq.substitute(var2, z);

        Assert.assertEquals(z, nnewEq.getLeft());
        Assert.assertEquals(z, nnewEq.getRight());
    }

    @Test
    public void testGetFunctions() {
        Sort s = new Sort("nat");
        Function f = new Function(s, "0");
        Function f2 = new Function(s, "1");

        Term t = new FunctionTerm(f);
        Term t2 = new FunctionTerm(f2);

        Equation eq = new Equation(t, t2);

        Set<Function> expected = new HashSet<>();
        expected.add(f);
        expected.add(f2);

        Assert.assertEquals(expected, eq.getFunctions());
    }

    @Test
    public void testToString() {
        Sort s = new Sort("sort");
        Variable var = new Variable(s, "x");
        Variable var2 = new Variable(s, "y");
        Equation eq = new Equation(var, var2);

        Assert.assertEquals("x = y", eq.toString());
    }

    @Test
    public void testEqualsNull() {
        Sort sort = new Sort("nat");
        Variable var = new Variable(sort, "x");
        Variable var2 = new Variable(sort, "y");
        Equation eq = new Equation(var, var2);
        Assert.assertNotEquals(null, eq);
    }

    @Test
    public void testEqualsReflexivity() {
        Sort sort = new Sort("nat");
        Variable var = new Variable(sort, "x");
        Variable var2 = new Variable(sort, "y");
        Equation eq = new Equation(var, var2);
        Assert.assertEquals(eq, eq);
    }

    @Test
    public void testEqualsDifferentNotEqual() {
        Sort sort = new Sort("nat");
        Variable var = new Variable(sort, "x");
        Variable var2 = new Variable(sort, "y");
        Equation eq = new Equation(var, var2);
        Equation eq2 = new Equation(var2, var);

        Assert.assertNotEquals(eq2, eq);
    }

    @Test
    public void testEqualsDifferentEqual() {
        Sort sort = new Sort("nat");
        Variable var = new Variable(sort, "x");
        Variable var2 = new Variable(sort, "y");
        Equation eq = new Equation(var, var2);
        Equation eq2 = new Equation(var, var2);

        Assert.assertEquals(eq2, eq);
    }

    @Test
    public void testEquivalentSimpleBidirectional() {
        Sort sort = new Sort("nat");
        Variable var = new Variable(sort, "x");
        Variable var2 = new Variable(sort, "y");
        Variable var3 = new Variable(sort, "a");
        Variable var4 = new Variable(sort, "b");
        Equation eq = new Equation(var, var2);
        Equation eq2 = new Equation(var3, var4);

        Assert.assertTrue(eq.equivalent(eq2, true));
    }

    @Test
    public void testEquivalentSimple() {
        Sort sort = new Sort("nat");
        Variable var = new Variable(sort, "x");
        Variable var2 = new Variable(sort, "y");
        Variable var3 = new Variable(sort, "a");
        Variable var4 = new Variable(sort, "b");
        Equation eq = new Equation(var, var2);
        Equation eq2 = new Equation(var3, var4);

        Assert.assertTrue(eq.equivalent(eq2, false));
    }

    @Test
    public void testEquivalentBidirectional() {
        List<Sort> inputs = new ArrayList<>();
        Sort s = new Sort("nat");
        inputs.add(s);
        Function f = new Function(s, inputs, "f");
        Function g = new Function(s, inputs, "g");
        List<Term> subterms = new ArrayList<>();
        Variable x = new Variable(s, "x");
        subterms.add(x);
        Term t = new FunctionTerm(f, subterms); // f(x)
        Term t2 = new FunctionTerm(g, subterms); // g(x)

        Equation eq = new Equation(t, t2); // f(x) = g(x)

        Variable y = new Variable(s, "y");
        subterms.clear();
        subterms.add(y);
        Term t3 = new FunctionTerm(f, subterms); // f(y)
        Term t4 = new FunctionTerm(g, subterms); // g(y)
        Equation eq2 = new Equation(t3, t4); // f(y) = g(y)

        Equation eq3 = new Equation(t4, t3); //g(y) = f(y)

        Assert.assertTrue(eq.equivalent(eq2, true));
        Assert.assertTrue(eq.equivalent(eq3, true));
    }

    @Test
    public void testEquivalentUnidirectional() {
        List<Sort> inputs = new ArrayList<>();
        Sort s = new Sort("nat");
        inputs.add(s);
        Function f = new Function(s, inputs, "f");
        Function g = new Function(s, inputs, "g");
        List<Term> subterms = new ArrayList<>();
        Variable x = new Variable(s, "x");
        subterms.add(x);
        Term t = new FunctionTerm(f, subterms); // f(x)
        Term t2 = new FunctionTerm(g, subterms); // g(x)

        Equation eq = new Equation(t, t2); // f(x) = g(x)

        Variable y = new Variable(s, "y");
        subterms.clear();
        subterms.add(y);
        Term t3 = new FunctionTerm(f, subterms); // f(y)
        Term t4 = new FunctionTerm(g, subterms); // g(y)
        Equation eq2 = new Equation(t3, t4); // f(y) = g(y)
        Equation eq3 = new Equation(t4, t3); //g(y) = f(y)

        Assert.assertTrue(eq.equivalent(eq2, false));
        Assert.assertFalse(eq.equivalent(eq3, false));
    }

    @Test
    public void testEquivalentFalse() {
        Sort sort = new Sort("nat");
        Variable var = new Variable(sort, "x");
        Variable var2 = new Variable(sort, "y");
        Variable var3 = new Variable(sort, "a");
        Equation eq = new Equation(var, var2);
        Equation eq2 = new Equation(var3, var3);

        Assert.assertFalse(eq.equivalent(eq2, true));
    }
}


