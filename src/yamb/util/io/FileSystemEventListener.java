package yamb.util.io;

import java.util.EventListener;

/**
 * @author manuel.laflamme
 * @since Feb 5, 2008
 */
public interface FileSystemEventListener extends EventListener
{
    public void directoryChanged(FileSystemEvent aEvent);

    public void watchedDirectoryGone(FileSystemEvent aEvent);
}
