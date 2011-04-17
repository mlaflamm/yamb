package yamb.util.swing;

import yamb.util.Disposable;

import java.util.Arrays;
import javax.swing.AbstractListModel;
import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

/**
 * @author manuel.laflamme
 * @since 6-Mar-2008
 */

public class BuggyFilteredListModel extends AbstractListModel implements ListDataListener, FilterEventListener, Disposable
{
    private static final int MIN_LENGTH = 50;
    private final ListModel mListModel;
    private final ObjectFilter mFilter;
    private int mItemCount;
    private int[] mIndexes;

    public BuggyFilteredListModel(ListModel aSourceModel, ObjectFilter aFilter)
    {
        mListModel = aSourceModel;
        mFilter = aFilter;

        if (mListModel.getSize() * 2 > MIN_LENGTH)
        {
            mIndexes = new int[mListModel.getSize() * 2];
        }
        else
        {
            mIndexes = new int[MIN_LENGTH];
        }

        computeViewMapping();
//        int j = 0;
//        for (int i = 0; i < mSourceModel.getSize(); i++)
//        {
//            if (mFilter.accept(mSourceModel.getElementAt(i)))
//            {
//                mIndexes[j++] = i;
//            }
//        }
//        mItemCount = j;

        // listen  to source model
        mListModel.addListDataListener(this);

        // listen to filter value change
        mFilter.addFilterEventListener(this);
    }

