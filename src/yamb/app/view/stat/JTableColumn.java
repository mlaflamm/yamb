package yamb.app.view.stat;

import java.util.List;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

/**
 * @author manuel.laflamme
 * @since 24-Aug-2008
 */
public abstract class JTableColumn
{
    private final String mColumnName;
    private final int mPreferedSize;
    private final int mMaxSize;
    private final Class mColumnClass;

    public JTableColumn(String aColumnName)
    {
        mColumnName = aColumnName;
        mPreferedSize = -1;
        mMaxSize = -1;
        mColumnClass = Object.class;
    }

    public JTableColumn(String aColumnName, int aPreferedSize)
    {
        mColumnName = aColumnName;
        mPreferedSize = aPreferedSize;
        mMaxSize = -1;
        mColumnClass = Object.class;
    }


    public JTableColumn(String aColumnName, int aPreferedSize, int aMaxSize)
    {
        mColumnName = aColumnName;
        mPreferedSize = aPreferedSize;
        mMaxSize = aMaxSize;
        mColumnClass = Object.class;
    }

    public JTableColumn(String aColumnName, Class aColumnClass, int aPreferedSize, int aMaxSize)
    {
        mColumnName = aColumnName;
        mPreferedSize = aPreferedSize;
        mMaxSize = aMaxSize;
        mColumnClass = aColumnClass;
    }

    public String getColumnName()
    {
        return mColumnName;
    }

    public int getPreferedSize()
    {
        return mPreferedSize;
    }

    public int getMaxSize()
    {
        return mMaxSize;
    }

    public Class<?> getColumnClass()
    {
        return mColumnClass;
    }

    public TableCellRenderer getTableCellRenderer()
    {
        return null;
    }

    public static TableColumnModel createTableColumnModel(List<JTableColumn> aColumns)
    {
        DefaultTableColumnModel model = new DefaultTableColumnModel();

        for (int i = 0; i < aColumns.size(); i++)
        {
            JTableColumn column = aColumns.get(i);
            model.addColumn(new TableColumn(i));
            model.getColumn(i).setHeaderValue(column.getColumnName());
            model.getColumn(i).setCellRenderer(column.getTableCellRenderer());
            model.getColumn(i).setPreferredWidth(column.getPreferedSize());
            if (column.getMaxSize() > 0)
            {
                model.getColumn(i).setMaxWidth(column.getMaxSize());
            }
        }

        return model;
    }

    public abstract Object getFormatedValue(Object value, int aRowIndex/*, int aColumnIndex*/);
}
