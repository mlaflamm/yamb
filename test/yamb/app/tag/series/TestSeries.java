package yamb.app.tag.series;

import junit.framework.TestCase;

import java.io.File;
import java.util.Arrays;

/**
 * @author manuel.laflamme
 * @since Aug 8, 2008
 */
public class TestSeries extends TestCase
{
    public void testGetSeriesName() throws Exception
    {
        String expected = "name";
        String[] names = new String[]{
                "name",
                "name_tag",
                "name (removed)",
                "name  (removed)",
                "name - removed",
                "name  -  removed",
                "name - removed (removed)",
//                "name-removed",
//                "name(removed)",
                "name (removed) - removed",
                "name 1",
                "name #1",
                "name 1 - removed",
                "name 1 (removed)",
                "name #1 (removed)",
                "name #1 -removed",
                "name  ",
        };

        for (String name : names)
        {
            assertEquals("'" + name + "'", "'" + expected + "'", "'" + Series.getSeriesName(name) + "'");
        }

    }

    public void testGetFileYearByFileName() throws Exception
    {
        String[] expected = new String[]{
                "2010",
                "2010",
                "2010",
                "2010",
                "2010",
                "2010",
                "2010",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
        };

        String[] names = new String[]{
                "name (2010)",
                "() name (2010)",
                "(1234) name (2010)",
                "name (2010-02)_tag",
                "name (2010-12-31)",
                "parent (2010)\\name  (removed)",
                "parent (2010)(2CD)\\name  (removed)",
                "name",
                "name (toto)",
                "name (123)",
                "name (12345)",
                "parent\\name",
                "parent\\name ()",
                "parent ()\\name ()",
        };

        assertEquals("count", expected.length, names.length);
        for (int i = 0; i < expected.length; i++)
        {
            assertEquals(names[i], expected[i], Series.getFileYearByFileName(new File(names[i])));
        }

    }


    public void testComparator() throws Exception
    {
        File[] files = asFileArray(
                "name ",
                "name 6_tag",
                "name (removed)",
                "name #2",
                "name 1",
                "name 03 -removed",
                "name 00010 (removed)",
                "name (removed) - removed");

        File[] expected = asFileArray(
                "name ",
                "name (removed)",
                "name (removed) - removed",
                "name 1",
                "name #2",
                "name 03 -removed",
                "name 6_tag",
                "name 00010 (removed)");

        Arrays.sort(files, Series.FILE_NAMECOMPARATOR);
        System.out.println(Arrays.asList(files));
        for (int i = 0; i < expected.length; i++)
        {
            assertEquals(expected[i], files[i]);
        }
    }

    private File[] asFileArray(String... names)
    {
        File[] files = new File[names.length];
        for (int i = 0; i < names.length; i++)
        {
            files[i] = new File(names[i]);
        }
        return files;
    }
}
