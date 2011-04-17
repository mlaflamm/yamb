import com.sun.jna.examples.FileMonitor;

import java.io.File;

/**
 * @author manuel.laflamme
 * @since Feb 22, 2008
 */
public class JnaFileMonitorTester
{
    public static void main(String[] aArgs) throws Exception
    {
        FileMonitor fileMonitor = FileMonitor.getInstance();
        fileMonitor.addFileListener(new FileMonitor.FileListener()
        {
            public void fileChanged(FileMonitor.FileEvent aEvent)
            {
                System.out.println("fileChanged: " + aEvent);
            }
        });

//        fileSystemWatcher.addWatchedDirectory(new File("R:\\Projects\\data\\"), true);
        fileMonitor.addWatch(new File("C:\\Projects\\_data"), FileMonitor.FILE_ANY, true);
        while (true)
        {
            Thread.sleep(1000);
        }
    }
}
