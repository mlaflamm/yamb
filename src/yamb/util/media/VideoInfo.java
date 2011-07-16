package yamb.util.media;

/**
 * @author manuel.laflamme
 * @since 1-Feb-2008
 */
public interface VideoInfo
{
    /**
     * Returns the video playtime in miliseconds (?);
     */
    Long getPlaytime();

    Integer getWidth();

    Integer getHeight();

    public String getVideoCodec();

    public String getAudioCodec();
}
