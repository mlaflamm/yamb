package yamb.app.tag;

import java.util.EventListener;

/**
 * @author manuel.laflamme
 * @since Feb 4, 2008
 */
public interface TagEventListener extends EventListener
{
    public void tagAdded(TagEvent aEvent);

    public void tagRemoved(TagEvent aEvent);
}
