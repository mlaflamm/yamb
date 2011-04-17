package yamb.app.view.stat;

import yamb.app.ApplicationContext;
import yamb.app.tag.TagManager;
import yamb.app.tag.library.LibraryEvent;
import yamb.app.tag.library.LibraryEventListener;
import yamb.app.tag.library.LibraryManager;
import yamb.app.tag.library.LibraryState;
import yamb.app.tag.library.StatisticsSnapshot;
import yamb.app.tag.series.SeriesManager;
import yamb.app.view.AbstractViewInternalFrame;
import yamb.app.view.DefaultViewContext;
import yamb.app.view.ViewContext;
import yamb.app.view.ViewFactory;
import yamb.util.Disposable;
import yamb.util.commands.ActionFactory;
import yamb.util.commands.ActionModel;
import yamb.util.commands.CommandGroupModel;
import yamb.util.commands.CommandProvider;
import yamb.util.commands.DefaultActionModel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.PieSectionEntity;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

/**
 * @author manuel.laflamme
 * @since 23-Aug-2008
 */
public class StatisticsViewInternalFrame extends AbstractViewInternalFrame implements ActionFactory
{
    private static final ImageIcon PIE_ICON = new ImageIcon(
            AbstractViewInternalFrame.class.getClassLoader().getResource("yamb/images/stats.gif"));
    private static final ImageIcon DETAILS_ICON = new ImageIcon(
            AbstractViewInternalFrame.class.getClassLoader().getResource("yamb/images/details.gif"));
    private static final int TOP_COUNT = 15;

    private final DefaultViewContext mViewContext;
    private final JTree mTree;

    public StatisticsViewInternalFrame(ApplicationContext aAppContext, CommandProvider aCommandProvider, CommandGroupModel aCommandGroupModel, ActionModel aActionModel)
    {
        super(aCommandGroupModel, aAppContext, aCommandProvider, aActionModel);
        mViewContext = new DefaultViewContext();
        mViewContext.setActionModel(new DefaultActionModel(this));

        // Title
        setTitle("Statistics");

        // Icon
        setFrameIcon(PIE_ICON);

        LibraryManager libraryManager = mAppContext.getLibraryManager();
        LibraryEventHandler libraryEventHandler = new LibraryEventHandler();
        addDisposable(libraryEventHandler);
        libraryManager.addLibraryEventListener(libraryEventHandler);
        StatisticsSnapshot statSnapshot = StatisticsSnapshot.createSnapshot(aAppContext.getTagManager(),
                libraryManager, aAppContext.getSeriesManager());

        DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");

        // Tag statistics
        ImageIcon tagIcon = new ImageIcon(getClass().getClassLoader().getResource("yamb/images/tags.gif"));
        DefaultMutableTreeNode tag = new DefaultMutableTreeNode(new StatNode("Tags Statstics", tagIcon));
        root.add(tag);
        tag.add(new DefaultMutableTreeNode(new TagTopPieChartPanelNode(statSnapshot, aAppContext.getViewFactory(), true)));
        tag.add(new DefaultMutableTreeNode(new TagTopFilesVsOthersPieChartPanelNode(statSnapshot)));
        tag.add(new DefaultMutableTreeNode(new TagBreakdownPieChartPanelNode(statSnapshot, true)));
        tag.add(new DefaultMutableTreeNode(new TagTopPieChartPanelNode(statSnapshot, aAppContext.getViewFactory(), false)));
        tag.add(new DefaultMutableTreeNode(new TagBreakdownPieChartPanelNode(statSnapshot, false)));
        tag.add(new DefaultMutableTreeNode(new TagStatisticsPanelNode(statSnapshot, aAppContext)));

        // Library statistics
        ImageIcon libraryIcon = new ImageIcon(getClass().getClassLoader().getResource("yamb/images/library.gif"));
        DefaultMutableTreeNode lib = new DefaultMutableTreeNode(new StatNode("Libraries Statstics", libraryIcon));
        root.add(lib);
        lib.add(new DefaultMutableTreeNode(new LibraryTopPieChartPanelNode(statSnapshot, false)));
        lib.add(new DefaultMutableTreeNode(new LibraryTopPieChartPanelNode(statSnapshot, true)));
        lib.add(new DefaultMutableTreeNode(new LibraryStatisticsPanelNode(statSnapshot)));

        // Group statistics
        ImageIcon seriesIcon = new ImageIcon(getClass().getClassLoader().getResource("yamb/images/groups.gif"));
        DefaultMutableTreeNode series = new DefaultMutableTreeNode(new StatNode("Series Statstics", seriesIcon));
        root.add(series);
        series.add(new DefaultMutableTreeNode(new SeriesTopFilesPieChartPanelNode(statSnapshot, aAppContext.getViewFactory())));
        series.add(new DefaultMutableTreeNode(new SeriesStatisticsPanelNode(statSnapshot, aAppContext.getViewFactory())));

        mTree = new JTree(root);
        mTree.setRootVisible(false);
        mTree.setCellRenderer(new StatNodeTreeCellRenderer());
        for (int i = 0; i < mTree.getRowCount(); i++)
        {
            mTree.expandRow(i);
        }

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, null, null);
        splitPane.setLeftComponent(new JScrollPane(mTree));
        splitPane.setDividerSize(5);
        splitPane.setOneTouchExpandable(false);
        splitPane.getLeftComponent().requestFocus();
        getContentPane().add(splitPane);

