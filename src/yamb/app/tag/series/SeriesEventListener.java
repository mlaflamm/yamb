package yamb.app.tag.series;

import java.util.EventListener;

/**
 * @author manuel.laflamme
 * @since Aug 8, 2008
 */
public interface SeriesEventListener extends EventListener
{
    public void seriesAdded(SeriesEvent aEvent);

    public void seriesRemoved(SeriesEvent aEvent);
}
