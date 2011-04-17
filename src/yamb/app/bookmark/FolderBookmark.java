package yamb.app.bookmark;

import java.io.File;

/**
 * @author manuel.laflamme
 * @since Mar 21, 2009
 */
public class FolderBookmark extends Bookmark
{
    public FolderBookmark(File aFolder)
    {
        super(BookmarkType.FOLDER, aFolder.getAbsolutePath());
    }

    FolderBookmark(String aValue)
    {
        super(BookmarkType.FOLDER, aValue);
    }
}
