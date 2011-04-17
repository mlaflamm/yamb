package yamb.util.media;

import java.io.File;
import java.io.FileFilter;

/**
 * @author manuel.laflamme
 * @since 1-Feb-2008
 */
public class VideoFileFilter implements FileFilter
{
    private final boolean mAcceptDirectory;

    public VideoFileFilter(boolean aAcceptDirectory)
    {
        mAcceptDirectory = aAcceptDirectory;
    }

    public boolean accept(File aPathname)
    {
        if ((mAcceptDirectory && aPathname.isDirectory()) || Videos.isVideoFile(aPathname))
        {
            return true;
        }
        return false;
    }
}
