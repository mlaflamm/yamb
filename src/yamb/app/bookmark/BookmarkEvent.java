package yamb.app.bookmark;

import java.util.EventObject;

/**
 * @author manuel.laflamme
 * @since 17-Oct-2008
 */
public class BookmarkEvent extends EventObject
{
    private Bookmark mBookmark;

    public BookmarkEvent(Object aSource, Bookmark aBookmark)
    {
        super(aSource);
        mBookmark = aBookmark;
    }

    public Bookmark getBookmark()
    {
        return mBookmark;
    }
}
