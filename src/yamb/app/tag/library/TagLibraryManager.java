package yamb.app.tag.library;

import yamb.app.tag.AbstractTagManager;
import yamb.app.tag.TagCategory;
import yamb.app.tag.TagSetMode;
import yamb.util.event.SwingSafeEventSupport;
import yamb.util.io.FileBlockingQueueConsumer;
import yamb.util.io.Files;
import yamb.util.io.TextDataSet;
import yamb.util.io.shell.FileOperation;
import yamb.util.io.shell.FileOperationEvent;
import yamb.util.io.shell.FileOperationListener;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author manuel.laflamme
 * @since Apr 11, 2008
 */
public class TagLibraryManager extends AbstractTagManager implements FileOperationListener, LibraryManager
{
    private static final Logger LOGGER = Logger.getLogger(TagLibraryManager.class);

    private final SwingSafeEventSupport mLibraryEventSupport = new SwingSafeEventSupport();
    private final SwingSafeEventSupport mProcessingEventSupport = new SwingSafeEventSupport();

    private final File mDatasetFile;
    private final File mDataDirectory;
    private final TextDataSet mDataset;

    private final SortedMap<File, Library> mLoadedLibraries = new TreeMap<File, Library>(Files.FILE_NAMECOMPARATOR);
    private final SortedSet<File> mLibraries = new TreeSet<File>(Files.FILE_NAMECOMPARATOR);
    private final Set<File> mRebuildingLibraries = new HashSet<File>();

    private final BlockingQueue<File> mLoadingQueue = new LinkedBlockingQueue<File>();

    private boolean mStarted = false;
    private int mCachedTagCount = -1;

    public TagLibraryManager(File aDatasetFile, File aDataDirectory, FileOperation aFileOperation) throws IOException
    {
        FileUtils.forceMkdir(aDataDirectory);
        if (!aDatasetFile.exists())
        {
            FileUtils.touch(aDatasetFile);
        }

        mDataset = new TextDataSet(new FileInputStream(aDatasetFile));
        mDatasetFile = aDatasetFile;
        mDataDirectory = aDataDirectory;

        List<String> values = mDataset.getValues();
        for (String value : values)
        {
            File file = new File(value);
            mLibraries.add(file);
            loadLibrary(file);
        }
        aFileOperation.addFileOperationListener(this);
    }

    public boolean addLibrary(File aLibraryRoot)
    {
        if (!aLibraryRoot.isDirectory())
        {
            LOGGER.warn("Library to add is not a directory: " + aLibraryRoot.getAbsolutePath());
            return false;
        }

        synchronized (mLibraries)
        {
            // Ensure root is not already a library
            if (mLibraries.contains(aLibraryRoot))
            {
                LOGGER.warn("Library already exist and not added: " + aLibraryRoot.getAbsolutePath());
                return false;
            }

            // Ensure new root is not the parent or a child of an existing library
            for (File libraryRoot : mLibraries)
            {
                if (Files.isChild(libraryRoot, aLibraryRoot) || Files.isChild(aLibraryRoot, libraryRoot))
                {
                    LOGGER.warn("Library is parent or descendant of another library: new=" +
                            aLibraryRoot.getAbsolutePath() + ", existing=" + libraryRoot.getAbsolutePath());
                    return false;
                }
            }

            // Add new root in dataset
            if (mDataset.add(aLibraryRoot.getAbsolutePath()))
            {
                LOGGER.debug("Library added: " + aLibraryRoot.getAbsolutePath());
                mLibraries.add(aLibraryRoot);
                write();

                fireLibraryAdded(Arrays.asList(new File[]{aLibraryRoot}));
                rebuildLibrary(aLibraryRoot);
                loadLibrary(aLibraryRoot);
                return true;
            }
            else
            {
                LOGGER.warn("Library already exist in dataset and not added: " + aLibraryRoot.getAbsolutePath());
                return false;
            }
        }
    }

