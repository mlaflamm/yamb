package yamb.app.bookmark;

/**
 * @author manuel.laflamme
 * @since Mar 21, 2009
 */
class BookmarkFactory
{
    static Bookmark fromString(BookmarkType aType, String aValue)
    {
        switch (aType)
        {
            case TAG:
                return new TagBookmark(aValue);
            case FOLDER:
                return new FolderBookmark(aValue);
            case SERIES:
                return new SeriesBookmark(aValue);
        }

        return null;
    }
}
