package yamb.util.media.thumbnail.video.jmf;

import yamb.util.media.thumbnail.video.AbstractVideoThumbnailGenerator;
import yamb.util.media.thumbnail.video.VideoThumbnailGenerator;

import java.awt.Image;
import java.io.File;
import java.io.IOException;

/**
 * @author manuel.laflamme
 * @since Feb 16, 2008
 */
public class JmfVideoThumbnailGenerator extends AbstractVideoThumbnailGenerator implements VideoThumbnailGenerator
{
    public Image generateThumbnailImage(File aInputVideoFile, int aPosition) throws IOException
    {
        JmfFrameGrabber snapper = null;
        Image frame;
        try
        {
            snapper = new JmfFrameGrabber(aInputVideoFile, aPosition);
            frame = snapper.getImage();
        }
        finally
        {
            if (snapper != null)
            {
                snapper.dispose();
            }
        }
        return frame;
    }
}
