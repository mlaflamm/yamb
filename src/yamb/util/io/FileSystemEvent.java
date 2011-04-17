package yamb.util.io;

import org.apache.commons.lang.builder.ToStringBuilder;

import java.io.File;
import java.util.EventObject;

/**
 * @author manuel.laflamme
 * @since Feb 5, 2008
 */
public class FileSystemEvent extends EventObject
{
    private final File mDirectory;

    public FileSystemEvent(Object aSource, File aDirectory)
    {
        super(aSource);
        mDirectory = aDirectory;
    }

    public File getDirectory()
    {
        return mDirectory;
    }

    public String toString()
    {
        ToStringBuilder builder = new ToStringBuilder(this);
        //builder.appendSuper(super.toString());
        builder.append("Directory", mDirectory);
        //builder.append("LastModified", new Timestamp(mLastModified));
        return builder.toString();
    }
}
