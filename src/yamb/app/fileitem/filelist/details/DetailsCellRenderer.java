package yamb.app.fileitem.filelist.details;

import yamb.app.fileitem.FileIconType;
import yamb.app.fileitem.filelist.FileListItem;

import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class DetailsCellRenderer extends DefaultTableCellRenderer
{
    public void setBounds(int x, int y, int width, int height)
    {
        super.setBounds(x, y, Math.min(width, this.getPreferredSize().width + 4),
                height);
    }

    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int columnIndex)
    {
        DetailsTableModel model = (DetailsTableModel) table.getModel();
        DetailsColumn column = model.getDetailsColumn(columnIndex);

        // Text aligment
        setHorizontalAlignment(column.getHorizontalAlignment());

        // Colors
        if (column == DetailsColumn.FILE_NAME &&
                table.isRowSelected(row) && table.isFocusOwner())
        {
            super.setForeground(table.getSelectionForeground());
            super.setBackground(table.getSelectionBackground());
        }
        else
        {
            super.setForeground(table.getForeground());
            super.setBackground(table.getBackground());
        }

        // Font
        setFont(table.getFont());

        // Icon
        FileListItem item = (FileListItem) value;
        if (column == DetailsColumn.FILE_NAME)
        {
            setIcon(item.getIcon(FileIconType.SMALL));
        }
        else
        {
            setIcon(null);
        }

        // Value
        setValue(column.getFormatedValue(value));

        return this;
    }
}