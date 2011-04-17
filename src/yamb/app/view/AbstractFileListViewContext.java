package yamb.app.view;

import yamb.app.fileitem.FileItem;
import yamb.app.fileitem.filelist.FileListSelectionManager;

/**
 * @author manuel.laflamme
 * @since 23-Aug-2008
 */
public class AbstractFileListViewContext extends DefaultViewContext implements FileListViewContext
{
    private FileListSelectionManager mSelectionManager;

    private FileItem mFocusedFileItem;
    private ListViewMode mListViewMode = ListViewMode.THUMBNAILS;

    public FileListSelectionManager getFileListSelectionManager()
    {
        assert(mSelectionManager != null);
        return mSelectionManager;
    }

    // todo : fix this hack
    public void setSelectionManager(FileListSelectionManager aSelectionManager)
    {
        mSelectionManager = aSelectionManager;
    }

    public FileItem getFocusedItem()
    {
        return mFocusedFileItem;
    }

    public void setFocusedItem(FileItem aFocusedFileItem)
    {
        FileItem newValue = aFocusedFileItem;
        FileItem oldValue = mFocusedFileItem;

        mFocusedFileItem = aFocusedFileItem;

        // Notify listeners
        mPropertySupport.firePropertyChange(FOCUSED_ITEM, oldValue, newValue);
    }

    public ListViewMode getListViewMode()
    {
        return mListViewMode;
    }

    public void setListViewMode(ListViewMode aListViewMode)
    {
        ListViewMode newValue = aListViewMode;
        ListViewMode oldValue = mListViewMode;

        mListViewMode = aListViewMode;

        // Notify listeners of list view mode change
        mPropertySupport.firePropertyChange(LISTVIEW_MODE, oldValue, newValue);
    }
}
