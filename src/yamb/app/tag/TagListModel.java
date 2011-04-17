package yamb.app.tag;

import yamb.util.Disposable;
import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.List;
import javax.swing.AbstractListModel;

/**
 * @author manuel.laflamme
 * @since 24-Feb-2008
 */
public class TagListModel extends AbstractListModel implements TagEventListener, Disposable
{
    private static final Logger LOGGER = Logger.getLogger(TagListModel.class);

    private final TagContext mTagContext;
    private final TagCategory mCategory;
    private final List<String> mTags;

    public TagListModel(TagContext aTagContext, TagCategory aCategory)
    {
        mTags = aTagContext.getTags(aCategory);
        aTagContext.addTagEventListener(this);
        Collections.sort(mTags);

        mTagContext = aTagContext;
        mCategory = aCategory;
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    // ListModel interface

    public int getSize()
    {
        return mTags.size();
    }

    public Object getElementAt(int aIndex)
    {
        return mTags.get(aIndex);
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    // Disposable interface

    public void dispose()
    {
        mTagContext.removeTagEventListener(this);
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    // TagEventListener interface

    public void tagAdded(TagEvent aEvent)
    {
        if (aEvent.getCategory() == mCategory)
        {
            List<String> tags = aEvent.getTags();
            for (String tag : tags)
            {
                int index = Collections.binarySearch(mTags, tag);
                // Not found, add it into list
                if (index < 0)
                {
                    mTags.add(-(index + 1), tag);
                    fireIntervalAdded(this, -(index + 1), -(index + 1));
                }
                // Found! This should not occurs!
                else
                {
                    LOGGER.warn("Tag '" + tag + "' already in list (" + mCategory + ")");
                }
            }
        }
        else if (aEvent.getCategory() == TagCategory.ACTIVE && mCategory != TagCategory.ACTIVE)
        {
            fireActiveTagsChange(aEvent);
        }
    }

    private void fireActiveTagsChange(TagEvent aEvent)
    {
        List<String> tags = aEvent.getTags();
        for (String tag : tags)
        {
            int index = Collections.binarySearch(mTags, tag);
            // Found, send update
            if (index >= 0)
            {
                fireContentsChanged(this, index, index);
            }
        }
    }

    public void tagRemoved(TagEvent aEvent)
    {
        if (aEvent.getCategory() == mCategory)
        {
            List<String> tags = aEvent.getTags();
            for (String tag : tags)
            {
                int index = Collections.binarySearch(mTags, tag);
                // Found, remove it from list
                if (index >= 0)
                {
                    mTags.remove(index);
                    fireIntervalRemoved(this, index, index);
                }
                // Not found! This should not occurs!
                else
                {
                    LOGGER.warn("Tag '" + tag + "' not in list (" + mCategory + ")");
                }
            }
        }
        else if (aEvent.getCategory() == TagCategory.ACTIVE && mCategory != TagCategory.ACTIVE)
        {
            fireActiveTagsChange(aEvent);
        }
    }
}
