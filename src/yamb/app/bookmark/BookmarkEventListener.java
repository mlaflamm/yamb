package yamb.app.bookmark;

import java.util.EventListener;

/**
 * @author manuel.laflamme
 * @since 17-Oct-2008
 */
public interface BookmarkEventListener extends EventListener
{
    public void bookmarkAdded(BookmarkEvent aEvent);
    public void bookmarkRemoved(BookmarkEvent aEvent);
}
