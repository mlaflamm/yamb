package yamb.app.tag;

import yamb.util.io.FileBlockingQueueConsumer;
import yamb.util.io.FileSystemEvent;
import yamb.util.io.FileSystemEventListener;
import yamb.util.io.FileSystemWatcher;
import yamb.util.io.Files;
import yamb.util.media.VideoFileFilter;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This class is thread safe and can be invoked from any thread. Events can are only fired from the swing event
 * dispatcher thread.
 *
 * @author manuel.laflamme
 * @since Feb 4, 2008
 */
public class AutoTagManager extends AbstractTagManager
{
    private static final Logger LOGGER = Logger.getLogger(AutoTagManager.class);

    // Global tag list
    private final Map<String, SortedSet<File>> mTagFileCache = new TreeMap<String, SortedSet<File>>();
    private final Map<File, Set<File>> mDirectoryFileCache = new HashMap<File, Set<File>>();

    // Autotagging support
    private final BlockingQueue<File> mAutotagQueue = new LinkedBlockingQueue<File>();
    private final FileSystemWatcher mFileSystemWatcher = new FileSystemWatcher();

    private final SortedSet<String> mRecentTags = new TreeSet<String>();

    private boolean mStarted = false;

    public AutoTagManager(List<File> aAutoTagDirectories)
    {
        queueAutotagDirectories(aAutoTagDirectories);
    }

    public void start()
    {
        if (mStarted)
        {
            throw new IllegalStateException("Already started!");
        }

        Thread autotagThread = new Thread(new AutotagQueueConsumer(mAutotagQueue), "Tag-Auto");
        autotagThread.setPriority(autotagThread.getPriority() / 2);
        autotagThread.setDaemon(true);
        autotagThread.start();

        mFileSystemWatcher.addFileSystemEventListener(new AutotagFileSystemEventListener());
        Thread watchThread = new Thread(mFileSystemWatcher, "Tag-Watch");
        watchThread.setPriority(watchThread.getPriority() / 4);
        watchThread.setDaemon(true);
        watchThread.start();

        mStarted = true;
    }

    public List<String> getTags()
    {
        synchronized (mTagFileCache)
        {
            return new ArrayList<String>(mTagFileCache.keySet());
        }
    }

    public int getTagsCount()
    {
        synchronized (mTagFileCache)
        {
            return mTagFileCache.size();
        }
    }

    public List<String> getRecentsTags()
    {
        synchronized (mTagFileCache) // Not a mistake, this Set is synchronized with the cache
        {
            return new ArrayList<String>(mRecentTags);
        }
    }

    public int getRecentTagsCount()
    {
        synchronized (mTagFileCache) // Not a mistake, this Set is synchronized with the cache
        {
            return mRecentTags.size();
        }
    }

    public boolean isRecentTag(String aTag)
    {
        synchronized (mTagFileCache) // Not a mistake, this Set is synchronized with the cache
        {
            return mRecentTags.contains(aTag);
        }
    }

//    /**
//     * Returns wheter the specified file is related to a favorite tag
//     */
//    public boolean isFavorite(File aFile)
//    {
//        List<String> favorites = getFavoriteTags();
//        synchronized (mTagFileCache)
//        {
//            for (String tag : favorites)
//            {
//                SortedSet<File> files = mTagFileCache.get(tag);
//                if (files.contains(aFile))
//                {
//                    return true;
//                }
//            }
//        }
//        return false;
//    }

    public List<File> getFiles(String aTag)
    {
        synchronized (mTagFileCache)
        {
            SortedSet<File> files = mTagFileCache.get(aTag);
            return files == null ? Arrays.asList(new File[0]) : new ArrayList<File>(files);
        }
    }


    public List<File> getFiles(List<String> aTags, TagSetMode aSetMode)
    {
        SortedSet<File> files = createFileSortedSet();

        switch (aSetMode)
        {
            case OR:
                synchronized (mTagFileCache)
                {
                    for (String tag : aTags)
                    {
                        files.addAll(mTagFileCache.get(tag));
                    }
                }
                break;
            case AND:
                synchronized (mTagFileCache)
                {
                    boolean initialized = false;
                    for (String tag : aTags)
                    {
                        SortedSet<File> tagFiles = mTagFileCache.get(tag);
                        if (!initialized)
                        {
                            files.addAll(tagFiles);
                            initialized = true;
                        }
                        else
                        {
                            files.retainAll(tagFiles);
                        }
                    }
                }
                break;
        }

        return new ArrayList<File>(files);
    }

    private TreeSet<File> createFileSortedSet()
    {
        return new TreeSet<File>(Files.FILE_NAMECOMPARATOR);
    }

    private boolean containsRecentFiles(File aDirectory)
    {
        return Files.isRecent(aDirectory);
    }

    /**
     * Add directories to the autotag queue
     */
    public void queueAutotagDirectories(List<File> aDirectories)
    {
        for (File directory : aDirectories)
        {
            mAutotagQueue.offer(directory);
        }
    }

    public List<File> getTaggedDirectories()
    {
        synchronized (mTagFileCache)
        {
            return new ArrayList<File>(mDirectoryFileCache.keySet());
        }
    }

