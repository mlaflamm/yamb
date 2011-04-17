package yamb.app.tag;

import yamb.util.event.SwingSafeEventSupport;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author manuel.laflamme
 * @since Apr 11, 2008
 */
public abstract class AbstractTagManager implements TagManager
{
    private final SwingSafeEventSupport mTagEventSupport = new SwingSafeEventSupport();
    private final SwingSafeEventSupport mTaggedFileEventSupport = new SwingSafeEventSupport();

    private final SortedSet<String> mFavoriteTags = new TreeSet<String>();

    public List<String> getFileTags(File aFile)
    {
        String[] tags = Tags.getTagsFromFileName(aFile.getName());
        return Arrays.asList(tags);
    }

    public List<String> getFavoriteTags()
    {
        synchronized (mFavoriteTags)
        {
            return new ArrayList<String>(mFavoriteTags);
        }
    }

    public int getFavoriteTagsCount()
    {
        synchronized (mFavoriteTags)
        {
            return mFavoriteTags.size();
        }
    }

    public void addFavoriteTags(List<String> aFavoriteTags)
    {
        synchronized (mFavoriteTags)
        {
            mFavoriteTags.addAll(aFavoriteTags);
        }

        fireTagAdded(TagCategory.FAVORITE, aFavoriteTags);
    }

    public boolean isFavoriteTag(String aTag)
    {
        synchronized (mFavoriteTags)
        {
            return mFavoriteTags.contains(aTag);
        }
    }

    public void addTagEventListener(TagEventListener aListener)
    {
        mTagEventSupport.addEventListener(aListener);
    }

    public void removeTagEventListener(TagEventListener aListener)
    {
        mTagEventSupport.removeEventListener(aListener);
    }

    protected void fireTagAdded(TagCategory aCategory, List<String> aNewTags)
    {
        mTagEventSupport.fireEvent("tagAdded", new TagEvent(this, aCategory, aNewTags));
    }

    protected void fireTagRemoved(TagCategory aCategory, List<String> aNewTags)
    {
        mTagEventSupport.fireEvent("tagRemoved", new TagEvent(this, aCategory, aNewTags));
    }


    public void addTaggedFileEventListener(TaggedFileEventListener aListener)
    {
        mTaggedFileEventSupport.addEventListener(aListener);
    }

    public void removeTaggedFileEventListener(TaggedFileEventListener aListener)
    {
        mTaggedFileEventSupport.removeEventListener(aListener);
    }

    protected void fireTaggedFileAdded(List<File> aFiles)
    {
        mTaggedFileEventSupport.fireEvent("taggedFileAdded", new TaggedFileEvent(this, aFiles));
    }

    protected void fireTaggedFileRemoved(List<File> aFiles)
    {
        mTaggedFileEventSupport.fireEvent("taggedFileRemoved", new TaggedFileEvent(this, aFiles));
    }
}
