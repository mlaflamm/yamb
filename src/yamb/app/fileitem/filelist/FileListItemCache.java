package yamb.app.fileitem.filelist;

import yamb.app.fileitem.FileItem;
import yamb.util.io.shell.FileOperation;
import yamb.util.io.shell.FileOperationEvent;
import yamb.util.io.shell.FileOperationListener;
import org.apache.commons.collections.map.LRUMap;
import sun.awt.shell.ShellFolder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FileListItemCache implements FileOperationListener
{
    private final Map<File, FileListItem> mLruCache = new LRUMap(512);

    private final FileOperation mFileOperation;

    public FileListItemCache(FileOperation aFileOperation)
    {
        mFileOperation = aFileOperation;
        mFileOperation.addFileOperationListener(this);
    }

    /**
     * Convert the specified list of File to a list of FileListItem.
     *
     * @param aFiles       the Files to convert
     * @param aSort        true if the resulting list must be sorted
     * @param aCanBeFolder If false all items will be flaged as files (i.e. FileListItem.isDirectory() will
     *                     always return false) and File.isDirectory() will NEVER be called. This is an optimization to prevent an extra
     *                     disk IO access. In doubt specify true.
     */
    public List<FileItem> createFileListItems(List<File> aFiles, boolean aSort, boolean aCanBeFolder)
    {
        // Perform sort if requested
        if (aSort)
        {
            sortFiles(aFiles, aCanBeFolder);
        }

        // Convert File objects to FileListItem objects
        List<FileItem> items = new ArrayList<FileItem>(aFiles.size());
        for (File file : aFiles)
        {
            FileListItem item = getItem(file, aCanBeFolder);
            if (item != null)
            {
                items.add(item);
            }
        }

        return items;
    }

    /**
     * Returns a cached item or creates a new one if not found. In either case the LRU cache is updated
     * but not the item cache.
     */
    public FileListItem getItem(File aFile, boolean aCanBeFolder)
    {
        synchronized (mLruCache)
        {
            // Not found in cache, try LRU cache
            FileListItem item = mLruCache.get(aFile);
            if (item != null && aFile.getName().equals(item.getFile().getName()))
            {
                return item;
            }

            // Not in either caches so creates a new item
            item = new FileListItem(aFile, aCanBeFolder, mFileOperation);
            {
                mLruCache.put(aFile, item);
                return item;
            }
        }
    }

    /**
     * Sort specified list of files. Put folders at the begining of the list.
     *
     * @param aFiles       list of files to sort
     * @param aCanBeFolder Specify true if the list can contain a directory or false if the list is known to not
     *                     contain any directory. A false value will prevent a call to File.isDirectory().
     */
    private void sortFiles(List<File> aFiles, boolean aCanBeFolder)
    {
        // No folder, simply perform the sort
        if (!aCanBeFolder)
        {
            ShellFolder.sortFiles(aFiles);
            return;
        }

        // No shortcut possible, must verify if each file is a folder
        List<File> fileList = new ArrayList<File>(aFiles.size());
        List<File> folderList = new ArrayList<File>(aFiles.size());

        // Separate folders from files to sort them seperatly
        for (File file : aFiles)
        {
            FileListItem item = getItem(file, aCanBeFolder);
            if (item != null)
            {
                if (item.isDirectory())
                {
                    folderList.add(file);
                }
                else
                {
                    fileList.add(file);
                }
            }
        }

        ShellFolder.sortFiles(folderList);
        ShellFolder.sortFiles(fileList);

        // Copy items in the right order in the original list
        List<File> fullList = new ArrayList<File>(aFiles.size());
        fullList.addAll(folderList);
        fullList.addAll(fileList);

        for (int i = 0; i < aFiles.size(); i++)
        {
            aFiles.set(i, fullList.get(i));
        }
    }

    public void fileRenamed(FileOperationEvent aEvent)
    {
        synchronized (mLruCache)
        {
            mLruCache.remove(aEvent.getSourceFile());
        }
    }

    public void fileCopied(FileOperationEvent aEvent)
    {
        // No op
    }

    public void fileMoved(FileOperationEvent aEvent)
    {
        synchronized (mLruCache)
        {
//            mItemCache.remove(aEvent.getSource());
            mLruCache.remove(aEvent.getSourceFile());
        }
    }

    public void fileDeleted(FileOperationEvent aEvent)
    {
        synchronized (mLruCache)
        {
//            mItemCache.remove(aEvent.getSource());
            mLruCache.remove(aEvent.getSourceFile());
        }
    }
}
