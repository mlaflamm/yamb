package yamb.app.fileitem.folders;

import sun.awt.shell.ShellFolder;

import java.awt.Component;
import java.io.File;
import java.io.FileNotFoundException;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JList;

/**
 * @author manuel.laflamme
 * @since 27-Feb-2008
 */
public class RecentFolderListCellRenderer extends DefaultListCellRenderer
{
    private Icon mFolderIcon = null;

    public void setBounds(int aX, int aY, int aWidth, int aHeight)
    {
        super.setBounds(aX, aY, Math.min(aWidth, this.getPreferredSize().width + 4),
                aHeight);
    }

    /**
     *
     */
    public Component getListCellRendererComponent(JList aList, Object aValue, int aIndex, boolean aIsSelected, boolean aCellHasFocus)
    {
        File file = (File) aValue;
        String displayValue = file.getParentFile().getName() + " / " + file.getName();

        super.getListCellRendererComponent(aList, displayValue, aIndex, aIsSelected,
                aCellHasFocus);

        if (mFolderIcon == null)
        {
            try
            {
                mFolderIcon = new ImageIcon(ShellFolder.getShellFolder(file).getIcon(false));
            }
            catch (FileNotFoundException e)
            {
                // TODO: Log this instead
                throw new IllegalStateException(e);
            }
        }
        setIcon(mFolderIcon);

        return this;

    }

}
