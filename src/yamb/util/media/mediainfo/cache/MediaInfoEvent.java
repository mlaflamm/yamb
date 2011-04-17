package yamb.util.media.mediainfo.cache;

import yamb.util.media.mediainfo.MediaInfo;

import java.io.File;
import java.util.EventObject;

/**
 * @author manuel.laflamme
 * @since Apr 14, 2008
 */
public class MediaInfoEvent extends EventObject
{
    private final File mMediaFile;
    private final MediaInfo mMediaInfo;

    public MediaInfoEvent(Object aSource, File aMediaFile, MediaInfo aMediaInfo)
    {
        super(aSource);
        mMediaFile = aMediaFile;
        mMediaInfo = aMediaInfo;
    }

    public File getMediaFile()
    {
        return mMediaFile;
    }

    public MediaInfo getMediaInfo()
    {
        return mMediaInfo;
    }


    public String toString()
    {
        return "MediaInfoEvent{" +
                "mMediaFile=" + mMediaFile +
                ", mMediaInfo=" + mMediaInfo +
                '}';
    }
}