    public boolean removeLibrary(File aLibraryRoot)
    {
        if (aLibraryRoot.exists() && !aLibraryRoot.isDirectory())
        {
            LOGGER.warn("Library to removed is not a directory: " + aLibraryRoot.getAbsolutePath());
            return false;
        }

        synchronized (mLibraries)
        {
            if (mLibraries.remove(aLibraryRoot))
            {
                mDataset.remove(aLibraryRoot.getAbsolutePath());

                unloadLibrary(Arrays.asList(new File[]{aLibraryRoot}));
                fireLibraryRemoved(Arrays.asList(new File[]{aLibraryRoot}));

                File libraryFile = getLibraryFile(aLibraryRoot);
                libraryFile.delete();

                LOGGER.debug("Library removed: " + aLibraryRoot.getAbsolutePath());
                write();
                return true;
            }
        }

        LOGGER.warn("Unknown library not removed: " + aLibraryRoot.getAbsolutePath());
        return false;
    }

    private void write()
    {
        try
        {
            LOGGER.debug("Writing library set file: " + mDatasetFile.getAbsolutePath());
            mDataset.write(new FileOutputStream(mDatasetFile));
        }
        catch (IOException e)
        {
            LOGGER.error("Cannot write library set file", e);
        }
    }

    public void start()
    {
        if (mStarted)
        {
            throw new IllegalStateException("Already started!");
        }

        Thread loadThread = new Thread(new LoadLibraryQueueConsumer(mLoadingQueue), "Load-Lib");
        loadThread.setPriority(loadThread.getPriority() * 2 / 3);
        loadThread.setDaemon(true);
        loadThread.start();

        mStarted = true;
    }

    public List<String> getTags()
    {
        return new ArrayList<String>(getTagSet());
    }

    private Set<String> getTagSet()
    {
        synchronized (mLibraries)
        {
            Set<String> tags = new TreeSet<String>();
            for (Library library : mLoadedLibraries.values())
            {
                tags.addAll(library.getTags());
            }
            return tags;
        }
    }

    private boolean containsTag(String aTag)
    {
        synchronized (mLibraries)
        {
            for (Library library : mLoadedLibraries.values())
            {
                if (library.getTags().contains(aTag))
                {
                    return true;
                }
            }
            return false;
        }
    }

    public int getTagsCount()
    {
        synchronized (mLibraries)
        {
            if (mCachedTagCount == -1)
            {
                mCachedTagCount = getTagSet().size();
            }
            return mCachedTagCount;
        }
    }

    public List<File> getFiles(String aTag)
    {
        ArrayList<File> fileList = new ArrayList<File>(getFileSet(aTag));
        Collections.sort(fileList, Files.FILE_NAMECOMPARATOR);
        return fileList;
    }

    private Set<File> getFileSet(String aTag)
    {
        synchronized (mLibraries)
        {
            Set<File> allFiles = new HashSet<File>();
            for (Library library : mLoadedLibraries.values())
            {
                Set<File> files = library.getFiles(aTag);
                if (files != null)
                {
                    allFiles.addAll(files);
                }
            }
            return allFiles;
        }
    }

    public List<File> getFiles(List<String> aTags, TagSetMode aSetMode)
    {
        Set<File> fileSet = new HashSet<File>();

        switch (aSetMode)
        {
            case OR:
                for (String tag : aTags)
                {
                    fileSet.addAll(getFileSet(tag));
                }
                break;
            case AND:
                boolean initialized = false;
                for (String tag : aTags)
                {
                    Set<File> tagFiles = getFileSet(tag);
                    if (!initialized)
                    {
                        fileSet.addAll(tagFiles);
                        initialized = true;
                    }
                    else
                    {
                        fileSet.retainAll(tagFiles);
                    }
                }
                break;
        }

        ArrayList<File> fileList = new ArrayList<File>(fileSet);
//        Collections.sort(fileList, Files.FILE_NAMECOMPARATOR);
        return fileList;
    }

    public List<File> getTaggedFiles()
    {
        Set<File> allFiles = new HashSet<File>();
        synchronized (mLibraries)
        {
            for (Library library : mLoadedLibraries.values())
            {
                Set<File> files = library.getTaggedFiles();
                if (files != null)
                {
                    allFiles.addAll(files);
                }
            }
        }

        ArrayList<File> fileList = new ArrayList<File>(allFiles);
        Collections.sort(fileList, Files.FILE_NAMECOMPARATOR);
        return fileList;
    }

    public int getTaggedFilesCount()
    {
        synchronized (mLibraries)
        {
            int count = 0;
            for (Library library : mLoadedLibraries.values())
            {
                count += library.getTaggedFiles().size();
            }
            return count;
        }
    }

