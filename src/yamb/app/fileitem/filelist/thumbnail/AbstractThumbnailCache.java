package yamb.app.fileitem.filelist.thumbnail;

import yamb.util.event.SwingSafeEventSupport;

import java.io.File;
import javax.swing.Icon;

/**
 * @author manuel.laflamme
 * @since Aug 5, 2008
 */
public class AbstractThumbnailCache
{
    private final SwingSafeEventSupport mEventSupport = new SwingSafeEventSupport();

    protected void fireThumbnailUpdated(File aMediaFile, Icon aThumbnail)
    {
        synchronized (mEventSupport)
        {
            mEventSupport.fireEvent("thumbnailUpdated", new ThumbnailEvent(this, aMediaFile, aThumbnail));
        }
    }

    public void addThumbnailEventListener(ThumbnailEventListener aListener)
    {
        synchronized (mEventSupport)
        {
            mEventSupport.addEventListener(aListener);
        }
    }

    public void removeThumbnailEventListener(ThumbnailEventListener aListener)
    {
        synchronized (mEventSupport)
        {
            mEventSupport.removeEventListener(aListener);
        }
    }
}
