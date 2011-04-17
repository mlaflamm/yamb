package yamb.app.fileitem.folders;

import yamb.util.Disposable;
import yamb.util.event.SwingSafeEventSupport;
import yamb.util.io.shell.FileOperation;
import yamb.util.io.shell.FileOperationEvent;
import yamb.util.io.shell.FileOperationListener;

import java.io.File;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;


public class FolderTreeModel implements TreeModel, FileOperationListener, Disposable
{
    private final SwingSafeEventSupport mEventSupport = new SwingSafeEventSupport();
    private final FileOperation mFileOperation;

    private FolderTreeItem mRoot = new RootFolderTreeItem();

    public FolderTreeModel(FileOperation aFileOperation)
    {
        mFileOperation = aFileOperation;
        mFileOperation.addFileOperationListener(this);
    }

    protected void fireTreeNodesChanged(TreePath aParentPath, int aChangedIndex,
            FolderTreeItem aChangedItem)
    {
        mEventSupport.fireEvent("treeNodesChanged", new TreeModelEvent(this, aParentPath,
                new int[]{aChangedIndex}, new Object[]{aChangedItem}));

    }

    protected void fireTreeNodesInserted(TreePath aParentPath, int aInsertIndex,
            FolderTreeItem aInsertedItem)
    {
        mEventSupport.fireEvent("treeNodesInserted", new TreeModelEvent(this, aParentPath,
                new int[]{aInsertIndex}, new Object[]{aInsertedItem}));
    }

    protected void fireTreeNodesRemoved(TreePath aParentPath, int aRemovedIndex,
            FolderTreeItem aRemovedItem)
    {
        mEventSupport.fireEvent("treeNodesRemoved", new TreeModelEvent(this, aParentPath,
                new int[]{aRemovedIndex}, new Object[]{aRemovedItem}));
    }

    protected void fireStructureChanged(TreePath aChangedPath)
    {
        mEventSupport.fireEvent("treeStructureChanged", new TreeModelEvent(this, aChangedPath));
    }

    //////////////////////////////////////////////////////////////////////////////////////////////
    // Disposable interface

    public void dispose()
    {
        mFileOperation.removeFileOperationListener(this);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////
    // FileOperationListener interface

    public void fileRenamed(FileOperationEvent aEvent)
    {
        File renamedFile = aEvent.getSourceFile();
        if (!renamedFile.isDirectory())
        {
            return;
        }

        // Search the renamed file parent path from the root
        File parent = renamedFile.getParentFile();
        RootFolderTreeItem root = (RootFolderTreeItem) getRoot();
        TreePath parentPath = root.createTreePath(parent, true);

        // Parent folder not found or not loaded
        if (parentPath == null)
        {
            return;
        }

        // Ignore if no children loaded in parent
        FolderTreeItem parentItem = ((FolderTreeItem) parentPath.getLastPathComponent());
        if (!parentItem.hasChildrenLoaded())
        {
            return;
        }

        // Update the item in the parent
        int index = parentItem.getIndexOfChild(renamedFile);
        FolderTreeItem newItem = new FolderTreeItem(aEvent.getDestinationFile());
        parentItem.setChild(index, newItem);

        fireTreeNodesChanged(parentPath, index, newItem);
    }

    public void fileCopied(FileOperationEvent aEvent)
    {
        File parent = aEvent.getDestinationFile();
        File newFile = new File(parent, aEvent.getSourceFile().getName());
        if (!newFile.isDirectory())
        {
            return;
        }

        // Search the new file parent path from the root
        RootFolderTreeItem root = (RootFolderTreeItem) getRoot();
        TreePath parentPath = root.createTreePath(parent, true);

        // Parent folder not found or not loaded
        if (parentPath == null)
        {
            return;
        }

        // Ignore if no children loaded in parent
        FolderTreeItem parentItem = ((FolderTreeItem) parentPath.getLastPathComponent());
        if (!parentItem.hasChildrenLoaded())
        {
            return;
        }

        // Add the item in the parent
        FolderTreeItem newItem = new FolderTreeItem(newFile);
        int index = parentItem.addChild(newItem);

        fireTreeNodesInserted(parentPath, index, newItem);
    }

    public void fileMoved(FileOperationEvent aEvent)
    {
        fileDeleted(aEvent);
        fileCopied(aEvent);
    }

    public void fileDeleted(FileOperationEvent aEvent)
    {
        File deletedFile = aEvent.getSourceFile();
        if (!deletedFile.isDirectory())
        {
            return;
        }

        // Search the deleted file parent path from the root
        File parent = deletedFile.getParentFile();
        RootFolderTreeItem root = (RootFolderTreeItem) getRoot();
        TreePath parentPath = root.createTreePath(parent, true);

        // Parent folder not found or not loaded
        if (parentPath == null)
        {
            return;
        }

        // Ignore if no children loaded in parent
        FolderTreeItem parentItem = ((FolderTreeItem) parentPath.getLastPathComponent());
        if (!parentItem.hasChildrenLoaded())
        {
            return;
        }

        // Remove the item from the parent
        int index = parentItem.getIndexOfChild(deletedFile);
        FolderTreeItem removedItem = parentItem.removeChild(index);

        fireTreeNodesRemoved(parentPath, index, removedItem);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////
    // TreeModel interface

    public Object getRoot()
    {
        return mRoot;
    }

    public Object getChild(Object aParent, int aIndex)
    {
        return ((FolderTreeItem) aParent).getChild(aIndex);
    }

    public int getChildCount(Object aParent)
    {
        return ((FolderTreeItem) aParent).getChildCount();
    }

    public boolean isLeaf(Object aNode)
    {
        return false;
    }

    public void valueForPathChanged(TreePath aTreePath, Object aNewValue)
    {
        // todo : implements

        // So far only a complete refresh of the whole tree is supported
        if (getRoot().equals(aTreePath.getLastPathComponent()))
        {
            mRoot = new RootFolderTreeItem();
            fireStructureChanged(new TreePath(mRoot));
        }
    }

    public int getIndexOfChild(Object aParent, Object aChild)
    {
        return ((FolderTreeItem) aParent).getIndexOfChild((FolderTreeItem) aChild);
    }

    public void addTreeModelListener(TreeModelListener aListener)
    {
        mEventSupport.addEventListener(aListener);
    }

    public void removeTreeModelListener(TreeModelListener aListener)
    {
        mEventSupport.removeEventListener(aListener);
    }
}