package yamb.app.fileitem.folders;

import yamb.app.tag.TagManager;
import yamb.app.tag.TaggedFileEvent;
import yamb.app.tag.TaggedFileEventListener;
import yamb.util.Disposable;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractListModel;

/**
 * @author manuel.laflamme
 * @since 25-Feb-2008
 */
public class RecentFolderListModel extends AbstractListModel implements TaggedFileEventListener, Disposable
{
    private static final Logger LOGGER = Logger.getLogger(RecentFolderListModel.class);

    private final List<File> mRecentFolders = new ArrayList<File>();
    private final TagManager mTagManager;

    public RecentFolderListModel(TagManager aTagManager)
    {
        mTagManager = aTagManager;
        List<File> taggedDirectories = new ArrayList<File>(getTaggedDirectories(aTagManager.getTaggedFiles()));
        for (File folder : taggedDirectories)
        {
            if (isRecentDirectory(folder))
            {
                mRecentFolders.add(folder);
            }
        }
        Collections.sort(mRecentFolders);

        mTagManager.addTaggedFileEventListener(this);
    }

    private boolean isRecentDirectory(File aDirectory)
    {
        String absolutePath = aDirectory.getAbsolutePath();
        return absolutePath.endsWith("\\New") ||
                absolutePath.indexOf("\\New\\") != -1 /*||
                absolutePath.indexOf("newsleecher") != -1*/;
    }

    public int getSize()
    {
        return mRecentFolders.size();
    }

    public Object getElementAt(int aIndex)
    {
        return mRecentFolders.get(aIndex);
    }

    public void dispose()
    {
        mTagManager.removeTaggedFileEventListener(this);
    }

    public void taggedFileAdded(TaggedFileEvent aEvent)
    {
        for (File directory : getTaggedDirectories(aEvent.getFiles()))
        {
            if (isRecentDirectory(directory))
            {
                int index = Collections.binarySearch(mRecentFolders, directory);
                // Not found, add it into list
                if (index < 0)
                {
                    mRecentFolders.add(-(index + 1), directory);
                    fireIntervalAdded(this, -(index + 1), -(index + 1));
                }
                // Found! This should not occurs!
                else
                {
                    LOGGER.warn("Tagged folder '" + directory + "' already in list");
                }
            }
        }
    }

    public void taggedFileRemoved(TaggedFileEvent aEvent)
    {
        for (File directory : getTaggedDirectories(aEvent.getFiles()))
        {
            if (isRecentDirectory(directory))
            {
                int index = Collections.binarySearch(mRecentFolders, directory);
                // Found, remove it from list
                if (index >= 0)
                {
                    mRecentFolders.remove(index);
                    fireIntervalRemoved(this, index, index);
                }
                // Not found! This should not occurs!
                else
                {
                    LOGGER.warn("Tagged folder '" + directory + "' not in list");
                }
            }
        }
    }

    private Set<File> getTaggedDirectories(List<File> aTaggedFiles)
    {
        Set<File> taggedDirectories = new HashSet<File>();
        for (File file : aTaggedFiles)
        {
            taggedDirectories.add(file.getParentFile());
        }
        return taggedDirectories;
    }

}
