package yamb.util.swing;

import org.apache.log4j.Logger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * @author manuel.laflamme
 * @since Feb 7, 2008
 */
public class RegexPaternFilter extends AbstractObjectFilter
{
    private static final Logger LOGGER = Logger.getLogger(RegexPaternFilter.class);

    private boolean mAcceptAll = false;
    private boolean mError = false;
    private Pattern mPattern;

    /**
     * @param aRegex The expression to be compiled
     * @param aFlags Match flags, a bit mask that may include
     *               {@link Pattern#CASE_INSENSITIVE}, {@link Pattern#MULTILINE}, {@link Pattern#DOTALL},
     *               {@link Pattern#UNICODE_CASE}, and {@link Pattern#CANON_EQ}
     */
    public RegexPaternFilter(String aRegex, int aFlags)
    {
        mPattern = Pattern.compile(aRegex, aFlags);
    }

    public static RegexPaternFilter createCaseInsensitiveLiteralFilter(String aRegex)
    {
        return new RegexPaternFilter(aRegex, Pattern.CASE_INSENSITIVE | Pattern.LITERAL);
    }

    public void setAcceptAll(boolean aAcceptAll)
    {
        boolean oldValue = mAcceptAll;
        boolean newValue = aAcceptAll;

        mAcceptAll = aAcceptAll;

        if (oldValue != newValue)
        {
            fireFilterChanged(new FilterEvent(this));
        }
    }

    public void setRegex(String aRegex) throws PatternSyntaxException
    {
        int flags = mPattern.flags();
        try
        {
            mPattern = Pattern.compile(aRegex, flags);
            mAcceptAll = false;
            mError = false;
            fireFilterChanged(new FilterEvent(this));
        }
        catch (PatternSyntaxException e)
        {
            mError = true;
            LOGGER.warn("Invalid pattern syntax: '" + aRegex + "'", e);

        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // ObjectFilter interface

    public boolean accept(Object aObject)
    {
        if (!acceptAll() && mPattern != null)
        {
            Matcher matcher = mPattern.matcher(aObject.toString());
            return matcher.find();
        }

        return true;
    }

    public boolean acceptAll()
    {
        return mAcceptAll;
    }
}
