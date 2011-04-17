package yamb.app.view.stat;

import yamb.app.tag.library.StatisticsSnapshot;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

/**
 * @author manuel.laflamme
 * @since 24-Aug-2008
 */
public class LibraryStatisticModel extends AbstractTableModel implements TableColumnModelFactory
{
    private final List<StatisticsSnapshot.LibStat> mStatistics;

    private final TableCellRenderer mPercentTableCellRenderer = new PercentTableCellRenderer();

    private final JTableColumn[] mColumns = new JTableColumn[]{
            new JTableColumn("Rank", Integer.class, 60, 60)
            {
                public Object getFormatedValue(Object value, int aRowIndex)
                {
                    return aRowIndex + 1;
                }
            },
            new JTableColumn("Library", 100)
            {
                public Object getFormatedValue(Object value, int aRowIndex)
                {
                    return mStatistics.get(aRowIndex).getLibraryName();
                }
            },
            new JTableColumn("Files", Integer.class, 50, 50)
            {
                public Object getFormatedValue(Object value, int aRowIndex)
                {
                    return mStatistics.get(aRowIndex).getTaggedFileCount();
                }
            },
            new JTableColumn("Files", Integer.class, 200, -1)
            {
                public Object getFormatedValue(Object value, int aRowIndex)
                {
                    return (double)mStatistics.get(aRowIndex).getTaggedFileCount() / mStatistics.get(0).getTaggedFileCount();
                }

                public TableCellRenderer getTableCellRenderer()
                {
                    return mPercentTableCellRenderer;
                }
            },
            new JTableColumn("Tags", Integer.class, 50, 50)
            {
                public Object getFormatedValue(Object value, int aRowIndex)
                {
                    return mStatistics.get(aRowIndex).getTagCount();
                }
            },
    };

    public LibraryStatisticModel(StatisticsSnapshot aSnapshot)
    {
        mStatistics = aSnapshot.getLibraryStatistics();
        Collections.sort(mStatistics, StatisticsSnapshot.LIB_FILECOUNT_COMPARATOR);
    }

    public TableColumnModel createTableColumnModel()
    {
        return JTableColumn.createTableColumnModel(Arrays.asList(mColumns));
    }

    public String getColumnName(int aColumnIndex)
    {
        return mColumns[aColumnIndex].getColumnName();
    }

    public Class<?> getColumnClass(int aColumnIndex)
    {
        return mColumns[aColumnIndex].getColumnClass();
    }

    public int getRowCount()
    {
        return mStatistics.size();
    }

    public int getColumnCount()
    {
        return mColumns.length;
    }

    public Object getValueAt(int aRowIndex, int aColumnIndex)
    {
        return mColumns[aColumnIndex].getFormatedValue(null, aRowIndex);
    }
}
