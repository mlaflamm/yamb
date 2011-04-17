package yamb.app.tag;

import java.util.ArrayList;

/**
 * @author manuel.laflamme
 * @since 2-Feb-2008
 */
public class Tags
{
    public static String[] getTagsFromFileName(String aFileName)
    {
        int start = aFileName.indexOf("_"); // intentionally exclude files starting with '_'
        if (start < 1)
        {
            return new String[0];
        }

        int end = aFileName.indexOf(".", start);
        if (end < 0)
        {
            return new String[0];
        }

        int emptyCount = 0;
        String[] tags = aFileName.substring(start + 1, end).split("[,&]");
        for (int i = 0; i < tags.length; i++)
        {
            tags[i] = tags[i].trim();
            emptyCount += (tags[i].length() == 0) ? 1 : 0;
        }

        if (emptyCount > 0)
        {
            ArrayList<String> tagList = new ArrayList<String>(tags.length);
            for (String tag : tags)
            {
                if (tag.length() > 0)
                {
                    tagList.add(tag);
                }
            }
            tags = tagList.toArray(new String[0]);
        }
        return tags;
    }

    public static String renameFileNameTag(String aFileName, String aOldTag, String aNewTag)
    {
        String[] tags = getTagsFromFileName(aFileName);
        for (int i = 0; i < tags.length; i++)
        {
            String tag = tags[i];
            if (tag.equals(aOldTag))
            {
                tags[i] = aNewTag;
            }
        }

        return setFileNameTags(aFileName, tags);
    }

    public static String setFileNameTags(String aFileName, String[] aTags)
    {
        int start = aFileName.indexOf("_"); // intentionally exclude files starting with '_'
        if (start < 1)
        {
            return aFileName;
        }

        int end = aFileName.indexOf(".", start);
        if (end < 0)
        {
            return aFileName;
        }

        StringBuilder builder = new StringBuilder();
        builder.append(aFileName.substring(0, start));
        builder.append("_");
        for (int i = 0; i < aTags.length; i++)
        {
            String tag = aTags[i];
            if (tag.length() > 0)
            {
                if (i > 0)
                {
                    builder.append(", ");
                }
                builder.append(tag);
            }
        }
        builder.append(aFileName.substring(end));
        return builder.toString();
    }
}
