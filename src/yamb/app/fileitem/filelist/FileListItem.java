package yamb.app.fileitem.filelist;

import yamb.app.fileitem.FileIconType;
import yamb.app.fileitem.FileItem;
import yamb.util.io.shell.FileOperation;
import sun.awt.shell.ShellFolder;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Date;
import javax.swing.ImageIcon;

public class FileListItem implements FileItem
{
    private static final Date NO_DATE = new Date();

    private File mFile = null;

    private String mDisplayName = null;
    private ImageIcon mSmallIcon = null;
    private Date mLastModified = NO_DATE;
    private long mSize = -1;
    private Boolean mDirectory = null;

    private final FileOperation mFileOperation;

    FileListItem(File aFile, boolean aCanBeDirectory, FileOperation aFileOperation)
    {
        mFile = aFile;
        mFileOperation = aFileOperation;

        if (!aCanBeDirectory)
        {
            mDirectory = Boolean.FALSE;
            mDisplayName = aFile.getName();
        }
    }

    /**
     *
     */
    private ShellFolder getShellFile() throws FileNotFoundException
    {
        if (!(mFile instanceof ShellFolder))
        {
            mFile = ShellFolder.getShellFolder(mFile);
        }

        return (ShellFolder) mFile;
    }

    public File getFile()
    {
        return mFile;
    }

    public ImageIcon getIcon(FileIconType aIconType)
    {
        try
        {
            if (aIconType == FileIconType.LARGE)
            {
                return new ImageIcon(getShellFile().getIcon(true));
            }

            if (mSmallIcon == null)
            {
                mSmallIcon = new ImageIcon(getShellFile().getIcon(false)/*, getShellFile().getFolderType()*/);
            }
            return mSmallIcon;
        }
        catch (FileNotFoundException e)
        {
            mFileOperation.fireFileDeleted(mFile);
            return null;
        }
    }

    public long getSize()
    {
        if (mSize == -1)
        {
            if (!isDirectory())
            {
                mSize = mFile.length();
            }
            else
            {
                mSize = 0;
            }
        }
        return mSize;
    }

    public boolean isDirectory()
    {
        if (mDirectory == null)
        {
            mDirectory = mFile.isDirectory();
        }
        return mDirectory;
    }

    public Date getLastModified()
    {
        try
        {
            if (mLastModified == NO_DATE)
            {
                long lastModified = 0;
                if (getShellFile().isFileSystem() && !ShellFolder.isFileSystemRoot(mFile))
                {
                    lastModified = mFile.lastModified();
                }
                mLastModified = lastModified == 0L ? null : new Date(lastModified);
            }
            return mLastModified;
        }
        catch (FileNotFoundException e)
        {
            return new Date(0L);
        }
    }

    public String getType()
    {
        try
        {
            return getShellFile().getFolderType();
        }
        catch (FileNotFoundException e)
        {
            mFileOperation.fireFileDeleted(mFile);
            return "";
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Object class

    public boolean equals(Object aObject)
    {
        if (this == aObject)
        {
            return true;
        }
        if (aObject == null || getClass() != aObject.getClass())
        {
            return false;
        }

        final FileListItem that = (FileListItem) aObject;

        return mFile.equals(that.mFile);

    }

    public int hashCode()
    {
        return mFile.hashCode();
    }

    /**
     *
     */
    public String toString()
    {
        try
        {
            if (mDisplayName == null)
            {
                mDisplayName = getShellFile().getDisplayName();
            }
            return mDisplayName;
        }
        catch (FileNotFoundException e)
        {
            mFileOperation.fireFileDeleted(mFile);
            return mFile.getName();
        }
    }


}