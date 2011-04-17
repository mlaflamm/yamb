package yamb.util.media.thumbnail.video;

import yamb.util.media.Images;
import org.apache.commons.io.FileUtils;

import java.awt.Image;
import java.io.File;
import java.io.IOException;

/**
 * @author manuel.laflamme
 * @since 24-Feb-2008
 */
public abstract class AbstractTempFileThumbnailGenerator extends AbstractVideoThumbnailGenerator implements VideoThumbnailGenerator
{
    private final File mTempDirectory;

    public AbstractTempFileThumbnailGenerator(File aTempDirectory) throws IOException
    {
        mTempDirectory = aTempDirectory;
        FileUtils.forceMkdir(mTempDirectory);
    }

    protected File getThumbnailTempFile(File aFile)
    {
        File tempFile = new File(mTempDirectory, aFile.getName() + ".jpg");
        tempFile.delete();
        return tempFile;
    }

    /**
     * Generate a thumbnail jpeg file of the specified video file. The thumbnail size should be the same than the
     * original video and does not need to be resized.
     *
     * @param aInputVideoFile The source video file
     * @param aPosition       The approximate position in the video of the thumbnail image. This a value between 0 to 100
     *                        representing a percentage of the total playtime.
     */
    public abstract File generateThumbnailFile(File aInputVideoFile, int aPosition) throws IOException;

    public Image generateThumbnailImage(File aInputVideoFile, int aPosition) throws IOException
    {
        File tempFile = generateThumbnailFile(aInputVideoFile, aPosition);
        if (tempFile != null && tempFile.exists() && tempFile.length() > 0)
        {
            return Images.read(tempFile.getAbsolutePath());
        }
        return null;
    }
}
