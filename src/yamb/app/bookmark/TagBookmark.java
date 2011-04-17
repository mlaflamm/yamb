package yamb.app.bookmark;

import java.util.List;
import java.util.Arrays;

/**
 * @author manuel.laflamme
 * @since Mar 21, 2009
 */
public class TagBookmark extends Bookmark
{
    public TagBookmark(List<String> aTags)
    {
        super(BookmarkType.TAG, asString(aTags));
    }

    TagBookmark(String aValue)
    {
        super(BookmarkType.TAG, aValue);
    }

    public List<String> getTags()
    {
        return Arrays.asList(getValue().split("\\|"));
    }

    private static String asString(List<String> aTags)
    {
        StringBuilder builder = new StringBuilder();
        for (String tag : aTags)
        {
            if (builder.length() > 0)
            {
                builder.append("|");
            }
            builder.append(tag);
        }
        return builder.toString();
    }
}
