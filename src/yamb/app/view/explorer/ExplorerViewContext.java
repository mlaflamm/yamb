package yamb.app.view.explorer;

import yamb.app.view.FileListViewContext;

/**
 * @author manuel.laflamme
 * @since 5-Aug-2008
 */
public interface ExplorerViewContext extends FileListViewContext
{
    String ACTIVE_FOLDER = "yamb.explorerViewContext.activeFolder";
    String FILETYPE_FILTER = "yamb.explorerViewContext.filteTypeFilter";
    String SHOW_FOLDERS = "yamb.explorerViewContext.showFolders";

    public ActiveFolder getActiveFolder();

    public void setActiveFolder(ActiveFolder aActiveFolder);

    public boolean getShowFolders();

    public void setShowFolders(boolean aShowFolders);

    FileTypeFilter getFileTypeFilter();

    void setFileTypeFilter(FileTypeFilter aFileTypeFilter);
}
