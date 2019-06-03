import core.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

public class VariableTests {


    @Test(expected = IllegalArgumentException.class)
    public void testConstructor() {
        Variable v = new Variable(new Sort("x"), "");
    }

    @Test
    public void testGetName() {
        Variable v = new Variable(new Sort("x"), "x");
        Assert.assertEquals("x", v.getName());
    }

    /**
     * If term is null we expect an IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void testGetSubstitutionTermNull() {
        Variable v1 = new Variable(new Sort("nat"), "x");
        v1.getSubstitution(null, new HashMap<>());
    }

    /**
     * If subs is null we expect an IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void testGetSubstitutionSubsNull() {
        Variable v1 = new Variable(new Sort("nat"), "x");
        v1.getSubstitution(v1, null);
    }

    /**
     * If x is not in the current substitutions map and y is of the same sort
     * then x.getSubstitution(y, subs) should return x=y
     */
    @Test
    public void testGetSubstitution() {
        Sort s = new Sort("nat");
        Variable v1 = new Variable(s, "x");
        Variable v2 = new Variable(s, "y");
        Map<Variable, Term> subs = new HashMap<>();
        Map<Variable, Term> expected = new HashMap<>();
        expected.put(v1, v2);
        Assert.assertEquals(expected, v1.getSubstitution(v2, subs));
    }

    /**
     * If x is already occupied in the current substitutions map for a substitution x=y
     * then x.getSubstitution(y, subs) should return x=y instead of null
     */
    @Test
    public void testGetSubstitutionCollisionSameTerm() {
        Sort s = new Sort("nat");
        Variable v1 = new Variable(s, "x");
        Variable v2 = new Variable(s, "y");
        Map<Variable, Term> subs = new HashMap<>();
        Map<Variable, Term> expected = new HashMap<>();
        expected.put(v1, v2);
        subs.put(v1, v2);
        Assert.assertEquals(expected, v1.getSubstitution(v2, subs));
    }

    /**
     * If x is already occupied in the current substitutions map for a substitution x=y
     * then x.getSubstitution(z, subs) should return null since x=y and x=z cannot hold
     * at the same time
     */
    @Test
    public void testGetSubstitutionCollisionDifferentTerm() {
        Sort s = new Sort("nat");
        Variable v1 = new Variable(s, "x");
        Variable v2 = new Variable(s, "y");
        Map<Variable, Term> subs = new HashMap<>();
        subs.put(v1, v2);
        Assert.assertNull(v1.getSubstitution(v1, subs));
    }

    /**
     * If terms are of a different sort a substitution is not possible
     * so null should be returned
     */
    @Test
    public void testGetSubstitutionDifferentSort() {
        Sort s = new Sort("nat");
        Variable v1 = new Variable(s, "x");
        Sort s2 = new Sort("string");
        Variable v2 = new Variable(s2, "x");
        Assert.assertNull(v1.getSubstitution(v2, new HashMap<>()));
    }

    /**
     * If substitution is null, expect IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void testApplySubstitutionNull() {
        Sort s = new Sort("nat");
        Variable v = new Variable(s, "x");
        v.applySubstitution(null);
    }

    /**
     * If substitution contains a variable and applySubstitution is called on that variable
     * we expect substitution.get(variable) to be returned
     */
    @Test
    public void testApplySubstitutionApplied() {
        Sort s = new Sort("nat");
        Variable v = new Variable(s, "x");
        Variable v2 = new Variable(s, "y");
        Map<Variable, Term> subs = new HashMap<>();
        subs.put(v, v2);
        Assert.assertEquals(v2, v.applySubstitution(subs));
    }

    /**
     * If substitution does not contain a variable and applySubstitution is called on that variable
     * we expect no changes
     */
    @Test
    public void testApplySubstitutionNotApplied() {
        Sort s = new Sort("nat");
        Variable v = new Variable(s, "x");
        Variable v2 = new Variable(s, "y");
        Map<Variable, Term> subs = new HashMap<>();
        subs.put(v2, v);
        Assert.assertEquals(v, v.applySubstitution(subs));
    }

    /**
     * A variable has no subterms so we expect an empty set here
     */
    @Test
    public void testGetAllSubTerms() {
        Variable v1 = new Variable(new Sort("x"), "x");
        Assert.assertTrue(v1.getAllSubTerms().isEmpty());
    }

