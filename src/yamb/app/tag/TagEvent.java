package yamb.app.tag;

import java.util.EventObject;
import java.util.List;

/**
 * @author manuel.laflamme
 * @since Feb 4, 2008
 */
public class TagEvent extends EventObject
{
    private final TagCategory mCategory;
    private final List<String> mTags;

    public TagEvent(Object aSource, TagCategory aCategory, List<String> aTags)
    {
        super(aSource);
        mCategory = aCategory;
        mTags = aTags;
    }

    public List<String> getTags()
    {
        return mTags;
    }

    public TagCategory getCategory()
    {
        return mCategory;
    }


    public String toString()
    {
        return "TagEvent{" +
                "mCategory=" + mCategory +
                ", mTags=" + mTags +
                '}';
    }
}
