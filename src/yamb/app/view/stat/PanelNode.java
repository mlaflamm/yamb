package yamb.app.view.stat;

import yamb.app.tag.library.StatisticsSnapshot;

import java.awt.Component;
import javax.swing.Icon;

/**
 * @author manuel.laflamme
 * @since 25-Aug-2008
 */
abstract class PanelNode extends StatNode
{
    private StatisticsSnapshot mSnapshot;

    protected PanelNode(String aTitle, Icon aIcon, StatisticsSnapshot aSnapshot)
    {
        super(aTitle, aIcon);
        mSnapshot = aSnapshot;
    }

    public StatisticsSnapshot getSnapshot()
    {
        return mSnapshot;
    }

    public void setSnapshot(StatisticsSnapshot aSnapshot)
    {
        mSnapshot = aSnapshot;
    }

    public abstract Component getPanel();
    public abstract void refresh(StatisticsSnapshot aSnapshot);
}
