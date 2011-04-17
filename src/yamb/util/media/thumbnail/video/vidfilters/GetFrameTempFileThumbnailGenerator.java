package yamb.util.media.thumbnail.video.vidfilters;

import yamb.util.media.VideoInfo;
import yamb.util.media.Videos;
import yamb.util.media.thumbnail.video.AbstractTempFileThumbnailGenerator;

import java.io.File;
import java.io.IOException;

/**
 * @author manuel.laflamme
 * @since Feb 15, 2008
 */
public class GetFrameTempFileThumbnailGenerator extends AbstractTempFileThumbnailGenerator
{
    public GetFrameTempFileThumbnailGenerator(File aTempDirectory) throws IOException
    {
        super(aTempDirectory);
    }

    public File generateThumbnailFile(File aInputVideoFile, int aPosition) throws IOException
    {
        // No temp file, extract frame
        File thumbnailTempFile = getThumbnailTempFile(aInputVideoFile);
//        if (!thumbnailTempFile.exists())
        {
            // Compute frame position in seconds
            VideoInfo videoInfo = Videos.createVideoInfo(aInputVideoFile);
            int position = -1;
            if (videoInfo != null)
            {
                float percent = ((float) aPosition / 100);
                position = (int) (((float) videoInfo.getPlaytime()) / 1000 * percent);
            }

            // Extract video frame to file system
            GetFrame.saveFrame(aInputVideoFile, thumbnailTempFile, position);
        }
        return thumbnailTempFile;
    }
}
