package yamb.app.view.stat;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;

/**
 * @author manuel.laflamme
 * @since 24-Aug-2008
 */
public class PercentTableCellRenderer extends JProgressBar implements TableCellRenderer
{
    private static final Border NO_FOCUS_BORDER = new EmptyBorder(1, 1, 1, 1);

    public PercentTableCellRenderer()
    {
        super(0, 1000);
    }

    public Component getTableCellRendererComponent(JTable aJTable, Object aValue, boolean aIsSelected,
            boolean aHasFocus, int aRow, int aColumn)
    {
        Double value = (Double) aValue;

        if (aIsSelected)
        {
            super.setForeground(aJTable.getSelectionForeground());
            super.setBackground(aJTable.getSelectionBackground());
        }
        else
        {
            super.setForeground(aJTable.getForeground());
            super.setBackground(aJTable.getBackground());
        }
        super.setForeground(Color.RED);

        setFont(aJTable.getFont());

        if (aHasFocus)
        {
            Border border = null;
            if (aIsSelected)
            {
                border = UIManager.getBorder("Table.focusSelectedCellHighlightBorder");
            }
            if (border == null)
            {
                border = UIManager.getBorder("Table.focusCellHighlightBorder");
            }
            setBorder(border);

            if (!aIsSelected && aJTable.isCellEditable(aRow, aColumn))
            {
                Color col;
                col = UIManager.getColor("Table.focusCellForeground");
                if (col != null)
                {
                    super.setForeground(col);
                }
                col = UIManager.getColor("Table.focusCellBackground");
                if (col != null)
                {
                    super.setBackground(col);
                }
            }
        }
        else
        {
            setBorder(NO_FOCUS_BORDER);
        }

        setStringPainted(true);
        setString("");
        setValue((int)(value * 1000));

        return this;
    }


}
