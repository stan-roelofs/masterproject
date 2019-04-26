import core.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

public class FunctionTermTests {

    /**
     * If a function is given as argument to the constructor with >0 inputs
     * and no subterms are given as argument we expect an IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorFunctionException() {
        List<Sort> inputs = new ArrayList<>();
        inputs.add(new Sort("x"));
        inputs.add(new Sort("y"));
        Function f = new Function("f", inputs, new Sort("z"));
        Term t = new FunctionTerm(f);
    }

    /**
     * If null is given as subterms, expect IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorFunctionTermsNullException() {
        List<Sort> inputs = new ArrayList<>();
        inputs.add(new Sort("x"));
        inputs.add(new Sort("y"));
        Function f = new Function("f", inputs, new Sort("z"));
        Term t = new FunctionTerm(f, null);
    }

    /**
     * If the number of inputs of a function does not match
     * the number of subterms expect IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorFunctionTermsSizeException() {
        List<Sort> inputs = new ArrayList<>();
        inputs.add(new Sort("x"));
        inputs.add(new Sort("z"));
        Function f = new Function("f", inputs, new Sort("x"));
        Term t = new FunctionTerm(f, new ArrayList<>());
    }

    @Test
    public void testGetFunction() {
        Function f = new Function(new Sort("sort"), "f");
        FunctionTerm t = new FunctionTerm(f);
        Assert.assertEquals(f, t.getFunction());
    }

    @Test
    public void testGetSubterms() {
        List<Sort> inputs = new ArrayList<>();
        Sort s = new Sort("x");
        inputs.add(s);
        Function f = new Function("f", inputs, s);
        List<Term> subterms = new ArrayList<>();
        Variable x = new Variable(s, "x");
        subterms.add(x);
        FunctionTerm t = new FunctionTerm(f, subterms);
        Assert.assertEquals(subterms, t.getSubTerms());
    }

    @Test
    public void testGetAllSubterms() {
        List<Sort> inputs = new ArrayList<>();
        Sort s = new Sort("x");
        inputs.add(s);
        Function f = new Function("f", inputs, s);
        List<Term> subterms = new ArrayList<>();
        Variable x = new Variable(s, "x");
        subterms.add(x);
        FunctionTerm t = new FunctionTerm(f, subterms);
        subterms.clear();
        subterms.add(t);
        FunctionTerm t2 = new FunctionTerm(f, subterms);

        Set<Term> expected = new HashSet<>();
        expected.add(x);
        expected.add(t);
        expected.add(t2);

        Assert.assertEquals(expected, t2.getAllSubTerms());
    }

    /**
     * If term is null we expect an IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void testGetSubstitutionTermNull() {
        Function f = new Function(new Sort("sort"), "f");
        FunctionTerm t = new FunctionTerm(f);
        t.getSubstitution(null, new HashMap<>());
    }

    /**
     * If subs is null we expect an IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void testGetSubstitutionSubsNull() {
        Function f = new Function(new Sort("sort"), "f");
        FunctionTerm t = new FunctionTerm(f);
        t.getSubstitution(t, null);
    }

    /**
     * If term is a variable a substitution is not possible, so expect null
     */
    @Test
    public void testGetSubstitutionVariable() {
        Function f = new Function(new Sort("sort"), "f");
        FunctionTerm t = new FunctionTerm(f);
        Map<Variable, Term> subs = t.getSubstitution(new Variable(new Sort("sort"), "x"), new HashMap<>());
        Assert.assertNull(subs);
    }

    /**
     * If term does not have the same function a substitution is not possible, so expect null
     */
    @Test
    public void testGetSubstitutionDifferentFunction() {
        Function f = new Function(new Sort("sort"), "f");
        FunctionTerm t = new FunctionTerm(f);
        Function g = new Function(new Sort("nat"), "g");
        FunctionTerm t2 = new FunctionTerm(g);
        Map<Variable, Term> subs = t.getSubstitution(t2, new HashMap<>());
        Assert.assertNull(subs);
    }

