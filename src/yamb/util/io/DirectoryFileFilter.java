package yamb.util.io;

import java.io.File;
import java.io.FileFilter;

/**
 * @author manuel.laflamme
 * @since Feb 5, 2008
 */
public class DirectoryFileFilter implements FileFilter
{
    public boolean accept(File aFile)
    {
        return aFile.isDirectory();
    }
}
