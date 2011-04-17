package yamb.util.io.shell;

import java.util.EventListener;

/**
 * @author manuel.laflamme
 * @since 12-Feb-2008
 */
public interface FileOperationListener extends EventListener
{
    public void fileRenamed(FileOperationEvent aEvent);

    public void fileCopied(FileOperationEvent aEvent);

    public void fileMoved(FileOperationEvent aEvent);

    public void fileDeleted(FileOperationEvent aEvent);
}
