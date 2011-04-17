package yamb.app.view.explorer;

import org.apache.commons.lang.builder.ToStringBuilder;

import java.io.File;

/**
 * @author manuel.laflamme
 * @since Feb 18, 2008
 */
public class ActiveFolder
{
    private final File mFile;
    private final boolean mRecursive;

    public ActiveFolder(File aFile, boolean aRecursive)
    {
        if (aFile == null)
        {
            throw new NullPointerException();
        }

        mFile = aFile;
        mRecursive = aRecursive;
    }

    public ActiveFolder(File aFile)
    {
        this(aFile, false);
    }

    public File getFile()
    {
        return mFile;
    }

    public String getAbsolutePath()
    {
        return mFile.getAbsolutePath();
    }

    public boolean isRecursive()
    {
        return mRecursive;
    }

    public boolean equals(Object aObject)
    {
        if (this == aObject)
        {
            return true;
        }
        if (aObject == null || getClass() != aObject.getClass())
        {
            return false;
        }

        final ActiveFolder that = (ActiveFolder) aObject;

        if (mRecursive != that.mRecursive)
        {
            return false;
        }
        if (!mFile.equals(that.mFile))
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        int result;
        result = mFile.hashCode();
        result = 29 * result + (mRecursive ? 1 : 0);
        return result;
    }


    public String toString()
    {
        ToStringBuilder builder = new ToStringBuilder(this);
        builder.append("mFile", mFile);
        builder.append("mRecursive", mRecursive);
        return builder.toString();
    }
}
