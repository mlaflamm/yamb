package yamb.app.fileitem;

import java.io.File;
import java.util.Date;
import javax.swing.ImageIcon;

/**
 * @author manuel.laflamme
 * @since Feb 17, 2008
 */
public interface FileItem
{
    File getFile();

    /**
     * Returns this file SMALL or LARGE icon. Large icon is not cached.
     *
     * @param aIconType
     */
    ImageIcon getIcon(FileIconType aIconType);

    long getSize();

    boolean isDirectory();

    Date getLastModified();

    String getType();
}
