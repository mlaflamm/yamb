package yamb.app.tag.series;

import java.util.EventObject;
import java.util.List;

/**
 * @author manuel.laflamme
 * @since Aug 8, 2008
 */
public class SeriesEvent extends EventObject
{
    private final List<String> mSeriesNames;

    public SeriesEvent(Object aSource, List<String> aSeriesNames)
    {
        super(aSource);
        mSeriesNames = aSeriesNames;
    }

    public List<String> getSeriesNames()
    {
        return mSeriesNames;
    }
}
