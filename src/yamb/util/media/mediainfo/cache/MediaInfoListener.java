package yamb.util.media.mediainfo.cache;

import java.util.EventListener;

/**
 * @author manuel.laflamme
 * @since Apr 14, 2008
 */
public interface MediaInfoListener extends EventListener
{
    public void mediaInfoUpdated(MediaInfoEvent aEvent);
}
