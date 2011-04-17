package yamb.app.tag.library;

import java.io.File;
import java.util.EventObject;
import java.util.List;

/**
 * @author manuel.laflamme
 * @since Apr 12, 2008
 */
public class LibraryEvent extends EventObject
{
    private final List<File> mLibraries;

    public LibraryEvent(Object aSource, List<File> aLibraries)
    {
        super(aSource);
        mLibraries = aLibraries;
    }

    public List<File> getLibraries()
    {
        return mLibraries;
    }
}
