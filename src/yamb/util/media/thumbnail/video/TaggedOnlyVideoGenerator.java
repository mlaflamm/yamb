package yamb.util.media.thumbnail.video;

import java.awt.Image;
import java.io.File;
import java.io.IOException;

/**
 * @author manuel.laflamme
 * @since 12-Sep-2008
 */
public class TaggedOnlyVideoGenerator extends FallbackVideoThumbnailGenerator
{
    public TaggedOnlyVideoGenerator(VideoThumbnailGenerator... aGenerators)
    {
        super(aGenerators);
    }

    public Image generateThumbnailImage(File aInputVideoFile, int aPosition) throws IOException
    {
        // todo: remove hack to exclude untaged video file
        if (aInputVideoFile.getName().indexOf("_") < 0)
        {
            return null;
        }

        return super.generateThumbnailImage(aInputVideoFile, aPosition);
    }
}
