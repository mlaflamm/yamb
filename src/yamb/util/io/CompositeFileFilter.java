package yamb.util.io;

import java.io.File;
import java.io.FileFilter;

/**
 * @author manuel.laflamme
 * @since 13-Feb-2008
 */
public class CompositeFileFilter implements FileFilter
{
    private final FileFilter[] filters;

    public CompositeFileFilter(FileFilter... aFilters)
    {
        if (aFilters.length == 0)
        {
            throw new IllegalArgumentException("Should have at least one filter");
        }
        filters = aFilters;
    }


    public boolean accept(File pathname)
    {
        for (int i = 0; i < filters.length; i++)
        {
            FileFilter filter = filters[i];
            if (!filter.accept(pathname))
            {
                return false;
            }
        }
        return true;
    }
}
