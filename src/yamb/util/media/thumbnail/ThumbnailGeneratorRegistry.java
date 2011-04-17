package yamb.util.media.thumbnail;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author manuel.laflamme
 * @since 20-Feb-2008
 */
public class ThumbnailGeneratorRegistry
{
    private SortedMap<String, ThumbnailGenerator> mGenerators = new TreeMap<String, ThumbnailGenerator>(String.CASE_INSENSITIVE_ORDER);

    public void registerGenerator(List<String> aFormats, ThumbnailGenerator aThumbnailGenerator)
    {
        for (String ext : aFormats)
        {
            registerGenerator(ext, aThumbnailGenerator);
        }
    }

    public void registerGenerator(String aFormat, ThumbnailGenerator aThumbnailGenerator)
    {
        mGenerators.put(aFormat.toLowerCase(), aThumbnailGenerator);
    }

    public ThumbnailGenerator getGenerator(String aFormat)
    {
        return mGenerators.get(aFormat.toLowerCase());
    }

    public boolean hasGenerator(String aFormat)
    {
        return mGenerators.containsKey(aFormat.toLowerCase());
    }
}
