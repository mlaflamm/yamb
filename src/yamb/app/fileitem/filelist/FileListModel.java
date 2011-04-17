package yamb.app.fileitem.filelist;

import yamb.app.fileitem.FileItem;
import yamb.app.fileitem.filelist.thumbnail.LocalThumbnailCache;
import yamb.app.fileitem.filelist.thumbnail.ThumbnailEvent;
import yamb.app.fileitem.filelist.thumbnail.ThumbnailEventListener;
import yamb.util.Disposable;
import yamb.util.io.CompositeFileFilter;
import yamb.util.io.Files;
import yamb.util.io.shell.FileOperation;
import yamb.util.io.shell.FileOperationEvent;
import yamb.util.io.shell.FileOperationListener;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.AbstractListModel;

public class FileListModel extends AbstractListModel implements Disposable, ThumbnailEventListener, FileOperationListener
{
    private final LocalThumbnailCache mThumbnailCache;
    private final FileListItemCache mItemCache;
    private final FileOperation mFileOperation;

    private final File mParentFolder;
    private List<FileItem> mItems = null;

    public FileListModel(File aParentFolder, FileFilter aFileFilter, FileListItemCache aFileListItemCache,
            LocalThumbnailCache aThumbnailCache, FileOperation aFileOperation, boolean aRecursive)
    {
        mItemCache = aFileListItemCache;
        mThumbnailCache = aThumbnailCache;
        mFileOperation = aFileOperation;

        mParentFolder = aParentFolder;
        File[] files = null;
        if (aRecursive)
        {
            files = Files.getRecursiveChildren(aParentFolder, new CompositeFileFilter(aFileFilter, new FileFilter()
            {
                public boolean accept(File aPathname)
                {
                    return aPathname.isFile();
                }
            }));
            Arrays.sort(files, Files.FILE_NAMECOMPARATOR);
            mItems = mItemCache.createFileListItems(Arrays.asList(files), false, false);
        }
        else
        {
            files = Files.getChildren(aParentFolder, aFileFilter);
            if (files != null)
            {
                mItems = mItemCache.createFileListItems(Arrays.asList(files), true, true);
            }
            // Parent does not exist anymore on file system
            else
            {
                mItems = new ArrayList<FileItem>();
            }
        }

        mFileOperation.addFileOperationListener(this);

        mThumbnailCache.addThumbnailEventListener(this);
        mThumbnailCache.clear();
        mThumbnailCache.queueThumbnailUpdate(mItems);
    }

    public FileListModel(File[] aFiles, FileListItemCache aFileListItemCache, LocalThumbnailCache aThumbnailCache,
            FileOperation aFileOperation, boolean aSort, boolean aCanContainsFolder)
    {
        mItemCache = aFileListItemCache;
        mThumbnailCache = aThumbnailCache;
        mFileOperation = aFileOperation;

        mParentFolder = null;
        mItems = mItemCache.createFileListItems(Arrays.asList(aFiles), aSort, aCanContainsFolder);

        mFileOperation.addFileOperationListener(this);

        mThumbnailCache.addThumbnailEventListener(this);
        mThumbnailCache.clear();
        mThumbnailCache.queueThumbnailUpdate(mItems);
    }

    ////////////////////////////////////////////////////////////////////////////
    // ListModel interface

    public int getSize()
    {
        return mItems.size();
    }

    public Object getElementAt(int aIndex)
    {
        return mItems.get(aIndex);
    }

    ////////////////////////////////////////////////////////////////////////////
    // FileOperationListener interface


    public void fileRenamed(FileOperationEvent aEvent)
    {
        // If source is in this list, replace it by destination.
        for (int i = 0; i < mItems.size(); i++)
        {
            FileItem item = mItems.get(i);
            if (item.getFile().equals(aEvent.getSourceFile()))
            {
                FileListItem newItem = mItemCache.getItem(aEvent.getDestinationFile(), item.isDirectory());
                mItems.set(i, newItem);
                fireContentsChanged(this, i, i);
                mThumbnailCache.queueThumbnailUpdate(newItem);
                return;
            }
        }
    }

    public void fileMoved(FileOperationEvent aEvent)
    {
        // Process source as a delete
        fileDeleted(aEvent);

        // Process destination as copy
        fileCopied(aEvent);
    }

    public void fileDeleted(FileOperationEvent aEvent)
    {
        // If source is in this list, remove it.
        for (int i = 0; i < mItems.size(); i++)
        {
            FileItem item = mItems.get(i);
            if (item.getFile().equals(aEvent.getSourceFile()))
            {
                mItems.remove(i);
                fireIntervalRemoved(this, i, i);
                return;
            }
        }
    }

    public void fileCopied(FileOperationEvent aEvent)
    {
        // If destination is parent of files in this list, add it at the end of the list
        if (mParentFolder != null && mParentFolder.equals(aEvent.getDestinationFile()))
        {
            FileListItem newItem = mItemCache.getItem(new File(mParentFolder, aEvent.getSourceFile().getName()), true);
            mItems.add(newItem);
            fireIntervalAdded(this, mItems.size() - 1, mItems.size() - 1);
            mThumbnailCache.queueThumbnailUpdate(newItem);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Disposable interface

    public void dispose()
    {
        mThumbnailCache.removeThumbnailEventListener(this);
        mFileOperation.removeFileOperationListener(this);

    }

    ////////////////////////////////////////////////////////////////////////////
    // ThumbnailListener interface

    public void thumbnailUpdated(ThumbnailEvent aEvent)
    {
        File mediaFile = aEvent.getFile();
        for (int i = 0; i < mItems.size(); i++)
        {
            FileItem item = mItems.get(i);
            if (mediaFile.equals(item.getFile()))
            {
                fireContentsChanged(this, i, i);
                break;
            }
        }
    }
}