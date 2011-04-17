package yamb.util.io;

import org.apache.commons.lang.WordUtils;

/**
 * @author manuel.laflamme
 * @since 9-Aug-2008
 */
public class FileNames
{
    public static String getExtension(String aFilePath)
    {
        return splitExtension(aFilePath)[1];
    }

    public static String[] splitExtension(String aFilePath)
    {
        int dotIndex = aFilePath.lastIndexOf(".");
        if (dotIndex == -1)
        {
            return new String[]{aFilePath, ""};
        }

        int sepIndex = aFilePath.lastIndexOf("\\");
        if (sepIndex > dotIndex)
        {
            return new String[]{aFilePath, ""};
        }

        String start = aFilePath.substring(0, dotIndex);
        String end = aFilePath.substring(dotIndex + 1);

        return new String[]{start, end};
    }

    public static String cleanNumberedName(String aName, int aMaxPad)
    {
        String[] parts = aName.split(" ");
        String lastPart = parts[parts.length - 1];
        lastPart = lastPart.replaceAll("#", "");

        try
        {
            int number = Integer.parseInt(lastPart);
            aName = "";
            for (int i = 0; i < parts.length - 1; i++)
            {
                String part = parts[i];
                aName += part + " ";
            }

            Padder padder = new Padder('0', aMaxPad);
            aName += padder.pad(number);
        }
        catch (NumberFormatException e)
        {
            // Not a number, not a big deal
        }
        return aName;
    }

    public static boolean isValidFileName(String aFileName)
    {
        // \/:*?"<>|
        String[] invalidTokens = {"\\", "/", ":", "*", "?", "\"", "<", ">", "|"};
        for (String token : invalidTokens)
        {
            if (aFileName.indexOf(token) != -1)
            {
                return false;
            }
        }

        return true;
    }
}
