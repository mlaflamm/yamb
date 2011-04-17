package yamb.util.swing;

import yamb.util.Disposable;

import javax.swing.AbstractListModel;
import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

/**
 * @author manuel.laflamme
 * @since 9-Aug-2008
 */
public class AdaptiveFilteredListModel extends AbstractListModel implements ListDataListener, FilterEventListener, Disposable
{
    private ListModel mListModel;
    private ObjectFilter mFilter;

    public AdaptiveFilteredListModel(ListModel aListModel, ObjectFilter aFilter)
    {
        mListModel = aListModel;
        mFilter = aFilter;

        mListModel.addListDataListener(this);
        mFilter.addFilterEventListener(this);

        if (!mFilter.acceptAll())
        {
            filterListModel();
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    // FilterEventListener interface

    public void filterChanged(FilterEvent aEvent)
    {
        if (!mFilter.acceptAll())
        {
            filterListModel();
        }
    }

    /**
     * todo: this method is buggy and will crash is called more than once!!! Please fix!!!
     */
    private void filterListModel()
    {
        mFilter.removeFilterEventListener(this);
        mListModel.removeListDataListener(this);

        // Replace the original model with a filtered model
        ListModel previousModel = mListModel;
        ListModel filteredModel = new FilteredListModel(mListModel, mFilter);

        mListModel = filteredModel;
        mListModel.addListDataListener(this);
        mFilter = null; // no need anymore, the real filtered model will take care of it

        int originalSize = previousModel.getSize();
        int filteredSize = filteredModel.getSize();
        assert (originalSize >= filteredSize);

        if (originalSize > filteredSize)
        {
            fireIntervalRemoved(this, filteredSize, originalSize - 1);
            if (filteredSize > 0)
            {
                fireContentsChanged(this, 0, filteredSize - 1);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    // ListModel interface

    public int getSize()
    {
        return mListModel.getSize();
    }

    public Object getElementAt(int aIndex)
    {
        return mListModel.getElementAt(aIndex);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    // ListDataListener interface

    public void intervalAdded(ListDataEvent aEvent)
    {
        fireIntervalAdded(this, aEvent.getIndex0(), aEvent.getIndex1());
    }

    public void intervalRemoved(ListDataEvent aEvent)
    {
        fireIntervalRemoved(this, aEvent.getIndex0(), aEvent.getIndex1());
    }

    public void contentsChanged(ListDataEvent aEvent)
    {
        fireContentsChanged(this, aEvent.getIndex0(), aEvent.getIndex1());
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    // Disposable interface

    public void dispose()
    {
        mListModel.removeListDataListener(this);
        if (mFilter != null)
        {
            mFilter.removeFilterEventListener(this);
        }

        if (mListModel instanceof Disposable)
        {
            ((Disposable) mListModel).dispose();
        }
    }
}
