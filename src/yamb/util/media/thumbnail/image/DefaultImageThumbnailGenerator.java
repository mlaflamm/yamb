package yamb.util.media.thumbnail.image;

import yamb.util.media.Images;

import java.awt.Image;
import java.io.File;
import java.io.IOException;

/**
 * @author manuel.laflamme
 * @since 17-Aug-2008
 */
public class DefaultImageThumbnailGenerator implements ImageThumbnailGenerator
{
    public Image generateThumbnailImage(File aInputFile) throws IOException
    {
        return Images.read(aInputFile.getAbsolutePath());
    }
}
