package yamb.util.media.mediainfo.cache;

import yamb.util.media.VideoInfo;

import java.io.File;
import java.util.EventObject;

/**
 * @author manuel.laflamme
 * @since Apr 14, 2008
 */
public class VideoInfoEvent extends EventObject
{
    private final File mMediaFile;
    private final VideoInfo mMediaInfo;

    public VideoInfoEvent(Object aSource, File aMediaFile, VideoInfo aMediaInfo)
    {
        super(aSource);
        mMediaFile = aMediaFile;
        mMediaInfo = aMediaInfo;
    }

    public File getMediaFile()
    {
        return mMediaFile;
    }

    public VideoInfo getMediaInfo()
    {
        return mMediaInfo;
    }


    public String toString()
    {
        return "VideoInfoEvent{" +
                "mMediaFile=" + mMediaFile +
                ", mMediaInfo=" + mMediaInfo +
                '}';
    }
}
