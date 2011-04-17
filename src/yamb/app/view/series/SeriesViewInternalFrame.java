package yamb.app.view.series;

import yamb.app.ApplicationContext;
import yamb.app.fileitem.filelist.FileListItemCache;
import yamb.app.fileitem.filelist.FileListModel;
import yamb.app.fileitem.filelist.FileListSelectionManager;
import yamb.app.fileitem.filelist.thumbnail.GlobalThumbnailCache;
import yamb.app.tag.series.Series;
import yamb.app.tag.series.SeriesJList;
import yamb.app.tag.series.SeriesListModel;
import yamb.app.view.AbstractFileListViewInternalFrame;
import yamb.app.view.FilePropertiesPanel;
import yamb.util.commands.ActionFactory;
import yamb.util.commands.ActionModel;
import yamb.util.commands.CommandException;
import yamb.util.commands.CommandGroupModel;
import yamb.util.commands.CommandItem;
import yamb.util.commands.CommandProvider;
import yamb.util.commands.CommandWidgetFactory;
import yamb.util.commands.DefaultActionModel;
import yamb.util.media.mediainfo.cache.MediaInfoCache;
import yamb.util.swing.DocumentPatternFilter;
import yamb.util.swing.ListModels;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.Action;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * @author manuel.laflamme
 * @since Aug 8, 2008
 */
public class SeriesViewInternalFrame extends AbstractFileListViewInternalFrame
        implements PropertyChangeListener, ActionFactory
{
    private static final Logger LOGGER = Logger.getLogger(SeriesViewInternalFrame.class);

    private final DefaultSeriesViewContext mViewContext;

    private SeriesJList mSeriesJList;
    private JList mDuplicateList = new JList();
    JTextField mSeriesFilterField = new JTextField();

    private final JMenu mSeriesPopupMenu;

    public SeriesViewInternalFrame(
            String aSelectedSeriesName,
            ApplicationContext aAppContext,
            FileListItemCache aFileListItemCache,
            GlobalThumbnailCache aGlobalThumbnailCache,
            CommandProvider aCommandProvider,
            CommandGroupModel aCommandGroupModel,
            MediaInfoCache aMediaInfoCache,
            ActionModel aGlobalActionModel) throws CommandException
    {
        super(aAppContext, aFileListItemCache, aGlobalThumbnailCache, aCommandProvider,
                aCommandGroupModel, aGlobalActionModel, aMediaInfoCache,
                new DefaultSeriesViewContext(aAppContext.getSeriesManager()));
        mViewContext = (DefaultSeriesViewContext) getViewContext();

        // Setup series name list
        Component listComponent = createSeriesListComponent(mViewContext);

        JSplitPane splitPane = ((JSplitPane) getContentPane().getComponent(0));
        int dividerLocation = splitPane.getDividerLocation();
        splitPane.setLeftComponent(listComponent);
        splitPane.setDividerLocation(dividerLocation);
        splitPane.getLeftComponent().requestFocus();

        // Icon
        setFrameIcon(new ImageIcon(getClass().getClassLoader().getResource("yamb/images/groups.gif")));

        // Popup menus
        mSeriesPopupMenu = createMenu("groupPopup", aGlobalActionModel);

        // Setup view context
        mViewContext.setSelectionManager(getFileListSelectionManager());
        mViewContext.setActionModel(new DefaultActionModel(this));
        mViewContext.addPropertyChangeListener(this);
        mViewContext.setSelectedSeriesName(aSelectedSeriesName);
        mSeriesFilterField.setText(aSelectedSeriesName);
    }

    private Component createSeriesListComponent(SeriesViewContext aViewContext) throws CommandException
    {
        // 1. Top pane: series list

        // Setup series list filter
//        JTextField mSeriesFilterField = new JTextField();
        DocumentPatternFilter seriesFilter = new DocumentPatternFilter();
        Component filterComponent = createSeriesFilterComponent(seriesFilter);

        // Setup series list
        mSeriesJList = new SeriesJList();
        addDisposable(mSeriesJList);
        ListModel model = new SeriesListModel(aViewContext.getSeriesManager());
        model = ListModels.filteredModel(model, seriesFilter);
        mSeriesJList.setModel(model);
        JScrollPane scrollPane = new JScrollPane(mSeriesJList);
//        scrollPane.setColumnHeaderView(filterComponent);

        // Mouse adapter
        mSeriesJList.addMouseListener(new SeriesListMouseAdapter());

        JComponent seriesListPanel = new JPanel(new BorderLayout());
        seriesListPanel.add("North", filterComponent);
        seriesListPanel.add("Center", scrollPane);

        // List selection listener
        mSeriesJList.addListSelectionListener(new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent aEvent)
            {
                JList list = (JList) aEvent.getSource();
                int index = list.getSelectedIndex();
                if (index >= 0)
                {
                    String item = (String) list.getModel().getElementAt(index);
                    mViewContext.setSelectedSeriesName(item);
                }
            }
        });

        // 2. Bottom pane: tabbed lists (selected file tags)

        // Setup selected files tags list
        JTabbedPane tabbedPane = new JTabbedPane();
        FilePropertiesPanel filePropertiesPanel = new FilePropertiesPanel(aViewContext, getMediaInfoCache());
        addDisposable(filePropertiesPanel);
        filePropertiesPanel.setFont(tabbedPane.getFont());
        tabbedPane.addTab("File Properties", new JScrollPane(filePropertiesPanel));
        tabbedPane.addTab("Dup. Series", createFindDuplicateComponent());
