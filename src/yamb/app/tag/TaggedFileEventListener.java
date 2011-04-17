package yamb.app.tag;

import java.util.EventListener;

/**
 * @author manuel.laflamme
 * @since 25-Feb-2008
 */
public interface TaggedFileEventListener extends EventListener
{
    public void taggedFileAdded(TaggedFileEvent aEvent);

    public void taggedFileRemoved(TaggedFileEvent aEvent);
}
