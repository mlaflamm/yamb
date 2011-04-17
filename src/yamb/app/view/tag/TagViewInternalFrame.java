package yamb.app.view.tag;

import yamb.app.ApplicationContext;
import yamb.app.tool.export.TagMklinkBatchWriter;
import yamb.app.fileitem.FileItem;
import yamb.app.fileitem.filelist.FileListItem;
import yamb.app.fileitem.filelist.FileListItemCache;
import yamb.app.fileitem.filelist.FileListModel;
import yamb.app.fileitem.filelist.FileListSelectionManager;
import yamb.app.fileitem.filelist.thumbnail.GlobalThumbnailCache;
import yamb.app.tag.DefaultTagContext;
import yamb.app.tag.SelectedFilesTagListModel;
import yamb.app.tag.TagCategory;
import yamb.app.tag.TagContext;
import yamb.app.tag.TagEvent;
import yamb.app.tag.TagEventListener;
import yamb.app.tag.TagJList;
import yamb.app.tag.TagListModel;
import yamb.app.tag.Tags;
import yamb.app.tag.library.LibraryJList;
import yamb.app.tag.library.LibraryListModel;
import yamb.app.tag.library.LibraryManager;
import yamb.app.tag.series.Series;
import yamb.app.view.AbstractFileListViewInternalFrame;
import yamb.app.view.FileListViewContext;
import yamb.app.view.FilePropertiesPanel;
import yamb.app.view.explorer.ActiveFolder;
import yamb.util.Disposable;
import yamb.util.commands.ActionFactory;
import yamb.util.commands.ActionModel;
import yamb.util.commands.CommandException;
import yamb.util.commands.CommandGroupModel;
import yamb.util.commands.CommandItem;
import yamb.util.commands.CommandProvider;
import yamb.util.commands.CommandWidgetFactory;
import yamb.util.commands.DefaultActionModel;
import yamb.util.io.Files;
import yamb.util.media.mediainfo.cache.MediaInfoCache;
import yamb.util.swing.AbstractObjectFilter;
import yamb.util.swing.DocumentPatternFilter;
import yamb.util.swing.FilterEvent;
import yamb.util.swing.ListModels;
import yamb.util.swing.ObjectFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
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
import javax.swing.JTree;
import javax.swing.ListModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
 * @author manuel.laflamme
 * @since 7-Aug-2008
 */
