package yamb.util.media.mediainfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Manuel Laflamme
 * @since Feb 16, 2006
 * @version $Revision: 1.1 $
 */
public class MediaInfoReader
{
//    MediaInfoRecord mGeneralRecord;
//    MediaInfoRecord mVideoRecord;
//    MediaInfoRecord mAudioRecord;

    private BufferedReader mReader;
    private String mLastRecordName = null;

    public MediaInfoReader(Reader aReader)
    {
        if (!(aReader instanceof BufferedReader))
        {
            aReader = new BufferedReader(aReader);
        }

        mReader = (BufferedReader)aReader;
    }

    public void close() throws IOException
    {
        mReader.close();
    }

    /**
     *
     * @return
     * @throws IOException
     */
    public MediaInfoRecord nextRecord() throws IOException
    {
//        String recordName = null;
        Map<String,String> fields = new HashMap<String,String>();

        String line;
        while ((line = mReader.readLine()) != null)
        {
//            System.out.println(line);
            String[] field = line.split(":", 2);

            switch(field.length)
            {
                // Not a field
                case 1:
                    // Start of a new record
                    if (line.trim().length() > 0 &&
                            line.toLowerCase().indexOf("mediainfolib") == -1)
                    {
                        if (mLastRecordName != null)
                        {
                            MediaInfoRecord record = new MediaInfoRecord(mLastRecordName,
                                    RecordType.forString(mLastRecordName), fields);
                            mLastRecordName = line;
                            return record;
                        }
                        mLastRecordName = line;
                    }
                    break;
                case 2:
                    fields.put(field[0].trim(), field[1].trim());
                    break;
                default:
                    throw new IOException(line);
            }
        }

        MediaInfoRecord record = null;
        if (mLastRecordName != null)
        {
            record = new MediaInfoRecord(mLastRecordName, RecordType.forString(mLastRecordName), fields);
            mLastRecordName = null;
        }
        return record;
    }

/*
General #0
Count                : 167
StreamCount          : 1
StreamKind           : General
StreamKindID         : 0
VideoCount           : 1
AudioCount           : 1
TextCount            : 0
ChaptersCount        : 0
CompleteName         : Z:\newsleecher\2006-02-13 newsleecher\msgid_1737517_Test\Test Video.avi
FolderName           : Z:\newsleecher\2006-02-13 newsleecher\msgid_1737517_Test
FileName             : Test Video
FileExtension        : avi
FileSize             : 1177313280
FileSize/String      : 1.10 GiB
FileSize/String1     : 1 GiB
FileSize/String2     : 1.1 GiB
FileSize/String3     : 1.10 GiB
FileSize/String4     : 1.096 GiB
Format               : AVI
Format/String        : Audio Video Interleave
Format/Extensions    : AVI
OveralBitRate        : 1341580
OveralBitRate/String : 1342 Kbps
PlayTime             : 7020454
PlayTime/String      : 1h 57mn
PlayTime/String1     : 1h 57mn 454ms
PlayTime/String2     : 1h 57mn
PlayTime/String3     : 01:57:00.454
Video_Codec_List     : XviD
Audio_Codec_List     : MPEG1/2 L3

Video #0
Count                : 35
StreamCount          : 1
StreamKind           : Video
StreamKindID         : 0
Codec                : XVID
Codec/String         : XviD
Codec/Info           : XviD project
Codec/Url            : http://www.koepi.org/xvid.shtml
BitRate              : 1136580
BitRate/String       : 1137 Kbps
Width                : 640
Height               : 480
AspectRatio          : 1.333
AspectRatio/String   : 4/3
FrameRate            : 29.970
FrameRate/String     : 29.97 fps
FrameCount           : 210403
Bits/(Pixel*Frame)   : 0.123

Audio #0
Count                : 36
StreamCount          : 1
StreamKind           : Audio
StreamKindID         : 0
Codec                : 55
Codec/String         : MPEG1/2 L3
Codec/Info           : MPEG1 or 2 Audio Layer 3
Codec/Url            : http://www.iis.fraunhofer.de/amm/index.html
BitRate              : 192000
BitRate/String       : 192 Kbps
Channel(s)           : 2
Channel(s)/String    : 2 channels
SamplingRate         : 44100
SamplingRate/String  : 44 KHz

using MediaInfoLib - v0.7.1.0
*/

}
