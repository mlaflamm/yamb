package yamb.app.fileitem;

import yamb.util.io.shell.FileOperation;
import yamb.util.io.shell.FileSelection;
import org.apache.log4j.Logger;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.TransferHandler;

/**
 * @author manuel.laflamme
 * @since 1-Mar-2008
 */
public abstract class FileTransferHandler extends TransferHandler
{
    private static final Logger LOGGER = Logger.getLogger(FileTransferHandler.class);
    private final FileOperation mFileOperation;

    protected FileTransferHandler(FileOperation aFileOperation)
    {
        mFileOperation = aFileOperation;
    }

    protected abstract File getDropFileTarget(TransferSupport aTransferSupport);

    public abstract List<File> getDragSourceFiles(JComponent aComponent);

    public FileOperation getFileOperation()
    {
        return mFileOperation;
    }

    public boolean canImport(TransferSupport aTransferSupport)
    {
        // Support only drop, file list and move or copy
        if (!aTransferSupport.isDrop() || !aTransferSupport.isDataFlavorSupported(DataFlavor.javaFileListFlavor) ||
                (COPY_OR_MOVE & aTransferSupport.getSourceDropActions()) == 0)
        {
            return false;
        }

        aTransferSupport.setShowDropLocation(true);

        File fileTarget = getDropFileTarget(aTransferSupport);
        if (fileTarget == null || fileTarget.isFile())
        {
            return false;
        }

        try
        {
            List<File> fileList = getTransferedFiles(aTransferSupport);
            if (fileList.size() > 0)
            {
                File file = fileList.get(0);
                if (file.getParentFile().equals(fileTarget))
                {
                    return false;
                }
//
//                String targetPrefix = FilenameUtils.getPrefix(fileTarget.getAbsolutePath());
//                String sourcePrefix = FilenameUtils.getPrefix(file.getAbsolutePath());
//                if (sourcePrefix.equalsIgnoreCase(targetPrefix))
//                {
//                    aTransferSupport.setDropAction(MOVE);
//                }
//                else
//                {
//                    aTransferSupport.setDropAction(COPY);
//                }
            }
        }
        catch (IOException e)
        {
            LOGGER.warn("Cannot import data", e);
            return false;
        }

        return true;
    }

    public boolean importData(TransferSupport aTransferSupport)
    {
        if (!canImport(aTransferSupport))
        {
            return false;
        }

        File fileTarget = getDropFileTarget(aTransferSupport);
        if (fileTarget == null)
        {
            return false;
        }

        try
        {
            List<File> fileList = getTransferedFiles(aTransferSupport);
            if ((MOVE & aTransferSupport.getUserDropAction()) == MOVE)
            {
                mFileOperation.moveFiles(fileList, fileTarget);
                return true;
            }
            else if ((COPY & aTransferSupport.getUserDropAction()) == COPY)
            {
                mFileOperation.copyFiles(fileList, fileTarget);
                return true;
            }

            return false;

        }
        catch (IOException e)
        {
            LOGGER.warn("Unable to import data", e);
            return false;
        }
    }

    private ArrayList<File> getTransferedFiles(TransferSupport aTransferSupport)
            throws IOException
    {
        try
        {
            return new ArrayList<File>((List) aTransferSupport.getTransferable().getTransferData(DataFlavor.javaFileListFlavor));
        }
        catch (UnsupportedFlavorException e)
        {
            throw new IOException(e);
        }
    }

    public int getSourceActions(JComponent c)
    {
        return COPY_OR_MOVE;
    }

    protected Transferable createTransferable(JComponent c)
    {
        List<File> dragSourceFiles = getDragSourceFiles(c);
        FileSelection fileSelection = new FileSelection(dragSourceFiles, false);
        return fileSelection;
    }

}
