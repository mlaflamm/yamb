package yamb.util.io;

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.List;

/**
 * @author manuel.laflamme
 * @since Aug 12, 2008
 */
public class TestAddressNavigationHistory extends TestCase
{
    private List<String> asList(String... aValues)
    {
        return Arrays.asList(aValues);
    }

    public void testAdd() throws Exception
    {
        AddressNavigationHistory history = new AddressNavigationHistory();

        // Setup
        assertEquals("current", null, history.getCurrent());
        assertEquals("back count", 0, history.getBackCount());
        assertEquals("back list", asList(), history.getBackList());
        assertEquals("forward  count", 0, history.getForwardCount());
        assertEquals("forward list", asList(), history.getForwardList());

        // Add a
        history.add("a");
        assertEquals("current", "a", history.getCurrent());
        assertEquals("back count", 0, history.getBackCount());
        assertEquals("back list", asList(), history.getBackList());
        assertEquals("forward  count", 0, history.getForwardCount());
        assertEquals("forward list", asList(), history.getForwardList());

        // Add b
        history.add("b");
        assertEquals("current", "b", history.getCurrent());
        assertEquals("back count", 1, history.getBackCount());
        assertEquals("back list", asList("a"), history.getBackList());
        assertEquals("forward  count", 0, history.getForwardCount());
        assertEquals("forward list", asList(), history.getForwardList());

        // Add c
        history.add("c");
        assertEquals("current", "c", history.getCurrent());
        assertEquals("back count", 2, history.getBackCount());
        assertEquals("back list", asList("a", "b"), history.getBackList());
        assertEquals("forward  count", 0, history.getForwardCount());
        assertEquals("forward list", asList(), history.getForwardList());
    }

    public void testBack() throws Exception
    {
        AddressNavigationHistory history = new AddressNavigationHistory();

        // Back beyond boundary
        try
        {
            history.back();
            fail("Should not be here!");
        }
        catch (IndexOutOfBoundsException e)
        {
            // Expected
        }

        // Setup
        assertEquals("current", null, history.getCurrent());
        assertEquals("back count", 0, history.getBackCount());
        assertEquals("back list", asList(), history.getBackList());
        assertEquals("forward  count", 0, history.getForwardCount());
        assertEquals("forward list", asList(), history.getForwardList());
        history.add("a");
        history.add("b");
        history.add("c");
        assertEquals("current", "c", history.getCurrent());
        assertEquals("back count", 2, history.getBackCount());
        assertEquals("back list", asList("a", "b"), history.getBackList());
        assertEquals("forward  count", 0, history.getForwardCount());
        assertEquals("forward list", asList(), history.getForwardList());

        // Back to b
        assertEquals("back", "b", history.back());
        assertEquals("current", "b", history.getCurrent());
        assertEquals("back count", 1, history.getBackCount());
        assertEquals("back list", asList("a"), history.getBackList());
        assertEquals("forward  count", 1, history.getForwardCount());
        assertEquals("forward list", asList("c"), history.getForwardList());

        // Back to a
        assertEquals("back", "a", history.back());
        assertEquals("current", "a", history.getCurrent());
        assertEquals("back count", 0, history.getBackCount());
        assertEquals("back list", asList(), history.getBackList());
        assertEquals("forward  count", 2, history.getForwardCount());
        assertEquals("forward list", asList("b", "c"), history.getForwardList());

        // Back beyond boundary
        try
        {
            history.back();
            fail("Should not be here!");
        }
        catch (IndexOutOfBoundsException e)
        {
            // Expected
        }
        assertEquals("current", "a", history.getCurrent());
        assertEquals("back count", 0, history.getBackCount());
        assertEquals("back list", asList(), history.getBackList());
        assertEquals("forward  count", 2, history.getForwardCount());
        assertEquals("forward list", asList("b", "c"), history.getForwardList());
    }

