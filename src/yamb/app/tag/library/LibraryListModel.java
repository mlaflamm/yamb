package yamb.app.tag.library;

import yamb.util.Disposable;
import yamb.util.io.Files;

import java.io.File;
import java.util.Collections;
import java.util.List;
import javax.swing.AbstractListModel;

/**
 * @author manuel.laflamme
 * @since Apr 12, 2008
 */
public class LibraryListModel extends AbstractListModel implements LibraryEventListener, Disposable
{
    private final List<File> mLibraries;
    private final LibraryManager mTagManager;

    public LibraryListModel(LibraryManager aTagManager)
    {
        mTagManager = aTagManager;
        mLibraries = mTagManager.getLibraryRoots();
        Collections.sort(mLibraries, Files.FILE_NAMECOMPARATOR);
        mTagManager.addLibraryEventListener(this);
    }

    public int getSize()
    {
        return mLibraries.size();
    }

    public Object getElementAt(int aIndex)
    {
        return mLibraries.get(aIndex);
    }

    public void dispose()
    {
        mTagManager.removeLibraryEventListener(this);
    }

    public void libraryLoaded(LibraryEvent aEvent)
    {
        fireLoadedLibraryChange(aEvent);
    }

    public void libraryUnloaded(LibraryEvent aEvent)
    {
        fireLoadedLibraryChange(aEvent);
    }

    private void fireLoadedLibraryChange(LibraryEvent aEvent)
    {
        for (File library : aEvent.getLibraries())
        {
            int index = Collections.binarySearch(mLibraries, library, Files.FILE_NAMECOMPARATOR);
            // Found, send update
            if (index > -1)
            {
                fireContentsChanged(this, index, index);
            }
        }
    }

    public void libraryAdded(LibraryEvent aEvent)
    {
        for (File library : aEvent.getLibraries())
        {
            int index = Collections.binarySearch(mLibraries, library, Files.FILE_NAMECOMPARATOR);
            if (index < 0)
            {
                mLibraries.add(-(index+1), library);
                fireIntervalAdded(this, -(index+1), -(index+1));
            }
        }
    }

    public void libraryRemoved(LibraryEvent aEvent)
    {
        for (File library : aEvent.getLibraries())
        {
            int index = Collections.binarySearch(mLibraries, library, Files.FILE_NAMECOMPARATOR);
            if (index > -1)
            {
                mLibraries.remove(index);
                fireIntervalRemoved(this, index, index);
            }
        }
    }
}
