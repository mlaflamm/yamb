package yamb.util.swing;

import java.util.EventListener;

/**
 *
 * @author manuel.laflamme
 * @since Feb 7, 2008
 */
public interface FilterEventListener extends EventListener
{
    public void filterChanged(FilterEvent aEvent);
}
