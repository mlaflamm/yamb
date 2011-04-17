package yamb.util.media.thumbnail.video;

import yamb.util.media.thumbnail.ThumbnailGenerator;

import java.awt.Image;
import java.io.File;
import java.io.IOException;

/**
 * @author manuel.laflamme
 * @since Feb 15, 2008
 */
public interface VideoThumbnailGenerator extends ThumbnailGenerator
{
    /**
     * Generate a thumbnail image of the specified video file.
     *
     * @param aInputVideoFile The source video file
     * @param aPosition       The approximate position in the video of the thumbnail image. This a value between 0 to 100
     *                        representing a percentage of the total playtime.
     */
    public Image generateThumbnailImage(File aInputVideoFile, int aPosition) throws IOException;
}
