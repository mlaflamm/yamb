package yamb.app.fileitem.filelist.details;

import yamb.util.Disposable;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

public class DetailsJTable extends JTable implements Disposable
{
    private boolean mIsColumnInitialized = false;

    public DetailsJTable()
    {
        setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        setShowGrid(false);
//        setSelectionModel(listSelectionModel);
        setDefaultRenderer(Object.class, new DetailsCellRenderer());
        putClientProperty("JTable.autoStartsEdit", Boolean.FALSE);

        addPropertyChangeListener("model", new PropertyChangeListener()
        {
            public void propertyChange(PropertyChangeEvent aEvent)
            {
                if (aEvent.getOldValue() instanceof Disposable)
                {
                    ((Disposable) aEvent.getOldValue()).dispose();
                }
            }
        });
    }

    public void setModel(TableModel dataModel)
    {
        super.setModel(dataModel);

        if (!mIsColumnInitialized && dataModel instanceof DetailsTableModel)
        {
            DetailsTableModel detailsModel = (DetailsTableModel) dataModel;
            TableColumnModel columnModel = getColumnModel();
            for (int i = 0; i < columnModel.getColumnCount(); i++)
            {
                DetailsColumn detailsColumn = detailsModel.getDetailsColumn(i);
                TableColumn column = columnModel.getColumn(i);
                column.setPreferredWidth(detailsColumn.getPreferedSize());
            }
            mIsColumnInitialized = true;
        }
    }

    public void dispose()
    {
        if (getModel() instanceof Disposable)
        {
            ((Disposable) getModel()).dispose();
        }
    }
}