package yamb.app.tag;

import yamb.util.event.SwingSafeEventSupport;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author manuel.laflamme
 * @since Aug 6, 2008
 */
public class DefaultTagContext implements TagContext
{
    private final TagManager mTagManager;

    private final SwingSafeEventSupport mTagEventSupport = new SwingSafeEventSupport();
    private final SortedSet<String> mActiveTags = new TreeSet<String>();

    public DefaultTagContext(TagManager aTagManager)
    {
        mTagManager = aTagManager;
    }

    public List<String> getTags(TagCategory aCategory)
    {
        switch (aCategory)
        {
            case ACTIVE:
                return getActiveTags();
            case FAVORITE:
                return mTagManager.getFavoriteTags();
            case GLOBAL:
                return mTagManager.getTags();
            case RECENT:
                return mTagManager.getRecentsTags();
            default:
                throw new IllegalStateException("Unsupported tag category: " + aCategory);
        }
    }

    public int getTagsCount(TagCategory aCategory)
    {
        switch (aCategory)
        {
            case ACTIVE:
                return getActiveTagsCount();
            case FAVORITE:
                return mTagManager.getFavoriteTagsCount();
            case GLOBAL:
                return mTagManager.getTagsCount();
            case RECENT:
                return mTagManager.getRecentTagsCount();
            default:
                throw new IllegalStateException("Unsupported tag category: " + aCategory);
        }
    }

    public boolean isCategory(String aTagName, TagCategory aCategory)
    {
        switch (aCategory)
        {
            case ACTIVE:
                return isActiveTag(aTagName);
            case FAVORITE:
                return mTagManager.isFavoriteTag(aTagName);
            case GLOBAL:
                // todo: validate if really a tag...
                return true;
            case RECENT:
                return mTagManager.isRecentTag(aTagName);
            default:
                throw new IllegalStateException("Unsupported tag category: " + aCategory);
        }
    }

    public void addTagEventListener(TagEventListener aListener)
    {
        mTagEventSupport.addEventListener(aListener);
        mTagManager.addTagEventListener(aListener);
    }

    public void removeTagEventListener(TagEventListener aListener)
    {
        mTagEventSupport.removeEventListener(aListener);
        mTagManager.removeTagEventListener(aListener);
    }

    public List<String> getFileTags(File aFile)
    {
        return mTagManager.getFileTags(aFile);
    }

    public List<File> getFiles(String aTag)
    {
        return mTagManager.getFiles(aTag);
    }

    public List<File> getFiles(List<String> aTags, TagSetMode aSetMode)
    {
        return mTagManager.getFiles(aTags, aSetMode);
    }

    // Active tags management
    List<String> getActiveTags()
    {
        synchronized (mActiveTags)
        {
            return new ArrayList<String>(mActiveTags);
        }
    }

    int getActiveTagsCount()
    {
        synchronized (mActiveTags)
        {
            return mActiveTags.size();
        }
    }

    boolean isActiveTag(String aTagName)
    {
        synchronized (mActiveTags)
        {
            return mActiveTags.contains(aTagName);
        }
    }

    public void addActiveTags(List<String> aTagNames)
    {
        boolean added;
        synchronized (mActiveTags)
        {
            added = mActiveTags.addAll(aTagNames);
        }

        if (added)
        {
            fireTagAdded(TagCategory.ACTIVE, new ArrayList<String>(aTagNames));
        }
    }

    public void removeActiveTags(List<String> aTagNames)
    {
        boolean removed;
        synchronized (mActiveTags)
        {
            removed = mActiveTags.removeAll(aTagNames);
        }

        if (removed)
        {
            fireTagRemoved(TagCategory.ACTIVE, aTagNames);
        }
    }

    protected void fireTagAdded(TagCategory aCategory, List<String> aNewTags)
    {
        mTagEventSupport.fireEvent("tagAdded", new TagEvent(this, aCategory, aNewTags));
    }

    protected void fireTagRemoved(TagCategory aCategory, List<String> aNewTags)
    {
        mTagEventSupport.fireEvent("tagRemoved", new TagEvent(this, aCategory, aNewTags));
    }

}
