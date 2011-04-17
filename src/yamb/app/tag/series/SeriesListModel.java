package yamb.app.tag.series;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.AbstractListModel;

/**
 * @author manuel.laflamme
 * @since Aug 8, 2008
 */
public class SeriesListModel extends AbstractListModel implements SeriesEventListener
{
    private static final Logger LOGGER = Logger.getLogger(SeriesListModel.class);

    private final List<String> mSeriesNames;
    private final SeriesManager mSeriesManager;

    public SeriesListModel(SeriesManager aSeriesManager)
    {
        mSeriesManager = aSeriesManager;
        mSeriesNames = new ArrayList<String>(aSeriesManager.getSeriesNames());
        Collections.sort(mSeriesNames);

        mSeriesManager.addSeriesEventListener(this);
    }

    public int getSize()
    {
        return mSeriesNames.size();
    }

    public Object getElementAt(int aIndex)
    {
        return mSeriesNames.get(aIndex);
    }

    public void dispose()
    {
        mSeriesManager.removeSeriesEventListener(this);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // SeriesEventListener interface

    public void seriesAdded(SeriesEvent aEvent)
    {
        for (String seriesName : aEvent.getSeriesNames())
        {
            int index = Collections.binarySearch(mSeriesNames, seriesName);
            // Not found, add it into list
            if (index < 0)
            {
                mSeriesNames.add(-(index + 1), seriesName);
                fireIntervalAdded(this, -(index + 1), -(index + 1));
            }
            // Found! This should not occurs!
            else
            {
                LOGGER.warn("Series '" + seriesName + "' already in list");
            }
        }
    }

    public void seriesRemoved(SeriesEvent aEvent)
    {
        for (String seriesName : aEvent.getSeriesNames())
        {
            int index = Collections.binarySearch(mSeriesNames, seriesName);
            if (index >= 0)
            {
                mSeriesNames.remove(index);
                fireIntervalRemoved(this, index, index);
            }
            // Found! This should not occurs!
            else
            {
                LOGGER.warn("Removed series '" + seriesName + "' not found in list");
            }
        }
    }
}
