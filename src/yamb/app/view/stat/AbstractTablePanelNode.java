package yamb.app.view.stat;

import yamb.app.tag.library.StatisticsSnapshot;
import yamb.util.Disposable;

import java.awt.Component;
import java.awt.event.MouseListener;
import javax.swing.Icon;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

/**
 * @author manuel.laflamme
 * @since 26-Aug-2008
 */
public abstract class AbstractTablePanelNode extends PanelNode implements Disposable
{
    private MouseListener mMouseListener = null;
    private JTable mJTable = null;

    protected AbstractTablePanelNode(StatisticsSnapshot aSnapshot, String aTitle, Icon aIcon)
    {
        super(aTitle, aIcon, aSnapshot);
    }

    public Component getPanel()
    {
        if (mJTable == null)
        {
            TableModel model = createTableModel(getSnapshot());
            TableColumnModel tableColumnModel = null;
            if (model instanceof TableColumnModelFactory)
            {
                tableColumnModel = ((TableColumnModelFactory)model).createTableColumnModel();
            }
            mJTable = new JTable(model, tableColumnModel);
            mJTable.setAutoCreateRowSorter(true);
            mMouseListener = createMouseListener();
            if (mMouseListener != null)
            {
                mJTable.addMouseListener(mMouseListener);
            }
        }
        return new JScrollPane(mJTable);
    }

    public void refresh(StatisticsSnapshot aSnapshot)
    {
        setSnapshot(aSnapshot);
        if (mJTable != null)
        {
            mJTable.setModel(createTableModel(getSnapshot()));
        }
    }

    public void dispose()
    {
        if (mMouseListener != null && mJTable != null)
        {
            mJTable.removeMouseListener(mMouseListener);
        }
    }
    
    protected abstract TableModel createTableModel(StatisticsSnapshot aSnapshot);

    protected MouseListener createMouseListener()
    {
        return null;
    }

}
