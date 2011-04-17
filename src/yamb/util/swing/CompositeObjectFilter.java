package yamb.util.swing;

/**
 * @author manuel.laflamme
 * @since 7-Sep-2008
 */

public class CompositeObjectFilter implements ObjectFilter
{
    private final ObjectFilter[] mFilters;

    public CompositeObjectFilter(ObjectFilter... aFilters)
    {
        mFilters = aFilters.clone();
    }

    public boolean accept(Object aObject)
    {
        for (ObjectFilter filter : mFilters)
        {
            if (!filter.accept(aObject))
            {
                return false;
            }
        }
        return true;
    }

    public boolean acceptAll()
    {
        for (ObjectFilter filter : mFilters)
        {
            if (!filter.acceptAll())
            {
                return false;
            }
        }
        return true;
    }

    public void addFilterEventListener(FilterEventListener aListener)
    {
        for (ObjectFilter filter : mFilters)
        {
            filter.addFilterEventListener(aListener);
        }
    }

    public void removeFilterEventListener(FilterEventListener aListener)
    {
        for (ObjectFilter filter : mFilters)
        {
            filter.removeFilterEventListener(aListener);
        }
    }
}
