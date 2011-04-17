package yamb.app.view.stat;

import java.awt.Component;
import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 * @author manuel.laflamme
 * @since 26-Aug-2008
 */
public class StatNodeTreeCellRenderer extends DefaultTreeCellRenderer
{
    public Component getTreeCellRendererComponent(JTree aTree, Object aValue,
            boolean aIsSelected, boolean aIsExpanded, boolean aIsLeaf, int aRow, boolean aHasFocus)
    {
        super.getTreeCellRendererComponent(aTree, aValue, aIsSelected, aIsExpanded,
                aIsLeaf, aRow, aHasFocus);

        DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) aValue;
        if (treeNode.getUserObject() instanceof StatNode)
        {
            Icon icon = ((StatNode) treeNode.getUserObject()).getIcon();
            if (icon != null)
            {
                setIcon(icon);
            }
        }

        return this;
    }
}
