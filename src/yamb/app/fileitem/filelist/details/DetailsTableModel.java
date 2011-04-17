package yamb.app.fileitem.filelist.details;

import javax.swing.ListModel;
import javax.swing.table.AbstractTableModel;

public class DetailsTableModel extends AbstractTableModel //implements Disposable
{
    private final ListModel mListModel;
    private final DetailsColumn[] mColumns;

    public DetailsTableModel(ListModel listModel)
    {
        mListModel = listModel;
        mColumns = DetailsColumn.COLUMNS;

    }

    DetailsColumn getDetailsColumn(int columnIndex)
    {
        return mColumns[columnIndex];
    }

    ////////////////////////////////////////////////////////////////////////////
    // TableModel interface

    /**
     *
     */
    public int getRowCount()
    {
        return mListModel.getSize();
    }

    /**
     *
     */
    public int getColumnCount()
    {
        return mColumns.length;
    }

    /**
     *
     */
    public String getColumnName(int columnIndex)
    {
        return mColumns[columnIndex].getName();
    }

    /**
     *
     */
    public Class getColumnClass(int columnIndex)
    {
        return Object.class;
    }

    /**
     *
     */
    public boolean isCellEditable(int rowIndex, int columnIndex)
    {
        return false;
    }

    /**
     *
     */
    public Object getValueAt(int rowIndex, int columnIndex)
    {
        return mListModel.getElementAt(rowIndex);
    }

    /**
     *
     */
    public void setValueAt(Object aValue, int rowIndex, int columnIndex)
    {
        throw new UnsupportedOperationException();
    }

}