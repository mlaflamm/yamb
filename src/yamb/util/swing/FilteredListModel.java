package yamb.util.swing;

import yamb.util.Disposable;

import java.util.ArrayList;
import java.util.Collections;
import javax.swing.AbstractListModel;
import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;


/**
 * @author manuel.laflamme
 * @since 7-Feb-2008
 */
public class FilteredListModel extends AbstractListModel implements ListDataListener, FilterEventListener, Disposable
{
    private final ListModel mListModel;
    private ObjectFilter mFilter;
    private RemoveRangeArrayList<Integer> mViewMapping = new RemoveRangeArrayList<Integer>();

    public FilteredListModel(ListModel aListModel, ObjectFilter aFilter)
    {
        mListModel = aListModel;
        mFilter = aFilter;

        mListModel.addListDataListener(this);
        mFilter.addFilterEventListener(this);

        computeViewMapping();
    }

    private void computeViewMapping()
    {
        int oldMappingSize = mViewMapping == null ? 0 : mViewMapping.size();

        // Compute new view mapping
        RemoveRangeArrayList<Integer> indexMapping = new RemoveRangeArrayList<Integer>(mListModel.getSize());
        for (int i = 0; i < mListModel.getSize(); i++)
        {
            Object item = mListModel.getElementAt(i);
            if (mFilter.accept(item))
            {
                indexMapping.add(i);
            }
        }
        mViewMapping = indexMapping;

        int newMappingSize = mViewMapping.size();
        if (oldMappingSize > newMappingSize)
        {
            fireIntervalRemoved(this, newMappingSize, oldMappingSize - 1);
            if (newMappingSize > 0)
            {
                fireContentsChanged(this, 0, newMappingSize - 1);
            }
        }
        else if (newMappingSize > oldMappingSize)
        {
            fireIntervalAdded(this, oldMappingSize, newMappingSize - 1);
            if (oldMappingSize > 0)
            {
                fireContentsChanged(this, 0, oldMappingSize - 1);
            }
        }
        else if (newMappingSize > 0)
        {
            fireContentsChanged(this, 0, Math.min(oldMappingSize, newMappingSize - 1));
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    // ListModel interface

    public int getSize()
    {
        return mViewMapping.size();
    }

    public Object getElementAt(int aIndex)
    {
        return mListModel.getElementAt(mViewMapping.get(aIndex));
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    // Disposable interface

    public void dispose()
    {
        mListModel.removeListDataListener(this);
        mFilter.removeFilterEventListener(this);

        if (mListModel instanceof Disposable)
        {
            ((Disposable) mListModel).dispose();
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    // ListDataListener interface

    public void intervalAdded(ListDataEvent aEvent)
    {
        int index0 = aEvent.getIndex0();
        int index1 = aEvent.getIndex1();

        int firstViewAddIndex = Collections.binarySearch(mViewMapping, index0);
        if (firstViewAddIndex < 0)
        {
            firstViewAddIndex = -(firstViewAddIndex + 1);
        }

        // Add accepted items
        int viewAddedCount = 0;
        int nextViewAddIndex = firstViewAddIndex;
        for (int i = index0; i < index1 + 1; i++)
        {
            Object item = mListModel.getElementAt(i);
            if (mFilter.accept(item))
            {
                mViewMapping.add(nextViewAddIndex, i);
                nextViewAddIndex++;
                viewAddedCount++;
            }
        }

        // Shift indexes after added items
        for (int i = nextViewAddIndex; i < mViewMapping.size(); i++)
        {
            mViewMapping.set(i, mViewMapping.get(i) + ((index1 + 1) - index0));
        }

        // Fire events if at least one item is added in view
        if (viewAddedCount > 0)
        {
            fireIntervalAdded(this, firstViewAddIndex, nextViewAddIndex - 1);
        }
    }

    public void intervalRemoved(ListDataEvent aEvent)
    {
        int index0 = aEvent.getIndex0();
        int index1 = aEvent.getIndex1();

        // Find view indexes
        int viewIndex0 = -1;
        for (int i = index0; i < index1 + 1; i++)
        {
            viewIndex0 = Collections.binarySearch(mViewMapping, i);
            if (viewIndex0 > -1)
            {
                break;
            }
        }

        int viewIndex1 = viewIndex0;
        if (index0 != index1)
        {
            for (int i = index1; i > index0; i--)
            {
                viewIndex1 = Collections.binarySearch(mViewMapping, i);
                if (viewIndex1 > -1)
                {
                    break;
                }
            }
        }

        // At least one item removed from the view
        if (viewIndex0 > -1)
        {
            // Remove items from view
            mViewMapping.remove(viewIndex0, viewIndex1 + 1);

            // Shift indexes after removed items
            int removedCount = (index1 + 1) - index0;
            for (int i = viewIndex0; i < mViewMapping.size(); i++)
            {
                mViewMapping.set(i, mViewMapping.get(i) - removedCount);
            }

            fireIntervalRemoved(this, viewIndex0, viewIndex1);
        }
        // No item removed from the view
        else
        {
            // Shift indexes after removed items
            int removedCount = (index1 + 1) - index0;
            for (int i = -(viewIndex0 + 1); i < mViewMapping.size(); i++)
            {
                mViewMapping.set(i, mViewMapping.get(i) - removedCount);
            }
        }
    }

    public void contentsChanged(ListDataEvent aEvent)
    {
        // Single item change optimization!
        if (aEvent.getIndex0() == aEvent.getIndex1())
        {
            int viewIndex = Collections.binarySearch(mViewMapping, aEvent.getIndex0());
            Object item = mListModel.getElementAt(aEvent.getIndex0());

            // Item is in view ...
            if (viewIndex > -1)
            {
                // Item continue to be in view.
                if (mFilter.accept(item))
                {
                    // Forward content change with view index
                    fireContentsChanged(this, viewIndex, viewIndex);
                    return;
                }
                // Item removed from the view
                else
                {
                    mViewMapping.remove(viewIndex);
                    fireIntervalRemoved(this, viewIndex, viewIndex);
                    return;
                }
            }

            // Item is not in view ...
            else
            {
                // Item added in view
                if (mFilter.accept(item))
                {
                    mViewMapping.add(-(viewIndex + 1), aEvent.getIndex0());
                    fireIntervalAdded(this, -(viewIndex + 1), -(viewIndex + 1));
                    return;
                }
                // Item continue to not be in view
                else
                {
                    // Ignore content change
                    return;
                }
            }
        }

        computeViewMapping();
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    // FilterEventListener interface

    public void filterChanged(FilterEvent aEvent)
    {
        computeViewMapping();
    }

    private static class RemoveRangeArrayList<E> extends ArrayList<E>
    {
        public RemoveRangeArrayList(int aInitialCapacity)
        {
            super(aInitialCapacity);
        }

        public RemoveRangeArrayList()
        {
        }

        public void remove(int aFromIndex, int aToIndex)
        {
            removeRange(aFromIndex, aToIndex);
        }
    }
}
