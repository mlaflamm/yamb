package yamb.app.view;

import yamb.app.view.explorer.ActiveFolder;

import java.util.List;

/**
 * @author manuel.laflamme
 * @since Aug 7, 2008
 */
public interface ViewFactory
{
    ViewContext createExploreView(ActiveFolder aActiveFolder);

    ViewContext createTagView(List<String> aActiveTags);

    ViewContext createSeriesView(String aSelectedSeriesName);

    ViewContext createStatisticsView();
}
