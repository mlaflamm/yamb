package yamb.app.fileitem.filelist;

import javax.swing.DefaultListSelectionModel;

/**
 * @author manuel.laflamme
 * @since 23-Feb-2008
 */
public class FileListSelectionModel extends DefaultListSelectionModel
{
    public void fireValueChanged(int firstIndex, int lastIndex)
    {
        super.fireValueChanged(firstIndex, lastIndex);
    }
}
