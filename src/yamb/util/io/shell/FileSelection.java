package yamb.util.io.shell;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author manuel.laflamme
 * @since 22-Feb-2008
 */
public class FileSelection implements Transferable, ClipboardOwner
{
    public static final DataFlavor CUT_FILES_FLAVOR = createConstant("application/x-java-file-list-cut;class=java.util.List");
    public static final DataFlavor COPY_FILES_FLAVOR = DataFlavor.javaFileListFlavor;

    private final DataFlavor[] mSupportedFlovors;
    private final List<File> mFiles;

    private static DataFlavor createConstant(String aMimeType)
    {
        try
        {
            return new DataFlavor(aMimeType);
        }
        catch (ClassNotFoundException e)
        {
            return null;
        }
    }

    public FileSelection(List<File> aFiles, boolean aCutOperation)
    {
        mFiles = aFiles;
        if (aCutOperation)
        {
            mSupportedFlovors = new DataFlavor[]{CUT_FILES_FLAVOR, COPY_FILES_FLAVOR};
        }
        else
        {
            mSupportedFlovors = new DataFlavor[]{COPY_FILES_FLAVOR};
        }
    }

    public FileSelection(File aFile, boolean aCutOperation)
    {
        mFiles = new ArrayList<File>();
        mFiles.add(aFile);
        if (aCutOperation)
        {
            mSupportedFlovors = new DataFlavor[]{CUT_FILES_FLAVOR, COPY_FILES_FLAVOR};
        }
        else
        {
            mSupportedFlovors = new DataFlavor[]{COPY_FILES_FLAVOR};
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Transferable interface

    public DataFlavor[] getTransferDataFlavors()
    {
        return mSupportedFlovors.clone();
    }

    public boolean isDataFlavorSupported(DataFlavor aFlavor)
    {
        for (DataFlavor supportedFlovor : mSupportedFlovors)
        {
            if (aFlavor.equals(supportedFlovor))
            {
                return true;
            }
        }
        return false;
    }

    public Object getTransferData(DataFlavor aFlavor) throws UnsupportedFlavorException, IOException
    {
        if (isDataFlavorSupported(aFlavor))
        {
            return Collections.unmodifiableList(mFiles);
        }
        throw new UnsupportedFlavorException(aFlavor);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // ClipboardOwner interface

    public void lostOwnership(Clipboard aClipboard, Transferable aContents)
    {
        // Don't care...
    }
}
