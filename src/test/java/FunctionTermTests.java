import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FunctionTermTests {

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorFunctionException() {
        List<Sort> inputs = new ArrayList<>();
        inputs.add(new Sort("x"));
        inputs.add(new Sort("y"));
        Function f = new Function("f", inputs, new Sort("z"));
        Term t = new FunctionTerm(f);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorFunctionTermsNullException() {
        List<Sort> inputs = new ArrayList<>();
        inputs.add(new Sort("x"));
        inputs.add(new Sort("y"));
        Function f = new Function("f", inputs, new Sort("z"));
        Term t = new FunctionTerm(f, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorFunctionTermsSizeException() {
        List<Sort> inputs = new ArrayList<>();
        inputs.add(new Sort("x"));
        inputs.add(new Sort("z"));
        Function f = new Function("f", inputs, new Sort("x"));
        Term t = new FunctionTerm(f, new ArrayList<>());
    }

    @Test
    public void testSubstituteEqual() {
        List<Sort> inputs = new ArrayList<>();
        Sort s = new Sort("x");
        inputs.add(s);
        Function f = new Function("f", inputs, s);
        List<Term> subterms = new ArrayList<>();
        Variable x = new Variable(s, "x");
        subterms.add(x);
        Term t = new FunctionTerm(f, subterms);
        Assert.assertEquals(t.toString(), t.substitute(new Variable(s, "y"), t).toString());
    }

    @Test
    public void testSubstituteSimple() {
        List<Sort> inputs = new ArrayList<>();
        Sort s = new Sort("x");
        inputs.add(s);
        Function f = new Function("f", inputs, s);
        List<Term> subterms = new ArrayList<>();
        Variable x = new Variable(s, "x");
        subterms.add(x);
        Term t = new FunctionTerm(f, subterms);
        Assert.assertEquals("f(f(x))", t.substitute(x, t).toString());
    }

    @Test
    public void testSubstituteDeep() {
        List<Sort> inputs = new ArrayList<>();
        Sort s = new Sort("x");
        inputs.add(s);
        Function f = new Function("f", inputs, s);
        List<Term> subterms = new ArrayList<>();
        Variable x = new Variable(s, "x");
        subterms.add(x);
        Term t = new FunctionTerm(f, subterms);
        Term t2 = t.substitute(x, t);
        Assert.assertEquals("f(f(f(x)))", t2.substitute(x, t).toString());
    }

    @Test
    public void testGetVariablesNone() {
        Sort s = new Sort("x");
        Function f = new Function("f", s);
        Term t = new FunctionTerm(f);
        Set<Variable> vs = new HashSet<>();
        Assert.assertEquals(vs, t.getVariables());
    }

    @Test
    public void testGetVariablesSimple() {
        List<Sort> inputs = new ArrayList<>();
        Sort s = new Sort("x");
        inputs.add(s);
        Function f = new Function("f", inputs, s);
        List<Term> subterms = new ArrayList<>();
        Variable x = new Variable(s, "x");
        subterms.add(x);
        Term t = new FunctionTerm(f, subterms);
        Set<Variable> vs = new HashSet<>();
        vs.add(x);
        Assert.assertEquals(vs, t.getVariables());
    }

    @Test
    public void testGetVariablesDeep() {
        List<Sort> inputs = new ArrayList<>();
        Sort s = new Sort("x");
        inputs.add(s);
        Function f = new Function("f", inputs, s);
        List<Term> subterms = new ArrayList<>();
        Variable x = new Variable(s, "x");
        subterms.add(x);
        Term t = new FunctionTerm(f, subterms);
        Term t2 = t.substitute(x, t);

        Set<Variable> vs = new HashSet<>();
        vs.add(x);

        Assert.assertEquals(vs, t2.getVariables());
    }

    @Test
    public void testGetVariablesDeepMultiple() {
        List<Sort> inputs = new ArrayList<>();
        Sort s = new Sort("x");
        inputs.add(s);
        inputs.add(s);
        Function f = new Function("f", inputs, s);
        List<Term> subterms = new ArrayList<>();
        Variable x = new Variable(s, "x");
        Variable y = new Variable(s, "y");
        subterms.add(x);
        subterms.add(y);
        Term t = new FunctionTerm(f, subterms);
        Term t2 = t.substitute(x, t);
        Term t3 = t2.substitute(y, t);

        Set<Variable> vs = new HashSet<>();
        vs.add(x);
        vs.add(y);

        Assert.assertEquals(vs, t3.getVariables());
    }

    @Test
    public void testToStringConstant() {
        Sort s = new Sort("x");
        Function f = new Function("f", s);
        Term t = new FunctionTerm(f);
        Assert.assertEquals("f", t.toString());
    }

    @Test
    public void testToStringFunctionSimple() {
        List<Sort> inputs = new ArrayList<>();
        Sort s = new Sort("x");
        inputs.add(s);
        Function f = new Function("f", inputs, s);
        List<Term> subterms = new ArrayList<>();
        Variable x = new Variable(s, "x");
        subterms.add(x);
        Term t = new FunctionTerm(f, subterms);
        Assert.assertEquals("f(x)", t.toString());
    }

    @Test
    public void testToStringFunctionDeep() {
        List<Sort> inputs = new ArrayList<>();
        Sort s = new Sort("x");
        inputs.add(s);
        Function f = new Function("f", inputs, s);
        List<Term> subterms = new ArrayList<>();
        Variable x = new Variable(s, "x");
        subterms.add(x);
        Term t = new FunctionTerm(f, subterms);
        Term t2 = t.substitute(x, t);
        Assert.assertEquals("f(f(x))", t2.toString());
    }

    @Test
    public void testEqualsNull() {
        Function f = new Function("f", new Sort("x"));
        FunctionTerm ft = new FunctionTerm(f);

        Assert.assertNotEquals(null, ft);
    }

    @Test
    public void testEqualsReflexivity() {
        Function f = new Function("f", new Sort("x"));
        FunctionTerm ft = new FunctionTerm(f);

        Assert.assertEquals(ft, ft);
    }

    @Test
    public void testEqualsCopy() {
        Function f = new Function("f", new Sort("x"));
        FunctionTerm ft = new FunctionTerm(f);
        FunctionTerm ft2 = new FunctionTerm(f);

        Assert.assertEquals(ft2, ft);
    }

    @Test
    public void testEqualsCopy2() {
        Sort s = new Sort("x");
        List<Sort> sorts = new ArrayList<>();
        sorts.add(s);
        Function f = new Function("f", sorts, s);

        List<Term> terms = new ArrayList<>();
        terms.add(new Variable(s, "x"));
        FunctionTerm ft = new FunctionTerm(f, terms);
        terms.clear();
        terms.add(new Variable(s, "x"));
        FunctionTerm ft2 = new FunctionTerm(f, terms);

        Assert.assertEquals(ft2, ft);
    }

    @Test
    public void testEqualsSortDifference() {
        Function f = new Function("f", new Sort("x"));
        FunctionTerm ft = new FunctionTerm(f);

        Function f2 = new Function("g", new Sort("x"));
        FunctionTerm ft2 = new FunctionTerm(f2);

        Assert.assertNotEquals(ft2, ft);
    }

    @Test
    public void testEqualsFunctionDifference() {
        Sort s = new Sort("x");
        Function f = new Function("f", s);
        FunctionTerm ft = new FunctionTerm(f);

        Function f2 = new Function("g", s);
        FunctionTerm ft2 = new FunctionTerm(f2);

        Assert.assertNotEquals(ft2, ft);
    }

    @Test
    public void testEqualsSubtermsDifference() {
        Sort s = new Sort("x");
        List<Sort> sorts = new ArrayList<>();
        sorts.add(s);
        Function f = new Function("f", sorts, s);

        List<Term> terms = new ArrayList<>();
        terms.add(new Variable(s, "x"));
        FunctionTerm ft = new FunctionTerm(f, terms);
        terms.clear();
        terms.add(new Variable(s, "y"));
        FunctionTerm ft2 = new FunctionTerm(f, terms);

        Assert.assertNotEquals(ft2, ft);
    }

    @Test
    public void testEqualsSubtermsSizeDifference() {
        Sort s = new Sort("x");
        List<Sort> sorts = new ArrayList<>();
        sorts.add(s);
        Function f = new Function("f", sorts, s);

        sorts.add(s);
        Function f2 = new Function("f", sorts, s);

        List<Term> terms = new ArrayList<>();
        terms.add(new Variable(s, "x"));
        FunctionTerm ft = new FunctionTerm(f, terms);
        terms.add(new Variable(s, "y"));
        FunctionTerm ft2 = new FunctionTerm(f2, terms);

        Assert.assertNotEquals(ft2, ft);
    }
}
