package yamb.app.view.stat;

import yamb.app.tag.TagManager;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * @author manuel.laflamme
 * @since 24-Aug-2008
 */
public class TagTableCellRenderer extends DefaultTableCellRenderer
{
    private final TagManager mTagManager;

    public TagTableCellRenderer(TagManager aTagManager)
    {
        mTagManager = aTagManager;
    }

    public Component getTableCellRendererComponent(JTable aTable, Object aValue, boolean aIsSelected, boolean aHasFocus, int aRow, int aColumn)
    {
        String value = aValue.toString();

        boolean isFavorite = mTagManager.isFavoriteTag(value);

        super.getTableCellRendererComponent(aTable, aValue, aIsSelected, aHasFocus, aRow, aColumn);

        if (!aIsSelected)
        {
            Color foreground = isFavorite ? Color.BLUE : aTable.getForeground();
            setForeground(foreground);
        }

        return this;
    }
}
