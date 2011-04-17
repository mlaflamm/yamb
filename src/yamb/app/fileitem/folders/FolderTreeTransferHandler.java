package yamb.app.fileitem.folders;

import yamb.app.fileitem.FileTransferHandler;
import yamb.util.io.shell.FileOperation;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.tree.TreePath;

/**
 * @author manuel.laflamme
 * @since 1-Mar-2008
 */
public class FolderTreeTransferHandler extends FileTransferHandler
{
    public FolderTreeTransferHandler(FileOperation aFileOperation)
    {
        super(aFileOperation);
    }

    protected File getDropFileTarget(TransferSupport aTransferSupport)
    {
        JTree.DropLocation dropLocation =
                (JTree.DropLocation) aTransferSupport.getDropLocation();
        TreePath path = dropLocation.getPath();
        if (path == null)
        {
            return null;
        }

        FolderTreeItem item = ((FolderTreeItem) path.getLastPathComponent());
        return item.getFile();
    }

    public List<File> getDragSourceFiles(JComponent aComponent)
    {
        JTree tree = (JTree) aComponent;
        TreePath[] selectionPaths = tree.getSelectionPaths();
        List<File> files = new ArrayList<File>(selectionPaths.length);
        for (TreePath path : selectionPaths)
        {
            FolderTreeItem item = ((FolderTreeItem) path.getLastPathComponent());
            files.add(item.getFile());
        }
        return files;
    }
}
