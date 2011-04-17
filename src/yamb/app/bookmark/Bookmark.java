package yamb.app.bookmark;

/**
 * @author manuel.laflamme
 * @since 17-Oct-2008
 */
public abstract class Bookmark implements Comparable
{
    private BookmarkType mType;
    private String mValue;

    public Bookmark(BookmarkType aType, String aValue)
    {
        mType = aType;
        mValue = aValue;
    }

    public BookmarkType getType()
    {
        return mType;
    }

    public String getValue()
    {
        return mValue;
    }

    public int compareTo(Object o)
    {
        return mValue.compareTo(((Bookmark)o).mValue);
    }

    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final Bookmark bookmark = (Bookmark) o;

        if (mType != bookmark.mType)
        {
            return false;
        }
        if (!mValue.equals(bookmark.mValue))
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        int result;
        result = mType.hashCode();
        result = 29 * result + mValue.hashCode();
        return result;
    }

    public String toString()
    {
        return mType + "=" + mValue;
    }

    public static Bookmark fromString(String aValue)
    {
        String[] strings = aValue.split("=");
        if (strings.length != 2)
        {
            return null;
        }

        BookmarkType type = BookmarkType.fromString(strings[0]);
        if (type != null)
        {
            return BookmarkFactory.fromString(type, strings[1].trim());
        }

        return null;
    }
}
