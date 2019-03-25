import org.junit.Assert;
import org.junit.Test;

public class TermTests {

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorException() {
        Term t = new Variable(null, "x");
    }

    @Test
    public void testGetSort() {
        Sort sort = new Sort("nat");
        Variable v = new Variable(sort, "x");
        Assert.assertEquals(sort, v.getSort());

        Function f = new Function(sort, "0");
        FunctionTerm zero = new FunctionTerm(f);
        Assert.assertEquals(sort, zero.getSort());
    }
}
