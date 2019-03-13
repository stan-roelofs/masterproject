import org.junit.Test;

public class TermTests {

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorException() {
        Term t = new Variable(null, "x");
    }
}
