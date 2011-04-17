package yamb.app.tag.library;

import java.io.File;
import java.util.EventObject;

/**
 * @author manuel.laflamme
 * @since 7-Nov-2008
 */
public class LibraryProcessingEvent extends EventObject
{
    private final File mLibraryRoot;
    private final File mProcessingFolder;

    public LibraryProcessingEvent(Object source, File aLibraryRoot, File aProcessingFolder)
    {
        super(source);
        mLibraryRoot = aLibraryRoot;
        mProcessingFolder = aProcessingFolder;
    }

    public File getLibraryRoot()
    {
        return mLibraryRoot;
    }

    public File getProcessingFolder()
    {
        return mProcessingFolder;
    }
}
