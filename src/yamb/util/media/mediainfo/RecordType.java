package yamb.util.media.mediainfo;

/**
 * @author manuel.laflamme
 * @since Feb 16, 2006
 */
public class RecordType
{
    public static final RecordType GENERAL = new RecordType("General");
    public static final RecordType VIDEO = new RecordType("Video");
    public static final RecordType AUDIO = new RecordType("Audio");

    private final String mName;

    private RecordType(String aType)
    {
        mName = aType;
    }

    static public RecordType forString(String aName)
    {
        if (aName.startsWith(GENERAL.getName()))
        {
            return GENERAL;
        }

        if (aName.startsWith(VIDEO.getName()))
        {
            return VIDEO;
        }

        if (aName.startsWith(AUDIO.getName()))
        {
            return AUDIO;
        }

        throw new IllegalArgumentException("Unknown record type: " + aName);
    }

    public String getName()
    {
        return mName;
    }

    public boolean isGeneral()
    {
        return this == GENERAL;
    }

    public boolean isVideo()
    {
        return this == VIDEO;
    }

    public boolean isAudio()
    {
        return this == AUDIO;
    }

    public String toString()
    {
        return mName;
    }
}