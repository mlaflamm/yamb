package yamb.app.view;

import yamb.app.bookmark.Bookmark;

/**
 * @author manuel.laflamme
 * @since Mar 22, 2009
 */
public interface BookmarkViewFactory
{
    public ViewContext createView(Bookmark aBookmark);
}
