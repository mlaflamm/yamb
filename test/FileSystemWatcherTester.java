import yamb.util.io.FileSystemEvent;
import yamb.util.io.FileSystemEventListener;
import yamb.util.io.FileSystemWatcher;

import java.io.File;

/**
 *
 * @author manuel.laflamme
 * @since Feb 5, 2008
 */
public class FileSystemWatcherTester
{
    public static void main(String[] aArgs) throws Exception
    {
        FileSystemWatcher fileSystemWatcher = new FileSystemWatcher();
        fileSystemWatcher.addFileSystemEventListener(new FileSystemEventListener()
        {
            public void directoryChanged(FileSystemEvent aEvent)
            {
                System.out.println("directoryChanged: " + aEvent);
            }

            public void watchedDirectoryGone(FileSystemEvent aEvent)
            {
                System.out.println("watchedDirectoryGone: " + aEvent);
            }
        });

        fileSystemWatcher.addWatchedDirectory(new File("Z:\\temp"), true);
        fileSystemWatcher.run();
    }

}
