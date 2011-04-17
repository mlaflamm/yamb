package yamb.util.media.thumbnail.video.vidfilters;

import yamb.util.media.VideoInfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @author manuel.laflamme
 * @since 31-Jan-2008
 */
public class GetFrame implements VideoInfo
{
    private Long mPlaytime;
    private Integer mWidth;
    private Integer mHeight;

    private GetFrame()
    {
    }

    public static VideoInfo createVideoInfo(File aMediaFile)
    {
        String resultLine = executeGetFrame(aMediaFile, null, -1);
        if (resultLine != null)
        {
            GetFrame getFrame = new GetFrame();
            String[] info = resultLine.split(" ");
            getFrame.mWidth = Integer.valueOf(info[0]);
            getFrame.mHeight = Integer.valueOf(info[1]);

            BigInteger length = new BigDecimal(info[2]).multiply(new BigDecimal(1000)).toBigInteger();
            getFrame.mPlaytime = Long.valueOf(length.toString());
            return getFrame;
        }

        return null;
    }

    /**
     * Save a jpeg image from the specified video file at the specified seconds
     *
     * @param aInputVideoFile  the video file
     * @param aOutputImageFile the target image file
     * @param aPosition        the video position in seconds.
     */
    public static void saveFrame(File aInputVideoFile, File aOutputImageFile, int aPosition)
    {
        executeGetFrame(aInputVideoFile, aOutputImageFile, aPosition);
    }

    /**
     * @param aInputMediaFile
     * @param aOutputImageFile
     * @param aPosition        position in seconds of the frame to extract. -1 = middle.
     */
    private static String executeGetFrame(File aInputMediaFile, File aOutputImageFile, int aPosition)
    {
        try
        {
            String path = System.getProperty("getframe.path", "");

            String command = new File(path, "GetFrame.exe").getCanonicalPath() +
                    " -Input \"" + aInputMediaFile.getAbsolutePath() + "\" -Width -Height -Length";
//            String command = "GetFrame.exe -Input \"" + aInputMediaFile.getAbsolutePath() + "\" -Width -Height -Length";
            if (aOutputImageFile != null)
            {
                command += " -Output \"" + aOutputImageFile.getAbsolutePath() + "\"";
            }
            if (aPosition >= 0)
            {
                command += " -Time " + aPosition;

            }
            Process process = Runtime.getRuntime().exec(command);
            final InputStream inputStream = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            String line = null;
            String firstLine = null;
            while ((line = reader.readLine()) != null)
            {
                if (firstLine == null)
                {
                    firstLine = line;
                }
            }

            return firstLine;
        }
        catch (IOException e)
        {
            IllegalStateException rte = new IllegalStateException(e.toString());
            rte.initCause(e);
            throw rte;
        }

    }

    public Long getPlaytime()
    {
        return mPlaytime;
    }

    public Integer getWidth()
    {
        return mWidth;
    }

    public Integer getHeight()
    {
        return mHeight;
    }


}
