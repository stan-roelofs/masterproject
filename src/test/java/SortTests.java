import org.junit.Assert;
import org.junit.Test;

public class SortTests {

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorException() {
        Sort sort = new Sort(null);
    }

    @Test
    public void testGetName() {
        Sort sort = new Sort("1234a");
        Assert.assertEquals("1234a", sort.getName());
    }

    @Test
    public void testEqualsReflexivity() {
        Sort sort = new Sort("sort");
        Assert.assertEquals(sort, sort);
    }

    @Test
    public void testEqualsNull() {
        Sort sort = new Sort("sort");
        Assert.assertNotEquals(null, sort);
    }

    @Test
    public void testEquals() {
        Sort sort = new Sort("sort");
        Sort sort2 = new Sort("sort");
        Assert.assertEquals(sort2, sort);
    }

    @Test
    public void testEqualsNameDifference() {
        Sort sort = new Sort("sort");
        Sort sort2 = new Sort("sort2");
        Assert.assertNotEquals(sort2, sort);
    }
}