    public List<File> getLibraryRoots()
    {
        synchronized (mLibraries)
        {
            return new ArrayList<File>(mLibraries);
        }
    }

    public int getLibraryCount()
    {
        synchronized (mLibraries)
        {
            return mLibraries.size();
        }
    }

    public int getLoadedLibraryCount()
    {
        synchronized (mLibraries)
        {
            return mLoadedLibraries.size();
        }
    }

    public boolean isLibraryRoot(File aLibraryRoot)
    {
        synchronized (mLibraries)
        {
            return mLibraries.contains(aLibraryRoot);
        }
    }

    public LibraryState getLibraryState(File aLibraryRoot)
    {
        synchronized (mLibraries)
        {
            Library library = mLoadedLibraries.get(aLibraryRoot);
            if (library == null)
            {
                return LibraryState.UNLOADED;
            }
            return library == Library.EMPTY_LIBRARY ? LibraryState.LOADING : LibraryState.LOADED;
        }
    }

    public void loadLibrary(File aLibraryRoot)
    {
        synchronized (mLibraries)
        {
            mLibraries.add(aLibraryRoot);
            if (!mLoadedLibraries.containsKey(aLibraryRoot))
            {
                mLoadedLibraries.put(aLibraryRoot, Library.EMPTY_LIBRARY);
            }
            mLoadingQueue.offer(aLibraryRoot);
            mCachedTagCount = -1;
        }

        fireLibraryLoaded(Arrays.asList(aLibraryRoot));
    }

    public void unloadLibrary(List<File> aLibraryRoots)
    {
        synchronized (mLibraries)
        {
            Set<String> oldTags = getTagSet();
            List<File> removedFiles = new ArrayList<File>();
            for (File libraryRoot : aLibraryRoots)
            {
                // Remove library from loaded collection
                Library removedLibrary = mLoadedLibraries.remove(libraryRoot);
                if (removedLibrary != null)
                {
                    removedFiles.addAll(removedLibrary.getTaggedFiles());
                }
            }
            mCachedTagCount = -1;

            // fire tagged files removed
            fireTaggedFileRemoved(removedFiles);

            // fire tags removed
            oldTags.removeAll(getTagSet());
            if (oldTags.size() > 0)
            {
                fireTagRemoved(TagCategory.GLOBAL, new ArrayList<String>(oldTags));
            }

            // fire library unloaded
            fireLibraryUnloaded(aLibraryRoots);
        }
    }

    public void rebuildLibrary(File aLibraryRoot)
    {
        synchronized (mLibraries)
        {
            mRebuildingLibraries.add(aLibraryRoot);

            if (getLibraryState(aLibraryRoot) != LibraryState.UNLOADED)
            {
                loadLibrary(aLibraryRoot);
            }
        }
    }

    public void rebuildContainingLibrary(File aFile)
    {
        File sourceLibraryRoot = getLibraryRoot(aFile);
        if (sourceLibraryRoot != null)
        {
            rebuildLibrary(sourceLibraryRoot);
        }
    }

    private File getLibraryFile(File aLibraryRootDirectory)
    {
        return new File(mDataDirectory, aLibraryRootDirectory.getName() + ".library");
    }

    public void addLibraryEventListener(LibraryEventListener aListener)
    {
        mLibraryEventSupport.addEventListener(aListener);
    }

    public void removeLibraryEventListener(LibraryEventListener aListener)
    {
        mLibraryEventSupport.removeEventListener(aListener);
    }

    protected void fireLibraryAdded(List<File> aNewLibrary)
    {
        mLibraryEventSupport.fireEvent("libraryAdded", new LibraryEvent(this, aNewLibrary));
    }

    protected void fireLibraryRemoved(List<File> aRemovedLibrary)
    {
        mLibraryEventSupport.fireEvent("libraryRemoved", new LibraryEvent(this, aRemovedLibrary));
    }

    protected void fireLibraryLoaded(List<File> aNewLibrary)
    {
        mLibraryEventSupport.fireEvent("libraryLoaded", new LibraryEvent(this, aNewLibrary));
    }

    protected void fireLibraryUnloaded(List<File> aRemovedLibrary)
    {
        mLibraryEventSupport.fireEvent("libraryUnloaded", new LibraryEvent(this, aRemovedLibrary));
    }