    /**
     * If term has the same function, but getSubstitution on one of the subterms fails,
     * expect null as no substitution is possible
     */
    @Test
    public void testGetSubstitutionSubtermsFail() {
        List<Sort> inputs = new ArrayList<>();
        Sort nat = new Sort("nat");
        inputs.add(nat);
        Function s = new Function("s", inputs, nat);
        Function zero = new Function(nat, "0");
        FunctionTerm t = new FunctionTerm(zero); // 0
        List<Term> subterms = new ArrayList<>();
        subterms.add(t);
        FunctionTerm t2 = new FunctionTerm(s, subterms); // s(0)
        subterms.clear();
        subterms.add(new Variable(nat, "x"));
        FunctionTerm t3 = new FunctionTerm(s, subterms); // s(x)
        Map<Variable, Term> subs = t2.getSubstitution(t3, new HashMap<>());
        Assert.assertNull(subs);
    }

    /**
     * If term has the same function, and getSubstitution succeeds on all subterms,
     * expect a valid substitution
     */
    @Test
    public void testGetSubstitutionSubtermsPass() {
        List<Sort> inputs = new ArrayList<>();
        Sort nat = new Sort("nat");
        inputs.add(nat);
        Function s = new Function("s", inputs, nat);
        Function zero = new Function(nat, "0");
        FunctionTerm t = new FunctionTerm(zero); // 0
        List<Term> subterms = new ArrayList<>();
        subterms.add(t);
        FunctionTerm t2 = new FunctionTerm(s, subterms); // s(0)
        subterms.clear();
        Variable x = new Variable(nat, "x");
        subterms.add(x);
        FunctionTerm t3 = new FunctionTerm(s, subterms); // s(x)
        Map<Variable, Term> subs = t3.getSubstitution(t2, new HashMap<>());

        Map<Variable, Term> expected = new HashMap<>();
        expected.put(x, t); // x -> 0

        Assert.assertEquals(expected, subs);
    }

    /**
     * If substitution is null, expect IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void testApplySubstitutionNull() {
        Sort s = new Sort("x");
        Function f = new Function(s, "f");
        Term t = new FunctionTerm(f);
        t.applySubstitution(null);
    }

    /**
     * If substitution contains a variable and applySubstitution is called on a core.FunctionTerm that
     * contains this variable in a subterm we expect the variable to be substituted
     */
    @Test
    public void testApplySubstitutionApplied() {
        List<Sort> inputs = new ArrayList<>();
        Sort s = new Sort("x");
        inputs.add(s);
        Function f = new Function("f", inputs, s);
        List<Term> subterms = new ArrayList<>();
        Variable x = new Variable(s, "x");
        subterms.add(x);
        Term t = new FunctionTerm(f, subterms); // f(x)

        subterms.clear();
        Variable y = new Variable(s, "y");
        subterms.add(y);
        Term t2 = new FunctionTerm(f, subterms);

        Map<Variable, Term> subs = new HashMap<>();
        subs.put(x, y);
        Assert.assertEquals(t2, t.applySubstitution(subs));
    }