        // Tree selection listener
        mTree.addTreeSelectionListener(new TreeSelectionListener()
        {
            public void valueChanged(TreeSelectionEvent aEvent)
            {
                TreePath path = aEvent.getNewLeadSelectionPath();
                if (path != null)
                {
                    DefaultMutableTreeNode item = (DefaultMutableTreeNode) path.getLastPathComponent();
                    if (item != null && item.getUserObject() instanceof PanelNode)
                    {
                        PanelNode panelNode = (PanelNode) item.getUserObject();
                        Component panel = panelNode.getPanel();

                        JSplitPane splitPane = ((JSplitPane) getContentPane().getComponent(0));
                        int dividerLocation = splitPane.getDividerLocation();
                        splitPane.setRightComponent(panel);
                        splitPane.setDividerLocation(dividerLocation);
                        panel.requestFocus();

                        setTitle(panelNode.getTitle());
                    }
                }
            }
        });

        mTree.addSelectionRow(1);
        splitPane.setDividerLocation(200);

    }

    public final ViewContext getViewContext()
    {
        return mViewContext;
    }

    private static class TagTopPieChartPanelNode extends AbstractChartPanelNode
    {
        private final boolean mTopFiles;
        private final ViewFactory mViewFactory;

        public TagTopPieChartPanelNode(StatisticsSnapshot aSnapshot, ViewFactory aViewFactory,
                boolean aTopFiles)
        {
            super(aSnapshot, aTopFiles ? "Tag Files: Top " + TOP_COUNT : "Tag Libraries: Top " + TOP_COUNT, PIE_ICON);
            mTopFiles = aTopFiles;
            mViewFactory = aViewFactory;
        }

        protected JFreeChart createChart(StatisticsSnapshot aSnapshot)
        {
            List<StatisticsSnapshot.TagStat> tags = aSnapshot.getTagStatistics();
            Collections.sort(tags, mTopFiles ?
                    StatisticsSnapshot.TAG_FILECOUNT_COMPARATOR :
                    StatisticsSnapshot.TAG_LIBCOUNT_COMPARATOR);
            List<StatisticsSnapshot.TagStat> tagTopXStats = tags.subList(0, Math.min(TOP_COUNT, tags.size()));

            // create a dataset...
            DefaultPieDataset data = new DefaultPieDataset();
            for (StatisticsSnapshot.TagStat stat : tagTopXStats)
            {
                int count = mTopFiles ? stat.getFileCount() : stat.getLibraryCount();
                data.setValue(stat.getTagName(), new Integer(count));
            }

            // create the chart...
            JFreeChart chart = ChartFactory.createPieChart(getTitle(),  // chart title
                    data,                // data
                    false,                // include legend
                    true,
                    false
            );

            // set the background color for the chart...
            PiePlot plot = (PiePlot) chart.getPlot();
            plot.setNoDataMessage("No data available");

            return chart;
        }

        protected ChartMouseListener createChartMouseListener()
        {
            return new ChartMouseListener()
            {
                public void chartMouseClicked(ChartMouseEvent aEvent)
                {
                    MouseEvent trigger = aEvent.getTrigger();
                    if (SwingUtilities.isLeftMouseButton(trigger) && trigger.getClickCount() == 2)
                    {
                        if (aEvent.getEntity() instanceof PieSectionEntity)
                        {
                            PieSectionEntity entity = (PieSectionEntity) aEvent.getEntity();
                            String tag = entity.getSectionKey().toString();
                            mViewFactory.createTagView(Arrays.asList(new String[]{tag}));
                        }
                    }
                }

                public void chartMouseMoved(ChartMouseEvent event)
                {
                }
            };
        }
    }

    private static class TagTopFilesVsOthersPieChartPanelNode extends AbstractChartPanelNode
    {
        public TagTopFilesVsOthersPieChartPanelNode(StatisticsSnapshot aSnapshot)
        {
            super(aSnapshot, "Tag Files: Top " + TOP_COUNT + " vs Others", PIE_ICON);
        }

        protected JFreeChart createChart(StatisticsSnapshot aSnapshot)
        {
            List<StatisticsSnapshot.TagStat> tagStatistics = aSnapshot.getTagStatistics();
            Collections.sort(tagStatistics, StatisticsSnapshot.TAG_FILECOUNT_COMPARATOR);

            // create a dataset...
            int topTotal = 0;
            int otherTotal = 0;
            DefaultPieDataset data = new DefaultPieDataset();
            for (int i = 0; i < tagStatistics.size(); i++)
            {
                StatisticsSnapshot.TagStat stat = tagStatistics.get(i);
                if (i < TOP_COUNT)
                {
                    topTotal += stat.getFileCount();
                }
                else
                {
                    otherTotal += stat.getFileCount();
                }

            }
            data.setValue("Top " + TOP_COUNT, new Integer(topTotal));
            data.setValue("Others", new Integer(otherTotal));

            // create the chart...
            JFreeChart chart = ChartFactory.createPieChart(getTitle(),  // chart title
                    data,                // data
                    true,                // include legend
                    true,
                    false
            );

            PiePlot plot = (PiePlot) chart.getPlot();
            plot.setNoDataMessage("No data available");
            return chart;
        }
    }


    private static class TagBreakdownPieChartPanelNode extends AbstractChartPanelNode
    {
        private static final int[] FILE_BREAKDOWN = new int[]{
                1,
                2,
                4,
                9,
                24,
                Integer.MAX_VALUE
        };

        private static final int[] LIB_BREAKDOWN = new int[]{
                1,
                2,
                4,
                9,
                Integer.MAX_VALUE
        };

        private final boolean mFileBreakdown;

        public TagBreakdownPieChartPanelNode(StatisticsSnapshot aSnapshot, boolean aFileBreakdown)
        {
            super(aSnapshot, aFileBreakdown ? "Tag Files: Breakdown" : "Tag Libraries: Breakdown", PIE_ICON);
            mFileBreakdown = aFileBreakdown;
        }

        protected JFreeChart createChart(StatisticsSnapshot aSnapshot)
        {
            int[] breakdown = mFileBreakdown ? FILE_BREAKDOWN : LIB_BREAKDOWN;


            List<StatisticsSnapshot.TagStat> tagStatistics = aSnapshot.getTagStatistics();

            // create a dataset...
            int[] values = new int[breakdown.length];
            for (int i = 0; i < values.length; i++)
            {
                values[i] = 0;
            }

            DefaultPieDataset data = new DefaultPieDataset();
            for (StatisticsSnapshot.TagStat stat : tagStatistics)
            {
                int fileCount = mFileBreakdown ? stat.getFileCount() : stat.getLibraryCount();
                for (int j = 0; j < breakdown.length; j++)
                {
                    if (fileCount <= breakdown[j])
                    {
                        values[j] = values[j] + 1;
                        break;
                    }
                }
            }

            for (int i = 0; i < breakdown.length; i++)
            {
                String label;
                if (i == 0 || breakdown[i - 1] == breakdown[i] - 1)
                {
                    label = String.valueOf(breakdown[i]);
                }
                else if (breakdown[i] == Integer.MAX_VALUE)
                {
                    label = (breakdown[i - 1] + 1) + "+";
                }
                else
                {
                    label = (breakdown[i - 1] + 1) + "-" + breakdown[i];
                }
                data.setValue(label, new Integer(values[i]));
            }

            // create the chart...
            JFreeChart chart = ChartFactory.createPieChart(getTitle(),  // chart title
                    data,                // data
                    true,                // include legend
                    true,
                    false
            );

            // set the background color for the chart...
            PiePlot plot = (PiePlot) chart.getPlot();
            plot.setNoDataMessage("No data available");

            return chart;
        }
    }

    private static class TagStatisticsPanelNode extends AbstractTablePanelNode
    {
        private final ApplicationContext mAppContext;

        public TagStatisticsPanelNode(StatisticsSnapshot aSnapshot, ApplicationContext aAppContext)
        {
            super(aSnapshot, "Tag Details", DETAILS_ICON);
            mAppContext = aAppContext;
        }

        protected TableModel createTableModel(StatisticsSnapshot aSnapshot)
        {
            return new TagStatisticModel(aSnapshot, mAppContext.getTagManager());
        }

        protected MouseListener createMouseListener()
        {
            return new MouseAdapter()
            {
                public void mouseClicked(MouseEvent aEvent)
                {
                    if (SwingUtilities.isLeftMouseButton(aEvent) && aEvent.getClickCount() == 2)
                    {
                        JTable table = ((JTable) aEvent.getComponent());
                        int index = table.rowAtPoint(aEvent.getPoint());
                        if (index >= 0)
                        {
                            String tag = (String) table.getValueAt(index, 1);
                            mAppContext.getViewFactory().createTagView(Arrays.asList(new String[]{tag}));
                        }
                    }
                }
            };
        }
    }

    private static class LibraryTopPieChartPanelNode extends AbstractChartPanelNode
    {
        private final boolean mTopFiles;

        public LibraryTopPieChartPanelNode(StatisticsSnapshot aSnapshot,
                boolean aTopFiles)
        {
            super(aSnapshot, aTopFiles ?
                    "Library Files: Top " + TOP_COUNT : "Library Tags: Top " + TOP_COUNT, PIE_ICON);
            mTopFiles = aTopFiles;
        }

        protected JFreeChart createChart(StatisticsSnapshot aSnapshot)
        {
            List<StatisticsSnapshot.LibStat> libraries = aSnapshot.getLibraryStatistics();
            Collections.sort(libraries, mTopFiles ?
                    StatisticsSnapshot.LIB_FILECOUNT_COMPARATOR :
                    StatisticsSnapshot.LIB_TAGCOUNT_COMPARATOR);
            List<StatisticsSnapshot.LibStat> libTopXStats = libraries.subList(0, Math.min(TOP_COUNT, libraries.size()));

            // create a dataset...
            DefaultPieDataset data = new DefaultPieDataset();
            for (StatisticsSnapshot.LibStat stat : libTopXStats)
            {
                int count = mTopFiles ? stat.getTaggedFileCount() : stat.getTagCount();
                data.setValue(stat.getLibraryName(), new Integer(count));
            }

            // create the chart...
            JFreeChart chart = ChartFactory.createPieChart(getTitle(),  // chart title
                    data,                // data
                    false,                // include legend
                    true,
                    false
            );

            // set the background color for the chart...
            PiePlot plot = (PiePlot) chart.getPlot();
            plot.setNoDataMessage("No data available");
            return chart;
        }
    }

    private static class LibraryStatisticsPanelNode extends AbstractTablePanelNode
    {
        public LibraryStatisticsPanelNode(StatisticsSnapshot aSnapshot)
        {
            super(aSnapshot, "Library Details", DETAILS_ICON);
        }

        protected TableModel createTableModel(StatisticsSnapshot aSnapshot)
        {
            return new LibraryStatisticModel(aSnapshot);
        }
    }

    private static class SeriesTopFilesPieChartPanelNode extends AbstractChartPanelNode
    {
        private final ViewFactory mViewFactory;

        public SeriesTopFilesPieChartPanelNode(StatisticsSnapshot aSnapshot, ViewFactory aViewFactory)
        {
            super(aSnapshot, "Series Files: Top " + TOP_COUNT, PIE_ICON);
            mViewFactory = aViewFactory;
        }

        protected JFreeChart createChart(StatisticsSnapshot aSnapshot)
        {
            List<StatisticsSnapshot.SeriesStat> series = aSnapshot.getSeriesStatistics();
            Collections.sort(series, StatisticsSnapshot.SERIES_FILECOUNT_COMPARATOR);
            List<StatisticsSnapshot.SeriesStat> seriesTopXStats = series.subList(0, Math.min(TOP_COUNT, series.size()));

            // create a dataset...
            DefaultPieDataset data = new DefaultPieDataset();
            for (StatisticsSnapshot.SeriesStat stat : seriesTopXStats)
            {
                data.setValue(stat.getSeriesName(), new Integer(stat.getFileCount()));
            }

            // create the chart...
            JFreeChart chart = ChartFactory.createPieChart(getTitle(),  // chart title
                    data,                // data
                    false,                // include legend
                    true,
                    false
            );

            // set the background color for the chart...
            PiePlot plot = (PiePlot) chart.getPlot();
            plot.setNoDataMessage("No data available");
            return chart;
        }

        protected ChartMouseListener createChartMouseListener()
        {
            return new ChartMouseListener()
            {
                public void chartMouseClicked(ChartMouseEvent aEvent)
                {
                    MouseEvent trigger = aEvent.getTrigger();
                    if (SwingUtilities.isLeftMouseButton(trigger) && trigger.getClickCount() == 2)
                    {
                        if (aEvent.getEntity() instanceof PieSectionEntity)
                        {
                            PieSectionEntity entity = (PieSectionEntity) aEvent.getEntity();
                            String series = entity.getSectionKey().toString();
                            mViewFactory.createSeriesView(series);
                        }
                    }
                }

                public void chartMouseMoved(ChartMouseEvent event)
                {
                }
            };
        }
    }

    private static class SeriesStatisticsPanelNode extends AbstractTablePanelNode
    {
        private final ViewFactory mViewFactory;

        public SeriesStatisticsPanelNode(StatisticsSnapshot aSnapshot, ViewFactory aViewFactory)
        {
            super(aSnapshot, "Series Details", DETAILS_ICON);
            mViewFactory = aViewFactory;
        }

        protected TableModel createTableModel(StatisticsSnapshot aSnapshot)
        {
            return new SeriesStatisticModel(aSnapshot);
        }

        protected MouseListener createMouseListener()
        {
            return new MouseAdapter()
            {
                public void mouseClicked(MouseEvent aEvent)
                {
                    if (SwingUtilities.isLeftMouseButton(aEvent) && aEvent.getClickCount() == 2)
                    {
                        JTable table = ((JTable) aEvent.getComponent());
                        int index = table.rowAtPoint(aEvent.getPoint());
                        if (index >= 0)
                        {
                            String series = (String) table.getValueAt(index, 1);
                            mViewFactory.createSeriesView(series);
                        }
                    }
                }
            };
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // LibraryEventListener interface

    private class LibraryEventHandler implements LibraryEventListener, Disposable
    {

        public void libraryLoaded(LibraryEvent aEvent)
        {
            LibraryManager libraryManager = mAppContext.getLibraryManager();
            for (File libraryRoot : aEvent.getLibraries())
            {
                if (libraryManager.getLibraryState(libraryRoot) != LibraryState.LOADING)
                {
                    refresh();
                    break;
                }
            }
        }

        public void libraryUnloaded(LibraryEvent aEvent)
        {
            refresh();
        }

        public void libraryAdded(LibraryEvent aEvent)
        {
        }

        public void libraryRemoved(LibraryEvent aEvent)
        {
        }

        public void dispose()
        {
            LibraryManager libraryManager = mAppContext.getLibraryManager();
            libraryManager.removeLibraryEventListener(this);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // ActionFactory interface

    public Action createAction(String aId)
    {
        if ("viewRefresh".equals(aId))
        {
            return new AbstractAction()
            {
                public void actionPerformed(ActionEvent aEvent)
                {
                    refresh();
                }
            };
        }

        return null;
    }

    private void refresh()
    {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) mTree.getModel().getRoot();

        TagManager tagManager = getAppContext().getTagManager();
        LibraryManager libraryManager = getAppContext().getLibraryManager();
        SeriesManager seriesManager = getAppContext().getSeriesManager();
        StatisticsSnapshot snapshot = StatisticsSnapshot.createSnapshot(tagManager, libraryManager, seriesManager);

        refreshDescendants(root, snapshot);
    }

    private void refreshDescendants(DefaultMutableTreeNode aNode, StatisticsSnapshot aSnapshot)
    {
        for (int i = 0; i < aNode.getChildCount(); i++)
        {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) aNode.getChildAt(i);
            refreshDescendants(child, aSnapshot);

            if (child.getUserObject() instanceof PanelNode)
            {
                ((PanelNode) child.getUserObject()).refresh(aSnapshot);
            }
        }

    }
}