    public void testForward() throws Exception
    {
        AddressNavigationHistory history = new AddressNavigationHistory();

        // Forward beyond boundary
        try
        {
            history.forward();
            fail("Should not be here!");
        }
        catch (IndexOutOfBoundsException e)
        {
            // Expected
        }

        // Setup
        assertEquals("current", null, history.getCurrent());
        assertEquals("back count", 0, history.getBackCount());
        assertEquals("back list", asList(), history.getBackList());
        assertEquals("forward  count", 0, history.getForwardCount());
        assertEquals("forward list", asList(), history.getForwardList());
        history.add("a");
        history.add("b");
        history.add("c");
        assertEquals("current", "c", history.getCurrent());
        assertEquals("back count", 2, history.getBackCount());
        assertEquals("back list", asList("a", "b"), history.getBackList());
        assertEquals("forward  count", 0, history.getForwardCount());
        assertEquals("forward list", asList(), history.getForwardList());

        // Back to a
        assertEquals("back", "b", history.back());
        assertEquals("back", "a", history.back());
        assertEquals("current", "a", history.getCurrent());
        assertEquals("back count", 0, history.getBackCount());
        assertEquals("back list", asList(), history.getBackList());
        assertEquals("forward  count", 2, history.getForwardCount());
        assertEquals("forward list", asList("b", "c"), history.getForwardList());

        // Forward to b
        assertEquals("forward", "b", history.forward());
        assertEquals("current", "b", history.getCurrent());
        assertEquals("back count", 1, history.getBackCount());
        assertEquals("back list", asList("a"), history.getBackList());
        assertEquals("forward  count", 1, history.getForwardCount());
        assertEquals("forward list", asList("c"), history.getForwardList());

        // Forward to c
        assertEquals("forward", "c", history.forward());
        assertEquals("current", "c", history.getCurrent());
        assertEquals("back count", 2, history.getBackCount());
        assertEquals("back list", asList("a", "b"), history.getBackList());
        assertEquals("forward  count", 0, history.getForwardCount());
        assertEquals("forward list", asList(), history.getForwardList());

        // Forward beyond boundary
        try
        {
            history.forward();
            fail("Should not be here!");
        }
        catch (IndexOutOfBoundsException e)
        {
            // Expected
        }
        assertEquals("current", "c", history.getCurrent());
        assertEquals("back count", 2, history.getBackCount());
        assertEquals("back list", asList("a", "b"), history.getBackList());
        assertEquals("forward  count", 0, history.getForwardCount());
        assertEquals("forward list", asList(), history.getForwardList());
    }

    public void testAddMiddle() throws Exception
    {
        AddressNavigationHistory history = new AddressNavigationHistory();

        // Setup
        history.add("a");
        history.add("b");
        history.add("c");
        history.add("d");
        history.add("e");
        assertEquals("current", "e", history.getCurrent());
        assertEquals("back count", 4, history.getBackCount());
        assertEquals("back list", asList("a", "b", "c", "d"), history.getBackList());
        assertEquals("forward  count", 0, history.getForwardCount());
        assertEquals("forward list", asList(), history.getForwardList());

        // Back to c
        assertEquals("back", "d", history.back());
        assertEquals("back", "c", history.back());
        assertEquals("current", "c", history.getCurrent());
        assertEquals("back count", 2, history.getBackCount());
        assertEquals("back list", asList("a", "b"), history.getBackList());
        assertEquals("forward  count", 2, history.getForwardCount());
        assertEquals("forward list", asList("d", "e"), history.getForwardList());

        // Add z (becomes a, b, c, z)
        history.add("z");
        assertEquals("current", "z", history.getCurrent());
        assertEquals("back count", 3, history.getBackCount());
        assertEquals("back list", asList("a", "b", "c"), history.getBackList());
        assertEquals("forward  count", 0, history.getForwardCount());
        assertEquals("forward list", asList(), history.getForwardList());

        // Forward beyond boundary
        try
        {
            history.forward();
            fail("Should not be here!");
        }
        catch (IndexOutOfBoundsException e)
        {
            // Expected
        }

        // Back to begining
        assertEquals("back", "c", history.back());
        assertEquals("back", "b", history.back());
        assertEquals("back", "a", history.back());
        assertEquals("current", "a", history.getCurrent());
        assertEquals("back count", 0, history.getBackCount());
        assertEquals("back list", asList(), history.getBackList());
        assertEquals("forward  count", 3, history.getForwardCount());
        assertEquals("forward list", asList("b", "c", "z"), history.getForwardList());
    }

