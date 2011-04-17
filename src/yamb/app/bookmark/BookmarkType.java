package yamb.app.bookmark;

/**
 * @author manuel.laflamme
 * @since 17-Oct-2008
 */
public enum BookmarkType
{
    TAG("T"), SERIES("G"), FOLDER("F");

    private final String mValue;

    BookmarkType(String aValue)
    {
        mValue = aValue;
    }

    public String toString()
    {
        return mValue;
    }

    public static BookmarkType fromString(String aValue)
    {
        if (TAG.toString().equals(aValue))
        {
            return TAG;
        }
        if (SERIES.toString().equals(aValue))
        {
            return SERIES;
        }
        if (FOLDER.toString().equals(aValue))
        {
            return FOLDER;
        }
        return null;
    }
}
