package yamb.app.tag.library;

import yamb.util.event.SwingSafeEventSupport;

import java.io.File;

/**
 * @author manuel.laflamme
 * @since 7-Nov-2008
 */
public class LibraryProcessingEventSupport
{
    private final SwingSafeEventSupport mLibraryEventSupport;
    private final File mLibraryRoot;

    public LibraryProcessingEventSupport(SwingSafeEventSupport aLibraryEventSupport, File aLibraryRoot)
    {
        mLibraryEventSupport = aLibraryEventSupport;
        mLibraryRoot = aLibraryRoot;
    }

    protected void fireLibraryProcessing(File aProcessingFolder)
    {
        mLibraryEventSupport.fireEvent("libraryProcessing", new LibraryProcessingEvent(this, mLibraryRoot, aProcessingFolder));
    }
}