    public void testAddStart() throws Exception
    {
        // Setup
        AddressNavigationHistory history = new AddressNavigationHistory();
        history.add("a");
        history.add("b");
        history.add("c");
        assertEquals("current", "c", history.getCurrent());
        assertEquals("back count", 2, history.getBackCount());
        assertEquals("back list", asList("a", "b"), history.getBackList());
        assertEquals("forward  count", 0, history.getForwardCount());
        assertEquals("forward list", asList(), history.getForwardList());

        // Back to a
        assertEquals("back", "b", history.back());
        assertEquals("back", "a", history.back());
        assertEquals("current", "a", history.getCurrent());
        assertEquals("back count", 0, history.getBackCount());
        assertEquals("back list", asList(), history.getBackList());
        assertEquals("forward  count", 2, history.getForwardCount());
        assertEquals("forward list", asList("b", "c"), history.getForwardList());

        // Add z (becomes a, z)
        history.add("z");
        assertEquals("current", "z", history.getCurrent());
        assertEquals("back count", 1, history.getBackCount());
        assertEquals("back list", asList("a"), history.getBackList());
        assertEquals("forward  count", 0, history.getForwardCount());
        assertEquals("forward list", asList(), history.getForwardList());

        // Forward beyond boundary
        try
        {
            history.forward();
            fail("Should not be here!");
        }
        catch (IndexOutOfBoundsException e)
        {
            // Expected
        }

        // Back to begining
        assertEquals("back", "a", history.back());
        assertEquals("current", "a", history.getCurrent());
        assertEquals("back count", 0, history.getBackCount());
        assertEquals("back list", asList(), history.getBackList());
        assertEquals("forward  count", 1, history.getForwardCount());
        assertEquals("forward list", asList("z"), history.getForwardList());
    }

    public void testReplaceAll() throws Exception
    {
        // Setup
        AddressNavigationHistory history = new AddressNavigationHistory();
        history.add("a");
        history.add("b");
        history.add("a");
        history.add("c");
        history.add("a");
        assertEquals("current", "a", history.getCurrent());
        assertEquals("back count", 4, history.getBackCount());
        assertEquals("back list", asList("a", "b", "a", "c"), history.getBackList());
        assertEquals("forward  count", 0, history.getForwardCount());
        assertEquals("forward list", asList(), history.getForwardList());

        // Back to middle a
        assertEquals("back", "c", history.back());
        assertEquals("back", "a", history.back());
        assertEquals("current", "a", history.getCurrent());
        assertEquals("back count", 2, history.getBackCount());
        assertEquals("back list", asList("a", "b"), history.getBackList());
        assertEquals("forward  count", 2, history.getForwardCount());
        assertEquals("forward list", asList("c", "a"), history.getForwardList());

        // Replace all a for z
        history.replaceAll("a", "z");
        assertEquals("current", "z", history.getCurrent());
        assertEquals("back count", 2, history.getBackCount());
        assertEquals("back list", asList("z", "b"), history.getBackList());
        assertEquals("forward  count", 2, history.getForwardCount());
        assertEquals("forward list", asList("c", "z"), history.getForwardList());

        // Replace y to x (no change expected)
        history.replaceAll("y", "x");
        assertEquals("current", "z", history.getCurrent());
        assertEquals("back count", 2, history.getBackCount());
        assertEquals("back list", asList("z", "b"), history.getBackList());
        assertEquals("forward  count", 2, history.getForwardCount());
        assertEquals("forward list", asList("c", "z"), history.getForwardList());
    }

    public void testRemoveAll() throws Exception
    {
        // Setup
        AddressNavigationHistory history = new AddressNavigationHistory();
        history.add("a");
        history.add("b");
        history.add("a");
        history.add("c");
        history.add("a");
        assertEquals("current", "a", history.getCurrent());
        assertEquals("back count", 4, history.getBackCount());
        assertEquals("back list", asList("a", "b", "a", "c"), history.getBackList());
        assertEquals("forward  count", 0, history.getForwardCount());
        assertEquals("forward list", asList(), history.getForwardList());

        // Back to middle a
        assertEquals("back", "c", history.back());
        assertEquals("back", "a", history.back());
        assertEquals("current", "a", history.getCurrent());
        assertEquals("back count", 2, history.getBackCount());
        assertEquals("back list", asList("a", "b"), history.getBackList());
        assertEquals("forward  count", 2, history.getForwardCount());
        assertEquals("forward list", asList("c", "a"), history.getForwardList());

        // Remove all a
        history.removeAll("a");
        assertEquals("current", "b", history.getCurrent());
        assertEquals("back count", 0, history.getBackCount());
        assertEquals("back list", asList(), history.getBackList());
        assertEquals("forward  count", 1, history.getForwardCount());
        assertEquals("forward list", asList("c"), history.getForwardList());

        // Add d
        history.add("d");
        assertEquals("current", "d", history.getCurrent());
        assertEquals("back count", 1, history.getBackCount());
        assertEquals("back list", asList("b"), history.getBackList());
        assertEquals("forward  count", 0, history.getForwardCount());
        assertEquals("forward list", asList(), history.getForwardList());

        // Remove y (no change expected)
        history.removeAll("z");
        assertEquals("current", "d", history.getCurrent());
        assertEquals("back count", 1, history.getBackCount());
        assertEquals("back list", asList("b"), history.getBackList());
        assertEquals("forward  count", 0, history.getForwardCount());
        assertEquals("forward list", asList(), history.getForwardList());
    }

}
