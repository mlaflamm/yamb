package yamb.util.swing;

/**
 * @author manuel.laflamme
 * @since Feb 7, 2008
 */
public interface ObjectFilter
{
    public boolean accept(Object aObject);
    public boolean acceptAll();

    public void addFilterEventListener(FilterEventListener aListener);

    public void removeFilterEventListener(FilterEventListener aListener);
}
