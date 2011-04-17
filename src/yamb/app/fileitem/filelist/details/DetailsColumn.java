package yamb.app.fileitem.filelist.details;

public abstract class DetailsColumn
{
    public static final DetailsColumn FILE_NAME = new FileNameColumn(150);
    public static final DetailsColumn FILE_SIZE = new FileSizeColumn(75);
    public static final DetailsColumn FILE_TYPE = new FileTypeColumn(130);
    public static final DetailsColumn FILE_DATE = new FileDateColumn(130);

    static final DetailsColumn[] COLUMNS = {
            FILE_NAME,
            FILE_SIZE,
            FILE_TYPE,
            FILE_DATE
    };

    private final String _name;
    private final int _preferedSize;
    private final int _horizontalAlignment;

    public DetailsColumn(String name, int preferedSize, int horizontalAlignment)
    {
        _name = name;
        _preferedSize = preferedSize;
        _horizontalAlignment = horizontalAlignment;
    }

    public String getName()
    {
        return _name;
    }

    public int getPreferedSize()
    {
        return _preferedSize;
    }

    public int getHorizontalAlignment()
    {
        return _horizontalAlignment;
    }

    public abstract Object getFormatedValue(Object value);

}