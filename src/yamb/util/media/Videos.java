package yamb.util.media;

import yamb.app.tag.Tags;
import yamb.util.media.mediainfo.MediaInfo;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author manuel.laflamme
 * @since 1-Feb-2008
 */
public class Videos
{
    private static final List<String> VIDEO_EXTENSIONS = Collections.unmodifiableList(new ArrayList<String>(
            Arrays.asList(new String[]{
                    "avi",
                    "wmv",
                    "wma",
                    "asf",
                    "mpeg",
                    "mpg",
                    "vob",
                    "ogg",
                    "ogm",
                    "mov",
                    "quicktime",
                    "mkv",
                    "mks",
                    "xvid",
                    "divx",
                    "mp4",
                    "m4v",
                    "rm",
                    "flv",
            })));

    public static boolean isVideoFile(File aFile)
    {
        return VIDEO_EXTENSIONS.contains(FilenameUtils.getExtension(aFile.getName()));
    }

    public static List<String> getVideoExtensions()
    {
        return VIDEO_EXTENSIONS;
    }

    public static VideoInfo createVideoInfo(File aVideoFile)
    {
        if (aVideoFile.isFile() && isVideoFile(aVideoFile))
        {
            return MediaInfo.createVideoInfo(aVideoFile);
        }
        return null;
    }

    public static String getDisplayPlaytime(long aPlaytime)
    {
        StringBuffer buffer = new StringBuffer();
        long milli = aPlaytime % 1000;
        long seconds = (aPlaytime / (1000)) % 60;
        long minutes = (aPlaytime / (60 * 1000)) % 60;
        long hours = (aPlaytime / (3600 * 1000));

        // 02h35m12s,056
        if (hours < 10)
        {
            buffer.append("0");
        }
        buffer.append(hours);
        buffer.append("h");

        if (minutes < 10)
        {
            buffer.append("0");
        }
        buffer.append(minutes);
        buffer.append("m");

        if (seconds < 10)
        {
            buffer.append("0");
        }
        buffer.append(seconds);
        buffer.append("s,");

        if (milli < 10)
        {
            buffer.append("00");
        }
        if (10 <= milli && milli < 100)
        {
            buffer.append("0");
        }
        buffer.append(milli);
        return buffer.toString();
    }

    // todo : make this more efficient!
    public static String getVideoDetailsHtml(File aFile, MediaInfo aMediaInfo)
    {
        String text = getVideoDetailsText(aFile, aMediaInfo);
        return "<html>" + text.replaceAll("\\r\\n", "<br>") + "<html/>";
    }

    // todo : remove dependency to Tags utility class!
    public static String getVideoDetailsText(File aFile, MediaInfo aMediaInfo)
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(out), true);
        writer.println(aFile.getName());
        writer.println(aFile.getParent());
        if (aMediaInfo != null)
        {
            writer.println(aMediaInfo.getWidth() + "x" + aMediaInfo.getHeight());
            Long playtime = aMediaInfo.getPlaytime();
            if (playtime != null)
            {
                writer.println(getDisplayPlaytime(playtime));
            }
            writer.println(aMediaInfo.getVideoCodec());
        }
        writer.println(FileUtils.byteCountToDisplaySize(aFile.length()));
        writer.print(new Timestamp(aFile.lastModified()));
//        writer.println(aMediaInfo.getAudioCodec());

        String[] tags = Tags.getTagsFromFileName(aFile.getName());
        if (tags.length > 0)
        {
            writer.println();
            for (String tag : tags)
            {
                writer.println();
                writer.print(tag);
            }
        }
        writer.flush();
        return out.toString();
    }

}
