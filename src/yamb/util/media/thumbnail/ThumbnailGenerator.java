package yamb.util.media.thumbnail;

import java.awt.Image;
import java.io.File;
import java.io.IOException;

/**
 * @author manuel.laflamme
 * @since Feb 15, 2008
 */
public interface ThumbnailGenerator
{
    public Image generateThumbnailImage(File aInputFile) throws IOException;
}