    public List<File> getTaggedFiles()
    {
        List<File> allTaggedFiles = new ArrayList<File>();
        synchronized (mTagFileCache)
        {
            for (Set<File> files : mDirectoryFileCache.values())
            {
                allTaggedFiles.addAll(files);
            }
        }

        Collections.sort(allTaggedFiles, Files.FILE_NAMECOMPARATOR);
        return allTaggedFiles;
    }

    public int getTaggedFilesCount()
    {
        throw new IllegalStateException("Not implemented!");
    }

    private class AutotagQueueConsumer extends FileBlockingQueueConsumer
    {
        public AutotagQueueConsumer(BlockingQueue<File> aQueue)
        {
            super(aQueue);
        }

        public void consume(File aDirectory)
        {
            synchronized (mTagFileCache)
            {
                // Skip if directory is up to date
                if (mDirectoryFileCache.containsKey(aDirectory))
                {
                    return;
                }
            }

            // Do not process if specified file is not a directory or does not exist
            if (!aDirectory.exists() || !aDirectory.isDirectory())
            {
                return;
            }

            File[] children = Files.getChildren(aDirectory, new VideoFileFilter(true)
            {
                public boolean accept(File aPathname)
                {
                    return super.accept(aPathname) && aPathname.getAbsolutePath().indexOf("Temp") < 0;
                }
            });

            // Watch directory
            mFileSystemWatcher.addWatchedDirectory(aDirectory, false);

            // Autotag directory
            LOGGER.debug("Autotaging '" + aDirectory.getAbsolutePath() + "'");
            List<File> subdirectories = new ArrayList<File>();
            Set<File> cachedChildren = new HashSet<File>();

            synchronized (mTagFileCache)
            {
                // Used to fire added tags event
                List<String> addedGlobalTags = new ArrayList<String>();
                List<String> addedRecentTags = containsRecentFiles(aDirectory) ? new ArrayList<String>() : null;

                for (File child : children)
                {
                    // Process subdirectories later outside this syncronized block
                    if (child.isDirectory())
                    {
                        subdirectories.add(child);
                    }
                    else
                    {
                        // Update tag cache
                        String[] tags = Tags.getTagsFromFileName(child.getName());
                        for (String tag : tags)
                        {
                            SortedSet<File> taggedFiles = mTagFileCache.get(tag);
                            if (taggedFiles == null)
                            {
                                taggedFiles = createFileSortedSet();
                                mTagFileCache.put(tag, taggedFiles);
                                addedGlobalTags.add(tag);
                            }
                            taggedFiles.add(child);
                        }

                        // Update recent cache if required
                        if (addedRecentTags != null)
                        {
                            for (String tag : tags)
                            {
                                if (!mRecentTags.contains(tag))
                                {
                                    addedRecentTags.add(tag);
                                    mRecentTags.add(tag);
                                }
                            }
                        }

                        // Update parent directory file cache
                        cachedChildren.add(child);
                    }
                }

                // Update parent directory file cache
                mDirectoryFileCache.put(aDirectory, cachedChildren);

                // Fire notification if global tags added
                if (addedGlobalTags.size() > 0)
                {
                    fireTagAdded(TagCategory.GLOBAL, addedGlobalTags);
                }

                // Fire notification if recent tags added
                if (addedRecentTags != null && addedRecentTags.size() > 0)
                {
                    fireTagAdded(TagCategory.RECENT, addedRecentTags);
                }
            }

            fireTaggedFileAdded(new ArrayList<File>(cachedChildren));

            // Process subdirectories outside of the syncronized block to not hold lock while
            // accessing the file system
            for (File subdirectory : subdirectories)
            {
                consume(subdirectory);
            }
        }
    }

    private class AutotagFileSystemEventListener implements FileSystemEventListener
    {
        public void directoryChanged(FileSystemEvent aEvent)
        {
            LOGGER.debug("directoryChanged '" + aEvent.getDirectory().getAbsolutePath() + "'");

            // Refresh tag cache with modified directory content
            clearDirectory(aEvent.getDirectory());
            queueAutotagDirectories(Arrays.asList(new File[]{aEvent.getDirectory()}));
        }

        public void watchedDirectoryGone(FileSystemEvent aEvent)
        {
            LOGGER.debug("watchedDirectoryGone '" + aEvent.getDirectory().getAbsolutePath() + "'");

            // Remove directory content from tag cache
            clearDirectory(aEvent.getDirectory());
        }

        private void clearDirectory(File aDirectory)
        {
            synchronized (mTagFileCache)
            {
                // Remove directory from parent directory file cache
                Set<File> children = mDirectoryFileCache.remove(aDirectory);

                // Remove cached children from tag cache
                for (File child : children)
                {
                    String[] tags = Tags.getTagsFromFileName(child.getName());
                    for (String tag : tags)
                    {
                        SortedSet<File> taggedFiles = mTagFileCache.get(tag);
                        if (taggedFiles != null)
                        {
                            taggedFiles.remove(child);
                        }
                    }
                }

                fireTaggedFileRemoved(new ArrayList<File>(children));
            }
        }
    }

}
