package yamb.util.io;

import junit.framework.TestCase;

import java.util.Arrays;

/**
 * @author Manuel Laflamme
 * @since Feb 25, 2006
 */
public class TestFileNames extends TestCase
{
    public void testSplitRelative() throws Exception
    {
        String[] expected = new String[]{"name", "ext"};
        String filePath = expected[0] + "." + expected[1];
        String[] actual = FileNames.splitExtension(filePath);
        assertEquals(filePath, Arrays.asList(expected), Arrays.asList(actual));
    }

    public void testSplitMultipleDot() throws Exception
    {
        String[] expected = new String[]{"na.me.", "ext"};
        String filePath = expected[0] + "." + expected[1];
        String[] actual = FileNames.splitExtension(filePath);
        assertEquals(filePath, Arrays.asList(expected), Arrays.asList(actual));
    }

    public void testSplitNoExt() throws Exception
    {
        String[] expected = new String[]{"name", ""};
        String filePath = expected[0];
        String[] actual = FileNames.splitExtension(filePath);
        assertEquals(filePath, Arrays.asList(expected), Arrays.asList(actual));
    }

    // C:\folder name\file name
    public void testSplitQualified() throws Exception
    {
        String[] expected = new String[]{"C:\\folder name\\file name", "ext"};
        String filePath = expected[0] + "." + expected[1];
        String[] actual = FileNames.splitExtension(filePath);
        assertEquals(filePath, Arrays.asList(expected), Arrays.asList(actual));
    }

    // C:\folder name.ext\file name
    public void testSplitQualifiedNoExtButDotInFolder() throws Exception
    {
        String[] expected = new String[]{"C:\\folder name.ext\\file name", ""};
        String filePath = expected[0];
        String[] actual = FileNames.splitExtension(filePath);
        assertEquals(filePath, Arrays.asList(expected), Arrays.asList(actual));
    }

    public void testCleanNumberedName1() throws Exception
    {
        String before = "a bc de. toto 001";
        String expected = "a bc de. toto 1";
        String actual = FileNames.cleanNumberedName(before, 0);
        assertEquals(before, expected, actual);
    }

    public void testCleanNumberedName2() throws Exception
    {
        String before = "a bc de. toto #001";
        String expected = "a bc de. toto 1";
        String actual = FileNames.cleanNumberedName(before, 0);
        assertEquals(before, expected, actual);
    }

    public void testCleanNumberedNameWithPadding() throws Exception
    {
        String before = "a bc de. toto 1";
        String expected = "a bc de. toto 01";
        String actual = FileNames.cleanNumberedName(before, 2);
        assertEquals(before, expected, actual);
    }
}
