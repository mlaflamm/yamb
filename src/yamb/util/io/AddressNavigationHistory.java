package yamb.util.io;

import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

/**
 * @author manuel.laflamme
 * @since Aug 12, 2008
 */
public class AddressNavigationHistory
{
    private final ArrayList<String> mHistoryList = new ArrayList<String>();
    private int mCurrentIndex = -1;

    public String back()
    {
        int backCount = getBackCount();
        if (backCount <= 0)
        {
            throw new IndexOutOfBoundsException(String.valueOf(backCount));
        }

        String address = mHistoryList.get(--mCurrentIndex);
        return address;
    }

    public String forward()
    {
        int forwardCount = getForwardCount();
        if (forwardCount <= 0)
        {
            throw new IndexOutOfBoundsException(String.valueOf(forwardCount));
        }

        String address = mHistoryList.get(++mCurrentIndex);
        return address;
    }

    public String getCurrent()
    {
        // Prevent
        if (mCurrentIndex >= 0)
        {
            return mHistoryList.get(mCurrentIndex);
        }

        return null;
    }

    public void add(String aAddress)
    {
        setSize(++mCurrentIndex);
        mHistoryList.add(aAddress);
    }

    public int removeAll(String aAddress)
    {
        int count = 0;
        for (ListIterator<String> it = mHistoryList.listIterator(); it.hasNext();)
        {
            int index = it.nextIndex();
            String address = it.next();
            if (address.equals(aAddress))
            {
                it.remove();
                if (index <= mCurrentIndex)
                {
                    mCurrentIndex--;
                }
                count++;
            }
        }
        return count;
    }

    public int replaceAll(String aOldAddress, String aNewAddress)
    {
        // replace all occurence of specified address
        int count = 0;
        for (int i = 0; i < mHistoryList.size(); i++)
        {
            String address = mHistoryList.get(i);
            if (address.equals(aOldAddress))
            {
                mHistoryList.set(i, aNewAddress);
                count++;
            }
        }
        return count;
    }

    private void setSize(int aSize)
    {
        for (int i = mHistoryList.size()-1; i >= aSize; i--)
        {
            mHistoryList.remove(i);
        }
    }

    public List<String> getBackList()
    {
        ArrayList<String> backList = new ArrayList<String>(mHistoryList.subList(0, getBackCount()));
        Collections.reverse(backList);
        return backList;
    }

    public List<String> getForwardList()
    {
        return new ArrayList<String>(mHistoryList.subList(mCurrentIndex + 1, mHistoryList.size()));
    }

    public int getBackCount()
    {
        return mCurrentIndex <= 0 ? 0 : mCurrentIndex;
    }

    public int getForwardCount()
    {
        return (mHistoryList.size() - 1) - mCurrentIndex;
    }


    public String toString()
    {
        ToStringBuilder builder = new ToStringBuilder(this);
        builder.append("current", getCurrent());
        builder.append("back", getBackList());
        builder.append("forward", getForwardList());
        builder.append("mHistoryList", mHistoryList);
        builder.append("mCurrentIndex", mCurrentIndex);
        return builder.toString();
    }
}
