package yamb.app.fileitem.folders;

import yamb.app.fileitem.FileIconType;
import yamb.app.fileitem.FileItem;
import sun.awt.shell.ShellFolder;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.swing.ImageIcon;

public class FolderTreeItem implements FileItem, Comparable<FolderTreeItem>
{
    private static final Date NO_DATE = new Date();

    private File mFile = null;
    private List<FolderTreeItem> mChildren = null;

    private String mDisplayName = null;
    private ImageIcon mSmallIcon = null;
    private Date mLastModified = NO_DATE;

    FolderTreeItem(File aDirectory)
    {
        mFile = aDirectory;
    }

    private ShellFolder getShellFile()
    {
        if (!(mFile instanceof ShellFolder))
        {
            try
            {
                mFile = ShellFolder.getShellFolder(mFile);
            }
            catch (java.io.FileNotFoundException e)
            {
                throw new IllegalStateException(e);
            }
        }

        return (ShellFolder) mFile;
    }

    public File getFile()
    {
        return mFile;
    }

    public ImageIcon getIcon(FileIconType aIconType)
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

    public boolean isDirectory()
    {
        return true;
    }

    public Date getLastModified()
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

    public String getType()
    {
        return getShellFile().getFolderType();
    }

    public long getSize()
    {
        return 0;
    }

    public FolderTreeItem getChild(int aIndex)
    {
        return getChildren().get(aIndex);
    }

    FolderTreeItem setChild(int aIndex, FolderTreeItem aItem)
    {
        return getChildren().set(aIndex, aItem);
    }


    int addChild(FolderTreeItem aItem)
    {
        List<FolderTreeItem> children = getChildren();
        children.add(aItem);
        Collections.sort(children);
        return getIndexOfChild(aItem);
    }

    FolderTreeItem removeChild(int aIndex)
    {
        return getChildren().remove(aIndex);
    }

    public int getChildCount()
    {
        return getChildren().size();
    }

    public int getIndexOfChild(FolderTreeItem aChildItem)
    {
        return getIndexOfChild(aChildItem.getFile());
    }

    int getIndexOfChild(File aChild)
    {
        List<FolderTreeItem> children = getChildren();
        for (int i = 0; i < children.size(); i++)
        {
            FolderTreeItem item = children.get(i);
            if (item.getFile().equals(aChild))
            {
                return i;
            }
        }
        return -1;
    }

    boolean hasChildrenLoaded()
    {
        return mChildren != null;
    }

    protected List<FolderTreeItem> getChildren()
    {
        if (mChildren == null)
        {
//            System.out.println("FolderTreeItem: " + getShellFile().getDisplayName());
            File[] files = mFile.listFiles();
            ShellFolder.sortFiles(Arrays.asList(files));
            if (files != null)
            {
                mChildren = toList(files);
            }
            else
            {
                mChildren = new ArrayList<FolderTreeItem>();
            }
        }

        return mChildren;
    }

    protected List<FolderTreeItem> toList(File[] aFiles)
    {
        List<FolderTreeItem> list = new ArrayList<FolderTreeItem>(aFiles.length);
        for (File file : aFiles)
        {
            if (file.isDirectory())
            {
                list.add(new FolderTreeItem(file));
            }
        }
        return list;
    }

    public int compareTo(FolderTreeItem aItem)
    {
        return getShellFile().compareTo(aItem.getShellFile());
    }

    ////////////////////////////////////////////////////////////////////////////
    // Object class

    /**
     *
     */
    public String toString()
    {
        if (mDisplayName == null)
        {
            mDisplayName = getShellFile().getDisplayName();
        }
        return mDisplayName;
    }

    /**
     *
     */
    public int hashCode()
    {
        return mFile.hashCode();
    }

    /**
     *
     */
    public boolean equals(Object aObject)
    {
        if (aObject instanceof FolderTreeItem)
        {
            return mFile.equals(((FolderTreeItem) aObject).getFile());
        }

        return false;
    }

}