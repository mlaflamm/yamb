package yamb.app.tag.series;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import java.io.File;
import java.util.Comparator;

/**
 * @author manuel.laflamme
 * @since Aug 8, 2008
 */
public class Series
{
    public static final Comparator<File> FILE_NAMECOMPARATOR = new FileNameComparator();

    private Series()
    {
    }

    public static String getSeriesName(String aFileName)
    {
        StringBuilder builder = cleanFileName(aFileName);
        cleanEndingNumber(builder);

        return builder.toString().trim();
    }

    private static int cleanEndingNumber(StringBuilder aBuffer)
    {
        // Remove ending numeric part
        int lastPartStart = aBuffer.lastIndexOf(" ");
        if (lastPartStart > -1)
        {
            String lastPart = aBuffer.substring(lastPartStart + 1);
            lastPart = lastPart.replaceAll("#", "");
            if (NumberUtils.isDigits(lastPart))
            {
                int number = Integer.parseInt(lastPart);
                aBuffer.setLength(lastPartStart);
                return number;
            }
        }

        return -1;
    }

    private static StringBuilder cleanFileName(String aFileName)
    {
        StringBuilder builder = new StringBuilder(aFileName);

        // Remove ending tags
        int end = aFileName.indexOf("_");
        if (end > -1)
        {
            builder.setLength(end);
        }

        // remove extra information after "("
        end = builder.indexOf(" (");
        if (end > -1)
        {
            builder.setLength(end);
        }

        // remove extra information after "-"
        end = builder.indexOf(" -");
        if (end > -1)
        {
            builder.setLength(end);
        }

        return builder;
    }

    /**
     * Returns the file year from the file name or parent directory. The year is expected to be between parentesis
     */
    public static String getFileYearByFileName(File aFile)
    {
        String result = getFileYearByFileName(aFile.getName());
        if (result != null)
        {
            return result;
        }

        File parentFile = aFile.getParentFile();
        if (parentFile != null)
        {
            return getFileYearByFileName(parentFile.getName());
        }

        return null;
    }

    private static String getFileYearByFileName(String aFileName)
    {
        int start = aFileName.indexOf("(");
        if (start == 0) // Explicitly skip starting initial parentesis
        {
            start = aFileName.indexOf("(", 1);
        }
        if (start < 0)
        {
            return null;
        }

        int end = aFileName.indexOf(")", start);
        if (end < 0)
        {
            return null;
        }

        String[] strings = aFileName.substring(start + 1, end).split("[-]");
        if (strings.length > 0)
        {
            String value = strings[0].trim();
            if (value.length() == 4 && StringUtils.isNumeric(value))
            {
                return value;
            }
        }

        return null;
    }


    public static String getNormalizedSeriesName(String aSeriesName)
    {
        String name = aSeriesName;
        if (name.endsWith(".com"))
        {
            name = name.substring(0, name.length() - 4);
        }

        if (name.isEmpty())
        {
            return name;
        }

        int nameLen = name.length();
        StringBuilder builder = new StringBuilder(nameLen);

        // Remove non-alpha and non-space chars
        for (int i = 0; i < nameLen; i++)
        {
            char ch = name.charAt(i);
            if (Character.isLetter(ch) || (ch == ' '))
            {
                builder.append(Character.toLowerCase(ch));
            }
        }

        // Remove trivial world
        String[] splitName = builder.toString().split(" ");
        builder = new StringBuilder(nameLen);
        for (String word : splitName)
        {
            if ("a".equals(word) || "n".equals(word) || "an".equals(word) || "and".equals(word) || "the".equals(word) || "to".equals(word))
            {
                continue;
            }

            builder.append(word);
        }

        String normalizedName = builder.toString();
        return normalizedName;
    }

    private static class FileNameComparator implements Comparator<File>
    {
        public int compare(File o1, File o2)
        {
            StringBuilder b1 = cleanFileName(o1.getName());
            int n1 = cleanEndingNumber(b1);

            StringBuilder b2 = cleanFileName(o2.getName());
            int n2 = cleanEndingNumber(b2);

            int result = b1.toString().trim().compareToIgnoreCase(b2.toString().trim());
            if (result != 0)
            {
                return result;
            }

            if (n1 - n2 != 0)
            {
                return n1 - n2;
            }

            return o1.getName().compareToIgnoreCase(o2.getName());
        }
    }
}