//        tabbedPane.addTab("Active Tags", new JScrollPane(activeTagList));
//        tabbedPane.addTab("Selected Files Tags", new JScrollPane(selectedFilesTagList));

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT, new JScrollPane(seriesListPanel), tabbedPane);
        splitPane.setOneTouchExpandable(false);
        splitPane.setDividerLocation(400);
        splitPane.setDividerSize(5);
        return splitPane;
    }

    private Component createSeriesFilterComponent(DocumentPatternFilter aSeriesFilter) throws CommandException
    {
        mSeriesFilterField.getDocument().addDocumentListener(aSeriesFilter);

        CommandProvider commandProvider = getCommandProvider();
        CommandItem toolbarItem = commandProvider.getToolBarItem("groupFilterToolbar");
        CommandGroupModel commandGroupModel = getCommandGroupModel();

        JToolBar toolbar = CommandWidgetFactory.buildToolBar(toolbarItem, getGlobalActionModel(),
                commandGroupModel);

        toolbar.add(mSeriesFilterField, 0);
        toolbar.setFloatable(false);

        return toolbar;
    }

    private void setSeriesFilterText(String aText)
    {
        if (mSeriesFilterField.getText().equals(aText))
        {
            return;
        }

        mSeriesFilterField.setText(aText);
    }

    private Component createFindDuplicateComponent()
    {
        SpinnerNumberModel model = new SpinnerNumberModel(0, 0, 3, 1);
        JSpinner levenshteinSpinner = new JSpinner(model);

        JToolBar toolbar = new JToolBar();
        JButton findButton = new JButton();
        findButton.setAction(new FindDuplicateAction(model));
        findButton.setText("Find...");
        findButton.setIcon(new ImageIcon(getClass().getClassLoader().getResource("yamb/images/search.gif")));
        toolbar.add(findButton);
        toolbar.addSeparator();
        toolbar.add(new JLabel("Levenshtein Distance "));
        toolbar.add(levenshteinSpinner);
        toolbar.setFloatable(false);

        mDuplicateList.addListSelectionListener(new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent e)
            {
                Set<String> values = (Set<String>) mDuplicateList.getSelectedValue();
                if (values != null)
                {
                    StringBuilder builder = new StringBuilder();
                    for (String value : values)
                    {
                        if (builder.length() > 0)
                        {
                            builder.append("|");
                        }
                        builder.append(value);
                    }
                    mSeriesFilterField.setText(builder.toString());
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(mDuplicateList);
        scrollPane.setColumnHeaderView(toolbar);

        return scrollPane;
    }

    private class FindDuplicateAction extends AbstractAction
    {
        SpinnerNumberModel mLevenshteinModel;

        public FindDuplicateAction(SpinnerNumberModel aLevenshteinModel)
        {
            mLevenshteinModel = aLevenshteinModel;
        }

        public void actionPerformed(ActionEvent e)
        {

            final List<Set<String>> duplicates = findDuplicates(mLevenshteinModel.getNumber().intValue());
            mDuplicateList.setModel(new AbstractListModel()
            {
                public int getSize()
                {
                    return duplicates.size();
                }

                public Object getElementAt(int index)
                {
                    return duplicates.get(index);
                }
            });
        }
    }

    private void setSelectedSeriesName(String aSeriesName)
    {
        // Update file list view
        if (aSeriesName != null)
        {
            setTitle(aSeriesName);
            List<File> files = mViewContext.getSeriesManager().getSeriesFiles(aSeriesName);
            Collections.sort(files, Series.FILE_NAMECOMPARATOR);
            ListModel model = new FileListModel(files.toArray(new File[0]), getFileListItemCache(), getThumbnailCache(),
                    getAppContext().getFileOperation(), false, false);
            setFileListModel(model);

            mSeriesJList.setSelectedValue(aSeriesName, true);
        }
        else
        {
            setTitle("");
            ListModel model = new DefaultListModel();
            setFileListModel(model);
            mSeriesJList.getSelectionModel().clearSelection();
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // PropertyChangeListener interface

    public void propertyChange(PropertyChangeEvent aEvent)
    {
        String propertyName = aEvent.getPropertyName();
        if (SeriesViewContext.SELECTED_SERIES.equals(propertyName))
        {
            String seriesNames = (String) aEvent.getNewValue();
            setSelectedSeriesName(seriesNames);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // MouseAdapter class

    private class SeriesListMouseAdapter extends MouseAdapter
    {
        public void mouseReleased(MouseEvent aEvent)
        {
            if (aEvent.isPopupTrigger())
            {
                JList seriesList = ((JList) aEvent.getComponent());
                int index = seriesList.locationToIndex(aEvent.getPoint());
                if (index >= 0 && seriesList.getCellBounds(index, index).contains(aEvent.getPoint()))
                {
                    JPopupMenu popupMenu = mSeriesPopupMenu.getPopupMenu();
                    popupMenu.setLightWeightPopupEnabled(true);
                    popupMenu.show(aEvent.getComponent(), aEvent.getX(), aEvent.getY());
                }
            }
        }
    }
    ////////////////////////////////////////////////////////////////////////////
    // ActionFactory interface

    @Override
    public Action createAction(String aId)
    {
        if ("viewRefresh".equals(aId))
        {
            return new AbstractAction()
            {
                public void actionPerformed(ActionEvent aEvent)
                {
                    FileListSelectionManager selectionManager = mViewContext.getFileListSelectionManager();
                    List<File> selectedFiles = selectionManager.getSelectedFiles();

//                    File activeFile = selectionManager.getActiveFile();
                    setSelectedSeriesName(mViewContext.getSelectedSeriesName());
                    selectionManager.addSelectedFiles(selectedFiles);
//                    selectionManager.setActiveFile(activeFile);
                }
            };
        }


        if ("groupFilterActiveGroup".equals(aId))
        {
            return new AbstractAction()
            {
                public void actionPerformed(ActionEvent aEvent)
                {
                    setSeriesFilterText(mViewContext.getSelectedSeriesName());
                }
            };
        }

        if ("groupFilterReset".equals(aId))
        {
            return new AbstractAction()
            {
                public void actionPerformed(ActionEvent aEvent)
                {
                    setSeriesFilterText("");
                }
            };
        }
        if ("groupCopy".equals(aId))
        {
            return new AbstractAction()
            {
                public void actionPerformed(ActionEvent aEvent)
                {
                    String selectedValue = (String) mSeriesJList.getSelectedValue();
                    if (selectedValue != null)
                    {
                        StringSelection stringSelection = new StringSelection(selectedValue);
                        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                        clipboard.setContents(stringSelection, stringSelection);
                    }

                }
            };
        }

        if ("groupRename".equals(aId))
        {
            return new AbstractAction()
            {
                public void actionPerformed(ActionEvent aEvent)
                {

                    String selectedValue = (String) mSeriesJList.getSelectedValue();
                    if (selectedValue != null)
                    {
                        String s = (String) JOptionPane.showInputDialog(
                                null,
                                "Rename '" + selectedValue + "' :",
                                "Rename '" + selectedValue + "'",
                                JOptionPane.PLAIN_MESSAGE,
                                null,
                                null,
                                selectedValue);

                        //If a string was returned, say so.
                        if ((s != null) && (s.length() > 0))
                        {
                            List<File> files = mViewContext.getSeriesManager().getSeriesFiles(selectedValue);
                            for (File file : files)
                            {
                                try
                                {
                                    String newFileName = s + file.getName().substring(selectedValue.length());
                                    getAppContext().getFileOperation().renameFile(file, newFileName);
                                }
                                catch (IOException e)
                                {
                                    LOGGER.error("Unable to rename " + file, e);
                                }
                            }
                        }
                    }

                }
            };
        }

        return super.createAction(aId);
    }

    public List<Set<String>> findDuplicates(int aLevenshteinDistance)
    {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try
        {
            List<String> allNames = mViewContext.getSeriesManager().getSeriesNames();

            int currentStep = 0;
//            ProgressMonitor progressMonitor = new ProgressMonitor(this, "Finding duplicates...", null, 0, 3);

            Map<String, Set<String>> normalizedMap = new HashMap<String, Set<String>>();
            for (String originalName : allNames)
            {
                String normalizedName = Series.getNormalizedSeriesName(originalName);
                if (normalizedName.length() > 0)
                {
                    if (!normalizedMap.containsKey(normalizedName))
                    {
                        normalizedMap.put(normalizedName, new HashSet<String>());
                    }
                    normalizedMap.get(normalizedName).add(originalName);
                }
            }
//            progressMonitor.setProgress(++currentStep);

            for (String originalName : allNames)
            {
                String name = originalName.toLowerCase();
                int index = name.indexOf("'s");
                if (index > -1)
                {
                    if (name.indexOf(" ") < index)
                    {
                        name = name.substring(index + 2);
                    }
                }

                String normalizedName = Series.getNormalizedSeriesName(name);
                if (normalizedName.length() > 0)
                {
                    if (!normalizedMap.containsKey(normalizedName))
                    {
                        normalizedMap.put(normalizedName, new HashSet<String>());
                    }
                    normalizedMap.get(normalizedName).add(originalName);
                }
            }
//            progressMonitor.setProgress(++currentStep);

            Set<Set<String>> duplicatesSet = new HashSet<Set<String>>();
            for (Set<String> set : normalizedMap.values())
            {
                if (set.size() > 1)
                {
                    duplicatesSet.add(set);
                }
            }

            if (aLevenshteinDistance > 0)
            {
                List<String> normalizedNames = new ArrayList<String>(normalizedMap.keySet());
//                progressMonitor.setMaximum(progressMonitor.getMaximum() + normalizedNames.size() + 1);

                Map<String, Set<String>> levenshteinMap = new HashMap<String, Set<String>>();
                for (int i = 0; i < normalizedNames.size(); i++)
                {
                    String name1 = normalizedNames.get(i);
                    for (int j = 0/*i + 1*/; j < normalizedNames.size(); j++)
                    {
                        String name2 = normalizedNames.get(j);
                        if (!name1.equals(name2) && name1.length() > 5 && name2.length() > 5 &&
                                StringUtils.getLevenshteinDistance(name1, name2) <= aLevenshteinDistance)
                        {
                            Set<String> matches = levenshteinMap.get(name1);
                            if (matches == null)
                            {
                                matches = new HashSet<String>();
                                matches.add(name1);
                                levenshteinMap.put(name1, matches);
                            }
                            matches.add(name2);
                        }
                    }
//                    progressMonitor.setProgress(++currentStep);
                }

                // Reduces dupes
                Set<Set<String>> normalizedSet = new HashSet<Set<String>>();
                for (Set<String> strings : levenshteinMap.values())
                {
                    normalizedSet.add(strings);
                }

                for (Set<String> matches : normalizedSet)
                {
                    Set<String> matchingNames = new HashSet<String>();
                    for (String matche : matches)
                    {
                        matchingNames.addAll(normalizedMap.get(matche));
                    }
                    duplicatesSet.add(matchingNames);
                }
            }

            return new ArrayList<Set<String>>(duplicatesSet);
        }
        finally
        {
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

        }
    }

}
