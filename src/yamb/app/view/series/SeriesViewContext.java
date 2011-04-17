package yamb.app.view.series;

import yamb.app.tag.series.SeriesManager;
import yamb.app.view.FileListViewContext;

/**
 * @author manuel.laflamme
 * @since Aug 8, 2008
 */
public interface SeriesViewContext extends FileListViewContext
{
    String SELECTED_SERIES = "yamb.groupViewContext.selectedGroupNames";

    public String getSelectedSeriesName();

    public void setSelectedSeriesName(String aSeries);

    public SeriesManager getSeriesManager();
}
