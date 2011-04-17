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
public class SeriesStatisticModel extends AbstractTableModel implements TableColumnModelFactory
{
    private final List<StatisticsSnapshot.SeriesStat> mStatistics;

    private final TableCellRenderer mPercentTableCellRenderer = new PercentTableCellRenderer();

    private final JTableColumn[] mColumns = new JTableColumn[]{
            new JTableColumn("Rank", Integer.class, 60, 60)
            {
                public Object getFormatedValue(Object value, int aRowIndex)
                {
                    return aRowIndex + 1;
                }
            },
            new JTableColumn("Series", 100)
            {
                public Object getFormatedValue(Object value, int aRowIndex)
                {
                    return mStatistics.get(aRowIndex).getSeriesName();
                }
            },
            new JTableColumn("Files", Integer.class, 50, 50)
            {
                public Object getFormatedValue(Object value, int aRowIndex)
                {
                    return mStatistics.get(aRowIndex).getFileCount();
                }
            },
            new JTableColumn("Files", Integer.class, 200, -1)
            {
                public Object getFormatedValue(Object value, int aRowIndex)
                {
                    return (double) mStatistics.get(aRowIndex).getFileCount() / mStatistics.get(0).getFileCount();
                }

                public TableCellRenderer getTableCellRenderer()
                {
                    return mPercentTableCellRenderer;
                }
            },
    };

    public SeriesStatisticModel(StatisticsSnapshot aSnapshot)
    {
        mStatistics = aSnapshot.getSeriesStatistics();
        Collections.sort(mStatistics, StatisticsSnapshot.SERIES_FILECOUNT_COMPARATOR);
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
