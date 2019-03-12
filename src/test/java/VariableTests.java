import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;

public class VariableTests {
    @Test
    public void testSubstituteVariable() {
        Sort s = new Sort();
        Variable v = new Variable(s, "x");
        Variable v2 = new Variable(s, "y");
        Assert.assertEquals("y", v.substitute(v, v2).toString());
    }

    @Test
    public void testSubstituteDifferentVariable() {
        Sort s = new Sort();
        Variable v = new Variable(s, "x");
        Variable v2 = new Variable(s, "y");
        Assert.assertEquals("x", v.substitute(v2, v2).toString());
    }

    @Test
    public void testSubstituteFunctionTerm() {
        // Create sort
        Sort nat = new Sort();

        // Create variable of this sort called "x"
        Variable v = new Variable(nat, "x");
        Term term = v;

        // Create a constant zero of this sort
        Function zero = new Function("0", nat);

        // Create a function "f" of this sort with one input
        Collection<Sort> input = new ArrayList<>();
        input.add(nat);
        Function f = new Function("f", input, nat);

        // Create a term 0, then a term f(0)
        Term zeroo = new FunctionTerm(zero);
        Collection<Term> subterms = new ArrayList<>();
        subterms.add(zeroo);
        Term f0 = new FunctionTerm(f, subterms);

        // Substitute x with f(0)
        Term newt = term.substitute(v, f0);

        Assert.assertEquals("f(0)", newt.toString());
    }

    @Test
    public void testToString() {
        Variable v = new Variable(new Sort(), "name123");
        Assert.assertEquals(v.getName(), "name123");
    }

    @Test
    public void testEqualsNull() {
        Variable v = new Variable(new Sort(), "x");
        Assert.assertNotEquals(null, v);
    }

    @Test
    public void testEqualsReflexivity() {
        Variable v = new Variable(new Sort(), "x");
        Assert.assertEquals(v, v);
    }

    @Test
    public void testEqualsCopy() {
        Sort s = new Sort();
        Variable v = new Variable(s, "x");
        Variable v2 = new Variable(s, "x");
        Assert.assertEquals(v, v2);
    }

    @Test
    public void testEqualsSortDifference() {
        Sort s = new Sort();
        Variable v = new Variable(s, "x");
        Sort s2 = new Sort();
        Variable v2 = new Variable(s2, "x");
        Assert.assertNotEquals(v, v2);
    }

    @Test
    public void testEqualsNameDifference() {
        Sort s = new Sort();
        Variable v = new Variable(s, "x");
        Variable v2 = new Variable(s, "y");
        Assert.assertNotEquals(v, v2);
    }

    @Test
    public void testEqualsNameSortDifference() {
        Sort s = new Sort();
        Variable v = new Variable(s, "x");
        Sort s2 = new Sort();
        Variable v2 = new Variable(s2, "y");
        Assert.assertNotEquals(v, v2);
    }
}
