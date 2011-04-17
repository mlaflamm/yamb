package yamb.app.fileitem.filelist;

import yamb.app.fileitem.FileIconType;
import yamb.app.view.FileListViewContext;
import org.apache.commons.io.FilenameUtils;

import java.awt.Component;
import java.util.HashMap;
import java.util.Map;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JList;

public class FileListRenderer extends DefaultListCellRenderer
{
    private final Map<String, Icon> mFileTypeIconCache = new HashMap<String, Icon>();

    private final FileListViewContext mViewContext;

    public FileListRenderer(FileListViewContext aViewContext)
    {
        mViewContext = aViewContext;
    }

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
        aCellHasFocus = aValue.equals(mViewContext.getFocusedItem());
        super.getListCellRendererComponent(aList, aValue, aIndex, aIsSelected,
                aCellHasFocus);

        FileListItem fileItem = (FileListItem) aValue;
        Icon icon = getFileTypeIcon(fileItem);
        if (icon != null)
        {
            setIcon(icon);
        }

        return this;

    }

    private Icon getFileTypeIcon(FileListItem aFileItem)
    {
        // Directory, take shell icon
        if (aFileItem.isDirectory())
        {
            return aFileItem.getIcon(FileIconType.SMALL);
        }

        // Lookup file type icon from memory
        String extension = FilenameUtils.getExtension(aFileItem.toString());
        Icon icon = mFileTypeIconCache.get(extension);
        if (icon != null)
        {
            return icon;
        }

        // Not in memory, cache and return shell icon
        icon = aFileItem.getIcon(FileIconType.SMALL);
        mFileTypeIconCache.put(extension, icon);

        return icon;
    }
}