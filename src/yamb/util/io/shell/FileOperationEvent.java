package yamb.util.io.shell;

import java.io.File;
import java.util.EventObject;

/**
 * @author manuel.laflamme
 * @since 12-Feb-2008
 */
public class FileOperationEvent extends EventObject
{
    private final File mSourceFile;
    private final File mDestinationFile;

    public FileOperationEvent(Object aEventSource, File aSourceFile, File aDestinationFile)
    {
        super(aEventSource);
        mSourceFile = aSourceFile;
        mDestinationFile = aDestinationFile;
    }

    public File getSourceFile()
    {
        return mSourceFile;
    }

    public File getDestinationFile()
    {
        return mDestinationFile;
    }
}
