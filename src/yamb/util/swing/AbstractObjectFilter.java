package yamb.util.swing;

import yamb.util.event.GenericEventSupport;

/**
 *
 * @author manuel.laflamme
 * @since Feb 7, 2008
 */
public abstract class AbstractObjectFilter implements ObjectFilter
{
    private final GenericEventSupport mEventSupport = new GenericEventSupport();

    protected void fireFilterChanged(FilterEvent aEvent)
    {
        mEventSupport.fireEvent("filterChanged", aEvent);
    }

    public void addFilterEventListener(FilterEventListener aListener)
    {
        mEventSupport.addEventListener(aListener);
    }

    public void removeFilterEventListener(FilterEventListener aListener)
    {
        mEventSupport.removeEventListener(aListener);
    }
}
