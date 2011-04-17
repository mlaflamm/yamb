package yamb.app.view;

import yamb.app.fileitem.FileItem;
import yamb.app.fileitem.filelist.FileListSelectionManager;

/**
 * @author manuel.laflamme
 * @since 23-Aug-2008
 */
public interface FileListViewContext extends ViewContext
{
    String LISTVIEW_MODE = "yamb.viewContext.listViewMode";
    String FOCUSED_ITEM = "yamb.viewContext.focusedItem";

    public FileListSelectionManager getFileListSelectionManager();

    public FileItem getFocusedItem();

    public void setFocusedItem(FileItem aFileItem);

    public ListViewMode getListViewMode();

    public void setListViewMode(ListViewMode aListViewMode);
}
