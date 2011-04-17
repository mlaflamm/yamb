package yamb.app.bookmark;

import java.util.List;

/**
 * @author manuel.laflamme
 * @since 17-Oct-2008
 */
public interface BookmarkManager
{
    List<Bookmark> getBookmarks(BookmarkType aBookmarkType);
    List<Bookmark> getBookmarks();
    void addBookmark(Bookmark aBookmark);
    void removeBookmark(Bookmark aBookmark);
    void replaceBookmark(Bookmark aOldBookmark, Bookmark aNewBookmark);

    void addBookmarkEventListener(BookmarkEventListener aListener);
    void removeBookmarkEventListener(BookmarkEventListener aListener);
}
