package yamb.app.tag.library;

import java.util.EventListener;

/**
 * @author manuel.laflamme
 * @since Apr 12, 2008
 */
public interface LibraryEventListener extends EventListener
{
    public void libraryAdded(LibraryEvent aEvent);
    public void libraryRemoved(LibraryEvent aEvent);    
    public void libraryLoaded(LibraryEvent aEvent);
    public void libraryUnloaded(LibraryEvent aEvent);
}
