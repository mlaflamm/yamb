package yamb.app;

import yamb.app.bookmark.Bookmark;
import yamb.app.bookmark.BookmarkManager;
import yamb.app.bookmark.BookmarkType;
import yamb.app.bookmark.TagBookmark;
import yamb.app.tool.export.PivotCollectionWriter;
import yamb.app.fileitem.filelist.FileListItemCache;
import yamb.app.fileitem.filelist.thumbnail.GlobalThumbnailCache;
import yamb.app.tag.TagManager;
import yamb.app.view.BookmarkViewFactory;
import yamb.app.view.ViewContext;
import yamb.app.view.ViewFactory;
import yamb.app.view.explorer.ActiveFolder;
import yamb.app.view.explorer.ExplorerViewInternalFrame;
import yamb.app.view.series.SeriesViewInternalFrame;
import yamb.app.view.stat.StatisticsViewInternalFrame;
import yamb.app.view.tag.TagViewInternalFrame;
import yamb.util.commands.ActionFactory;
import yamb.util.commands.ActionModel;
import yamb.util.commands.CommandException;
import yamb.util.commands.CommandGroupModel;
import yamb.util.commands.CommandItem;
import yamb.util.commands.CommandProvider;
import yamb.util.commands.CommandWidgetFactory;
import yamb.util.commands.CompositeActionFactory;
import yamb.util.commands.DefaultActionModel;
import yamb.util.io.Files;
import yamb.util.media.mediainfo.cache.MediaInfoCache;
import org.apache.log4j.Logger;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDesktopPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JToolBar;

public class MdiMainFrame extends JFrame implements ViewFactory, ActionFactory, BookmarkViewFactory
{
    private static final Logger LOGGER = Logger.getLogger(MdiMainFrame.class);

    private static final String APP_TITLE = "Yet Another Multimedia Browser";

    private final CommandGroupModel mCommandGroupModel = new CommandGroupModel();
    private final ApplicationContext mAppContext;
    private final GlobalThumbnailCache mThumbnailCache;
    private final FileListItemCache mFileListItemCache;
    private final MediaInfoCache mMediaInfoCache = new MediaInfoCache();
    private final ActionModel mActionModel;
    private CommandProvider mCommandProvider;
    private SessionBookmarkManager mSessionBookmarkManager;

    private final InternalFrameSelectorTabbedPane mSelectorPane;
    private StatusPanel mStatusPanel;
    private JMenu mBookmarksMenu = new JMenu();

    private static final int DEFAULT_WIDTH = 1100;
    private static final int DEFAULT_HEIGHT = 800;

    private static final int CASCADE_X_INCREMENT = 20;
    private static final int CASCADE_Y_INCREMENT = 25;

    private int mCascadeY = 0;
    private int mCascadeX = 0;

    public MdiMainFrame(ApplicationContext aAppContext, CommandProvider aCommandProvider,
            FileListItemCache aFileListItemCache, GlobalThumbnailCache aGlobalThumbnailCache) throws Exception
    {
        mAppContext = aAppContext;
        mThumbnailCache = aGlobalThumbnailCache;
        mFileListItemCache = aFileListItemCache;
        mCommandProvider = aCommandProvider;
        mActionModel = new DefaultActionModel(new CompositeActionFactory(this, new DefaultActionFactory(mAppContext)));

        setTitle(APP_TITLE);
        setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        setVisible(false);

        JDesktopPane desktop = new JDesktopPane();
        getContentPane().add(desktop);

        // Make dragging a little faster but perhaps uglier.
        desktop.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);

/*
        // Popup menus
        mFilePopupMenu = CommandWidgetFactory.buildMenu(aCommandProvider.getMenuItem("filePopup"), mActionModel);
        mFolderPopupMenu = CommandWidgetFactory.buildMenu(aCommandProvider.getMenuItem("folderPopup"), mActionModel);
        mLibraryPopupMenu = CommandWidgetFactory.buildMenu(aCommandProvider.getMenuItem("libraryPopup"), mActionModel);

*/
        // Menubar
        CommandItem menuBarItem = aCommandProvider.getMenuBarItem("yamb");
        HashMap<String, JMenu> menuMap = new HashMap<String, JMenu>();
        menuMap.put("bookmarks", mBookmarksMenu);
        JMenuBar menuBar = CommandWidgetFactory.buildMenuBar(menuBarItem, mActionModel, mCommandGroupModel, menuMap);
        setJMenuBar(menuBar);

        // Toolbar
        CommandItem toolbarItem = aCommandProvider.getToolBarItem("yamb");
        JToolBar toolbar = CommandWidgetFactory.buildToolBar(toolbarItem, mActionModel, mCommandGroupModel);
//        toolbar.addSeparator();
//        toolbar.add(new ActiveFolderTextField(mViewContext));
        toolbar.setFloatable(false);
//        getContentPane().add(toolbar, java.awt.BorderLayout.NORTH);

