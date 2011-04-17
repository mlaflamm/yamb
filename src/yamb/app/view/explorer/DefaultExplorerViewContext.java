package yamb.app.view.explorer;

import yamb.app.fileitem.folders.RootFolderTreeItem;
import yamb.app.view.AbstractFileListViewContext;

/**
 * @author manuel.laflamme
 * @since Aug 6, 2008
 */
public class DefaultExplorerViewContext extends AbstractFileListViewContext implements ExplorerViewContext
{
    private ActiveFolder mActiveFolder;
    private boolean mShowFolders = false;
    private FileTypeFilter mFileTypeFilter = FileTypeFilter.ALL;

    public DefaultExplorerViewContext()
    {
        super();
        mActiveFolder = new ActiveFolder(new RootFolderTreeItem().getChild(0).getFile());
    }

    public ActiveFolder getActiveFolder()
    {
        return mActiveFolder;
    }

    public void setActiveFolder(ActiveFolder aActiveFolder)
    {
        if (aActiveFolder.getFile().isDirectory() && aActiveFolder.getFile().exists())
        {
            ActiveFolder newValue = aActiveFolder;
            ActiveFolder oldValue = mActiveFolder;

            mActiveFolder = aActiveFolder;

            // Notify listeners of active folder change
            mPropertySupport.firePropertyChange(ACTIVE_FOLDER, oldValue, newValue);
        }
    }

    public boolean getShowFolders()
    {
        return mShowFolders;
    }

    public void setShowFolders(boolean aShowFolders)
    {
        boolean newValue = aShowFolders;
        boolean oldValue = mShowFolders;
        mShowFolders = aShowFolders;
        mPropertySupport.firePropertyChange(SHOW_FOLDERS, oldValue, newValue);
    }

    public FileTypeFilter getFileTypeFilter()
    {
        return mFileTypeFilter;
    }

    public void setFileTypeFilter(FileTypeFilter aFileTypeFilter)
    {
        FileTypeFilter newValue = aFileTypeFilter;
        FileTypeFilter oldValue = mFileTypeFilter;
        mFileTypeFilter = aFileTypeFilter;
        mPropertySupport.firePropertyChange(FILETYPE_FILTER, oldValue, newValue);

    }
}
