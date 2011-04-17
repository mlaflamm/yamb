package yamb.util.io;

import yamb.util.event.SwingSafeEventSupport;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;


/**
 * @author manuel.laflamme
 * @since Feb 5, 2008
 */
public class FileSystemWatcher implements Runnable
{
    private static final Logger LOGGER = Logger.getLogger(FileSystemWatcher.class);
    private static final int DEFAULT_WAIT = 5000;

    private final Map<File, Long> mWatchedDirectory = new TreeMap<File, Long>();
    private final SwingSafeEventSupport mEventSupport = new SwingSafeEventSupport();
    private final int mWait;

    public FileSystemWatcher(int aWait)
    {
        mWait = aWait;
    }

    public FileSystemWatcher()
    {
        mWait = DEFAULT_WAIT;
    }

    public void addWatchedDirectory(File aDirectory, boolean aRecursive)
    {
        if (aDirectory.isDirectory() && aDirectory.exists())
        {
            long lastModified = aDirectory.lastModified();
            synchronized (mWatchedDirectory)
            {
                if (!mWatchedDirectory.containsKey(aDirectory))
                {
                    mWatchedDirectory.put(aDirectory, lastModified);
                }
            }

            if (aRecursive)
            {
                File[] subdirectories = aDirectory.listFiles(new DirectoryFileFilter());
                for (File subdirectory : subdirectories)
                {
                    addWatchedDirectory(subdirectory, true);
                }
            }
        }
    }

    public void removeWatchedDirectory(File aDirectory, boolean aRecursive)
    {
        Set<File> directories = null;
        synchronized (mWatchedDirectory)
        {
            mWatchedDirectory.remove(aDirectory);
            directories = new HashSet<File>(mWatchedDirectory.keySet());
        }

        for (File directory : directories)
        {
            if (Files.isChild(aDirectory, directory))
            {
                removeWatchedDirectory(directory, false);
            }
        }
    }

    public void clearWatchedDirectory(File aDirectory)
    {
        synchronized (mWatchedDirectory)
        {
            mWatchedDirectory.clear();
        }
    }

    public void run()
    {
        try
        {
            while (true)
            {
                Thread.sleep(mWait);

                HashMap<File, Long> watchedDirCopy = null;
                synchronized (mWatchedDirectory)
                {
                    watchedDirCopy = new HashMap<File, Long>(mWatchedDirectory);
                }

                Set<Entry<File, Long>> entries = watchedDirCopy.entrySet();
                for (Entry<File, Long> entry : entries)
                {
                    File directory = entry.getKey();
                    long lastModified = directory.lastModified();
                    if (lastModified == 0)
                    {
                        removeWatchedDirectory(directory, false);
                        fireWatchedDirectoryGone(directory);
                    }
                    else if (!entry.getValue().equals(lastModified))
                    {
                        synchronized (mWatchedDirectory)
                        {
                            mWatchedDirectory.put(directory, lastModified);
                        }
                        fireDirectoryChanged(directory);
                    }
                }
            }
        }
        catch (InterruptedException e)
        {
            LOGGER.fatal("Unhandled InterruptedException", e);
        }
    }

    private void fireDirectoryChanged(File aDirectory)
    {
        synchronized (mEventSupport)
        {
            mEventSupport.fireEvent("directoryChanged", new FileSystemEvent(this, aDirectory));
        }
    }

    private void fireWatchedDirectoryGone(File aDirectory)
    {
        synchronized (mEventSupport)
        {
            mEventSupport.fireEvent("watchedDirectoryGone", new FileSystemEvent(this, aDirectory));
        }
    }

    public void addFileSystemEventListener(FileSystemEventListener aListener)
    {
        synchronized (mEventSupport)
        {
            mEventSupport.addEventListener(aListener);
        }
    }

    public void removeFileSystemEventListener(FileSystemEventListener aListener)
    {
        synchronized (mEventSupport)
        {
            mEventSupport.removeEventListener(aListener);
        }
    }
}
