package yamb.app.fileitem.filelist.details;

import yamb.app.fileitem.filelist.FileListItem;

import javax.swing.SwingConstants;

public class FileTypeColumn extends DetailsColumn
{
    public FileTypeColumn(int preferedSize)
    {
        super("Type", preferedSize, SwingConstants.LEADING);
    }

    ////////////////////////////////////////////////////////////////////////////
    // DetailsColumn class

    /**
     *
     */
    public Object getFormatedValue(Object value)
    {
        FileListItem item = (FileListItem) value;
        return item.getType();
    }
}