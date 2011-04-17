package yamb.app.tag.series;

import yamb.app.tag.TagManager;
import yamb.app.tag.TaggedFileEvent;
import yamb.app.tag.TaggedFileEventListener;
import yamb.app.tag.library.StatisticsSnapshot;
import yamb.util.Disposable;
import yamb.util.event.SwingSafeEventSupport;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author manuel.laflamme
 * @since Aug 8, 2008
 */
public class DefaultSeriesManager implements TaggedFileEventListener, Disposable, SeriesManager
{
    private final SwingSafeEventSupport mSeriesEventSupport = new SwingSafeEventSupport();
    private final TagManager mTagManager;
    private final Map<String, List<File>> mSeriesCache = new TreeMap<String, List<File>>();

    public DefaultSeriesManager(TagManager aTagManager)
    {
        mTagManager = aTagManager;
        mTagManager.addTaggedFileEventListener(this);
        addTaggedFiles(mTagManager.getTaggedFiles());
    }

    public List<String> getSeriesNames()
    {
        return new ArrayList<String>(mSeriesCache.keySet());
    }

    public List<File> getSeriesFiles(String aSeriesName)
    {
        List<File> SeriesFiles = new ArrayList<File>();
        if (mSeriesCache.containsKey(aSeriesName))
        {
            SeriesFiles.addAll(mSeriesCache.get(aSeriesName));
        }
//        Collections.sort(SeriesFiles, Files.FILE_NAMECOMPARATOR);
        return SeriesFiles;
    }

    public void addSeriesEventListener(SeriesEventListener aListener)
    {
        mSeriesEventSupport.addEventListener(aListener);
    }

    public void removeSeriesEventListener(SeriesEventListener aListener)
    {
        mSeriesEventSupport.removeEventListener(aListener);
    }

    private void fireSeriesAdded(List<String> aSeriesNames)
    {
        mSeriesEventSupport.fireEvent("seriesAdded", new SeriesEvent(this, aSeriesNames));
    }

    private void fireSeriesRemoved(List<String> aSeriesNames)
    {
        mSeriesEventSupport.fireEvent("seriesRemoved", new SeriesEvent(this, aSeriesNames));
    }

    private void addTaggedFiles(List<File> aFiles)
    {
        List<String> addedSeriesNames = null;
        for (File file : aFiles)
        {
            String seriesName = getSeriesName(file);
            List<File> seriesFiles = mSeriesCache.get(seriesName);
            if (seriesFiles == null)
            {
                seriesFiles = new ArrayList<File>();
                mSeriesCache.put(seriesName, seriesFiles);

                if (addedSeriesNames == null)
                {
                    addedSeriesNames = new ArrayList<String>(aFiles.size());
                }
                addedSeriesNames.add(seriesName);
            }
            seriesFiles.add(file);
        }

        if (addedSeriesNames != null && addedSeriesNames.size() > 0)
        {
            fireSeriesAdded(addedSeriesNames);
        }
    }

    private String getSeriesName(File aFile)
    {
        return Series.getSeriesName(aFile.getName());
    }

    //////////////////////////////////////////////////////////////////////////////////////////////
    // Disposable interface

    public void dispose()
    {
        mTagManager.removeTaggedFileEventListener(this);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////
    // TaggedFileEventListener interface

    public void taggedFileAdded(TaggedFileEvent aEvent)
    {
        addTaggedFiles(aEvent.getFiles());
    }

    public void taggedFileRemoved(TaggedFileEvent aEvent)
    {
        List<String> removedSeriesNames = null;
        for (File file : aEvent.getFiles())
        {
            String seriesName = getSeriesName(file);
            List<File> seriesFiles = mSeriesCache.get(seriesName);
            if (seriesFiles != null)
            {
                seriesFiles.remove(file);
                if (seriesFiles.isEmpty())
                {
                    mSeriesCache.remove(seriesName);

                    if (removedSeriesNames == null)
                    {
                        removedSeriesNames = new ArrayList<String>();
                    }
                    removedSeriesNames.add(seriesName);
                }
            }
        }

        if (removedSeriesNames != null && removedSeriesNames.size() > 0)
        {
            fireSeriesRemoved(removedSeriesNames);
        }
    }

    public int getSeriesNameCount()
    {
        return mSeriesCache.size();
    }

    public void initializeStatisticsSnapshot(StatisticsSnapshot aSnapshot)
    {
        aSnapshot.setSeriesStatisticsData(mSeriesCache);
    }
}
