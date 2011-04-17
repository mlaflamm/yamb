package yamb.util.media.mediainfo;

import java.util.Map;

/**
 * @author Manuel Laflamme
 * @since Feb 16, 2006
 */
public class MediaInfoRecord
{
    // General record
    public static final String GENERAL_PLAYTIME  = "PlayTime";

    // Video record
    public static final String VIDEO_CODEC  = "Codec/String";
//    public static final String VIDEO_BITRATE  = "BitRate";
    public static final String VIDEO_WIDTH  = "Width";
    public static final String VIDEO_HEIGHT  = "Height";

    public static final String AUDIO_CODEC = "Codec/String";

    private final String mRecordName;
    private final RecordType mRecordType;
    private final Map mFields;

    public MediaInfoRecord(String aRecordName, RecordType aRecordType, Map aFields)
    {
        mRecordName = aRecordName;
        mRecordType = aRecordType;
        mFields = aFields;
    }

    public String getRecordName()
    {
        return mRecordName;
    }

    public RecordType getRecordType()
    {
        return mRecordType;
    }

    public Map getFields()
    {
        return mFields;
    }


    public String toString()
    {
        return "MediaInfoRecord{" +
                "mRecordName='" + mRecordName + '\'' +
                ", mRecordType=" + mRecordType +
                ", mFields=" + mFields +
                '}';
    }
}
