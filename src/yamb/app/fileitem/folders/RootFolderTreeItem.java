package yamb.app.fileitem.folders;

import yamb.app.fileitem.FileIconType;
import sun.awt.shell.ShellFolder;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.TreePath;


/**
 * @author Manuel Laflamme
 * @version 1.0
 */

public class RootFolderTreeItem extends FolderTreeItem
{
    private List<FolderTreeItem> mChildren = null;

    public RootFolderTreeItem()
    {
        super(null);
    }

    /**
     * Create a folder tree path of the specified file.
     *
     * @param aLoadedOnly if true, return the path if the folder is loaded from the tree root or null if it is not.
     * @return return the path or null if the path cannot be created
     */
    public TreePath createTreePath(File aFile, boolean aLoadedOnly)
    {
        // Root
        if (aFile == null)
        {
            return new TreePath(this);
        }

        if (!aFile.isDirectory())
        {
            return null;
        }

        List<File> folders = toFolderList(aFile);
        FolderTreeItem[] path = new FolderTreeItem[folders.size() + 1];

        path[0] = this;
        for (int i = 0; i < folders.size(); i++)
        {
            if (aLoadedOnly && !path[i].hasChildrenLoaded())
            {
                return null;
            }

            int index = path[i].getIndexOfChild(folders.get(i));
            if (index < 0)
            {
                return null;
            }

            path[i+1] = path[i].getChild(index);
        }

        return new TreePath(path);
    }

    private List<File> toFolderList(File aFile)
    {
        List<File> list = new ArrayList<File>();

        File folder = aFile;
        while (folder != null)
        {
            if (!(folder instanceof ShellFolder))
            {
                try
                {
                    folder = ShellFolder.getShellFolder(folder);
                }
                catch (java.io.FileNotFoundException e)
                {
                    throw new IllegalStateException(e);
                }
            }
            list.add(folder);
            folder = folder.getParentFile();
        }

        Collections.reverse(list);
        return list;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////
    // FolderTreeItem object

    @Override
    protected List<FolderTreeItem> getChildren()
    {
        if (mChildren == null)
        {
            mChildren = toList(FileSystemView.getFileSystemView().getRoots());
        }

        return mChildren;
    }

    @Override
    public ImageIcon getIcon(FileIconType aIconType)
    {
        return null;
    }

    @Override
    boolean hasChildrenLoaded()
    {
        return mChildren != null;
    }

    @Override
    public int compareTo(FolderTreeItem aItem)
    {
        return aItem instanceof RootFolderTreeItem ? 0 : -1;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Object class

    public String toString()
    {
        return "root";
    }

    public int hashCode()
    {
        return toString().hashCode();
    }

    public boolean equals(Object aObject)
    {
        return aObject instanceof RootFolderTreeItem;
    }

}