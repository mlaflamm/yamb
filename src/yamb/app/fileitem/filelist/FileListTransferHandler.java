package yamb.app.fileitem.filelist;

import yamb.app.fileitem.FileTransferHandler;
import yamb.util.io.shell.FileOperation;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;

/**
 * @author manuel.laflamme
 * @since 1-Mar-2008
 */
public class FileListTransferHandler extends FileTransferHandler
{
    public FileListTransferHandler(FileOperation aFileOperation)
    {
        super(aFileOperation);
    }

    protected File getDropFileTarget(TransferSupport aTransferSupport)
    {
        JList.DropLocation dropLocation = (JList.DropLocation) aTransferSupport.getDropLocation();
        if (dropLocation.getIndex() == -1)
        {
            return null;
        }

        ListModel listModel = ((JList) aTransferSupport.getComponent()).getModel();
        return ((FileListItem) listModel.getElementAt(dropLocation.getIndex())).getFile();
    }
    
    public List<File> getDragSourceFiles(JComponent aComponent)
    {
        JList fileList = (JList) aComponent;
        ArrayList<File> files = new ArrayList<File>();
        ListSelectionModel selectionModel = fileList.getSelectionModel();
        if (selectionModel.isSelectionEmpty())
        {
            return files;
        }

        ListModel listModel = fileList.getModel();
        int min = selectionModel.getMinSelectionIndex();
        int max = selectionModel.getMaxSelectionIndex();
        for (int i = min; i <= max; i++)
        {
            if (selectionModel.isSelectedIndex(i))
            {
                FileListItem item = (FileListItem) listModel.getElementAt(i);
                files.add(item.getFile());
            }
        }

        return files;
    }
}
