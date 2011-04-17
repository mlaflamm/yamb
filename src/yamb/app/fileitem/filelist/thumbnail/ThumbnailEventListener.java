package yamb.app.fileitem.filelist.thumbnail;

import java.util.EventListener;

/**
 * @author manuel.laflamme
 * @since 1-Feb-2008
 */
public interface ThumbnailEventListener extends EventListener
{
    public void thumbnailUpdated(ThumbnailEvent aEvent);
}
