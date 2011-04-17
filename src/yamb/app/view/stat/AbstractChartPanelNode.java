package yamb.app.view.stat;

import yamb.app.tag.library.StatisticsSnapshot;
import yamb.util.Disposable;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import java.awt.Component;
import javax.swing.Icon;

/**
 * @author manuel.laflamme
 * @since 26-Aug-2008
 */
public abstract class AbstractChartPanelNode extends PanelNode implements Disposable
{
    private ChartMouseListener mChartMouseListener = null;
    private ChartPanel mChartPanel = null;

    protected AbstractChartPanelNode(StatisticsSnapshot aSnapshot, String aTitle, Icon aIcon)
    {
        super(aTitle, aIcon, aSnapshot);
    }

    public Component getPanel()
    {
        if (mChartPanel == null)
        {
            JFreeChart chart = createChart(getSnapshot());
            mChartPanel = new ChartPanel(chart);
            mChartMouseListener = createChartMouseListener();
            if (mChartMouseListener != null)
            {
                mChartPanel.addChartMouseListener(mChartMouseListener);
            }
        }
        return mChartPanel;
    }

    public void refresh(StatisticsSnapshot aSnapshot)
    {
        setSnapshot(aSnapshot);
        if (mChartPanel != null)
        {
            mChartPanel.setChart(createChart(getSnapshot()));
        }
    }

    public void dispose()
    {
        if (mChartMouseListener != null && mChartPanel != null)
        {
            mChartPanel.removeChartMouseListener(mChartMouseListener);
        }
    }

    protected abstract JFreeChart createChart(StatisticsSnapshot aSnapshot);

    protected ChartMouseListener createChartMouseListener()
    {
        return null;
    }
}
