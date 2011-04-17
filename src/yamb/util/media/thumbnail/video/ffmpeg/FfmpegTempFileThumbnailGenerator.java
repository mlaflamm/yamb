package yamb.util.media.thumbnail.video.ffmpeg;

import yamb.util.media.VideoInfo;
import yamb.util.media.Videos;
import yamb.util.media.thumbnail.video.AbstractTempFileThumbnailGenerator;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author manuel.laflamme
 * @since Feb 20, 2008
 */
public class FfmpegTempFileThumbnailGenerator extends AbstractTempFileThumbnailGenerator
{
    private static final Logger LOGGER = Logger.getLogger(FfmpegTempFileThumbnailGenerator.class);

    public FfmpegTempFileThumbnailGenerator(File aTempDirectory) throws IOException
    {
        super(aTempDirectory);
    }

    public File generateThumbnailFile(File aInputVideoFile, int aPosition) throws IOException
    {
        // No temp file, extract frame
        File thumbnailTempFile = getThumbnailTempFile(aInputVideoFile);
        // Compute frame position in seconds
        VideoInfo videoInfo = Videos.createVideoInfo(aInputVideoFile);
        int position = 30 * 1000; // default position is 30 seconds is no playtime information is available
        if (videoInfo != null && videoInfo.getPlaytime() != null)
        {
            Long playtime = videoInfo.getPlaytime();
            position = (int) (playtime / 1000 * (aPosition / 100.0f));
        }
        else
        {
            LOGGER.error("No playtime information " + aInputVideoFile.getAbsolutePath() + ". " + videoInfo);
        }

        // Extract video frame to file system
        executeFfmpeg(aInputVideoFile, thumbnailTempFile, position);
        return thumbnailTempFile;
    }

    private void executeFfmpeg(File aInputMediaFile, File aOutputImageFile, int aSeekSeconds) throws IOException
    {
        String path = System.getProperty("ffmpeg.path", "");

        String command = "\"" + new File(path, "ffmpeg.exe").getCanonicalPath() +
                "\" -i \"" + aInputMediaFile.getAbsolutePath() + "\" -an -ss " + aSeekSeconds +
                " -r 1 -vframes 1 -f mjpeg -y \"" + aOutputImageFile.getAbsolutePath() + "\"";

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();

        final InputStream inputStream = process.getInputStream();
        while (inputStream.read() != -1)
        {
        }
    }
}
