package yamb.app.fileitem.filelist.thumbnail;

import yamb.app.fileitem.FileItem;
import yamb.app.fileitem.filelist.FileListItem;
import yamb.util.Disposable;
import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.swing.Icon;

/**
 * @author manuel.laflamme
 * @since Aug 5, 2008
 */
public class LocalThumbnailCache extends AbstractThumbnailCache implements Disposable, ThumbnailEventListener
{
//    private final Map<File, Icon> mFileCache = new HashMap<File, Icon>();
    private final Map<File, Icon> mFileCache = new LRUMap(1024)
    {
        protected boolean removeLRU(LinkEntry aEntry)
        {
            if (aEntry.getValue() instanceof Disposable)
            {
                ((Disposable) aEntry.getValue()).dispose();
            }
            return super.removeLRU(aEntry);
        }
    };
    private final GlobalThumbnailCache mGlobalCache;
    private String mCacheName = null;

    public LocalThumbnailCache(GlobalThumbnailCache aGlobalCache)
    {
        mGlobalCache = aGlobalCache;
        mGlobalCache.addThumbnailEventListener(this);
    }

    public String getCacheName()
    {
        return mCacheName;
    }

    public void setCacheName(String aCacheName)
    {
        mCacheName = aCacheName;
    }

    /**
     * Returns the specified file thumbnail. If the thumbnail is not found in the local cache, a query to the global
     * cache is done.
     */
    public Icon getThumbnail(FileListItem aFileItem)
    {
        Icon icon = mFileCache.get(aFileItem.getFile());
        if (icon == null)
        {
            icon = mGlobalCache.getThumbnail(aFileItem, this);
            mFileCache.put(aFileItem.getFile(), icon);
        }
        return icon;
    }

    public void activate()
    {
        mGlobalCache.setActiveLocalCache(this);
    }

    /**
     * Returns the specified file thumbnail if present in this local cache or null if not. This method does not
     * fallback to the global cache if the thumbnail is not in this local cache.
     */
    Icon getLocalThumbnail(FileListItem aFileItem)
    {
        return mFileCache.get(aFileItem.getFile());
    }

    /**
     * Add files to the thumbnail update queue
     */
    public void queueThumbnailUpdate(List<FileItem> aItems)
    {
/*        // Add to local cache missing files
        for (FileListItem item : aItems)
        {
            if (!mFileCache.containsKey(item.getFile()))
            {
                mFileCache.put(item.getFile(), null);
            }
        }*/

        mGlobalCache.queueThumbnailUpdate(aItems, this);
    }

    public void queueThumbnailUpdate(FileItem aItem)
    {
        queueThumbnailUpdate(Arrays.asList(aItem));
    }

    public void clear()
    {
        mGlobalCache.clear(this);

        for (Icon icon : mFileCache.values())
        {
            if (icon instanceof Disposable)
            {
                ((Disposable) icon).dispose();
            }
        }
        mFileCache.clear();
    }

    public void regenerate(FileItem aItem)
    {
        mGlobalCache.regenerate(aItem, this);
    }

    public int getThumbnailWidth()
    {
        return mGlobalCache.getThumbnailWidth();
    }

    public int getThumbnailHeight()
    {
        return mGlobalCache.getThumbnailHeight();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Disposable

    public void dispose()
    {
        mGlobalCache.removeThumbnailEventListener(this);
        clear();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // ThumbnailEventListener

    public void thumbnailUpdated(ThumbnailEvent aEvent)
    {
        if (mFileCache.containsKey(aEvent.getFile()))
        {
            mFileCache.put(aEvent.getFile(), aEvent.getThumbnail());
            fireThumbnailUpdated(aEvent.getFile(), aEvent.getThumbnail());
        }
    }

    public final boolean equals(Object aObject)
    {
        return super.equals(aObject);
    }

    public final int hashCode()
    {
        return super.hashCode();
    }


    public String toString()
    {
        ToStringBuilder builder = new ToStringBuilder(this);
        builder.appendSuper(super.toString());
//        builder.append("CacheName", mCacheName);
        builder.append("FileCount", mFileCache.size());
        return builder.toString();
    }
}
