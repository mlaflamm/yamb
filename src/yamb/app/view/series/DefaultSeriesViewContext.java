package yamb.app.view.series;

import yamb.app.tag.series.SeriesManager;
import yamb.app.view.AbstractFileListViewContext;

/**
 * @author manuel.laflamme
 * @since Aug 8, 2008
 */
public class DefaultSeriesViewContext extends AbstractFileListViewContext implements SeriesViewContext
{
    private final SeriesManager mSeriesManager;
    private String mSelectedSeriesName;

    public DefaultSeriesViewContext(SeriesManager aSeriesManager)
    {
        mSeriesManager = aSeriesManager;
    }

    public String getSelectedSeriesName()
    {
        return mSelectedSeriesName;
    }

    public void setSelectedSeriesName(String aSelectedSeriesName)
    {
        String oldValue = mSelectedSeriesName;
        String newValue = aSelectedSeriesName;

        mSelectedSeriesName = aSelectedSeriesName;

        // Notify listeners of list view mode change
        mPropertySupport.firePropertyChange(SELECTED_SERIES, oldValue, newValue);
    }

    public SeriesManager getSeriesManager()
    {
        return mSeriesManager;
    }
}
