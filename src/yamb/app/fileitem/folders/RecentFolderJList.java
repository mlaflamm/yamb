package yamb.app.fileitem.folders;

import yamb.util.swing.AutoDisposeModelJList;

import javax.swing.DropMode;
import javax.swing.JList;
import javax.swing.ListSelectionModel;

/**
 * @author manuel.laflamme
 * @since 28-Feb-2008
 */
public class RecentFolderJList extends AutoDisposeModelJList
{
    public RecentFolderJList()
    {
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setCellRenderer(new RecentFolderListCellRenderer());
        setLayoutOrientation(JList.VERTICAL);
        setVisibleRowCount(-1);
        setDropMode(DropMode.ON);
    }
}