    /**
     * If substitution does not contain a variable and applySubstitution is called on that variable
     * we expect no changes
     */
    @Test
    public void testApplySubstitutionNotApplied() {
        List<Sort> inputs = new ArrayList<>();
        Sort s = new Sort("x");
        inputs.add(s);
        Function f = new Function("f", inputs, s);
        List<Term> subterms = new ArrayList<>();
        Variable x = new Variable(s, "x");
        subterms.add(x);
        Term t = new FunctionTerm(f, subterms); // f(x)

        subterms.clear();
        Variable y = new Variable(s, "y");
        subterms.add(y);
        Term t2 = new FunctionTerm(f, subterms);

        Map<Variable, Term> subs = new HashMap<>();
        subs.put(y, x);
        Assert.assertEquals(t, t.applySubstitution(subs));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSubstituteNullFirst() {
        List<Sort> inputs = new ArrayList<>();
        Sort s = new Sort("x");
        inputs.add(s);
        Function f = new Function("f", inputs, s);
        List<Term> subterms = new ArrayList<>();
        Variable x = new Variable(s, "x");
        subterms.add(x);
        Term t = new FunctionTerm(f, subterms);
        t.substitute(null, t);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSubstituteNullSecond() {
        List<Sort> inputs = new ArrayList<>();
        Sort s = new Sort("x");
        inputs.add(s);
        Function f = new Function("f", inputs, s);
        List<Term> subterms = new ArrayList<>();
        Variable x = new Variable(s, "x");
        subterms.add(x);
        Term t = new FunctionTerm(f, subterms);
        t.substitute(t, null);
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

    /**
     * If t contains no variables, expect empty list
     */
    @Test
    public void testGetVariablesNone() {
        Sort s = new Sort("x");
        Function f = new Function(s, "f");
        Term t = new FunctionTerm(f);
        Set<Variable> vs = new HashSet<>();
        Assert.assertEquals(vs, t.getVariables());
    }

    /**
     * If t has a single variable as direct subterm
     */
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

    /**
     * If t has a single variable as subterm of a subterm
     */
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

    /**
     * If t has multiple variables in subterms
     */
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

    /**
     * If t is a constant
     */
    @Test
    public void testGetFunctionsSimple() {
        Sort s = new Sort("nat");
        Function f = new Function(s, "0");

        Set<Function> expected = new HashSet<>();
        expected.add(f);

        FunctionTerm term = new FunctionTerm(f);

        Assert.assertEquals(expected, term.getFunctions());
    }

    /**
     * If t is a term with only variables as subterms
     */
    @Test
    public void testGetFunctionsSimple2() {
        List<Sort> inputs = new ArrayList<>();
        Sort s = new Sort("x");
        inputs.add(s);
        Function f = new Function("f", inputs, s);
        List<Term> subterms = new ArrayList<>();
        Variable x = new Variable(s, "x");
        subterms.add(x);
        Term t = new FunctionTerm(f, subterms);

        Set<Function> expected = new HashSet<>();
        expected.add(f);
        Assert.assertEquals(expected, t.getFunctions());
    }

    /**
     * If t contains distinct functions in its subterms + itself
     */
    @Test
    public void testGetFunctionsDeep() {
        List<Sort> inputs = new ArrayList<>();
        Sort s = new Sort("x");
        inputs.add(s);
        Function f = new Function("f", inputs, s);
        Function g = new Function("g", inputs, s);
        List<Term> subterms = new ArrayList<>();
        Variable x = new Variable(s, "x");
        subterms.add(x);
        Term t = new FunctionTerm(f, subterms); // f(x)
        Term t2 = new FunctionTerm(g, subterms); // g(x)

        Term t3 = t.substitute(x, t2); // f(g(x))

        Set<Function> expected = new HashSet<>();
        expected.add(f);
        expected.add(g);

        Assert.assertEquals(expected, t3.getFunctions());
    }

    @Test
    public void testToStringConstant() {
        Sort s = new Sort("x");
        Function f = new Function(s, "f");
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
        Function f = new Function(new Sort("x"), "f");
        FunctionTerm ft = new FunctionTerm(f);

        Assert.assertNotEquals(null, ft);
    }

    @Test
    public void testEqualsReflexivity() {
        Function f = new Function(new Sort("x"), "f");
        FunctionTerm ft = new FunctionTerm(f);

        Assert.assertEquals(ft, ft);
    }

    @Test
    public void testEqualsCopy() {
        Function f = new Function(new Sort("x"), "f");
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
        Function f = new Function(new Sort("x"), "f");
        FunctionTerm ft = new FunctionTerm(f);

        Function f2 = new Function(new Sort("x"), "g");
        FunctionTerm ft2 = new FunctionTerm(f2);

        Assert.assertNotEquals(ft2, ft);
    }

    @Test
    public void testEqualsFunctionDifference() {
        Sort s = new Sort("x");
        Function f = new Function(s, "f");
        FunctionTerm ft = new FunctionTerm(f);

        Function f2 = new Function(s, "g");
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
