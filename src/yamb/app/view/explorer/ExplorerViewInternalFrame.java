package yamb.app.view.explorer;

import yamb.app.ApplicationContext;
import yamb.app.fileitem.FileItem;
import yamb.app.fileitem.filelist.FileListItem;
import yamb.app.fileitem.filelist.FileListItemCache;
import yamb.app.fileitem.filelist.FileListModel;
import yamb.app.fileitem.filelist.FileListSelectionManager;
import yamb.app.fileitem.filelist.thumbnail.GlobalThumbnailCache;
import yamb.app.fileitem.folders.FolderJTree;
import yamb.app.fileitem.folders.FolderTreeCellRenderer;
import yamb.app.fileitem.folders.FolderTreeItem;
import yamb.app.fileitem.folders.FolderTreeModel;
import yamb.app.fileitem.folders.FolderTreeTransferHandler;
import yamb.app.fileitem.folders.RootFolderTreeItem;
import yamb.app.view.AbstractFileListViewInternalFrame;
import yamb.util.Disposable;
import yamb.util.commands.ActionFactory;
import yamb.util.commands.ActionModel;
import yamb.util.commands.CommandException;
import yamb.util.commands.CommandGroupModel;
import yamb.util.commands.CommandItem;
import yamb.util.commands.CommandProvider;
import yamb.util.commands.CommandWidgetFactory;
import yamb.util.commands.DefaultActionModel;
import yamb.util.io.AddressNavigationHistory;
import yamb.util.io.shell.FileOperationEvent;
import yamb.util.io.shell.FileOperationListener;
import yamb.util.media.VideoFileFilter;
import yamb.util.media.mediainfo.cache.MediaInfoCache;
import yamb.util.swing.DefaultPopupMenuButtonAction;
import de.cismet.tools.gui.JPopupMenuButton;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileFilter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.ListModel;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 * @author manuel.laflamme
 * @since 6-Aug-2008
 */