        //////////////////
        mSelectorPane = new InternalFrameSelectorTabbedPane();
//        mSelectorPane.setPreferredSize(new Dimension(300, 30));
//        mSelectorPane.setSize(300, 30);

        JPanel toolbarPanel = new JPanel(new BorderLayout());
        toolbarPanel.add(toolbar, java.awt.BorderLayout.NORTH);
        toolbarPanel.add(mSelectorPane, BorderLayout.SOUTH);
        getContentPane().add(toolbarPanel, java.awt.BorderLayout.NORTH);
        ////////////

        // Statusbar
        mStatusPanel = new StatusPanel(aAppContext);
        getContentPane().add(mStatusPanel, BorderLayout.SOUTH);

        // Window listener
        addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent aEvent)
            {
                System.exit(0);
            }
        });

        // Session bookmark
//        BookmarkManager sessionBookmarkManager = ;
        mSessionBookmarkManager = new SessionBookmarkManager(mAppContext.getSessionBookmarkManager(), this);
        mSessionBookmarkManager.initialize();
//        List<Bookmark> boomarks = sessionBookmarkManager.getBookmarks();
//        for (Bookmark bookmark : boomarks)
//        {
//            createView(bookmark);
//        }


        rebuildBookmarksMenu();
    }

    protected JDesktopPane getDesktopPane()
    {
        return (JDesktopPane) getContentPane().getComponent(0);
    }

    public ViewContext createExploreView(ActiveFolder aActiveFolder)
    {
        try
        {
            ExplorerViewInternalFrame frame = new ExplorerViewInternalFrame(aActiveFolder, mAppContext, mFileListItemCache, mThumbnailCache,
                    mCommandProvider, mCommandGroupModel, mMediaInfoCache, mActionModel);

            addInternalFrame(frame);
            return frame.getViewContext();
        }
        catch (CommandException e)
        {
            LOGGER.error("Cannot create explore view", e);
            return null;
        }
    }

    public ViewContext createTagView(List<String> aActiveTags)
    {
        try
        {
            TagViewInternalFrame frame = new TagViewInternalFrame(aActiveTags, mAppContext, mFileListItemCache,
                    mThumbnailCache, mCommandProvider, mCommandGroupModel, mMediaInfoCache, mActionModel);

            addInternalFrame(frame);
            return frame.getViewContext();
        }
        catch (CommandException e)
        {
            LOGGER.error("Cannot create tag view", e);
            return null;
        }
    }

    public ViewContext createSeriesView(String aSelectedGroupName)
    {
        try
        {
            SeriesViewInternalFrame frame = new SeriesViewInternalFrame(aSelectedGroupName, mAppContext, mFileListItemCache,
                    mThumbnailCache, mCommandProvider, mCommandGroupModel, mMediaInfoCache, mActionModel);

            addInternalFrame(frame);
            return frame.getViewContext();
        }
        catch (CommandException e)
        {
            LOGGER.error("Cannot create series view", e);
            return null;
        }
    }

    public ViewContext createStatisticsView()
    {
        StatisticsViewInternalFrame frame = new StatisticsViewInternalFrame(mAppContext, mCommandProvider,
                mCommandGroupModel, mActionModel);

        addInternalFrame(frame);
        return frame.getViewContext();
    }

    private void addInternalFrame(JInternalFrame aFrame)
    {
        Dimension cascadeSize = calcCascadeSize();

        JDesktopPane desktopPane = getDesktopPane();
        if (desktopPane.getAllFrames().length == 0 ||
                (mCascadeX + cascadeSize.getWidth()) > desktopPane.getWidth() ||
                (mCascadeY + cascadeSize.getHeight()) > desktopPane.getHeight())
        {
            // Since there is no Frame opened, reset the cascading info
            mCascadeX = 0;
            mCascadeY = 0;
        }

        aFrame.setBounds(mCascadeX, mCascadeY, cascadeSize.width,
                cascadeSize.height);

        // increment cascading location info
        mCascadeX += CASCADE_X_INCREMENT;
        mCascadeY += CASCADE_Y_INCREMENT;

        aFrame.addInternalFrameListener(mSelectorPane);
        aFrame.addInternalFrameListener(mSessionBookmarkManager);
        desktopPane.add(aFrame);
        aFrame.setVisible(true);
        try
        {
            if (desktopPane.getAllFrames().length == 1)
            {
                aFrame.setMaximum(true);
            }
            aFrame.setSelected(true);
        }
        catch (java.beans.PropertyVetoException e)
        {
            LOGGER.error("Cannot display explorer frame", e);
        }
    }

    private Dimension calcCascadeSize()
    {
        Dimension cascadeSize = new Dimension();
        cascadeSize.width = (int) (0.8000000000000004D * (double) getDesktopPane().getSize().width);
        cascadeSize.height = (int) (0.8000000000000004D * (double) getDesktopPane().getSize().height);
//        cascadeSize.width = (int)(0.80000000000000004D * (double)DEFAULT_WIDTH);
//        cascadeSize.height = (int)(0.80000000000000004D * (double)DEFAULT_HEIGHT);
        return cascadeSize;
    }

    public ViewContext createView(Bookmark aBookmark)
    {
        switch (aBookmark.getType())
        {
            case TAG:
                return createTagView(((TagBookmark) aBookmark).getTags());
            case SERIES:
                return createSeriesView(aBookmark.getValue());
            case FOLDER:
                return createExploreView(new ActiveFolder(new File(aBookmark.getValue())));
        }
        return null;
    }

    private void rebuildBookmarksMenu()
    {
        mBookmarksMenu.removeAll();

        BookmarkManager bookmarkManager = mAppContext.getUserBookmarkManager();

        List<Bookmark> tagBoomarks = bookmarkManager.getBookmarks(BookmarkType.TAG);
        for (Bookmark bookmark : tagBoomarks)
        {
            mBookmarksMenu.add(new BookmarkAction(bookmark));
        }

        List<Bookmark> seriesBoomarks = bookmarkManager.getBookmarks(BookmarkType.SERIES);
        for (Bookmark bookmark : seriesBoomarks)
        {
            mBookmarksMenu.add(new BookmarkAction(bookmark));
        }
    }

    private Icon getBookmarkIcon(Bookmark aBookmark)
    {
        switch (aBookmark.getType())
        {
            case TAG:
                return new ImageIcon(getClass().getClassLoader().getResource("yamb/images/tags.gif"));
            case SERIES:
                return new ImageIcon(getClass().getClassLoader().getResource("yamb/images/groups.gif"));
        }

        return null;
    }

    private class BookmarkAction extends AbstractAction
    {
        private Bookmark mBookmark;

        public BookmarkAction(Bookmark aBookmark)
        {
            super(aBookmark.getValue(), getBookmarkIcon(aBookmark));
            mBookmark = aBookmark;
        }

        public void actionPerformed(ActionEvent e)
        {
            createView(mBookmark);
        }
    }

    public Action createAction(String aId)
    {

        if (aId.equals("thumbnailCacheCleanup"))
        {
            return new ThumbnailCacheCleanup();
        }

        if (aId.equals("exportPivots"))
        {
            return new ExportPivots();
        }
        return null;
    }

    private class ThumbnailCacheCleanup extends javax.swing.AbstractAction
    {
        public void actionPerformed(ActionEvent aEvent)
        {
            List<File> taggedFiles = mAppContext.getTagManager().getTaggedFiles();
            Set<File> taggedThumbnailFiles = new HashSet<File>(taggedFiles.size());
            for (File file : taggedFiles)
            {
                taggedThumbnailFiles.add(mThumbnailCache.getThumbnailFile(file));
            }
            LOGGER.info("Tagged files count: " + taggedThumbnailFiles.size());

            File cacheDirectory = mThumbnailCache.getThumbnailCacheDirectory();
            Set<File> foundThumbnailFiles = new HashSet<File>(Arrays.asList(Files.getRecursiveChildren(cacheDirectory, new FileFilter()
            {
                public boolean accept(File pathname)
                {
                    return pathname.isFile();
                }
            })));
            LOGGER.info("Thumbnail files count: " + foundThumbnailFiles.size());

            foundThumbnailFiles.removeAll(taggedThumbnailFiles);
            LOGGER.info("Thumbnail to delete count: " + foundThumbnailFiles.size());
            for (File file : foundThumbnailFiles)
            {
                LOGGER.debug("Deleting thumbnail: " + file.getName());
                file.delete();
            }
        }
    }

    private class ExportPivots extends javax.swing.AbstractAction
    {
        public void actionPerformed(java.awt.event.ActionEvent aEvent)
        {

            JFileChooser chooser = new JFileChooser();
            chooser.setMultiSelectionEnabled(false);
//            chooser.setFileFilter(new FileNameExtensionFilter("Comma Separated Values File", "csv"));
            chooser.setSelectedFile(new File("pivots.cxml"));
//            chooser.setCurrentDirectory(mAppContext.getActiveFolder().getFile());
            int option = chooser.showSaveDialog(null);
            if (option == JFileChooser.APPROVE_OPTION)
            {
                TagManager tagManager = mAppContext.getTagManager();

                File selectedFile = chooser.getSelectedFile();
                try
                {
                    File inDzcFile = new File("dzc_output.xml");
                    PivotCollectionWriter.write(selectedFile, tagManager, inDzcFile, mThumbnailCache);
                }
                catch (IOException e)
                {
                    LOGGER.warn("Export error", e);
                }
            }
        }
    }


}
