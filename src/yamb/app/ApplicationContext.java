package yamb.app;

import yamb.app.bookmark.BookmarkManager;
import yamb.app.tag.TagManager;
import yamb.app.tag.library.LibraryManager;
import yamb.app.tag.series.SeriesManager;
import yamb.app.view.ViewContext;
import yamb.app.view.ViewFactory;
import yamb.util.io.shell.FileOperation;

import java.beans.PropertyChangeListener;
import java.io.File;

public interface ApplicationContext //extends ViewContext, TagViewContext, ExplorerViewContext
{
    public static String ACTIVE_VIEWCONTEXT = "yamb.activeViewContext";

    public File getDataDirectory();

    public TagManager getTagManager();

    public LibraryManager getLibraryManager();

    public SeriesManager getSeriesManager();

    public BookmarkManager getUserBookmarkManager();

    public BookmarkManager getSessionBookmarkManager();

    public FileOperation getFileOperation();

    public ViewFactory getViewFactory();

    public ViewContext getActiveViewContext();

    public void setActiveViewContext(ViewContext aViewContext);

    public void addPropertyChangeListener(PropertyChangeListener aListener);

    public void addPropertyChangeListener(String aPropertyName,
            PropertyChangeListener aListener);

    public void removePropertyChangeListener(PropertyChangeListener aListener);

    public void removePropertyChangeListener(String aPropertyName,
            PropertyChangeListener aListener);

}

