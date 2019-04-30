import core.*;
import core.parsing.InputParser;
import core.parsing.InvalidFunctionArgumentException;
import core.parsing.NoSortException;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ParserTests {

    @Test(expected = IllegalArgumentException.class)
    public void testParseFunctionNullString() {
        Function f = InputParser.parseFunction(null, null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseFunctionEmptyString() {
        Function f = InputParser.parseFunction("", null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseFunctionNullSigma() {
        Function f = InputParser.parseFunction("0 nat", null, new HashSet<>());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseFunctionNullC() {
        Function f = InputParser.parseFunction("0 nat", new HashSet<>(), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseFunctionNoSorts() {
        Function f = InputParser.parseFunction("f", new HashSet<>(), new HashSet<>());
    }

    @Test
    public void testParseFunctionConstant() {
        Function f = InputParser.parseFunction("0 nat", new HashSet<>(), new HashSet<>());

        Function expected = new Function(new Sort("nat"), "0");

        Assert.assertEquals(expected, f);
    }

    @Test
    public void testParseFunctionAddSigma() {
        Set<Function> sigma = new HashSet<>();
        Function f = InputParser.parseFunction("0 nat", sigma, new HashSet<>());
        Assert.assertTrue(sigma.contains(f));
    }

    @Test
    public void testParseFunctionAddC() {
        Set<Function> sigma = new HashSet<>();
        Set<Function> C = new HashSet<>();
        Function f = InputParser.parseFunction("** 0 nat", sigma, C);
        Assert.assertTrue(sigma.contains(f));
        Assert.assertTrue(C.contains(f));
    }

    @Test
    public void testParseFunctionInputSorts() {
        Set<Function> sigma = new HashSet<>();
        Set<Function> C = new HashSet<>();

        Sort nat = new Sort("nat");
        Function f = InputParser.parseFunction("x nat nat", sigma, C);

        List<Sort> sorts = new ArrayList<>();
        sorts.add(nat);
        Function expected = new Function(nat, sorts, "x");
        Assert.assertEquals(expected, f);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseFunctionAddCMissingInformation() {
        Set<Function> sigma = new HashSet<>();
        Set<Function> C = new HashSet<>();
        Function f = InputParser.parseFunction("** 0", sigma, C);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseEquationFunctionsNull() throws NoSortException, InvalidFunctionArgumentException {
        InputParser.parseEquation(null, "x = y");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseEquationLineNull() throws NoSortException, InvalidFunctionArgumentException {
        InputParser.parseEquation(new HashSet<>(), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseEquationLineEmpty() throws NoSortException, InvalidFunctionArgumentException {
        InputParser.parseEquation(new HashSet<>(), "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseEquationLineNoEquals() throws NoSortException, InvalidFunctionArgumentException {
        InputParser.parseEquation(new HashSet<>(), "x");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseEquationLineMultipleEquals() throws NoSortException, InvalidFunctionArgumentException {
        InputParser.parseEquation(new HashSet<>(), "x = y = z");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseEquationLineLeftEmpty() throws NoSortException, InvalidFunctionArgumentException {
        InputParser.parseEquation(new HashSet<>(), "= y");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseEquationLineRightEmpty() throws NoSortException, InvalidFunctionArgumentException {
        InputParser.parseEquation(new HashSet<>(), "x =");
    }

    @Test(expected = NoSortException.class)
    public void testParseEquationNoSort() throws NoSortException, InvalidFunctionArgumentException {
        InputParser.parseEquation(new HashSet<>(), "x = y");
    }

    @Test
    public void testParseEquationRightNoSort() throws NoSortException, InvalidFunctionArgumentException {
        Set<Function> fs = new HashSet<>();
        Sort sort = new Sort("nat");
        List<Sort> inputs = new ArrayList<>();
        inputs.add(sort);

        Function f = new Function(sort, inputs, "f");
        fs.add(f);
        Equation eq = InputParser.parseEquation(fs, "f(x) = y");

        Variable x = new Variable(sort, "x");
        List<Term> subterms = new ArrayList<>();
        subterms.add(x);
        FunctionTerm fx = new FunctionTerm(f, subterms);

        Variable y = new Variable(sort, "y");

        Assert.assertEquals(fx, eq.getLeft());
        Assert.assertEquals(y, eq.getRight());
    }

    @Test
    public void testParseEquationLeftNoSort() throws NoSortException, InvalidFunctionArgumentException {
        Set<Function> fs = new HashSet<>();
        Sort sort = new Sort("nat");
        List<Sort> inputs = new ArrayList<>();
        inputs.add(sort);

        Function f = new Function(sort, inputs, "f");
        fs.add(f);
        Equation eq = InputParser.parseEquation(fs, "y = f(x)");

        Variable x = new Variable(sort, "x");
        List<Term> subterms = new ArrayList<>();
        subterms.add(x);
        FunctionTerm fx = new FunctionTerm(f, subterms);

        Variable y = new Variable(sort, "y");

        Assert.assertEquals(y, eq.getLeft());
        Assert.assertEquals(fx, eq.getRight());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseTermFunctionsNull() throws NoSortException, InvalidFunctionArgumentException {
        InputParser.parseTerm(null, "x", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseTermLineNull() throws NoSortException, InvalidFunctionArgumentException {
        InputParser.parseTerm(new HashSet<>(), null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseTermLineEmpty() throws NoSortException, InvalidFunctionArgumentException {
        InputParser.parseTerm(new HashSet<>(), "", null);
    }

    @Test(expected = NoSortException.class)
    public void testParseTermVariableNoSort() throws NoSortException, InvalidFunctionArgumentException {
        InputParser.parseTerm(new HashSet<>(), "x", null);
    }

    @Test
    public void testParseTermVariable() throws NoSortException, InvalidFunctionArgumentException {
        Sort nat = new Sort("nat");
        Term parsed = InputParser.parseTerm(new HashSet<>(), "x", nat);
        Variable expected = new Variable(nat, "x");
        Assert.assertEquals(expected, parsed);
    }

    @Test
    public void testParseTermConstant() throws NoSortException, InvalidFunctionArgumentException {
        Sort nat = new Sort("nat");
        Function zero = new Function(nat, "0");
        Set<Function> functions = new HashSet<>();
        functions.add(zero);

        Term parsed = InputParser.parseTerm(functions, "0", null);
        Term expected = new FunctionTerm(zero);

        Assert.assertEquals(expected, parsed);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseTermFunctionTermInvalidFormat() throws NoSortException, InvalidFunctionArgumentException {
        Sort nat = new Sort("nat");

        List<Sort> inputs = new ArrayList<>();
        inputs.add(nat);

        Function f = new Function(nat, inputs,"f");
        Set<Function> functions = new HashSet<>();
        functions.add(f);

        Variable x = new Variable(nat, "x");
        List<Term> subterms = new ArrayList<>();
        subterms.add(x);

        Term expected = new FunctionTerm(f, subterms);
        Term parsed = InputParser.parseTerm(functions, "f(x", null);

        Assert.assertEquals(expected, parsed);
    }

    @Test(expected = NoSortException.class)
    public void testParseTermFunctionTermDuplicateFunctionNoSort() throws NoSortException, InvalidFunctionArgumentException {
        Sort nat = new Sort("nat");
        Sort nat2 = new Sort("nat2");

        Function zero = new Function(nat, "0");
        Function zero2 = new Function(nat2, "0");

        Set<Function> functions = new HashSet<>();
        functions.add(zero);
        functions.add(zero2);

        InputParser.parseTerm(functions, "0", null);
    }

    @Test
    public void testParseTermFunctionTermDuplicateFunction() throws NoSortException, InvalidFunctionArgumentException {
        Sort nat = new Sort("nat");
        Sort nat2 = new Sort("nat2");

        Function zero = new Function(nat, "0");
        Function zero2 = new Function(nat2, "0");

        Set<Function> functions = new HashSet<>();
        functions.add(zero);
        functions.add(zero2);

        Term parsed = InputParser.parseTerm(functions, "0", nat);
        Term expected = new FunctionTerm(zero);

        Assert.assertEquals(expected, parsed);
    }

    @Test
    public void testParseTermFunctionTermDuplicateFunctionNested() throws NoSortException, InvalidFunctionArgumentException {
        Sort nat = new Sort("nat");
        Sort nat2 = new Sort("nat2");

        Function zero = new Function(nat, "0");
        Function zero2 = new Function(nat2, "0");

        Set<Function> functions = new HashSet<>();
        functions.add(zero);
        functions.add(zero2);

        List<Sort> inputs = new ArrayList<>();
        inputs.add(nat);

        Function f = new Function(nat, inputs,"f");
        functions.add(f);

        FunctionTerm zeroTerm = new FunctionTerm(zero);
        List<Term> subterms = new ArrayList<>();
        subterms.add(zeroTerm);

        Term expected = new FunctionTerm(f, subterms);
        Term parsed = InputParser.parseTerm(functions, "f(0)", null);

        Assert.assertEquals(expected, parsed);
    }

    @Test
    public void testParseTermFunctionTermVariable() throws NoSortException, InvalidFunctionArgumentException {
        Sort nat = new Sort("nat");

        List<Sort> inputs = new ArrayList<>();
        inputs.add(nat);

        Function f = new Function(nat, inputs,"f");
        Set<Function> functions = new HashSet<>();
        functions.add(f);

        Variable x = new Variable(nat, "x");
        List<Term> subterms = new ArrayList<>();
        subterms.add(x);

        Term expected = new FunctionTerm(f, subterms);
        Term parsed = InputParser.parseTerm(functions, "f(x)", null);

        Assert.assertEquals(expected, parsed);
    }

    @Test
    public void testParseTermFunctionTermConstant() throws NoSortException, InvalidFunctionArgumentException {
        Sort nat = new Sort("nat");

        Function zero = new Function(nat, "0");

        List<Sort> inputs = new ArrayList<>();
        inputs.add(nat);

        Function f = new Function(nat, inputs,"f");
        Set<Function> functions = new HashSet<>();
        functions.add(f);
        functions.add(zero);

        FunctionTerm zeroTerm = new FunctionTerm(zero);
        List<Term> subterms = new ArrayList<>();
        subterms.add(zeroTerm);

        Term expected = new FunctionTerm(f, subterms);
        Term parsed = InputParser.parseTerm(functions, "f(0)", null);

        Assert.assertEquals(expected, parsed);
    }

    @Test
    public void testParseTermFunctionTermNestedVariable() throws NoSortException, InvalidFunctionArgumentException {
        Sort nat = new Sort("nat");

        List<Sort> inputs = new ArrayList<>();
        inputs.add(nat);

        Function f = new Function(nat, inputs,"f");
        Set<Function> functions = new HashSet<>();
        functions.add(f);

        Variable x = new Variable(nat, "x");
        List<Term> subterms = new ArrayList<>();
        subterms.add(x);

        Term fx = new FunctionTerm(f, subterms);
        subterms.clear();
        subterms.add(fx);
        Term expected = new FunctionTerm(f, subterms);

        Term parsed = InputParser.parseTerm(functions, "f(f(x))", null);

        Assert.assertEquals(expected, parsed);
    }

    @Test
    public void testParseTermFunctionTermNestedConstant() throws NoSortException, InvalidFunctionArgumentException {
        Sort nat = new Sort("nat");
        Function zero = new Function(nat, "0");

        List<Sort> inputs = new ArrayList<>();
        inputs.add(nat);

        Function f = new Function(nat, inputs,"f");
        Set<Function> functions = new HashSet<>();
        functions.add(f);
        functions.add(zero);

        FunctionTerm zeroTerm = new FunctionTerm(zero);
        List<Term> subterms = new ArrayList<>();
        subterms.add(zeroTerm);

        Term f0 = new FunctionTerm(f, subterms);
        subterms.clear();
        subterms.add(f0);
        Term expected = new FunctionTerm(f, subterms);

        Term parsed = InputParser.parseTerm(functions, "f(f(0))", null);

        Assert.assertEquals(expected, parsed);
    }

    @Test
    public void testParseTermFunctionTermMultipleSubterms() throws NoSortException, InvalidFunctionArgumentException {
        Sort nat = new Sort("nat");

        Function zero = new Function(nat, "0");

        List<Sort> inputs = new ArrayList<>();
        inputs.add(nat);
        inputs.add(nat);

        Function f = new Function(nat, inputs,"f");
        Set<Function> functions = new HashSet<>();
        functions.add(f);
        functions.add(zero);

        FunctionTerm zeroTerm = new FunctionTerm(zero);
        Variable x = new Variable(nat, "x");
        List<Term> subterms = new ArrayList<>();
        subterms.add(zeroTerm);
        subterms.add(x);

        Term expected = new FunctionTerm(f, subterms);
        Term parsed = InputParser.parseTerm(functions, "f(0, x)", null);

        Assert.assertEquals(expected, parsed);
    }

}
