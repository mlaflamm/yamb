package yamb.app.tag;

import junit.framework.TestCase;

import java.util.Arrays;

/**
 * @author manuel.laflamme
 * @since Feb 17, 2008
 */
public class TestTags extends TestCase
{
    public void testGetTagsFromFileNameMultipleTags() throws Exception
    {
        String[] fileNames = new String[]{
                "name_tag1 & tag2a tag2b.ext",
                "name_tag1 & tag2a tag2b.ext",
                "name_tag1&tag2a tag2b.ext",
                "name_tag1  &tag2a tag2b.ext",
                "name_tag1&    tag2a tag2b.ext",
                "name_tag1     &    tag2a tag2b.ext",
                "site.domain_tag1 & tag2a tag2b.zip.ext",
                "site.domain_tag1 & tag2a tag2b.zip._ext",
        };
        String[] expected = new String[]{
                "tag1",
                "tag2a tag2b",
        };

        for (String fileName : fileNames)
        {
            String[] actual = Tags.getTagsFromFileName(fileName);
            assertEquals(fileName, Arrays.asList(expected), Arrays.asList(actual));

        }
    }

    public void testGetTagsFromFileNameMultipleTagsWithComa() throws Exception
    {
        String[] fileNames = new String[]{
                "name_tag1, tag2a tag2b & tag3.ext",
                "name_tag1 & tag2a tag2b & tag3.ext",
                "site.domain_tag1, tag2a tag2b, tag3.zip.ext",
                "site.domain_tag1,tag2a tag2b,tag3.ext",
                "site.domain_tag1  ,  tag2a tag2b   ,   tag3.ext",
        };
        String[] expected = new String[]{
                "tag1",
                "tag2a tag2b",
                "tag3",
        };

        for (String fileName : fileNames)
        {
            String[] actual = Tags.getTagsFromFileName(fileName);
            assertEquals(fileName, Arrays.asList(expected), Arrays.asList(actual));

        }
    }

    public void testGetTagsFromFileNameSingleTag() throws Exception
    {
        String[] fileNames = new String[]{
                "name_tag1.ext",
                "site.domain_tag1.zip.ext",
                "site.domain_tag1.zip._ext",
        };
        String[] expected = new String[]{
                "tag1",
        };

        for (String fileName : fileNames)
        {
            String[] actual = Tags.getTagsFromFileName(fileName);
            assertEquals(fileName, Arrays.asList(expected), Arrays.asList(actual));

        }
    }

    public void testGetTagsFromFileNameNone() throws Exception
    {
        String[] fileNames = new String[]{
                "name_.ext",
                "",
                "name.ext",
                "name & something.ext",
                "_name & something.ext",
        };

        for (String fileName : fileNames)
        {
            String[] actual = Tags.getTagsFromFileName(fileName);
            assertEquals(fileName, 0, actual.length);

        }
    }

    public void testRenameFileNameTag() throws Exception
    {
        String[] fileNames = new String[]{
                "name_tag1 & tag2a tag2b.ext",
                "name_tag1&tag2a tag2b.ext",
                "name_tag1  &tag2a tag2b.ext",
                "name_tag1&    tag2a tag2b.ext",
                "name_tag1 ,,    &    tag2a tag2b.ext",
                "name_tag1, tag2 & tag3.ext",
                "name_tag1.ext",
                "name_tag2.ext",
                "name.ext",
                "site.domain_tag1 & tag2a tag2b.zip.ext",
                "site.domain_tag1 & tag2a tag2b.zip._ext",
                "site.domain_tag1.zip._ext",
                "site.domain.zip._ext",
        };
        String[] expectedNames = new String[]{
                "name_tag, tag2a tag2b.ext",
                "name_tag, tag2a tag2b.ext",
                "name_tag, tag2a tag2b.ext",
                "name_tag, tag2a tag2b.ext",
                "name_tag, tag2a tag2b.ext",
                "name_tag, tag2, tag3.ext",
                "name_tag.ext",
                "name_tag2.ext",
                "name.ext",
                "site.domain_tag, tag2a tag2b.zip.ext",
                "site.domain_tag, tag2a tag2b.zip._ext",
                "site.domain_tag.zip._ext",
                "site.domain.zip._ext",
        };

        for (int i = 0; i < fileNames.length; i++)
        {
            String fileName = fileNames[i];
            String expectedName = expectedNames[i];
            String actual = Tags.renameFileNameTag(fileName, "tag1", "tag");
//            System.out.println(actual);
            assertEquals(i + ":" + fileName, expectedName, actual);
        }
    }
}