    private void computeViewMapping()
    {
        int oldMappingSize = mItemCount;
        int newMappingSize = 0;

        // Compute new view mapping
        ensureCapacity(mListModel.getSize());
//        List<Integer> indexMapping = new ArrayList<Integer>(mSourceModel.getSize());
        for (int i = 0; i < mListModel.getSize(); i++)
        {
            Object item = mListModel.getElementAt(i);
            if (mFilter.accept(item))
            {
                mIndexes[newMappingSize++] = i;
            }
        }
        mItemCount = newMappingSize;

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

    /**
     * Asserts that index is valid
     */
    private void assertValidIndex(int aIndex)
    {
        if (aIndex < 0)
        {
            throw new IndexOutOfBoundsException("invalid index '" + aIndex + "' < 0");
        }
        if (aIndex >= mItemCount)
        {
            throw new IndexOutOfBoundsException("invalid index '" + aIndex + "' >= " + mItemCount);
        }
    }

    public String toString()
    {
        int iMax = mItemCount - 1;
        if (iMax == -1)
        {
            return "[]";
        }

        StringBuilder b = new StringBuilder();
        b.append('[');
        for (int i = 0; ; i++)
        {
            b.append(mIndexes[i]);
            b.append("=");
            b.append(mListModel.getElementAt(mIndexes[i]));
            if (i == iMax)
            {
                return b.append(']').toString();
            }
            b.append(", ");
        }
    }

    /**
     * this method do a dichotomic search on the mapping array _indexes
     * looking for key starting at position 0 and ending at postion _rowCount-1
     * it returns -1 if the key was not found or _rowCount==0
     */
    private int getMappingRow(int aKey)
    {
        return Arrays.binarySearch(mIndexes, 0, mItemCount, aKey);
//        if (mItemCount == 0)
//        {
//            return -1;
//        }
//
//        int start = 0;
//        int end = mItemCount - 1;
//        int middle;
//
//        while (true)
//        {
//            middle = (end - start) / 2;
//
//            if (middle == 0)
//            {
//                if (mIndexes[start] == key)
//                {
//                    return start;
//                }
//                else if (mIndexes[end] == key)
//                {
//                    return end;
//                }
//                else
//                {
//                    return -1;
//                }
//            }
//            else
//            {
//                middle += start;
//                if (mIndexes[middle] < key)
//                {
//                    start = middle;
//                }
//                else if (mIndexes[middle] > key)
//                {
//                    end = middle;
//                }
//                else // mIndexes[middle] == key
//                {
//                    return middle;
//                }
//            }
//
//        }

    }

    /**
     * this method do a dichotomic search on the mapping array _indexes
     * looking for index postion where to insert key
     */
    private int getInsertionIndex(int key)
    {
        int index = Arrays.binarySearch(mIndexes, 0, mItemCount, key);
        if (index >= 0)
        {
            return index;
        }
        else
        {
            return -(index + 1);
        }

/*
        if (mItemCount == 0)
        {
            return 0;
        }

        int start = 0;
        int end = mItemCount - 1;
        int middle;

        while (true)
        {
            middle = (end - start) / 2;

            if (middle == 0)
            {
                if (mIndexes[start] > key)
                {
                    return start;
                }
                else if (mIndexes[end] > key)
                {
                    return end;
                }
                else
                {
                    return mItemCount;
                }
            }
            else
            {
                middle += start;
                if (mIndexes[middle] < key)
                {
                    start = middle;
                }
                else if (mIndexes[middle] > key)
                {
                    end = middle;
                }
                else // mIndexes[middle] == key
                {
                    return middle;
                }
            }
        }
*/
    }

    /**
     * to remove an entry in the mapping this method left-shift the array
     * update mapping if absolute deletion and erase _indexes[position]
     */
    private void removeFromMapping(int position, boolean absolute)
    {
        for (int i = position; i < mItemCount - 1; i++)
        {
            if (absolute)
            {
                mIndexes[i] = mIndexes[i + 1] - 1;
            }
            else
            {
                mIndexes[i] = mIndexes[i + 1];
            }
        }
        mItemCount--;
    }

    /**
     * TODO: return insertion position
     * insert key a the last position of the mapping ie:_rowCount
     * if no  more space is available just double the capacity of _indexes
     */
    private void addToMapping(int key)
    {

        // there is no more space in the array
        ensureCapacity(mItemCount + 1);


        int insertPosition = getInsertionIndex(key);

        // shift table
        for (int i = mItemCount; i > insertPosition; i--)
        {
            mIndexes[i] = mIndexes[i - 1];
        }
        mIndexes[insertPosition] = key;

        mItemCount++;
    }

    private void ensureCapacity(int aMinCapacity)
    {
        if (aMinCapacity > mIndexes.length)
        {
            int newCapacity = (mIndexes.length * 3) / 2 + 1;
            if (aMinCapacity > newCapacity)
            {
                newCapacity = aMinCapacity;
            }
            int[] temp = new int[newCapacity];
            System.arraycopy(mIndexes, 0, temp, 0, mIndexes.length);
            mIndexes = temp;
        }
    }

    /**
     * this method is called when the value of the filtering property or
     * a property in source has changed, it checks for a row in source if it
     * matches or not match any more and it fires events to listeners.
     */
    private void resetMapping(int row, ListDataEvent event)
    {
        int viewRow = getMappingRow(row);
        if (viewRow > -1)
        {
            // no more matching remove from mapping and notify
            // listener if any of view DELETED_ROW
            if (!mFilter.accept(mListModel.getElementAt(row)))
            {
                removeFromMapping(viewRow, false);

                fireIntervalRemoved(this, viewRow, viewRow);
            }
            // notify listener if any and TableEvent that CHANGE_VALUE or
            // CHANGE_ROW
            else
            {
                if (event != null)
                {
                    switch (event.getType())
                    {
                        case ListDataEvent.CONTENTS_CHANGED:
                            fireContentsChanged(this, viewRow, viewRow);
                            break;
                        case ListDataEvent.INTERVAL_ADDED:
                            fireIntervalAdded(this, viewRow, viewRow);
                            break;
                        case ListDataEvent.INTERVAL_REMOVED:
                            fireIntervalRemoved(this, viewRow, viewRow);
                            break;
                    }
                }
            }

        }
        else
        {
            // now row matching add to mapping and notify
            // listener if any of view INSERT_ROW
            if (mFilter.accept(mListModel.getElementAt(row)))
            {
                addToMapping(row);

                int newIndex = getMappingRow(row);
                fireIntervalAdded(this, newIndex, newIndex);
            }

        }
    }

    /**
     *
     */
    private void resetMapping(int row)
    {
        resetMapping(row, null);
    }

    ///////////////////////////////////////////////////////////////////////////
    // ListModel interface methods

    public int getSize()
    {
        return mItemCount;
    }

    public Object getElementAt(int index)
    {
        assertValidIndex(index);
        return mListModel.getElementAt(mIndexes[index]);
    }

    ////////////////////////////////////////////////////////////////////////////
    // ListDataListener interface

    public void intervalAdded(ListDataEvent event)
    {
        int minIndex = Math.min(event.getIndex0(), event.getIndex1());
        int maxIndex = Math.max(event.getIndex0(), event.getIndex1());
        for (int i = minIndex; i <= maxIndex; i++)
        {
            int insertionIndex = getInsertionIndex(i);
            if (mItemCount > 0 && i < mIndexes[mItemCount - 1])
            {
                for (int j = insertionIndex; j < mItemCount; j++)
                {
                    mIndexes[j] = mIndexes[j] + 1;
                }
            }

            // if row is matching update mapping and notify listeners if any
            if (mFilter.accept(mListModel.getElementAt(i)))
            {
                addToMapping(i);
//                int viewRow = getMappingRow(i);
                int viewRow = insertionIndex;
                fireIntervalAdded(this, viewRow, viewRow);
            }
        }
    }

    public void intervalRemoved(ListDataEvent event)
    {
        int minIndex = Math.min(event.getIndex0(), event.getIndex1());
        int maxIndex = Math.max(event.getIndex0(), event.getIndex1());
        for (int i = minIndex; i <= maxIndex; i++)
        {
            // if row is on mapping remove it and notify listeners if any
            int viewRow = getMappingRow(i);
            if (viewRow > -1)
            {
                removeFromMapping(viewRow, true);
                fireIntervalRemoved(this, viewRow, viewRow);
            }

            // Update the mapping if i < max(mIndexes[])
            else if (mItemCount > 0 && i < mIndexes[mItemCount - 1])
            {
//                updateMapping(getInsertionIndex(i));
                for (int j = getInsertionIndex(i); j < mItemCount; j++)
                {
                    mIndexes[j] = mIndexes[j] - 1;
                }
            }

        }
    }

    public void contentsChanged(ListDataEvent event)
    {
        int minIndex = Math.min(event.getIndex0(), event.getIndex1());
        int maxIndex = Math.max(event.getIndex0(), event.getIndex1());
        for (int i = minIndex; i <= maxIndex; i++)
        {
            resetMapping(i, event);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // FilterEventListener interface

    public void filterChanged(FilterEvent aEvent)
    {
        computeViewMapping();
//        for (int i = 0; i < mSourceModel.getSize(); i++)
//        {
//            resetMapping(i);
//        }
    }

    ///////////////////////////////////////////////////////////////////////////
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
}
