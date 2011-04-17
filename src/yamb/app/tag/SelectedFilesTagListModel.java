package yamb.app.tag;

import yamb.app.fileitem.filelist.FileListSelectionManager;
import yamb.util.Disposable;

import java.io.File;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.AbstractListModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * @author manuel.laflamme
 * @since Feb 17, 2008
 */
public class SelectedFilesTagListModel extends AbstractListModel implements ListSelectionListener, Disposable
{
    private final TagContext mTagContext;
    private final FileListSelectionManager mFileListSelectionManager;

    private String[] mTags;

    public SelectedFilesTagListModel(TagContext aTagContext, FileListSelectionManager aFileListSelectionManager)
    {
        mTagContext = aTagContext;
        mFileListSelectionManager = aFileListSelectionManager;
        mFileListSelectionManager.addListSelectionListener(this);

        mTags = createSelectedFilesTags();
    }

    private String[] createSelectedFilesTags()
    {
        SortedSet<String> tags = new TreeSet<String>();

        List<File> selectedFiles = mFileListSelectionManager.getSelectedFiles();
        for (File file : selectedFiles)
        {
            tags.addAll(mTagContext.getFileTags(file));
        }

        return tags.toArray(new String[0]);
    }

    ////////////////////////////////////////////////////////////////////////////////
    // ListModel interface

    public int getSize()
    {
        return mTags.length;
    }

    public Object getElementAt(int aIndex)
    {
        return mTags[aIndex];
    }

    ////////////////////////////////////////////////////////////////////////////////
    // ListSelectionListener interface

    public void valueChanged(ListSelectionEvent aEvent)
    {

        int oldSize = mTags.length;
        mTags = createSelectedFilesTags();
        int newSize = mTags.length;
/*
        fireIntervalRemoved(this, 0, oldSize);
        fireIntervalAdded(this, 0, newSize);
*/
        if (oldSize > newSize)
        {
            fireIntervalRemoved(this, 0, oldSize - newSize);
            fireContentsChanged(this, 0, newSize);
        }
        else if (newSize > oldSize)
        {
            fireIntervalAdded(this, 0, newSize - oldSize);
            fireContentsChanged(this, 0, oldSize);
        }
        else
        {
            fireContentsChanged(this, 0, Math.min(oldSize, newSize));
        }
    }

    ////////////////////////////////////////////////////////////////////////////////
    // Disposable interface

    public void dispose()
    {
        mFileListSelectionManager.removeListSelectionListener(this);
    }

}
