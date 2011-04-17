package yamb.util.media;

import yamb.util.media.mediainfo.MediaInfoReader;
import yamb.util.media.mediainfo.MediaInfoRecord;
import junit.framework.TestCase;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author manuel.laflamme
 * @since Feb 16, 2006
 */
public class TestMediaInfoReader extends TestCase
{
/*
    public void testExec() throws Exception
    {
        Process process = Runtime.getRuntime().exec("..\\_3rdparties\\mediainfo-0.7.1.0\\MediaInfo_0.7.7.6_CLI_Win32\\MediaInfo.exe -f \"C:\\text.mp4\"");
        final InputStream inputStream = process.getInputStream();
//        final InputStream inputStream = new FileInputStream("C:\\projects\\vlibgrab\\data\\mediainfo.out");
//        final InputStream inputStream = new FileInputStream("N:\\temp.out");
        final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        String line = null;
        while ((line = reader.readLine()) != null)
        {
            System.out.println(line);
        }

        MediaInfoReader mediaInfoReader = new MediaInfoReader(reader);
        MediaInfoRecord record = null;
        while ((record = mediaInfoReader.nextRecord()) != null)
        {
            System.out.println(record);
        }
    }
*/

    public void testReadOutput() throws Exception
    {
        String content =
                "General #0\n" +
                        "Count                : 167\n" +
                        "StreamCount          : 1\n" +
                        "StreamKind           : General\n" +
                        "StreamKindID         : 0\n" +
                        "VideoCount           : 1\n" +
                        "AudioCount           : 1\n" +
                        "TextCount            : 0\n" +
                        "ChaptersCount        : 0\n" +
                        "CompleteName         : Z:\\newsleecher\\2006-02-13 newsleecher\\msgid_1737517_Test\\Test Video.avi\n" +
                        "FolderName           : Z:\\newsleecher\\2006-02-13 newsleecher\\msgid_1737517_Test\n" +
                        "FileName             : Test Video\n" +
                        "FileExtension        : avi\n" +
                        "FileSize             : 1177313280\n" +
                        "FileSize/String      : 1.10 GiB\n" +
                        "FileSize/String1     : 1 GiB\n" +
                        "FileSize/String2     : 1.1 GiB\n" +
                        "FileSize/String3     : 1.10 GiB\n" +
                        "FileSize/String4     : 1.096 GiB\n" +
                        "Format               : AVI\n" +
                        "Format/String        : Audio Video Interleave\n" +
                        "Format/Extensions    : AVI\n" +
                        "OveralBitRate        : 1341580\n" +
                        "OveralBitRate/String : 1342 Kbps\n" +
                        "PlayTime             : 7020454\n" +
                        "PlayTime/String      : 1h 57mn\n" +
                        "PlayTime/String1     : 1h 57mn 454ms\n" +
                        "PlayTime/String2     : 1h 57mn\n" +
                        "PlayTime/String3     : 01:57:00.454\n" +
                        "Video_Codec_List     : XviD\n" +
                        "Audio_Codec_List     : MPEG1/2 L3\n" +
                        "\n" +
                        "Video #0\n" +
                        "Count                : 35\n" +
                        "StreamCount          : 1\n" +
                        "StreamKind           : Video\n" +
                        "StreamKindID         : 0\n" +
                        "Codec                : XVID\n" +
                        "Codec/String         : XviD\n" +
                        "Codec/Info           : XviD project\n" +
                        "Codec/Url            : http://www.koepi.org/xvid.shtml\n" +
                        "BitRate              : 1136580\n" +
                        "BitRate/String       : 1137 Kbps\n" +
                        "Width                : 640\n" +
                        "Height               : 480\n" +
                        "AspectRatio          : 1.333\n" +
                        "AspectRatio/String   : 4/3\n" +
                        "FrameRate            : 29.970\n" +
                        "FrameRate/String     : 29.97 fps\n" +
                        "FrameCount           : 210403\n" +
                        "Bits/(Pixel*Frame)   : 0.123\n" +
                        "\n" +
                        "Audio #0\n" +
                        "Count                : 36\n" +
                        "StreamCount          : 1\n" +
                        "StreamKind           : Audio\n" +
                        "StreamKindID         : 0\n" +
                        "Codec                : 55\n" +
                        "Codec/String         : MPEG1/2 L3\n" +
                        "Codec/Info           : MPEG1 or 2 Audio Layer 3\n" +
                        "Codec/Url            : http://www.iis.fraunhofer.de/amm/index.html\n" +
                        "BitRate              : 192000\n" +
                        "BitRate/String       : 192 Kbps\n" +
                        "Channel(s)           : 2\n" +
                        "Channel(s)/String    : 2 channels\n" +
                        "SamplingRate         : 44100\n" +
                        "SamplingRate/String  : 44 KHz\n" +
                        "\n" +
                        "using MediaInfoLib - v0.7.1.0";

        ByteArrayInputStream in = new ByteArrayInputStream(content.getBytes());
        InputStreamReader reader = new InputStreamReader(in);

        MediaInfoReader mediaInfoReader = new MediaInfoReader(reader);
        MediaInfoRecord record = null;
        while ((record = mediaInfoReader.nextRecord()) != null)
        {
            System.out.println(record);
        }
    }
}