    /**
     * If null is used as term expect IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void testSubstituteNullTerm() {
        Sort s = new Sort("x");
        Variable v = new Variable(s, "x");
        Variable v2 = new Variable(s, "y");
        v.substitute(null, v);
    }

    /**
     * If null is used as substitute expect IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void testSubstituteNull() {
        Sort s = new Sort("x");
        Variable v = new Variable(s, "x");
        Variable v2 = new Variable(s, "y");
        v.substitute(v, null);
    }

    /**
     * If substitute.sort does not match x.sort if x.substitute(x, substitute) is called
     * then expect IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void testSubstituteDifferentSort() {
        Sort s = new Sort("x");
        Variable v = new Variable(s, "x");
        Variable v2 = new Variable(new Sort("y"), "y");
        v.substitute(v, v2);
    }

    /**
     * If we call x.substitute(x, term) then expect
     * term to be returned
     */
    @Test
    public void testSubstituteVariable() {
        Sort s = new Sort("x");
        Variable v = new Variable(s, "x");
        Variable v2 = new Variable(s, "y");
        Assert.assertEquals("y", v.substitute(v, v2).toString());
    }

    /**
     * If we call x.substitute(y, term) and x != y then expect
     * x to be returned (no substitution happens)
     */
    @Test
    public void testSubstituteDifferentVariable() {
        Sort s = new Sort("x");
        Variable v = new Variable(s, "x");
        Variable v2 = new Variable(s, "y");
        Assert.assertEquals(v.toString(), v.substitute(v2, v2).toString());
    }

    /**
     * Tests substitution of x for a functionterm of the same sort
     */
    @Test
    public void testSubstituteFunctionTerm() {
        // Create sort
        Sort nat = new Sort("x");

        // Create variable of this sort called "x"
        Variable v = new Variable(nat, "x");
        Term term = v;

        // Create a constant zero of this sort
        Function zero = new Function(nat, "0");

        // Create a function "f" of this sort with one input
        List<Sort> input = new ArrayList<>();
        input.add(nat);
        Function f = new Function(nat, input, "f");

        // Create a term 0, then a term f(0)
        Term zeroo = new FunctionTerm(zero);
        List<Term> subterms = new ArrayList<>();
        subterms.add(zeroo);
        Term f0 = new FunctionTerm(f, subterms);

        // Substitute x with f(0)
        Term newt = term.substitute(v, f0);

        Assert.assertEquals("f(0)", newt.toString());
    }

    /**
     * getVariables should return a collection that contains just
     * the variable and nothing else
     */
    @Test
    public void testGetVariables() {
        Variable v = new Variable(new Sort("x"), "x");
        Set<Variable> vs = new HashSet<>();
        vs.add(v);
        Assert.assertEquals(vs, v.getVariables());
    }

    /**
     * getUniqueFunctions should return an empty set as a variable
     * does not contain any functions
     */
    @Test
    public void testGetFunctions() {
        Variable v = new Variable(new Sort("x"), "x");
        Assert.assertTrue(v.getUniqueFunctions().isEmpty());
    }

    @Test
    public void testToString() {
        Variable v = new Variable(new Sort("x"), "name123");
        Assert.assertEquals(v.getName(), "name123");
    }

    /**
     * Any variable should not be equal to null
     */
    @Test
    public void testEqualsNull() {
        Variable v = new Variable(new Sort("x"), "x");
        Assert.assertNotEquals(null, v);
    }

    /**
     * Any variable should be equal to itself
     */
    @Test
    public void testEqualsReflexivity() {
        Variable v = new Variable(new Sort("x"), "x");
        Assert.assertEquals(v, v);
    }

    /**
     * Any variable should be equal to an identical variable where the only difference
     * is the reference
     */
    @Test
    public void testEqualsCopy() {
        Sort s = new Sort("x");
        Variable v = new Variable(s, "x");
        Variable v2 = new Variable(s, "x");
        Assert.assertEquals(v, v2);
    }

    /**
     * Two variables should not be the same if their sorts are different
     */
    @Test
    public void testEqualsSortDifference() {
        Sort s = new Sort("x");
        Variable v = new Variable(s, "x");
        Sort s2 = new Sort("y");
        Variable v2 = new Variable(s2, "x");
        Assert.assertNotEquals(v, v2);
    }

    /**
     * Two variables should not be the same if their names are different
     */
    @Test
    public void testEqualsNameDifference() {
        Sort s = new Sort("x");
        Variable v = new Variable(s, "x");
        Variable v2 = new Variable(s, "y");
        Assert.assertNotEquals(v, v2);
    }
}
