package yamb.app.tag;

import java.io.File;
import java.util.EventObject;
import java.util.List;

/**
 * @author manuel.laflamme
 * @since 25-Feb-2008
 */
public class TaggedFileEvent extends EventObject
{
    private final List<File> mFiles;

    public TaggedFileEvent(Object aSource, List<File> aFiles)
    {
        super(aSource);
        mFiles = aFiles;
    }

    public List<File> getFiles()
    {
        return mFiles;
    }


    public String toString()
    {
        return "TaggedFileEvent{" +
                ", mFiles=" + mFiles +
                '}';
    }
}
