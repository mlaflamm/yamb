package yamb.app.fileitem.filelist.details;

import java.text.DateFormat;
import java.util.Locale;
import javax.swing.SwingConstants;

public class FileDateColumn extends DetailsColumn
{
    protected final DateFormat dateFormat = DateFormat.getDateTimeInstance(
            DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault());

    public FileDateColumn(int preferedSize)
    {
        super("Modified", preferedSize, SwingConstants.LEADING);
    }

    ////////////////////////////////////////////////////////////////////////////
    // DetailsColumn class

    /**
     *
     */
    public Object getFormatedValue(Object value)
    {
        yamb.app.fileitem.filelist.FileListItem item = (yamb.app.fileitem.filelist.FileListItem) value;
        java.util.Date lastModified = item.getLastModified();
        return lastModified == null ? null : dateFormat.format(lastModified);
    }
}