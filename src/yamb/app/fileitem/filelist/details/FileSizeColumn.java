package yamb.app.fileitem.filelist.details;

import javax.swing.SwingConstants;

public class FileSizeColumn extends DetailsColumn
{
    public FileSizeColumn(int preferedSize)
    {
        super("Size", preferedSize, SwingConstants.TRAILING);
    }

    ////////////////////////////////////////////////////////////////////////////
    // DetailsColumn class

    /**
     *
     */
    public Object getFormatedValue(Object value)
    {
        yamb.app.fileitem.filelist.FileListItem item = (yamb.app.fileitem.filelist.FileListItem) value;
        if (item.getFile().isDirectory())
        {
            return null;
        }
        return (item.getSize() / 1024 + 1) + "KB";
    }
}