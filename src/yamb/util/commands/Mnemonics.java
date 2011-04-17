package yamb.util.commands;

import java.util.StringTokenizer;
import javax.swing.KeyStroke;

/**
 * @author Manuel Laflamme
 * @since 2001
 */
public class Mnemonics
{
    private static final String DELIM = "#";

    /**
     *
     */
    public static int parseMnemonic(String title)
    {

        int firstOccurenceIndex = title.indexOf(DELIM);
        if (firstOccurenceIndex == -1)
        {
            return java.awt.event.KeyEvent.VK_UNDEFINED;
        }

        // Verify if other occurence of '#' exist in the string
        int secondOccurenceIndex = title.indexOf(DELIM, (firstOccurenceIndex + 1));
        if (secondOccurenceIndex != -1)
        {
            // Verify if the occurence of '#' is right beside the other one
            if ((secondOccurenceIndex - firstOccurenceIndex) == 1)
            {
                // continue to look foward for a mnemonic
                return parseMnemonic(title.substring((secondOccurenceIndex + 1)));
            }
        }
        // veryfy that the mnemonic is not the last char in the sequence like: 'Menue#'
        if ((firstOccurenceIndex + 1) == title.length())
        {
            return java.awt.event.KeyEvent.VK_UNDEFINED;
        }

        //return the characther that mnemonic is on
        String mnemonic = title.substring((firstOccurenceIndex + 1),
                (firstOccurenceIndex + 2));
        return KeyStroke.getKeyStroke(mnemonic.toUpperCase()).getKeyCode();
    }

    /**
     *
     */
    public static String normalize(String string)
    {
        StringBuffer buffer = new StringBuffer("");
        StringTokenizer st = new StringTokenizer(string, DELIM, true);
        String token;
        boolean isLastTokenDelim = false;
        while (st.hasMoreTokens())
        {
            token = st.nextToken();
            if (token.equals(DELIM) && isLastTokenDelim == true)
            {
                buffer.append(token);
                isLastTokenDelim = false;
            }
            else if (token.equals(DELIM))
            {
                isLastTokenDelim = true;
            }
            else
            {
                buffer.append(token);
                isLastTokenDelim = false;
            }
        }
        return buffer.toString();
    }
}