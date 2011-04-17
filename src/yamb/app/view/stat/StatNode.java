package yamb.app.view.stat;

import javax.swing.Icon;

/**
 * @author manuel.laflamme
 * @since 26-Aug-2008
 */
class StatNode
{
    private final String mTitle;
    private final Icon mIcon;

    public StatNode(String aTitle, Icon aIcon)
    {
        mTitle = aTitle;
        mIcon = aIcon;
    }

    public String getTitle()
    {
        return mTitle;
    }

    public Icon getIcon()
    {
        return mIcon;
    }

    public String toString()
    {
        return getTitle();
    }
}
