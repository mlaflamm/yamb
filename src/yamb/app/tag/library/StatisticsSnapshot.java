package yamb.app.tag.library;

import yamb.app.tag.TagManager;
import yamb.app.tag.series.DefaultSeriesManager;
import yamb.app.tag.series.SeriesManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author manuel.laflamme
 * @since 23-Aug-2008
 */
public class StatisticsSnapshot
{
    private final Map<String, TagStat> mTagStatistics = new HashMap<String, TagStat>();

    public static final Comparator<TagStat> TAG_FILECOUNT_COMPARATOR = new Comparator<TagStat>()
    {
        public int compare(TagStat o1, TagStat o2)
        {
            int o1Total = o1.getFileCount();
            int o2Total = o2.getFileCount();
            if (o1Total == o2Total)
            {
                return o1.getTagName().compareTo(o2.getTagName());
            }

            return o1Total > o2Total ? -1 : 1;
        }
    };

    public static final Comparator<TagStat> TAG_LIBCOUNT_COMPARATOR = new Comparator<TagStat>()
    {
        public int compare(TagStat o1, TagStat o2)
        {
            int o1Total = o1.getLibraryCount();
            int o2Total = o2.getLibraryCount();
            if (o1Total == o2Total)
            {
                return o1.getTagName().compareTo(o2.getTagName());
            }

            return o1Total > o2Total ? -1 : 1;
        }
    };

    private final Map<String, LibStat> mLibStatistics = new HashMap<String, LibStat>();

    public static final Comparator<LibStat> LIB_FILECOUNT_COMPARATOR = new Comparator<LibStat>()
    {
        public int compare(LibStat o1, LibStat o2)
        {
            int o1Total = o1.getTaggedFileCount();
            int o2Total = o2.getTaggedFileCount();
            if (o1Total == o2Total)
            {
                return o1.getLibraryName().compareTo(o2.getLibraryName());
            }

            return o1Total > o2Total ? -1 : 1;
        }
    };

    public static final Comparator<LibStat> LIB_TAGCOUNT_COMPARATOR = new Comparator<LibStat>()
    {
        public int compare(LibStat o1, LibStat o2)
        {
            int o1Total = o1.getTagCount();
            int o2Total = o2.getTagCount();
            if (o1Total == o2Total)
            {
                return o1.getLibraryName().compareTo(o2.getLibraryName());
            }

            return o1Total > o2Total ? -1 : 1;
        }
    };

    private final Map<String, SeriesStat> mSeriesStatistics = new HashMap<String, SeriesStat>();

    public static final Comparator<SeriesStat> SERIES_FILECOUNT_COMPARATOR = new Comparator<SeriesStat>()
    {
        public int compare(SeriesStat aO1, SeriesStat aO2)
        {
            int o1Total = aO1.getFileCount();
            int o2Total = aO2.getFileCount();
            if (o1Total == o2Total)
            {
                return aO1.getSeriesName().compareTo(aO2.getSeriesName());
            }

            return o1Total > o2Total ? -1 : 1;
        }
    };

    public void setSeriesStatisticsData(Map<String, List<File>> aSeriesCache)
    {
        for (Map.Entry<String, List<File>> entry : aSeriesCache.entrySet())
        {
            SeriesStat stat = new SeriesStat(entry.getKey(), entry.getValue().size());
            mSeriesStatistics.put(stat.getSeriesName(), stat);
        }
    }

    private StatisticsSnapshot()
    {
    }

    public static StatisticsSnapshot createSnapshot(TagManager aTagManager, LibraryManager aLibraryManager, SeriesManager aSeriesManager)
    {
        StatisticsSnapshot snapshot = new StatisticsSnapshot();
        if (aLibraryManager instanceof TagLibraryManager)
        {
            ((TagLibraryManager) aLibraryManager).initializeStatisticsSnapshot(snapshot);
        }
        if (aSeriesManager instanceof DefaultSeriesManager)
        {
            ((DefaultSeriesManager) aSeriesManager).initializeStatisticsSnapshot(snapshot);
        }
        return snapshot;
    }

    public void addLbrary(String aLibraryName, Library aLibrary)
    {
        // tag statistics
        for (String tag : aLibrary.getTags())
        {
            TagStat stat = mTagStatistics.get(tag);
            if (stat == null)
            {
                stat = new TagStat(tag);
                mTagStatistics.put(tag, stat);
            }

            Set<File> files = aLibrary.getFiles(tag);
            stat.mFileCount += files.size();
            stat.mLibraryCount += 1;
        }

        // library statistics
        LibStat libStat = new LibStat(aLibraryName);
        libStat.mTagCount = aLibrary.getTags().size();
        libStat.mTaggedFileCount = aLibrary.getTaggedFiles().size();
        mLibStatistics.put(aLibraryName, libStat);

    }

    public List<TagStat> getTagStatistics()
    {
        List<TagStat> tagStats = new ArrayList<TagStat>(mTagStatistics.values());
        return tagStats;
    }

    static public class TagStat
    {
        final String mTagName;
        int mFileCount = 0;
        int mLibraryCount = 0;

        public TagStat(String aTagName)
        {
            mTagName = aTagName;
        }

        public String getTagName()
        {
            return mTagName;
        }

        public int getFileCount()
        {
            return mFileCount;
        }

        public int getLibraryCount()
        {
            return mLibraryCount;
        }
    }

    public List<LibStat> getLibraryStatistics()
    {
        List<LibStat> libStats = new ArrayList<LibStat>(mLibStatistics.values());
        return libStats;
    }

    static public class LibStat
    {
        final String mLibraryName;
        int mTaggedFileCount = 0;
        int mTagCount = 0;

        public LibStat(String aLibraryName)
        {
            mLibraryName = aLibraryName;
        }

        public String getLibraryName()
        {
            return mLibraryName;
        }

        public int getTagCount()
        {
            return mTagCount;
        }

        public int getTaggedFileCount()
        {
            return mTaggedFileCount;
        }
    }

    public List<SeriesStat> getSeriesStatistics()
    {
        List<SeriesStat> tagStats = new ArrayList<SeriesStat>(mSeriesStatistics.values());
        return tagStats;
    }

    static public class SeriesStat
    {
        final String mSeriesName;
        int mFileCount = 0;

        public SeriesStat(String aSeriesName, int aFileCount)
        {
            mSeriesName = aSeriesName;
            mFileCount = aFileCount;
        }

        public String getSeriesName()
        {
            return mSeriesName;
        }

        public int getFileCount()
        {
            return mFileCount;
        }
    }

}
