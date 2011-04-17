package yamb.util.media.thumbnail.video;

import java.awt.Image;
import java.io.File;
import java.io.IOException;

/**
 * @author manuel.laflamme
 * @since 17-Aug-2008
 */
public abstract class AbstractVideoThumbnailGenerator implements VideoThumbnailGenerator
{
    public Image generateThumbnailImage(File aInputFile) throws IOException
    {
        return generateThumbnailImage(aInputFile, 20);
    }
}
