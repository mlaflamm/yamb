package yamb.app;

import yamb.app.bookmark.Bookmark;
import yamb.app.bookmark.BookmarkManager;
import yamb.app.bookmark.FolderBookmark;
import yamb.app.bookmark.SeriesBookmark;
import yamb.app.bookmark.TagBookmark;
import yamb.app.tag.TagCategory;
import yamb.app.view.AbstractViewInternalFrame;
import yamb.app.view.BookmarkViewFactory;
import yamb.app.view.ViewContext;
import yamb.app.view.explorer.ExplorerViewContext;
import yamb.app.view.series.SeriesViewContext;
import yamb.app.view.tag.TagViewContext;
import org.apache.commons.lang.StringUtils;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JInternalFrame;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

/**
 * @author manuel.laflamme
 * @since Mar 21, 2009
 */
public class SessionBookmarkManager implements InternalFrameListener, PropertyChangeListener
{
    private final List<JInternalFrame> mFrameList = new ArrayList<JInternalFrame>();
    private final List<Bookmark> mBookmarkList = new ArrayList<Bookmark>();
    private final BookmarkManager mBookmarkManager;
    private final BookmarkViewFactory mBookmarkViewFactory;
    private boolean mInitialized = false;

    public SessionBookmarkManager(BookmarkManager aBookmarkManager, BookmarkViewFactory aBookmarkViewFactory)
    {
        mBookmarkManager = aBookmarkManager;
        mBookmarkViewFactory = aBookmarkViewFactory;
    }

    public void initialize()
    {
        if (!mInitialized)
        {
            List<Bookmark> boomarks = mBookmarkManager.getBookmarks();
            for (Bookmark bookmark : boomarks)
            {
                mBookmarkViewFactory.createView(bookmark);
            }
            mInitialized = true;
        }
    }

    private int getFrameIndex(JInternalFrame aFrame)
    {
        for (int i = 0; i < mFrameList.size(); i++)
        {
            JInternalFrame frame = mFrameList.get(i);
            if (aFrame == frame)
            {
                return i;
            }
        }

        return -1;
    }

    private void updateBookmark(AbstractViewInternalFrame aFrame)
    {
        int index = getFrameIndex(aFrame);
        Bookmark newBookmark = createViewBookmark(aFrame);
        Bookmark oldBookmark = mBookmarkList.set(index, newBookmark);
        if (mInitialized)
        {
            if (newBookmark != null && oldBookmark != null)
            {
                mBookmarkManager.replaceBookmark(oldBookmark, newBookmark);
            }
            else if (oldBookmark != null)
            {
                mBookmarkManager.removeBookmark(oldBookmark);
            }
            else if (newBookmark != null)
            {
                mBookmarkManager.addBookmark(newBookmark);
            }
        }
    }

    private void addBookmark(AbstractViewInternalFrame aFrame)
    {
        Bookmark newBookmark = createViewBookmark(aFrame);
        mFrameList.add(aFrame);
        mBookmarkList.add(newBookmark);
        if (mInitialized)
        {
            if (newBookmark != null)
            {
                mBookmarkManager.addBookmark(newBookmark);
            }
        }
    }

    private void removeBookmark(AbstractViewInternalFrame aFrame)
    {
        int index = getFrameIndex(aFrame);
        mFrameList.remove(index);
        Bookmark oldBookmark = mBookmarkList.remove(index);
        if (mInitialized)
        {
            if (oldBookmark != null)
            {
                mBookmarkManager.removeBookmark(oldBookmark);
            }
        }
    }

    private Bookmark createViewBookmark(AbstractViewInternalFrame aFrame)
    {
        ViewContext viewContext = aFrame.getViewContext();
        if (viewContext instanceof TagViewContext)
        {
            List<String> tags = ((TagViewContext) viewContext).getTagContext().getTags(TagCategory.ACTIVE);
            if (tags.size() > 0)
            {
                return new TagBookmark(tags);
            }
        }
        else if (viewContext instanceof SeriesViewContext)
        {
            String selectedSeriesName = ((SeriesViewContext) viewContext).getSelectedSeriesName();
            if (StringUtils.isNotEmpty(selectedSeriesName))
            {
                return new SeriesBookmark(selectedSeriesName);
            }
        }
        else if (viewContext instanceof ExplorerViewContext)
        {
            return new FolderBookmark(((ExplorerViewContext) viewContext).getActiveFolder().getFile());
        }

        return null;
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    // PropertyChangeListener interface
    //

    public void propertyChange(PropertyChangeEvent aEvent)
    {
        updateBookmark((AbstractViewInternalFrame) aEvent.getSource());
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    // InternalFrameListener interface

    public void internalFrameOpened(InternalFrameEvent aEvent)
    {
        AbstractViewInternalFrame frame = (AbstractViewInternalFrame) aEvent.getSource();
        addBookmark(frame);
        frame.addPropertyChangeListener(JInternalFrame.TITLE_PROPERTY, this);
    }

    public void internalFrameClosing(InternalFrameEvent e)
    {
    }

    public void internalFrameClosed(InternalFrameEvent aEvent)
    {
        AbstractViewInternalFrame frame = (AbstractViewInternalFrame) aEvent.getSource();
        removeBookmark(frame);
        frame.removePropertyChangeListener(JInternalFrame.TITLE_PROPERTY, this);
        frame.removeInternalFrameListener(this);
    }

    public void internalFrameIconified(InternalFrameEvent e)
    {
    }

    public void internalFrameDeiconified(InternalFrameEvent e)
    {
    }

    public void internalFrameActivated(InternalFrameEvent e)
    {
    }

    public void internalFrameDeactivated(InternalFrameEvent e)
    {
    }

}
