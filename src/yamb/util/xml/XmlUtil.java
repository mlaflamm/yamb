package yamb.util.xml;

/**
 * @author Manuel Laflamme
 * @since Mar 1, 2006
 */
public class XmlUtil
{
    static public String escapeXml(String str)
    {
        str = replace(str, "&", "&amp;");
        str = replace(str, "<", "&lt;");
        str = replace(str, ">", "&gt;");
        str = replace(str, "\"", "&quot;");
//        str = replace(str, "'", "&apos;");
        return str;
    }

    static private String replace(String value, String original, String replacement)
    {
        StringBuffer buffer = null;

        int startIndex = 0;
        int lastEndIndex = 0;
        for (; ;)
        {
            startIndex = value.indexOf(original, lastEndIndex);
            if (startIndex == -1)
            {
                if (buffer != null)
                {
                    buffer.append(value.substring(lastEndIndex));
                }
                break;
            }

            if (buffer == null)
            {
                buffer = new StringBuffer((int)(original.length() * 1.5));
            }
            buffer.append(value.substring(lastEndIndex, startIndex));
            buffer.append(replacement);
            lastEndIndex = startIndex + original.length();
        }

        return buffer == null ? value : buffer.toString();
    }



}
