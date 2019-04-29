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
}
