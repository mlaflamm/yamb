package yamb.util.media.mediainfo;

import yamb.util.media.VideoInfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

/**
 * @author manuel.laflamme
 * @since 31-Jan-2008
 */
public class MediaInfo implements VideoInfo
{
    private Long mPlaytime;
    private Integer mWidth;
    private Integer mHeight;
    private String mVideoCodec;
    private String mAudioCodec;

    private MediaInfoRecord mGeneralRecord;
    private MediaInfoRecord mVideoRecord;
    private MediaInfoRecord mAudioRecord;

    private MediaInfo()
    {
    }

    public static MediaInfo createVideoInfo(File aMediaFile)
    {
        MediaInfo mediaInfo = new MediaInfo();
        boolean initialized = false;
        try
        {
            String path = System.getProperty("mediainfo.path", "");
            String command = new File(path, "MediaInfo.exe").getCanonicalPath() +
                    " --Inform=\"file://bin/mediainfo.template\" \"" + aMediaFile.getAbsolutePath() + "\"";
//            String command = "MediaInfo.exe -f \"" + aMediaFile.getAbsolutePath() + "\"";
            Process process = Runtime.getRuntime().exec(command);
            final InputStream inputStream = process.getInputStream();
            MediaInfoReader reader = new MediaInfoReader(
                    new BufferedReader(new InputStreamReader(inputStream)));

            MediaInfoRecord infoRecord;
            while ((infoRecord = reader.nextRecord()) != null)
            {
                if (infoRecord.getRecordType().isGeneral())
                {
                    mediaInfo.mGeneralRecord = infoRecord;
                    Map fields = infoRecord.getFields();
                    String playtime = (String) fields.get(MediaInfoRecord.GENERAL_PLAYTIME);

                    if (playtime != null && playtime.length() > 0)
                    {
                        mediaInfo.mPlaytime = Long.valueOf(playtime);
                        initialized = true;
                    }
                }
                else if (infoRecord.getRecordType().isVideo())
                {
                    mediaInfo.mVideoRecord = infoRecord;
                    Map fields = infoRecord.getFields();
                    String codec = (String) fields.get(MediaInfoRecord.VIDEO_CODEC);
//                String bitrate = (String)fields.get(MediaInfoRecord.VIDEO_BITRATE);
                    String width = (String) fields.get(MediaInfoRecord.VIDEO_WIDTH);
                    String height = (String) fields.get(MediaInfoRecord.VIDEO_HEIGHT);

                    if (codec != null)
                    {
                        mediaInfo.mVideoCodec = codec;
                        initialized = true;
                    }

//                if (bitrate != null)
//                {
//                    bitrate = bitrate.substring(0, bitrate.length()-3);
//                    aContentRecord.setBitrate(bitrate);
//                }

                    if (width != null && width.length() > 0)
                    {
                        mediaInfo.mWidth = Integer.valueOf(width);
                        initialized = true;
                    }

                    if (height != null && height.length() > 0)
                    {
                        mediaInfo.mHeight = Integer.valueOf(height);
                        initialized = true;
                    }
                }
                else if (infoRecord.getRecordType().isAudio())
                {
                    mediaInfo.mAudioRecord = infoRecord;
                    Map fields = infoRecord.getFields();
                    String codec = (String) fields.get(MediaInfoRecord.AUDIO_CODEC);
//                String bitrate = (String)fields.get(MediaInfoRecord.VIDEO_BITRATE);

                    if (codec != null)
                    {
                        mediaInfo.mAudioCodec = codec;
                        initialized = true;
                    }
                }

            }
        }
        catch (IOException e)
        {
            IllegalStateException rte = new IllegalStateException(e.toString());
            rte.initCause(e);
            throw rte;
        }

        return initialized ? mediaInfo : null;
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

    public String getVideoCodec()
    {
        return mVideoCodec;
    }

    public String getAudioCodec()
    {
        return mAudioCodec;
    }

    public MediaInfoRecord getGeneralRecord()
    {
        return mGeneralRecord;
    }

    public MediaInfoRecord getVideoRecord()
    {
        return mVideoRecord;
    }

    public MediaInfoRecord getAudioRecord()
    {
        return mAudioRecord;
    }


    public String toString()
    {
        return "MediaInfo{" +
                "mPlaytime=" + mPlaytime +
                ", mWidth=" + mWidth +
                ", mHeight=" + mHeight +
                ", mVideoCodec='" + mVideoCodec + '\'' +
                ", mAudioCodec='" + mAudioCodec + '\'' +
                ", mGeneralRecord=" + mGeneralRecord +
                ", mVideoRecord=" + mVideoRecord +
                ", mAudioRecord=" + mAudioRecord +
                '}';
    }
}