public class TagViewInternalFrame extends AbstractFileListViewInternalFrame
        implements PropertyChangeListener, ActionFactory
{
    private static final Logger LOGGER = Logger.getLogger(TagViewInternalFrame.class);
    private static List<String> EMPTY_STRINGLIST = Collections.emptyList();

    private final DefaultTagViewContext mViewContext;

    private final JMenu mLibraryPopupMenu;
    private LibraryJList mLibraryJList;
    private ObjectFilter mRecentFilter;
    private JTree mDuplicateFilesTree = new JTree();
    private final JTextField mTagFilterField = new TagFilterField();


    public TagViewInternalFrame(
            List<String> aActiveTags,
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
                new DefaultTagViewContext(new DefaultTagContext(aAppContext.getTagManager())));
        mViewContext = (DefaultTagViewContext) getViewContext();
        mViewContext.setSelectionManager(getFileListSelectionManager());
        mRecentFilter = new RecentObjectFilter();

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("All", new ImageIcon(getClass().getClassLoader().getResource("yamb/images/tags.gif")),
                new JScrollPane(createGlobalTagListComponent(mViewContext, false/*aActiveTags.size() > 0*/)));
        tabbedPane.addTab("Favorites", new ImageIcon(getClass().getClassLoader().getResource("yamb/images/favorites.gif")),
                new JScrollPane(createFavoriteTagsComponent(mViewContext)));
//        TagManager tagManager = aAppContext.getTagManager();
        LibraryManager libraryManager = mAppContext.getLibraryManager();
        tabbedPane.addTab("Dup. Files", new ImageIcon(getClass().getClassLoader().getResource("yamb/images/search.gif")),
                createDuplicateFilesComponent());
        tabbedPane.addTab("Libraries", new ImageIcon(getClass().getClassLoader().getResource("yamb/images/library.gif")),
                createLibraryComponent(libraryManager, aCommandProvider));

        JSplitPane splitPane = ((JSplitPane) getContentPane().getComponent(0));
        int dividerLocation = splitPane.getDividerLocation();
        splitPane.setLeftComponent(tabbedPane);
        splitPane.setDividerLocation(dividerLocation);
        splitPane.getLeftComponent().requestFocus();

        // Popup menus
        mLibraryPopupMenu = createMenu("libraryPopup", aGlobalActionModel);

        setFrameIcon(new ImageIcon(getClass().getClassLoader().getResource("yamb/images/tags.gif")));

        // Internal frame listener (activated & closed)
        addInternalFrameListener(new InternalFrameHandler());

        // Setup view context
        mViewContext.setSelectionManager(getFileListSelectionManager());
        mViewContext.setActionModel(new DefaultActionModel(this));
        mViewContext.addPropertyChangeListener(this);
        TagEventHandler tagEventHandler = new TagEventHandler();
        addDisposable(tagEventHandler);
        mViewContext.getTagContext().addTagEventListener(tagEventHandler);
        mViewContext.getTagContext().addActiveTags(aActiveTags);

        if (aActiveTags.isEmpty())
        {
            updateActiveTags(mViewContext);
        }
        else
        {
            // Set tags filter
            setTagFilterText(createFilterValue(aActiveTags));
        }
    }

    private Component createLibraryComponent(LibraryManager aTagManager, CommandProvider aCommandProvider) throws CommandException
    {
        mLibraryJList = new LibraryJList(aTagManager);
        addDisposable(mLibraryJList);
        mLibraryJList.setModel(new LibraryListModel(aTagManager));
        mLibraryJList.addMouseListener(new LibraryListMouseAdapter());

        CommandItem toolbarItem = aCommandProvider.getToolBarItem("libraryToolbar");
        JToolBar toolbar = CommandWidgetFactory.buildToolBar(toolbarItem, getGlobalActionModel(), getCommandGroupModel());
        toolbar.setFloatable(false);

        JScrollPane libraryScrollPane = new JScrollPane(mLibraryJList);
        libraryScrollPane.setColumnHeaderView(toolbar);

        return libraryScrollPane;
    }

    private Component createFavoriteTagsComponent(TagViewContext aViewContext)
    {
        TagContext tagContext = aViewContext.getTagContext();

        TagJList favoriteList = new TagJList(tagContext);
        addDisposable(favoriteList);
        favoriteList.setModel(new TagListModel(tagContext, TagCategory.FAVORITE));
        favoriteList.addMouseListener(new TagListMouseAdapter());
        return favoriteList;
    }


    private Component createGlobalTagListComponent(TagViewContext aViewContext, boolean aSelectActiveTab) throws CommandException
    {
        TagContext tagContext = aViewContext.getTagContext();

        // 1. Top pane: global tags list

        // Setup global tag list filter
        DocumentPatternFilter tagFilter = new DocumentPatternFilter();
        Component filterComponent = createTagFilterComponent(tagFilter);

        // Setup global tags list
        TagJList globalTagList = new TagJList(tagContext);
        addDisposable(globalTagList);
        ListModel model = new TagListModel(tagContext, TagCategory.GLOBAL);
        model = ListModels.filteredModel(model, tagFilter);
        globalTagList.setModel(model);
        globalTagList.addMouseListener(new TagListMouseAdapter());

        JScrollPane globalScrollPane = new JScrollPane(globalTagList);
//        globalScrollPane.setColumnHeaderView(filterComponent);

        JComponent globalTagListPanel = new JPanel(new BorderLayout());
        globalTagListPanel.add("North", filterComponent);
        globalTagListPanel.add("Center", globalScrollPane);

        // 2. Bottom pane: tabbed tag lists (properties, active tags & selected file tags)

        // Setup selected files tags list
        TagJList selectedFilesTagList = new TagJList(tagContext);
        addDisposable(selectedFilesTagList);
        selectedFilesTagList.setModel(new SelectedFilesTagListModel(
                tagContext, aViewContext.getFileListSelectionManager()));
        selectedFilesTagList.addMouseListener(new TagListMouseAdapter());

        // Setup active tags list
        TagJList activeTagList = new TagJList(aViewContext.getTagContext());
        addDisposable(activeTagList);
        activeTagList.setModel(new TagListModel(aViewContext.getTagContext(), TagCategory.ACTIVE));
        activeTagList.addMouseListener(new TagListMouseAdapter());

        JTabbedPane tabbedPane = new JTabbedPane();
        FilePropertiesPanel filePropertiesPanel = new FilePropertiesPanel(aViewContext, getMediaInfoCache());
        addDisposable(filePropertiesPanel);
        filePropertiesPanel.setFont(tabbedPane.getFont());
        tabbedPane.addTab("File Properties", new JScrollPane(filePropertiesPanel));
        tabbedPane.addTab("Active Tags", new JScrollPane(activeTagList));
        tabbedPane.addTab(/*"Selected */"Files Tags", new JScrollPane(selectedFilesTagList));
        tabbedPane.addTab("Dup. Tags", createDuplicateTagsComponent());
//        tabbedPane.addTab("Duplicates", new JScrollPane(selectedFilesTagList));
        if (aSelectActiveTab)
        {
            tabbedPane.setSelectedIndex(1);
        }

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT, /*globalScrollPane*/globalTagListPanel, tabbedPane);
        splitPane.setOneTouchExpandable(false);
        splitPane.setDividerLocation(400);
        splitPane.setDividerSize(5);
        return splitPane;
    }

    private Component createTagFilterComponent(DocumentPatternFilter aTagFilter) throws CommandException
    {
        mTagFilterField.getDocument().addDocumentListener(aTagFilter);

        CommandProvider commandProvider = getCommandProvider();
        CommandItem toolbarItem = commandProvider.getToolBarItem("tagFilterToolbar");
        CommandGroupModel commandGroupModel = getCommandGroupModel();

        JToolBar toolbar = CommandWidgetFactory.buildToolBar(toolbarItem, getGlobalActionModel(),
                commandGroupModel);

        toolbar.add(mTagFilterField, 0);
        toolbar.setFloatable(false);

        return toolbar;
    }

    private void setTagFilterText(String aText)
    {
        if (mTagFilterField.getText().equals(aText))
        {
            return;
        }

        mTagFilterField.setText(aText);
    }

    private Component createDuplicateFilesComponent()
    {
        JToolBar toolbar = new JToolBar();
        JButton findButton = new JButton();
        findButton.setAction(new FindDuplicateFilesAction());
        findButton.setText("Find...");
        findButton.setIcon(new ImageIcon(getClass().getClassLoader().getResource("yamb/images/search.gif")));
        toolbar.add(findButton);
        toolbar.addSeparator();
        toolbar.setFloatable(false);

        mDuplicateFilesTree.setRootVisible(false);
        mDuplicateFilesTree.addTreeSelectionListener(new TreeSelectionListener()
        {
            public void valueChanged(TreeSelectionEvent aEvent)
            {
                TreePath path = aEvent.getPath();
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();

                String tag = null;
                String fileListFilter = null;

                // Tag selected, create a file filter with all children groups
                if (node.getChildCount() > 0)
                {
                    tag = (String) node.getUserObject();
                    StringBuilder builder = new StringBuilder();
                    for (int i = 0; i < node.getChildCount(); i++)
                    {
                        if (builder.length() > 0)
                        {
                            builder.append("|");
                        }
                        builder.append(((DefaultMutableTreeNode) node.getChildAt(i)).getUserObject());
                    }
                    fileListFilter = builder.toString();
                }
                // Group selected
                else
                {
                    tag = (String) ((DefaultMutableTreeNode) node.getParent()).getUserObject();
                    fileListFilter = (String) node.getUserObject();
                }

                setFileFilterText(fileListFilter);
                TagContext tagContext = mViewContext.getTagContext();
                tagContext.removeActiveTags(tagContext.getTags(TagCategory.ACTIVE));
                tagContext.addActiveTags(Arrays.asList(tag));
            }
        });

        JScrollPane scrollPane = new JScrollPane(mDuplicateFilesTree);
        scrollPane.setColumnHeaderView(toolbar);

        return scrollPane;
    }

    private class FindDuplicateFilesAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            DefaultTreeModel treeModel = new DefaultTreeModel(createDuplicateFilesNodes());
            mDuplicateFilesTree.setModel(treeModel);

            // Expand tag with more than one series
            TreeNode root = (TreeNode) treeModel.getRoot();
            for (int i = 0; i < root.getChildCount(); i++)
            {
                TreeNode child = root.getChildAt(i);
                if (child.getChildCount() > 1)
                {
                    mDuplicateFilesTree.expandPath(new TreePath(new Object[]{root, child}));
                }
            }
        }
    }

    public DefaultMutableTreeNode createDuplicateFilesNodes()
    {
        List<String> allNames = mViewContext.getTagContext().getTags(TagCategory.GLOBAL);

        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("root");

        Map<String, List<String>> tagSeries = new TreeMap<String, List<String>>();
        for (String name : allNames)
        {
            List<File> files = mViewContext.getTagContext().getFiles(name);
            List<String> series = new ArrayList<String>(files.size());

            for (File file : files)
            {
                series.add(Series.getSeriesName(file.getName()));
            }
            tagSeries.put(name, series);
        }

        for (Map.Entry<String, List<String>> entry : tagSeries.entrySet())
        {
            List<String> seriesList = entry.getValue();
            Collections.sort(seriesList);

            DefaultMutableTreeNode tagNode = null;

            String lastSeries = null;
            int count = 0;
            for (String series : seriesList)
            {
                if (!series.equals(lastSeries))
                {
                    if (count > 1)
                    {
                        if (tagNode == null)
                        {
                            tagNode = new DefaultMutableTreeNode(entry.getKey());
                            rootNode.add(tagNode);
                        }

                        DefaultMutableTreeNode seriesNode = new DefaultMutableTreeNode(lastSeries);
                        tagNode.add(seriesNode);
                    }
                    count = 0;
                }

                count++;
                lastSeries = series;
            }
        }

        return rootNode;
    }

    private Component createDuplicateTagsComponent()
    {
        final JList duplicateTagList = new JList();
        final SpinnerNumberModel spinnerModel = new SpinnerNumberModel(0, 0, 3, 1);

        JToolBar toolbar = new JToolBar();
        JButton findButton = new JButton();
        findButton.setAction(new AbstractAction()
        {
            public void actionPerformed(ActionEvent e)
            {
                final List<Set<String>> duplicateTags = findDuplicateTags(spinnerModel.getNumber().intValue());
                duplicateTagList.setModel(new AbstractListModel()
                {
                    public int getSize()
                    {
                        return duplicateTags.size();
                    }

                    public Object getElementAt(int index)
                    {
                        return duplicateTags.get(index);
                    }
                });
            }
        });
        findButton.setText("Find...");
        findButton.setIcon(new ImageIcon(getClass().getClassLoader().getResource("yamb/images/search.gif")));
        toolbar.add(findButton);
        toolbar.addSeparator();
        toolbar.add(new JLabel("Levenshtein Distance "));
        toolbar.add(new JSpinner(spinnerModel));
        toolbar.setFloatable(false);

        duplicateTagList.addListSelectionListener(new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent aEvent)
            {
                // Reset selected tags
                TagContext tagContext = mViewContext.getTagContext();
                tagContext.removeActiveTags(tagContext.getTags(TagCategory.ACTIVE));

                // Set new active tags
                Collection<String> tags = (Collection<String>) duplicateTagList.getSelectedValue();
                tagContext.addActiveTags(tags != null ? new ArrayList<String>(tags) : EMPTY_STRINGLIST);

                // Set tags filter
                setTagFilterText(createFilterValue(tags));
            }
        });

        JScrollPane scrollPane = new JScrollPane(duplicateTagList);
        scrollPane.setColumnHeaderView(toolbar);

        return scrollPane;
    }

    private class FindDuplicateTagsAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
        }
    }

    private void updateActiveTags(TagViewContext aViewContext)
    {
        updateTagCommandGrouping();

        TagContext tagContext = aViewContext.getTagContext();
        List<String> activeTags = tagContext.getTags(TagCategory.ACTIVE);

        setTitle(activeTags.toString());
        List<File> files = tagContext.getFiles(activeTags, aViewContext.getTagSetMode());
        Collections.sort(files, Series.FILE_NAMECOMPARATOR);
        ListModel model = new FileListModel(files.toArray(new File[0]), getFileListItemCache(), getThumbnailCache(),
                getAppContext().getFileOperation(), false, false);
        setFileListModel(model, mRecentFilter);
    }

    private void updateTagCommandGrouping()
    {
        switch (mViewContext.getTagSetMode())
        {
            case AND:
                mCommandGroupModel.setSelected("tagSetAnd", true);
                break;
            case OR:
                mCommandGroupModel.setSelected("tagSetOr", true);
                break;
        }

        mCommandGroupModel.setSelected("tagRecentOnly", mViewContext.displaysRecentOnly());
    }

    private class InternalFrameHandler extends InternalFrameAdapter
    {
        public void internalFrameActivated(InternalFrameEvent aEvent)
        {
            updateTagCommandGrouping();
        }

        public void internalFrameDeactivated(InternalFrameEvent aEvent)
        {
            mCommandGroupModel.setSelected("tagSetAnd", false);
            mCommandGroupModel.setSelected("tagSetOr", false);
            mCommandGroupModel.setSelected("tagRecentOnly", false);
        }

        public void internalFrameClosed(InternalFrameEvent aEvent)
        {
        }
    }

    private class TagFilterField extends JTextField
    {
        @Override
        public void paste()
        {
            try
            {
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                if (clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor))
                {
                    Transferable contents = clipboard.getContents(null);
                    String value = (String) contents.getTransferData(DataFlavor.stringFlavor);
                    replaceSelection(value.replace(",", "|").replace("&", "|"));
                }
            }
            catch (UnsupportedFlavorException e)
            {
                LOGGER.error("Paste error", e);
            }
            catch (IOException e)
            {
                LOGGER.error("Paste error", e);
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // TagEventListener interface

    private class TagEventHandler implements TagEventListener, Disposable
    {
        public void tagAdded(TagEvent aEvent)
        {
            if (aEvent.getCategory() == TagCategory.ACTIVE)
            {
                updateActiveTags(mViewContext);
            }
        }

        public void tagRemoved(TagEvent aEvent)
        {
            if (aEvent.getCategory() == TagCategory.ACTIVE)
            {
                updateActiveTags(mViewContext);
            }
        }

        public void dispose()
        {
            getAppContext().getTagManager().removeTagEventListener(this);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // PropertyChangeListener interface

    public void propertyChange(PropertyChangeEvent aEvent)
    {
        String propertyName = aEvent.getPropertyName();
        if (TagViewContext.TAGSET_MODE.equals(propertyName))
        {
            updateActiveTags(mViewContext);
        }
//        else if (TagViewContext.RECENT_ONLY.equals(propertyName))
//        {
//            updateActiveTags(mViewContext);
//        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // ActionFactory interface

    @Override
    public Action createAction(String aId)
    {
        // Override the default action to exclude the active tags
        if ("fileFilterSelectedTags".equals(aId))
        {
            return new AbstractAction()
            {
                public void actionPerformed(ActionEvent aEvent)
                {
                    FileListSelectionManager selectionManager = mViewContext.getFileListSelectionManager();
                    List<FileItem> selectedItems = selectionManager.getSelectedItems();
                    Set<String> tags = new TreeSet<String>();
                    for (FileItem item : selectedItems)
                    {
                        tags.addAll(Arrays.asList(Tags.getTagsFromFileName(item.getFile().getName())));
                    }
                    // Excludes actives tags, otherwise the the filter is useless
                    tags.removeAll(mViewContext.getTagContext().getTags(TagCategory.ACTIVE));
                    setFileFilterText(createFilterValue(tags));
                }
            };
        }

        if (aId.equals("fileNormalizeTagNames"))
        {
            return new AbstractAction()
            {
                public void actionPerformed(ActionEvent aEvent)
                {
                    if (mAppContext.getActiveViewContext() instanceof FileListViewContext)
                    {
                        FileListViewContext viewContext = (FileListViewContext) mAppContext.getActiveViewContext();
                        FileItem focusedItem = viewContext.getFocusedItem();
                        if (focusedItem instanceof FileListItem)
                        {
                            FileListSelectionManager selectionManager = viewContext.getFileListSelectionManager();
                            if (!selectionManager.isSelectionEmpty())
                            {
                                List<File> selectedFiles = selectionManager.getSelectedFiles();
                                for (File file : selectedFiles)
                                {
                                    try
                                    {
                                        String fileName = file.getName();
                                        String[] tags = Tags.getTagsFromFileName(fileName);
                                        Arrays.sort(tags);
                                        if (tags.length > 0)
                                        {
                                            String newFileName = Tags.setFileNameTags(fileName, tags);
                                            if (!fileName.equals(newFileName))
                                            {
                                                getAppContext().getFileOperation().renameFile(file, newFileName);
                                            }
                                        }
                                    }
                                    catch (IOException e)
                                    {
                                        LOGGER.error("Unable to rename " + file, e);
                                    }
                                }
                            }
                        }
                    }
                }
            };
        }


        if ("tagFilterActiveTags".equals(aId))
        {
            return new AbstractAction()
            {
                public void actionPerformed(ActionEvent aEvent)
                {
                    String newFilter = createFilterValue(mViewContext.getTagContext().getTags(TagCategory.ACTIVE));
                    setTagFilterText(newFilter);
                }
            };
        }

        if ("tagFilterReset".equals(aId))
        {
            return new AbstractAction()
            {
                public void actionPerformed(ActionEvent aEvent)
                {
                    setTagFilterText("");
                }
            };
        }

        if ("exportMklinkBatch".equals(aId))
        {
            return new AbstractAction()
            {
                public void actionPerformed(ActionEvent aEvent)
                {
                    JFileChooser chooser = new JFileChooser();
                    chooser.setMultiSelectionEnabled(false);
                    chooser.setSelectedFile(new File("tagmklink.bat"));
                    int option = chooser.showSaveDialog(null);
                    if (option == JFileChooser.APPROVE_OPTION)
                    {
                        TagMklinkBatchWriter.write(mViewContext.getTagContext(), chooser.getSelectedFile());
                    }
                }
            };
        }

        if ("viewRefresh".equals(aId))
        {
            return new AbstractAction()
            {
                public void actionPerformed(ActionEvent aEvent)
                {
                    FileListSelectionManager selectionManager = mViewContext.getFileListSelectionManager();
                    List<File> selectedFiles = selectionManager.getSelectedFiles();

//                    File activeFile = selectionManager.getActiveFile();
                    updateActiveTags(mViewContext);
                    selectionManager.addSelectedFiles(selectedFiles);
//                    selectionManager.setActiveFile(activeFile);
                }
            };
        }

        if ("libraryLoadSelected".equals(aId))
        {
            return new AbstractAction()
            {
                public void actionPerformed(ActionEvent aEvent)
                {
                    LibraryManager libraryManager = mAppContext.getLibraryManager();
                    Object[] selectedLibraries = mLibraryJList.getSelectedValues();
                    for (Object selectedValue : selectedLibraries)
                    {
                        File library = (File) selectedValue;
                        libraryManager.loadLibrary(library);
                    }
                }
            };
        }

        if ("libraryCopy".equals(aId))
        {
            return new AbstractAction()
            {
                public void actionPerformed(ActionEvent aEvent)
                {
                    Object[] selectedLibraries = mLibraryJList.getSelectedValues();
                    StringBuilder libraries = new StringBuilder();
                    for (Object selectedValue : selectedLibraries)
                    {
                        File library = (File) selectedValue;
                        if (libraries.length() > 0)
                        {
                            libraries.append(", ");
                        }
                        libraries.append(library.getAbsolutePath());
                    }

                    StringSelection stringSelection = new StringSelection(libraries.toString());
                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    clipboard.setContents(stringSelection, stringSelection);
                }
            };
        }

        if ("libraryLoadSelectedOnly".equals(aId))
        {
            return new AbstractAction()
            {
                public void actionPerformed(ActionEvent aEvent)
                {
                    LibraryManager libraryManager = mAppContext.getLibraryManager();
                    libraryManager.unloadLibrary(libraryManager.getLibraryRoots());

                    Object[] selectedLibraries = mLibraryJList.getSelectedValues();
                    for (Object selectedValue : selectedLibraries)
                    {
                        File library = (File) selectedValue;
                        libraryManager.loadLibrary(library);
                    }
                }
            };
        }

        if ("libraryRebuildSelected".equals(aId))
        {
            return new AbstractAction()
            {
                public void actionPerformed(ActionEvent aEvent)
                {
                    LibraryManager libraryManager = mAppContext.getLibraryManager();
                    Object[] selectedLibraries = mLibraryJList.getSelectedValues();
                    for (Object selectedValue : selectedLibraries)
                    {
                        File library = (File) selectedValue;
                        libraryManager.rebuildLibrary(library);
                    }
                }
            };
        }

        if ("libraryUnloadSelected".equals(aId))
        {
            return new AbstractAction()
            {
                public void actionPerformed(ActionEvent aEvent)
                {
                    LibraryManager libraryManager = mAppContext.getLibraryManager();
                    ListModel model = mLibraryJList.getModel();

                    int[] selectedIndices = mLibraryJList.getSelectedIndices();
                    ArrayList<File> libraries = new ArrayList<File>(selectedIndices.length);
                    for (int index : selectedIndices)
                    {
                        libraries.add((File) model.getElementAt(index));

                    }
                    libraryManager.unloadLibrary(libraries);
                }
            };
        }

        if ("libraryRemoveSelected".equals(aId))
        {
            return new AbstractAction()
            {
                public void actionPerformed(ActionEvent aEvent)
                {
                    LibraryManager libraryManager = mAppContext.getLibraryManager();
                    ListModel model = mLibraryJList.getModel();

                    Object[] selectedLibraries = mLibraryJList.getSelectedValues();
                    for (int i = 0; i < selectedLibraries.length; i++)
                    {
                        // todo : batch remove
                        File libraryRoot = (File) selectedLibraries[i];
                        libraryManager.removeLibrary(libraryRoot);
                    }
                }
            };
        }
        if ("libraryOpenExplorerView".equals(aId))
        {
            return new AbstractAction()
            {
                public void actionPerformed(ActionEvent aEvent)
                {
                    ListModel model = mLibraryJList.getModel();

                    int[] selectedIndices = mLibraryJList.getSelectedIndices();
                    for (int index : selectedIndices)
                    {
                        File libraryRoot = (File) model.getElementAt(index);
                        getAppContext().getViewFactory().createExploreView(new ActiveFolder(libraryRoot));
                    }
                }
            };
        }

        return super.createAction(aId);
    }

    private class RecentObjectFilter extends AbstractObjectFilter implements ObjectFilter, PropertyChangeListener
    {
        public RecentObjectFilter()
        {
            mViewContext.addPropertyChangeListener(this);
        }

        ///////////////////////////////////////////////////////////////////////////
        // ObjectFilter interface

        public boolean accept(Object aObject)
        {
            if (mViewContext.displaysRecentOnly())
            {
                FileItem fileItem = (FileItem) aObject;
                return Files.isRecent(fileItem.getFile());
            }

            return true;
        }

        public boolean acceptAll()
        {
            return !mViewContext.displaysRecentOnly();
        }

        ///////////////////////////////////////////////////////////////////////////
        // PropertyChangeListener interface

        public void propertyChange(PropertyChangeEvent aEvent)
        {
            String propertyName = aEvent.getPropertyName();
            if (TagViewContext.RECENT_ONLY.equals(propertyName))
            {
                updateTagCommandGrouping();
                fireFilterChanged(new FilterEvent(this));
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // MouseAdapter class

    private class LibraryListMouseAdapter extends MouseAdapter
    {
        public void mouseReleased(MouseEvent aEvent)
        {
            if (aEvent.isPopupTrigger())
            {
                JList libraryList = ((JList) aEvent.getComponent());
                int index = libraryList.locationToIndex(aEvent.getPoint());
                if (index >= 0 && libraryList.getCellBounds(index, index).contains(aEvent.getPoint()))
                {
                    JPopupMenu popupMenu = mLibraryPopupMenu.getPopupMenu();
                    popupMenu.setLightWeightPopupEnabled(true);
                    popupMenu.show(aEvent.getComponent(), aEvent.getX(), aEvent.getY());
                }
            }
        }
    }

    private class TagListMouseAdapter extends MouseAdapter
    {
        public void mouseReleased(MouseEvent aEvent)
        {
            if (aEvent.isPopupTrigger())
            {
                try
                {
                    JList tagList = ((JList) aEvent.getComponent());
                    int index = tagList.locationToIndex(aEvent.getPoint());
                    if (index >= 0 && tagList.getCellBounds(index, index).contains(aEvent.getPoint()))
                    {
                        JMenu tagMenu = CommandWidgetFactory.buildMenu(getCommandProvider().getMenuItem("tagPopup"),
                                new TagListActionModel(tagList, getGlobalActionModel()));

                        JPopupMenu popupMenu = tagMenu.getPopupMenu();
                        popupMenu.setLightWeightPopupEnabled(true);
                        popupMenu.show(aEvent.getComponent(), aEvent.getX(), aEvent.getY());
                    }
                }
                catch (CommandException e)
                {
                    throw new IllegalStateException(e);
                }
            }
        }
    }

    private class TagListActionModel implements ActionModel
    {
        private ActionModel mActionModel;
        private JList mTagList;

        public TagListActionModel(JList aTagList, ActionModel aActionModel)
        {
            mActionModel = aActionModel;
            mTagList = aTagList;
        }

        public Action getAction(String aId)
        {
            if (aId.equals("tagCopy"))
            {
                return new TagCopy();
            }

            if (aId.equals("tagRename"))
            {
                return new TagRename();
            }

            if (aId.equals("tagActivateSelected"))
            {
                return new TagActivateSelected();
            }

            if (aId.equals("tagActivateSelectedOnly"))
            {
                return new TagActivateSelectedOnly();
            }

            if (aId.equals("tagDeactivateSelected"))
            {
                return new TagDeactivateSelected();
            }

            if (aId.equals("tagActivateAll"))
            {
                return new TagActivateAll();
            }

            if (aId.equals("tagDeactivateAll"))
            {
                return new TagDeactivateAll();
            }

            if (aId.equals("tagSearchIafd"))
            {
                return new TagSearch();
            }

            return mActionModel.getAction(aId);
        }


        private class TagActivateSelected extends javax.swing.AbstractAction
        {
            public void actionPerformed(ActionEvent aEvent)
            {
                // Take a snapshot of selected tags from list model
                int[] indexes = mTagList.getSelectedIndices();
                ListModel model = mTagList.getModel();
                List<String> selectedTags = new ArrayList<String>(indexes.length);
                for (int i = 0; i < indexes.length; i++)
                {
                    selectedTags.add((String) model.getElementAt(indexes[i]));
                }

                TagContext tagContext = mViewContext.getTagContext();
                tagContext.addActiveTags(selectedTags);
            }
        }

        private class TagCopy extends javax.swing.AbstractAction
        {
            public void actionPerformed(ActionEvent aEvent)
            {
                // Take a snapshot of selected tags from list model
                int[] indexes = mTagList.getSelectedIndices();
                ListModel model = mTagList.getModel();
                StringBuilder selectedTags = new StringBuilder();
                for (int indexe : indexes)
                {
                    if (selectedTags.length() > 0)
                    {
                        selectedTags.append(", ");
                    }
                    selectedTags.append(model.getElementAt(indexe));
                }

                StringSelection stringSelection = new StringSelection(selectedTags.toString());
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(stringSelection, stringSelection);
            }
        }

        private class TagRename extends javax.swing.AbstractAction
        {
            public void actionPerformed(ActionEvent aEvent)
            {
                String selectedValue = (String) mTagList.getSelectedValue();
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

                    // If a string was returned, rename the tag.
                    if ((s != null) && (s.length() > 0) && !s.equals(selectedValue))
                    {
                        List<File> files = mViewContext.getTagContext().getFiles(selectedValue);
                        for (File file : files)
                        {
                            try
                            {
                                String fileName = file.getName();
                                String newFileName = Tags.renameFileNameTag(fileName, selectedValue, s);
                                if (!fileName.equals(newFileName))
                                {
                                    getAppContext().getFileOperation().renameFile(file, newFileName);
                                }
                            }
                            catch (IOException e)
                            {
                                LOGGER.error("Unable to rename " + file, e);
                            }
                        }
                    }

                }
            }
        }

        private class TagSearch extends javax.swing.AbstractAction
        {
            public void actionPerformed(ActionEvent aEvent)
            {
                String iafd = "http://www.iafd.com/results.asp?searchtype=comprehensive&searchstring=";
                String fifialfa = "http://fifialfa.com/search.php?search=";

                String selectedValue = (String) mTagList.getSelectedValue();
                if (selectedValue != null)
                {
                    String url = iafd + selectedValue.replaceAll(" ", "+");
                    try
                    {
                        Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
                    }
                    catch (IOException e)
                    {
                        LOGGER.error("Cannot open: " + url, e);
                    }
                }
            }
        }

        private class TagActivateSelectedOnly extends javax.swing.AbstractAction
        {
            public void actionPerformed(ActionEvent aEvent)
            {
                // Take a snapshot of selected tags from list model
                int[] selectedIndices = mTagList.getSelectedIndices();
                ListModel model = mTagList.getModel();
                List<String> selectedTags = new ArrayList<String>(selectedIndices.length);
                for (int i = 0; i < selectedIndices.length; i++)
                {
                    selectedTags.add((String) model.getElementAt(selectedIndices[i]));
                }

                TagContext tagContext = mViewContext.getTagContext();
                List<String> activeTags = tagContext.getTags(TagCategory.ACTIVE);
                activeTags.removeAll(selectedTags);
                tagContext.removeActiveTags(activeTags);
                tagContext.addActiveTags(selectedTags);
            }
        }

        private class TagActivateAll extends javax.swing.AbstractAction
        {
            public void actionPerformed(ActionEvent aEvent)
            {
                // Take a snapshot of all tags from list model
                ListModel model = mTagList.getModel();
                List<String> tags = new ArrayList<String>();
                for (int i = 0; i < model.getSize(); i++)
                {
                    tags.add((String) model.getElementAt(i));
                }

                TagContext tagContext = mViewContext.getTagContext();
                tagContext.addActiveTags(tags);
            }
        }

        private class TagDeactivateAll extends javax.swing.AbstractAction
        {
            public void actionPerformed(ActionEvent aEvent)
            {
                // Take a snapshot of all tags from list model
                ListModel model = mTagList.getModel();
                List<String> tags = new ArrayList<String>();
                for (int i = 0; i < model.getSize(); i++)
                {
                    tags.add((String) model.getElementAt(i));
                }

                TagContext tagContext = mViewContext.getTagContext();
                tagContext.removeActiveTags(tags);
            }
        }

        private class TagDeactivateSelected extends javax.swing.AbstractAction
        {
            public void actionPerformed(ActionEvent aEvent)
            {
                // Take a snapshot of selected tags from list model
                int[] selectedIndices = mTagList.getSelectedIndices();
                ListModel model = mTagList.getModel();
                List<String> selectedTags = new ArrayList<String>(selectedIndices.length);
                for (int i = 0; i < selectedIndices.length; i++)
                {
                    selectedTags.add((String) model.getElementAt(selectedIndices[i]));
                }

                TagContext tagContext = mViewContext.getTagContext();
                tagContext.removeActiveTags(selectedTags);
            }
        }
    }

    public List<Set<String>> findDuplicateTags(int aLevenshteinDistance)
    {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try
        {
            List<String> allNames = mViewContext.getTagContext().getTags(TagCategory.GLOBAL);

            Map<String, List<String>> normalizedMap = new HashMap<String, List<String>>();
            for (String name : allNames)
            {
                int nameLen = name.length();
                StringBuilder builder = new StringBuilder(nameLen);

                for (int i = 0; i < nameLen; i++)
                {
                    char ch = name.charAt(i);
                    if (Character.isLetter(ch))
                    {
                        builder.append(Character.toLowerCase(ch));
                    }
                }

                String normalizedName = builder.toString();
                if (!normalizedMap.containsKey(normalizedName))
                {
                    normalizedMap.put(normalizedName, new ArrayList<String>());
                }
                normalizedMap.get(normalizedName).add(name);
            }


            Set<Set<String>> duplicatesSet = new HashSet<Set<String>>();
            for (List<String> list : normalizedMap.values())
            {
                if (list.size() > 1)
                {
                    duplicatesSet.add(new HashSet<String>(list));
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
