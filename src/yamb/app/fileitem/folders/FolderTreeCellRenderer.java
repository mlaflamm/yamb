package yamb.app.fileitem.folders;

import yamb.app.fileitem.FileIconType;
import yamb.app.tag.library.LibraryManager;
import yamb.app.view.FileListViewContext;

import java.awt.Component;
import java.io.File;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

public class FolderTreeCellRenderer extends DefaultTreeCellRenderer
{
    private final FileListViewContext mViewContext;
    private final LibraryManager mTagManager;
    private final ImageIcon mLibraryIcon = new ImageIcon(getClass().getClassLoader().getResource("yamb/images/library.gif"));


    public FolderTreeCellRenderer(FileListViewContext aViewContext, LibraryManager aTagManager)
    {
        mViewContext = aViewContext;
        mTagManager = aTagManager;
    }

    public Component getTreeCellRendererComponent(JTree aTree, Object aValue,
            boolean aIsSelected, boolean aIsExpanded, boolean aIsLeaf, int aRow, boolean aHasFocus)
    {
        aHasFocus = aValue.equals(mViewContext.getFocusedItem());
        super.getTreeCellRendererComponent(aTree, aValue, aIsSelected, aIsExpanded,
                aIsLeaf, aRow, aHasFocus);

        FolderTreeItem treeItem = (FolderTreeItem) aValue;
        File file = treeItem.getFile();
//        boolean isLibraryRoot = false;
        boolean isLibraryRoot = file != null && mTagManager.isLibraryRoot(file);
        Icon icon = isLibraryRoot ? mLibraryIcon : treeItem.getIcon(FileIconType.SMALL);
        if (icon != null)
        {
            setIcon(icon);
        }

        return this;
    }
}