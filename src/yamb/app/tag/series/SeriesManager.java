package yamb.app.tag.series;

import java.io.File;
import java.util.List;

/**
 * @author manuel.laflamme
 * @since Aug 27, 2008
 */
public interface SeriesManager
{
    List<String> getSeriesNames();

    List<File> getSeriesFiles(String aSeriesName);

    void addSeriesEventListener(SeriesEventListener aListener);

    void removeSeriesEventListener(SeriesEventListener aListener);

    int getSeriesNameCount();
}
