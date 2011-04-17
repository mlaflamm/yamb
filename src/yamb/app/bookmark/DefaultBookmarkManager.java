package yamb.app.bookmark;

import yamb.util.event.SwingSafeEventSupport;
import yamb.util.io.TextDataSet;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Arrays;

/**
 * @author manuel.laflamme
 * @since 17-Oct-2008
 */
public class DefaultBookmarkManager implements BookmarkManager
{
    private static final Logger LOGGER = Logger.getLogger(DefaultBookmarkManager.class);

    private final SwingSafeEventSupport mEventSupport = new SwingSafeEventSupport();

    private final File mDatasetFile;
    private final TextDataSet mDataset;

    private List<Bookmark> mBookmarks = new ArrayList<Bookmark>();

    public DefaultBookmarkManager(File aDatasetFile) throws IOException
    {
        if (aDatasetFile.exists())
        {
            mDataset = new TextDataSet(new FileInputStream(aDatasetFile));
        }
        else
        {
            mDataset = new TextDataSet(new ByteArrayInputStream(new byte[0]));
        }
        mDatasetFile = aDatasetFile;

        List<String> values = mDataset.getValues();
        for (String value : values)
        {
            Bookmark bookmark = Bookmark.fromString(value);
            if (bookmark != null)
            {
                mBookmarks.add(bookmark);
            }
        }

    }

    public List<Bookmark> getBookmarks(BookmarkType aBookmarkType)
    {
        List<Bookmark> bookmarks = new ArrayList<Bookmark>(mBookmarks.size());
        for (Bookmark bookmark : mBookmarks)
        {
            if (aBookmarkType == bookmark.getType())
            {
                bookmarks.add(bookmark);
            }
        }
        Collections.sort(bookmarks);
        return bookmarks;
    }

    public List<Bookmark> getBookmarks()
    {
        return Collections.unmodifiableList(mBookmarks);
    }

    public void addBookmark(Bookmark aBookmark)
    {
        if (mBookmarks.add(aBookmark))
        {
            try
            {
                mDataset.add(aBookmark.toString());
                mDataset.write(new FileOutputStream(mDatasetFile));
            }
            catch (IOException e)
            {
                LOGGER.error("Cannot write bookmarks file", e);
                return;
            }

            fireBookmarkAdded(aBookmark);
        }
    }

    public void removeBookmark(Bookmark aBookmark)
    {
        if (mBookmarks.remove(aBookmark))
        {
            try
            {
                mDataset.remove(aBookmark.toString());
                mDataset.write(new FileOutputStream(mDatasetFile));
            }
            catch (IOException e)
            {
                LOGGER.error("Cannot write bookmarks file", e);
                return;
            }

            fireBookmarkRemoved(aBookmark);
        }
    }

    /* todo: reimplement more efficiently... */
    public void replaceBookmark(Bookmark aOldBookmark, Bookmark aNewBookmark)
    {
        boolean removed = mBookmarks.remove(aOldBookmark);
        boolean added = mBookmarks.add(aNewBookmark);
        if (removed || added)
        {
            try
            {
                mDataset.remove(aOldBookmark.toString());
                mDataset.add(aNewBookmark.toString());
                mDataset.write(new FileOutputStream(mDatasetFile));
            }
            catch (IOException e)
            {
                LOGGER.error("Cannot write bookmarks file", e);
                return;
            }

            if (removed)
            {
                fireBookmarkRemoved(aOldBookmark);
            }

            if (added)
            {
                fireBookmarkAdded(aNewBookmark);
            }
        }
    }

    public void addBookmarkEventListener(BookmarkEventListener aListener)
    {
        mEventSupport.addEventListener(aListener);
    }

    public void removeBookmarkEventListener(BookmarkEventListener aListener)
    {
        mEventSupport.removeEventListener(aListener);
    }

    protected void fireBookmarkAdded(Bookmark aAddedBookmark)
    {
        mEventSupport.fireEvent("bookmarkAdded", new BookmarkEvent(this, aAddedBookmark));
    }

    protected void fireBookmarkRemoved(Bookmark aRemovedBookmark)
    {
        mEventSupport.fireEvent("bookmarkRemoved", new BookmarkEvent(this, aRemovedBookmark));
    }
}
