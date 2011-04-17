package yamb.app.tool.export;

import yamb.app.tag.TagManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author manuel.laflamme
 * @since Apr 27, 2010
 */
public class CsvTagStatWriter
{
    private static final Logger LOGGER = Logger.getLogger(CsvTagStatWriter.class);

    public static void write(TagManager aTagManager, File aFile)
    {
        SortedSet<TagEntry> statSet = new TreeSet<TagEntry>(new TagEntryCountComparator());
        List<String> tags = aTagManager.getTags();
        for (String tag : tags)
        {
            statSet.add(new TagEntry(tag, aTagManager.getFiles(tag)));
        }

        try
        {
            PrintStream out = new PrintStream(new FileOutputStream(aFile));
            try
            {
                int totalFileCount = 0;
                for (TagEntry entry : statSet)
                {
                    out.print(entry.getTag());
                    out.print(",");
                    out.println(entry.getFileCount());
                    totalFileCount += entry.getFileCount();
                }
                out.flush();
                LOGGER.info("Tag count=" + tags.size() + ", file count=" + totalFileCount);
            }
            finally
            {
                out.close();
            }
        }
        catch (IOException e)
        {
            LOGGER.warn("Export error", e);
        }
    }

    private static class TagEntry
    {
        private final String mTag;
        private final List<File> mFiles;

        public TagEntry(String aTag, List<File> aFiles)
        {
            mTag = aTag;
            mFiles = aFiles;
        }

        public String getTag()
        {
            return mTag;
        }

        public int getFileCount()
        {
            return mFiles.size();
        }
    }

    private static class TagEntryCountComparator implements Comparator<TagEntry>
    {
        public int compare(TagEntry o1, TagEntry o2)
        {
            int o1Count = o1.getFileCount();
            int o2Count = o2.getFileCount();
            if (o1Count == o2Count)
            {
                return o1.getTag().compareToIgnoreCase(o2.getTag());
            }
            return o1Count < o2Count ? 1 : -1;
        }
    }

    private static class TagEntryComparator implements Comparator<TagEntry>
    {
        public int compare(TagEntry o1, TagEntry o2)
        {
            return o1.getTag().compareToIgnoreCase(o2.getTag());
        }
    }
}
