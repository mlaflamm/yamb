package yamb.app.fileitem.filelist.thumbnail;

import java.io.File;
import java.util.EventObject;
import javax.swing.Icon;

/**
 * @author manuel.laflamme
 * @since 1-Feb-2008
 */
public class ThumbnailEvent extends EventObject
{
    private final File mFile;
    private final Icon mThumbnail;

    public ThumbnailEvent(Object aSource, File aFile, Icon aThumbnail)
    {
        super(aSource);
        mFile = aFile;
        mThumbnail = aThumbnail;
    }

    public File getFile()
    {
        return mFile;
    }

    public Icon getThumbnail()
    {
        return mThumbnail;
    }
}
