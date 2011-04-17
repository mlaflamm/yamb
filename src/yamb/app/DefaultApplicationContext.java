package yamb.app;

import yamb.app.bookmark.BookmarkManager;
import yamb.app.tag.TagManager;
import yamb.app.tag.library.LibraryManager;
import yamb.app.tag.series.DefaultSeriesManager;
import yamb.app.tag.series.SeriesManager;
import yamb.app.view.ViewContext;
import yamb.app.view.ViewFactory;
import yamb.util.io.shell.FileOperation;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;

public class DefaultApplicationContext implements ApplicationContext
{
    private final PropertyChangeSupport mPropertySupport;
    private final FileOperation mFileOperation;
    private final TagManager mTagManager;
    private final LibraryManager mLibraryManager;
    private final SeriesManager mSeriesManager;
    private final BookmarkManager mUserBookmarkManager;
    private final BookmarkManager mSessionBookmarkManager;
    private ViewFactory mViewFactory;

    private ViewContext mViewContext;
    private File mDataFolder;

    public DefaultApplicationContext(File aDataFolder, TagManager aTagManager,
            LibraryManager aLibraryManager, BookmarkManager aUserBookmarkManager,
            BookmarkManager aSessionBookmarkManager, FileOperation aFileOperation)
    {
        mDataFolder = aDataFolder;
        mTagManager = aTagManager;
        mLibraryManager = aLibraryManager;
        mSeriesManager = new DefaultSeriesManager(aTagManager);
        mUserBookmarkManager = aUserBookmarkManager;
        mSessionBookmarkManager = aSessionBookmarkManager;
        mFileOperation = aFileOperation;
        mPropertySupport = new PropertyChangeSupport(this);
    }

    public ViewContext getActiveViewContext()
    {
        return mViewContext;
    }

    public void setActiveViewContext(ViewContext aViewContext)
    {
        ViewContext newValue = aViewContext;
        ViewContext oldValue = mViewContext;

        mViewContext = aViewContext;

        // Notify listeners of active view context change
        mPropertySupport.firePropertyChange(ACTIVE_VIEWCONTEXT, oldValue, newValue);
    }

    public TagManager getTagManager()
    {
        return mTagManager;
    }

    public LibraryManager getLibraryManager()
    {
        return mLibraryManager;
    }

    public SeriesManager getSeriesManager()
    {
        return mSeriesManager;
    }

    public BookmarkManager getUserBookmarkManager()
    {
        return mUserBookmarkManager;
    }

    public BookmarkManager getSessionBookmarkManager()
    {
        return mSessionBookmarkManager;
    }

    public ViewFactory getViewFactory()
    {
        assert (mViewFactory != null);
        return mViewFactory;
    }

    // todo : fix this hack with a builder

    public void setViewFactory(ViewFactory aViewFactory)
    {
        mViewFactory = aViewFactory;
    }

    public File getDataDirectory()
    {
        return mDataFolder;
    }

    public FileOperation getFileOperation()
    {
        return mFileOperation;
    }

    public void addPropertyChangeListener(PropertyChangeListener aListener)
    {
        mPropertySupport.addPropertyChangeListener(aListener);
    }

    public void addPropertyChangeListener(String aPropertyName,
            PropertyChangeListener aListener)
    {
        mPropertySupport.addPropertyChangeListener(aPropertyName, aListener);
    }

    public void removePropertyChangeListener(PropertyChangeListener aListener)
    {
        mPropertySupport.removePropertyChangeListener(aListener);
    }

    public void removePropertyChangeListener(String aPropertyName,
            PropertyChangeListener aListener)
    {
        mPropertySupport.removePropertyChangeListener(aPropertyName, aListener);
    }
}