public class ExplorerViewInternalFrame extends AbstractFileListViewInternalFrame
        implements PropertyChangeListener, ActionFactory//, FileOperationListener
{
    private final DefaultExplorerViewContext mViewContext;

    private final AddressNavigationHistory mAddressHistory = new AddressNavigationHistory();
    private final JTree mFolderJTree;
    private int mLastDividerLocation;

    private final JPopupMenu mFolderPopupMenu;
    private final JPopupMenu mBackPopupMenu;
    private final JPopupMenu mForwardPopupMenu;
//    private final JPopupMenu mFileTypeFilterPopupMenu;

//    private JPopupMenuButton mFileTypeFilterButton;

    public ExplorerViewInternalFrame(
            ActiveFolder aActiveFolder,
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
                new DefaultExplorerViewContext());
        mViewContext = (DefaultExplorerViewContext) getViewContext();

        // Setup tree
        mFolderJTree = new FolderJTree();
        mFolderJTree.setCellRenderer(new FolderTreeCellRenderer(mViewContext, aAppContext.getLibraryManager()));
        FolderTreeModel treeModel = new FolderTreeModel(aAppContext.getFileOperation());
        addDisposable(treeModel);
        mFolderJTree.setModel(treeModel);
        mFolderJTree.setTransferHandler(new FolderTreeTransferHandler(aAppContext.getFileOperation()));
        mFolderJTree.setDragEnabled(true);

        ActiveFileOperationListener fileOperationListener = new ActiveFileOperationListener();
        addDisposable(fileOperationListener);
        aAppContext.getFileOperation().addFileOperationListener(fileOperationListener);

        // Tree selection listener
        mFolderJTree.addTreeSelectionListener(new TreeSelectionListener()
        {
            public void valueChanged(TreeSelectionEvent aEvent)
            {
                TreePath path = aEvent.getNewLeadSelectionPath();
                if (path != null)
                {
                    FolderTreeItem item = (FolderTreeItem) path.getLastPathComponent();
                    if (item != null && !item.getFile().equals(mViewContext.getActiveFolder().getFile()))
                    {
                        mViewContext.setActiveFolder(new ActiveFolder(item.getFile()));
                    }
                }
            }
        });

        // Tree mouse listener
        mFolderJTree.addMouseListener(new FileTreeMouseAdapter());


        JSplitPane splitPane = ((JSplitPane) getContentPane().getComponent(0));
        mLastDividerLocation = splitPane.getDividerLocation();
        if (mViewContext.getShowFolders())
        {
            splitPane.setLeftComponent(new JScrollPane(mFolderJTree));
            splitPane.setDividerLocation(mLastDividerLocation);
            splitPane.getLeftComponent().requestFocus();
        }

        // Address bar
        getContentPane().add(createToolbar(), java.awt.BorderLayout.NORTH);

        // Icon
        setFrameIcon(new ImageIcon(getClass().getClassLoader().getResource("yamb/images/folders2.gif")));

        // Popup menus
        mFolderPopupMenu = createMenu("folderPopup", aGlobalActionModel).getPopupMenu();
//        mFileTypeFilterPopupMenu = createMenu("fileTypeFilterPopup", aGlobalActionModel).getPopupMenu();
        mBackPopupMenu = new JPopupMenu();
        mForwardPopupMenu = new JPopupMenu();

        // Setup view context
        mViewContext.setSelectionManager(getFileListSelectionManager());
        mViewContext.setActionModel(new DefaultActionModel(this));
        mViewContext.addPropertyChangeListener(this);
        if (aActiveFolder != null)
        {
            mViewContext.setActiveFolder(aActiveFolder);
        }
    }

    private JToolBar createToolbar() throws CommandException
    {
        CommandProvider commandProvider = getCommandProvider();
        CommandItem toolbarItem = commandProvider.getToolBarItem("explorerToolbar");
        CommandGroupModel commandGroupModel = getCommandGroupModel();

        // Initialize the toolbar button map
        HashMap<String, AbstractButton> buttonMap = new HashMap<String, AbstractButton>();
/*
        JPopupMenuButton backButton = new JPopupMenuButton();
        backButton.setPopupMenu(mBackPopupMenu);
        buttonMap.put("explorerBack", backButton);

        JPopupMenuButton forwardButton = new JPopupMenuButton();
        forwardButton.setPopupMenu(mForwardPopupMenu);
        buttonMap.put("explorerForward", forwardButton);
*/

        JToolBar toolbar = CommandWidgetFactory.buildToolBar(toolbarItem, getGlobalActionModel(),
                commandGroupModel, buttonMap);


        // Add file type filter button
        toolbar.addSeparator();
        JPopupMenuButton filterButton = new JPopupMenuButton();
        JPopupMenu filterPopupMenu = createMenu("fileTypeFilterPopup", new DefaultActionModel(this)).getPopupMenu();
        filterButton.setAction(new DefaultPopupMenuButtonAction(filterButton, filterPopupMenu));
        filterButton.setPopupMenu(filterPopupMenu);
        ImageIcon filterIcon = new ImageIcon(getClass().getClassLoader().getResource("yamb/images/filter.gif"));
        filterButton.setIcon(filterIcon);
        toolbar.add(filterButton);
        setFileTypeFilterCommandGroupModel(commandGroupModel, mViewContext.getFileTypeFilter());

        // Add address field
        toolbar.addSeparator();
        toolbar.add(new JLabel("Address "));
        toolbar.add(new ActiveFolderTextField(mViewContext));
        toolbar.setFloatable(false);

        return toolbar;
    }

    private void updateNavigationPopupButtons()
    {
        mBackPopupMenu.removeAll();
        if (mAddressHistory.getBackCount() > 0)
        {
            List<String> backList = mAddressHistory.getBackList();
            for (int i = 0; i < Math.min(backList.size(), 5); i++)
            {
                JMenuItem menuItem = new JMenuItem(new File(backList.get(i)).getName());
                menuItem.setAction(new AddressHistoryAction(-i-1));
                mBackPopupMenu.add(menuItem);
            }
        }

        mForwardPopupMenu.removeAll();
        if (mAddressHistory.getForwardCount() > 0)
        {
            List<String> forwardList = mAddressHistory.getForwardList();
            for (int i = 0; i < Math.min(forwardList.size(), 5); i++)
            {
                JMenuItem menuItem = new JMenuItem(new File(forwardList.get(i)).getName());
                menuItem.setAction(new AddressHistoryAction(i));
                mForwardPopupMenu.add(menuItem);
            }
        }
    }

    private class AddressHistoryAction extends AbstractAction
    {
        private final int mCurrentDiff;

        public AddressHistoryAction(int aDiff)
        {
            assert(aDiff != 0);
            mCurrentDiff = aDiff;
        }

        public void actionPerformed(ActionEvent aEvent)
        {
            if (mCurrentDiff < 0)
            {
                for (int i = 0; i < -mCurrentDiff; i++)
                {
                    mAddressHistory.back();
                }
            }
            else
            {
                for (int i = 0; i < -mCurrentDiff; i++)
                {
                    mAddressHistory.forward();
                }
            }

            setActiveFolder(new ActiveFolder(new File(mAddressHistory.getCurrent())));
        }
    }

    private void setActiveFolder(ActiveFolder aActiveFolder) //throws IOException
    {
        // Update file list view
        if (aActiveFolder != null)
        {
            setTitle(aActiveFolder.getFile().getName());
            FileFilter fileFilter;
            switch (mViewContext.getFileTypeFilter())
            {
                case VIDEOS:
                    fileFilter = new VideoFileFilter(true);
                    break;
                case ALL:
                    fileFilter = TrueFileFilter.INSTANCE;
                    break;
                default:
                    throw new IllegalStateException("Unhandled filter " + mViewContext.getFileTypeFilter());
            }
            ListModel model = new FileListModel(aActiveFolder.getFile(), fileFilter,
                    getFileListItemCache(), getThumbnailCache(), getAppContext().getFileOperation(), aActiveFolder.isRecursive());
            setFileListModel(model);

            // Update tree selection
            if (mViewContext.getShowFolders())
            {

                RootFolderTreeItem root = (RootFolderTreeItem) mFolderJTree.getModel().getRoot();
                TreePath path = root.createTreePath(aActiveFolder.getFile(), false);
                mFolderJTree.scrollPathToVisible(path);
                mFolderJTree.setSelectionPath(path);
            }

            String currentHistory = mAddressHistory.getCurrent();
            String activeAddress = aActiveFolder.getAbsolutePath();
            if (currentHistory == null || !currentHistory.equalsIgnoreCase(activeAddress))
            {
                mAddressHistory.add(activeAddress);
                updateNavigationPopupButtons();
            }

/*
            // Update recent folder
            mRecentFolderList.clearSelection();
            mRecentFolderList.setSelectedValue(aActiveFolder.getFile(), true);
*/
        }
        else
        {
            setTitle("");
            ListModel model = new DefaultListModel();
            setFileListModel(model);
        }
    }

    private void setFileTypeFilterCommandGroupModel(CommandGroupModel aCommandGroupModel, FileTypeFilter aFileTypeFilter)
    {
        switch (aFileTypeFilter)
        {
            case VIDEOS:
                aCommandGroupModel.setSelected("fileTypeFilterVideos", true);
                break;
            case ALL:
                aCommandGroupModel.setSelected("fileTypeFilterAll", true);
                break;
            default:
                throw new IllegalStateException("Unhandled filter " + aFileTypeFilter);
        }
    }

    @Override
    protected JPopupMenu createFileItemPopupMenu(FileItem aFileItem)
    {
        if (aFileItem == null)
        {
            ActiveFolder activeFolder = mViewContext.getActiveFolder();
            FileListItem item = getFileListItemCache().getItem(activeFolder.getFile(), true);
            mViewContext.setFocusedItem(item);
            return mFolderPopupMenu;
        }

        if (aFileItem.isDirectory())
        {
            return mFolderPopupMenu;
        }

        return super.createFileItemPopupMenu(aFileItem);
    }

    ////////////////////////////////////////////////////////////////////////////
    // PropertyChangeListener interface

    public void propertyChange(PropertyChangeEvent aEvent)
    {
        String propertyName = aEvent.getPropertyName();
        if (ExplorerViewContext.ACTIVE_FOLDER.equals(propertyName))
        {
            ActiveFolder activeFolder = (ActiveFolder) aEvent.getNewValue();
            setActiveFolder(activeFolder);
        }
        if (ExplorerViewContext.FILETYPE_FILTER.equals(propertyName))
        {
            setActiveFolder(mViewContext.getActiveFolder());
        }
        else if (ExplorerViewContext.SHOW_FOLDERS.equals(propertyName))
        {
            JSplitPane splitPane = ((JSplitPane) getContentPane().getComponent(0));
            Boolean showFolders = (Boolean) aEvent.getNewValue();
            if (showFolders)
            {
                splitPane.setLeftComponent(new JScrollPane(mFolderJTree));
                splitPane.setDividerLocation(mLastDividerLocation);

                RootFolderTreeItem root = (RootFolderTreeItem) mFolderJTree.getModel().getRoot();
                TreePath path = root.createTreePath(mViewContext.getActiveFolder().getFile(), false);
                mFolderJTree.setSelectionPath(path);
                mFolderJTree.scrollPathToVisible(path);
            }
            else
            {
                mLastDividerLocation = splitPane.getDividerLocation();
                splitPane.setLeftComponent(null);
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // FileOperationListener interface

    private class ActiveFileOperationListener implements FileOperationListener, Disposable
    {
        public void fileRenamed(FileOperationEvent aEvent)
        {
            // Skip non directory
            if (!aEvent.getSourceFile().isDirectory())
            {
                return;
            }

            // Replace renamed folder in the navigation history
            if (mAddressHistory.replaceAll(aEvent.getSourceFile().getAbsolutePath(),
                    aEvent.getDestinationFile().getAbsolutePath()) > 0)
            {
                updateNavigationPopupButtons();
            }

            // If renamed folder is the active folder, set active folder to the destination
            ActiveFolder activeFolder = mViewContext.getActiveFolder();
            if (activeFolder != null)
            {
                if (aEvent.getSourceFile().equals(activeFolder.getFile()))
                {
                    mViewContext.setActiveFolder(new ActiveFolder(aEvent.getDestinationFile()));
                }
            }
        }

        public void fileCopied(FileOperationEvent aEvent)
        {
        }

        public void fileMoved(FileOperationEvent aEvent)
        {
            // Skip non directory
            if (!aEvent.getSourceFile().isDirectory())
            {
                return;
            }

            // Replace moved folder in the navigation history
            File sourceFile = aEvent.getSourceFile();
            File destinationFile = new File(aEvent.getDestinationFile(), sourceFile.getName());
            if (mAddressHistory.replaceAll(aEvent.getSourceFile().getAbsolutePath(),
                    destinationFile.getAbsolutePath()) > 0)
            {
                updateNavigationPopupButtons();
            }

            // If moved folder is the active folder, set active folder to the destination
            ActiveFolder activeFolder = mViewContext.getActiveFolder();
            if (activeFolder != null)
            {
                if (sourceFile.equals(activeFolder.getFile()))
                {
                    mViewContext.setActiveFolder(new ActiveFolder(destinationFile));
                }
            }
        }

        public void fileDeleted(FileOperationEvent aEvent)
        {
            // Skip non directory
            if (!aEvent.getSourceFile().isDirectory())
            {
                return;
            }

            // Remove deleted folder from the navigation history
            if (mAddressHistory.removeAll(aEvent.getSourceFile().getAbsolutePath()) > 0)
            {
                updateNavigationPopupButtons();
            }

            // If deleted folder is the active folder, set an alternate active folder
            ActiveFolder activeFolder = mViewContext.getActiveFolder();
            if (activeFolder != null)
            {
                if (aEvent.getSourceFile().equals(activeFolder.getFile()))
                {
                    File newActiveFolder = getAlternateActiveFolder(activeFolder.getFile());
                    setActiveFolder(new ActiveFolder(newActiveFolder));
                }
            }
        }

        public void dispose()
        {
            getAppContext().getFileOperation().removeFileOperationListener(this);
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
                    // Remember selected files in file list
                    FileListSelectionManager selectionManager = mViewContext.getFileListSelectionManager();
                    List<File> selectedFiles = selectionManager.getSelectedFiles();

                    // Remember expanded tree paths in folder tree
                    TreeModel model = mFolderJTree.getModel();
                    TreePath rootPath = new TreePath(model.getRoot());
                    Enumeration<TreePath> expandedPaths = mFolderJTree.getExpandedDescendants(rootPath);

                    // todo : Remember first visible path. Need to deal with proper scroll pane viewport
//                    TreePath oldVisiblePath = mFolderJTree.getClosestPathForLocation(1, 1);

                    // Refresh folder tree
                    model.valueForPathChanged(rootPath, null);

                    // Restore expanded tree paths
                    RootFolderTreeItem newRoot = (RootFolderTreeItem) model.getRoot();
                    while (expandedPaths.hasMoreElements())
                    {
                        TreePath oldPath = expandedPaths.nextElement();
                        TreePath newPath = newRoot.createTreePath(
                                ((FolderTreeItem) oldPath.getLastPathComponent()).getFile(), false);
                        mFolderJTree.expandPath(newPath);
                    }

                    // todo : Restore the first visible path
//                    TreePath newVisiblePath = newRoot.createTreePath(
//                            ((FolderTreeItem) oldVisiblePath.getLastPathComponent()).getFile(), newRoot, false);
//                    mFolderJTree.scrollPathToVisible(newVisiblePath);

                    // Refresh file list with active folder content.
                    // If the active folder does not exist anymore, fallback to a parent folder
                    // or sibling
                    File activeFolderFile = mViewContext.getActiveFolder().getFile();
                    if (!activeFolderFile.exists())
                    {
                        activeFolderFile = getAlternateActiveFolder(activeFolderFile);
                    }
                    setActiveFolder(new ActiveFolder(activeFolderFile));

                    // Restore selected files
                    selectionManager.addSelectedFiles(selectedFiles);
                }
            };
        }

        if ("explorerBack".equals(aId))
        {
            return new AbstractAction()
            {
                public void actionPerformed(ActionEvent aEvent)
                {
                    if (mAddressHistory.getBackCount() > 0)
                    {
                        String backAddress = mAddressHistory.back();
                        updateNavigationPopupButtons();

                        mViewContext.setActiveFolder(new ActiveFolder(new File(backAddress)));
                    }
                }
            };
        }

        if ("explorerForward".equals(aId))
        {
            return new AbstractAction()
            {
                public void actionPerformed(ActionEvent aEvent)
                {
                    if (mAddressHistory.getForwardCount() > 0)
                    {
                        String forwardAddress = mAddressHistory.forward();
                        updateNavigationPopupButtons();

                        mViewContext.setActiveFolder(new ActiveFolder(new File(forwardAddress)));
                    }
                }
            };
        }

        if ("explorerUp".equals(aId))
        {
            return new AbstractAction()
            {
                public void actionPerformed(ActionEvent aEvent)
                {
                    TreePath path = mFolderJTree.getSelectionPath();
                    if (path != null && path.getParentPath() != null)
                    {
                        TreePath parentPath = path.getParentPath();
                        FolderTreeItem item = (FolderTreeItem) parentPath.getLastPathComponent();
                        if (item != null && !(item instanceof RootFolderTreeItem))
                        {
                            mViewContext.setActiveFolder(new ActiveFolder(item.getFile()));
                        }
                    }
                }
            };
        }

        if ("fileTypeFilterAll".equals(aId))
        {
            return new AbstractAction()
            {
                public void actionPerformed(ActionEvent aEvent)
                {
                    mViewContext.setFileTypeFilter(FileTypeFilter.ALL);
                }
            };
        }

        if ("fileTypeFilterVideos".equals(aId))
        {
            return new AbstractAction()
            {
                public void actionPerformed(ActionEvent aEvent)
                {
                    mViewContext.setFileTypeFilter(FileTypeFilter.VIDEOS);
                }
            };
        }

/*
        if ("fileTypeFilterPopup".equals(aId))
        {
            return new AbstractAction()
            {
                public void actionPerformed(ActionEvent aEvent)
                {
                    mFileTypeFilterPopupMenu.setVisible(true);
                    mFileTypeFilterPopupMenu.show(mFileTypeFilterButton, 0, mFileTypeFilterButton.getHeight());
                }
            };
        }
*/

        return super.createAction(aId);
    }

    private File getAlternateActiveFolder(File aActiveFolder)
    {
        File activeFolder = aActiveFolder;
        while (!activeFolder.exists())
        {
            activeFolder = activeFolder.getParentFile();
        }

        // Active folder does not exist anymore, try to find a sibling or if none found keep existing parent
        if (!activeFolder.equals(mViewContext.getActiveFolder().getFile()))
        {
            RootFolderTreeItem root = (RootFolderTreeItem) mFolderJTree.getModel().getRoot();
            TreePath parentPath = root.createTreePath(activeFolder, false);
            FolderTreeItem parentItem = (FolderTreeItem) parentPath.getLastPathComponent();
            if (parentItem.getChildCount() > 0)
            {
                activeFolder = parentItem.getChild(0).getFile();
            }
        }
        return activeFolder;
    }

    ////////////////////////////////////////////////////////////////////////////
    // MouseAdapter class

    private class FileTreeMouseAdapter extends MouseAdapter
    {
        public void mousePressed(MouseEvent aEvent)
        {
            JTree fileTree = (JTree) aEvent.getSource();
            TreePath treePath = fileTree.getClosestPathForLocation(aEvent.getX(), aEvent.getY());
            if (treePath != null)
            {
                FolderTreeItem item = (FolderTreeItem) treePath.getLastPathComponent();
                mViewContext.setFocusedItem(item);
            }
        }

        public void mouseReleased(MouseEvent aEvent)
        {
            if (aEvent.isPopupTrigger())
            {
                JPopupMenu popupMenu = createFileItemPopupMenu(mViewContext.getFocusedItem());
                popupMenu.setLightWeightPopupEnabled(true);
                popupMenu.show(aEvent.getComponent(), aEvent.getX(), aEvent.getY());
            }
        }
    }
}
