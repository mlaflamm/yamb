package yamb.app.fileitem.filelist.details;

import javax.swing.SwingConstants;

public class FileNameColumn extends DetailsColumn
{
    public FileNameColumn(int preferedSize)
    {
        super("Name", preferedSize, SwingConstants.LEADING);
    }

    ////////////////////////////////////////////////////////////////////////////
    // DetailsColumn class

    /**
     *
     */
    public Object getFormatedValue(Object value)
    {
        yamb.app.fileitem.filelist.FileListItem item = (yamb.app.fileitem.filelist.FileListItem) value;
        return value.toString();
    }
}