    public void addLibraryProcessingEventListener(LibraryProcessingListener aListener)
    {
        mProcessingEventSupport.addEventListener(aListener);
    }

    public void removeLibraryProcessingEventListener(LibraryProcessingListener aListener)
    {
        mProcessingEventSupport.removeEventListener(aListener);
    }

    public void initializeStatisticsSnapshot(StatisticsSnapshot aSnapshot)
    {
        synchronized (mLibraries)
        {
            for (Map.Entry<File, Library> entry : mLoadedLibraries.entrySet())
            {
                Library value = entry.getValue();
                if (value != null && value != Library.EMPTY_LIBRARY)
                {
                    String libraryName = entry.getKey().getName();
                    aSnapshot.addLbrary(libraryName, value);
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // FileOperationListener

    public void fileRenamed(FileOperationEvent aEvent)
    {
        File source = aEvent.getSourceFile();
        File destination = aEvent.getDestinationFile();

        File sourceLibraryRoot = getLibraryRoot(source);
        if (sourceLibraryRoot != null)
        {
            // todo : more efficient directory refresh
            if (source.isDirectory())
            {
                rebuildLibrary(sourceLibraryRoot);
                return;
            }

            synchronized (mLibraries)
            {
                LibraryState libraryState = getLibraryState(sourceLibraryRoot);
                if (libraryState == LibraryState.LOADED)
                {
                    Library library = mLoadedLibraries.get(sourceLibraryRoot);
                    boolean removed = library.removeFile(source);
                    boolean added = library.processFile(destination);
                    if (removed || added)
                    {
                        library.storeLibrary(getLibraryFile(sourceLibraryRoot));
                        fireFileOperationUpdate(source, destination);
                    }
                }
                else
                {
                    rebuildLibrary(sourceLibraryRoot);
                }
            }
        }
    }

    public void fileCopied(FileOperationEvent aEvent)
    {
        File source = aEvent.getSourceFile();
        File destination = new File(aEvent.getDestinationFile(), source.getName());

        File destinationLibraryRoot = getLibraryRoot(destination);
        if (destinationLibraryRoot != null)
        {
            // todo : more efficient directory refresh
            if (source.isDirectory())
            {
                rebuildLibrary(destinationLibraryRoot);
                return;
            }

            synchronized (mLibraries)
            {
                LibraryState libraryState = getLibraryState(destination);
                if (libraryState == LibraryState.LOADED)
                {
                    Library library = mLoadedLibraries.get(destination);
                    if (library.getTaggedFiles().remove(source))
                    {
                        if (library.processFile(destination))
                        {
                            library.storeLibrary(getLibraryFile(destinationLibraryRoot));
                            fireFileOperationUpdate(null, destination);
                        }
                    }
                }
                else
                {
                    rebuildLibrary(destinationLibraryRoot);
                }
            }
        }
    }

    public void fileMoved(FileOperationEvent aEvent)
    {
        fileCopied(aEvent);
        fileDeleted(aEvent);
    }

    public void fileDeleted(FileOperationEvent aEvent)
    {
        File source = aEvent.getSourceFile();
        File sourceLibraryRoot = getLibraryRoot(source);
        if (sourceLibraryRoot != null)
        {
            // todo : more efficient directory refresh
            if (source.isDirectory())
            {
                rebuildLibrary(sourceLibraryRoot);
                return;
            }

            synchronized (mLibraries)
            {
                LibraryState libraryState = getLibraryState(sourceLibraryRoot);
                if (libraryState == LibraryState.LOADED)
                {
                    Library library = mLoadedLibraries.get(sourceLibraryRoot);
                    if (library.removeFile(source))
                    {
                        library.storeLibrary(getLibraryFile(sourceLibraryRoot));
                        fireFileOperationUpdate(source, null);
                    }
                }
                else
                {
                    rebuildLibrary(sourceLibraryRoot);
                }
            }
        }
    }

    private void fireFileOperationUpdate(File aRemovedFile, File aAddedFile)
    {
        if (aRemovedFile != null)
        {
            // Compute removed tags if any
            List<String> removedTags = new ArrayList<String>();
            String[] oldTags = Library.getTags(aRemovedFile);
            for (String tag : oldTags)
            {
                if (!containsTag(tag))
                {
                    removedTags.add(tag);
                }
            }

            if (removedTags.size() > 0)
            {
                fireTagRemoved(TagCategory.GLOBAL, removedTags);
            }

            fireTaggedFileRemoved(Arrays.asList(aRemovedFile));
        }

        if (aAddedFile != null)
        {
            // Assumes that all tag from added file are new and let the tag event listeners deal with the dups.
            String[] newTags = Library.getTags(aAddedFile);
            fireTagAdded(TagCategory.GLOBAL, Arrays.asList(newTags));

            fireTaggedFileAdded(Arrays.asList(aAddedFile));
        }
    }

    private File getLibraryRoot(File aFile)
    {
        File file = aFile.isFile() ? aFile.getParentFile() : aFile;
        while (file != null)
        {
            if (mLibraries.contains(file))
            {
                return file;
            }

            file = file.getParentFile();
        }

        return null;
    }

    private class LoadLibraryQueueConsumer extends FileBlockingQueueConsumer
    {
        public LoadLibraryQueueConsumer(BlockingQueue<File> aQueue)
        {
            super(aQueue);
        }

        protected void consume(File aLibraryRoot) throws IOException
        {
            Library newLibrary;
            Library oldLibrary;
            File libraryFile = getLibraryFile(aLibraryRoot);

            // If the library need to be rebuilded, delete the library file
            synchronized (mLibraries)
            {
                if (mRebuildingLibraries.remove(aLibraryRoot))
                {
                    libraryFile.delete();
                }
            }

            if (libraryFile.exists())
            {
                newLibrary = Library.loadLibrary(libraryFile);
            }
            else
            {
                LibraryProcessingEventSupport processingEventSupport =
                        new LibraryProcessingEventSupport(mProcessingEventSupport, aLibraryRoot);
                newLibrary = Library.createLibrary(aLibraryRoot, processingEventSupport);
                newLibrary.storeLibrary(libraryFile);
            }

            // Take a snapshot of the tagged files before adding the library to the loaded cache.
            // This will be used later to fire tagged file events.
            // No syncronization is required since no outsider can access it yet.
            SortedSet<File> newTaggedFiles = new TreeSet<File>(newLibrary.getTaggedFiles());

            synchronized (mLibraries)
            {
                // Well, the library been flaged for rebuild while loading/creating the library file, stop and requeue
                if (mRebuildingLibraries.contains(aLibraryRoot))
                {
                    mLoadingQueue.offer(aLibraryRoot);
                    return;
                }

                // Library is not loaded anymore, stop here
                if (!mLoadedLibraries.containsKey(aLibraryRoot))
                {
                    return;
                }

                Set<String> oldTags = getTagSet();
                oldLibrary = mLoadedLibraries.put(aLibraryRoot, newLibrary);
                mCachedTagCount = -1;

                // fire library loaded
                fireLibraryLoaded(Arrays.asList(new File[]{aLibraryRoot}));

                // Compute tags diff
                Set<String> newTags = getTagSet();
                newTags.removeAll(oldTags);
                oldTags.removeAll(getTagSet());

                if (oldTags.size() > 0)
                {
                    fireTagRemoved(TagCategory.GLOBAL, new ArrayList<String>(oldTags));
                }

                if (newTags.size() > 0)
                {
                    fireTagAdded(TagCategory.GLOBAL, new ArrayList<String>(newTags));
                }
            }

            // Compute tagged file diff. This is safe to access the old library without syncronization
            // since no outsider can access it anymore
            SortedSet<File> oldTaggedFiles = new TreeSet<File>();
            if (oldLibrary != null)
            {
                oldTaggedFiles.addAll(oldLibrary.getTaggedFiles());
                oldTaggedFiles.removeAll(newTaggedFiles);
                newTaggedFiles.removeAll(oldLibrary.getTaggedFiles());
            }

            if (oldTaggedFiles.size() > 0)
            {
                // fire tagged files removed events
                fireTaggedFileRemoved(new ArrayList<File>(oldTaggedFiles));
            }

            if (newTaggedFiles.size() > 0)
            {
                // fire tagged files added events
                fireTaggedFileAdded(new ArrayList<File>(newTaggedFiles));
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // Unsupported TagManager methods

    public List<String> getRecentsTags()
    {
        return new ArrayList<String>();
    }

    public int getRecentTagsCount()
    {
        return 0;
    }

    public boolean isRecentTag(String aTag)
    {
        return false;
    }
}
