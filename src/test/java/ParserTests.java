import core.Function;
import core.Sort;
import core.parsing.InputParser;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashSet;
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
        Function f = InputParser.parseFunction("true 0 nat", sigma, C);
        Assert.assertTrue(sigma.contains(f));
        Assert.assertTrue(C.contains(f));
    }


}
