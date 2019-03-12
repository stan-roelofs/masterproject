import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;

public class FunctionTermTests {

    @Test
    public void testEqualsNull() {
        Function f = new Function("f", new Sort());
        FunctionTerm ft = new FunctionTerm(f);

        Assert.assertNotEquals(null, ft);
    }

    @Test
    public void testEqualsReflexivity() {
        Function f = new Function("f", new Sort());
        FunctionTerm ft = new FunctionTerm(f);

        Assert.assertEquals(ft, ft);
    }

    @Test
    public void testEqualsCopy() {
        Function f = new Function("f", new Sort());
        FunctionTerm ft = new FunctionTerm(f);
        FunctionTerm ft2 = new FunctionTerm(f);

        Assert.assertEquals(ft2, ft);
    }

    @Test
    public void testEqualsCopy2() {
        Sort s = new Sort();
        Collection<Sort> sorts = new ArrayList<>();
        sorts.add(s);
        Function f = new Function("f", sorts, s);

        Collection<Term> terms = new ArrayList<>();
        terms.add(new Variable(s, "x"));
        FunctionTerm ft = new FunctionTerm(f, terms);
        terms.clear();
        terms.add(new Variable(s, "x"));
        FunctionTerm ft2 = new FunctionTerm(f, terms);

        Assert.assertEquals(ft2, ft);
    }

    @Test
    public void testEqualsSortDifference() {
        Function f = new Function("f", new Sort());
        FunctionTerm ft = new FunctionTerm(f);

        Function f2 = new Function("g", new Sort());
        FunctionTerm ft2 = new FunctionTerm(f2);

        Assert.assertNotEquals(ft2, ft);
    }

    @Test
    public void testEqualsFunctionDifference() {
        Sort s = new Sort();
        Function f = new Function("f", s);
        FunctionTerm ft = new FunctionTerm(f);

        Function f2 = new Function("g", s);
        FunctionTerm ft2 = new FunctionTerm(f2);

        Assert.assertNotEquals(ft2, ft);
    }

    @Test
    public void testEqualsSubtermsDifference() {
        Sort s = new Sort();
        Collection<Sort> sorts = new ArrayList<>();
        sorts.add(s);
        Function f = new Function("f", sorts, s);

        Collection<Term> terms = new ArrayList<>();
        terms.add(new Variable(s, "x"));
        FunctionTerm ft = new FunctionTerm(f, terms);
        terms.clear();
        terms.add(new Variable(s, "y"));
        FunctionTerm ft2 = new FunctionTerm(f, terms);

        Assert.assertNotEquals(ft2, ft);
    }

    @Test
    public void testEqualsSubtermsSizeDifference() {
        Sort s = new Sort();
        Collection<Sort> sorts = new ArrayList<>();
        sorts.add(s);
        Function f = new Function("f", sorts, s);

        sorts.add(s);
        Function f2 = new Function("f", sorts, s);

        Collection<Term> terms = new ArrayList<>();
        terms.add(new Variable(s, "x"));
        FunctionTerm ft = new FunctionTerm(f, terms);
        terms.add(new Variable(s, "y"));
        FunctionTerm ft2 = new FunctionTerm(f2, terms);

        Assert.assertNotEquals(ft2, ft);
    }
}
