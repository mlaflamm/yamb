package yamb.app.tag.library;

import java.util.EventListener;

/**
 * @author manuel.laflamme
 * @since 7-Nov-2008
 */
public interface LibraryProcessingListener extends EventListener
{
    public void libraryProcessing(LibraryProcessingEvent aEvent);    
}
