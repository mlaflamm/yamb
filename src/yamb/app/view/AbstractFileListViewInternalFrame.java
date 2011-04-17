package yamb.app.view;

import yamb.app.ApplicationContext;
import yamb.app.fileitem.FileItem;
import yamb.app.fileitem.filelist.FileJList;
import yamb.app.fileitem.filelist.FileListItem;
import yamb.app.fileitem.filelist.FileListItemCache;
import yamb.app.fileitem.filelist.FileListSelectionManager;
import yamb.app.fileitem.filelist.FileListSelectionModel;
import yamb.app.fileitem.filelist.FileListTransferHandler;
import yamb.app.fileitem.filelist.details.DetailsJTable;
import yamb.app.fileitem.filelist.thumbnail.GlobalThumbnailCache;
import yamb.app.fileitem.filelist.thumbnail.LocalThumbnailCache;
import yamb.app.fileitem.filelist.thumbnail.ThumbnailJList;
import yamb.app.tag.Tags;
import yamb.app.tag.library.LibraryEvent;
import yamb.app.tag.library.LibraryEventListener;
import yamb.app.tag.series.Series;
import yamb.util.Disposable;
import yamb.util.commands.ActionModel;
import yamb.util.commands.CommandException;
import yamb.util.commands.CommandGroupModel;
import yamb.util.commands.CommandItem;
import yamb.util.commands.CommandProvider;
import yamb.util.commands.CommandWidgetFactory;
import yamb.util.io.shell.FileOperationEvent;
import yamb.util.io.shell.FileOperationListener;
import yamb.util.media.mediainfo.cache.MediaInfoCache;
import yamb.util.swing.CompositeObjectFilter;
import yamb.util.swing.DocumentPatternFilter;
import yamb.util.swing.ListModels;
import yamb.util.swing.ObjectFilter;
import org.apache.commons.lang.ObjectUtils;
import org.apache.log4j.Logger;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListModel;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * @author manuel.laflamme
 * @since 6-Aug-2008
 */
public abstract class AbstractFileListViewInternalFrame extends AbstractViewInternalFrame//implements FileOperationListener
{
    private static final Logger LOGGER = Logger.getLogger(AbstractFileListViewInternalFrame.class);

    private final LocalThumbnailCache mThumbnailCache;
    private final FileListItemCache mFileListItemCache;
    private final MediaInfoCache mMediaInfoCache;
    private final FileListViewContext mViewContext;

    private final FileJList mFileList;
    private final ThumbnailJList mFileThumbnailList;
    private final FileListTaskPaneContainer mFileTaskContainer;
    private final DetailsJTable mFileDetails;
    private final FileListSelectionManager mFileListSelectionManager;
    //    private FileListModelType mListModelType;
    private final JTextField mFileFilterField;
    private final DocumentPatternFilter mFileFilter;
    private final Component mFileFilterComponent;
    private final JMenu mFileItemMenu;

    protected AbstractFileListViewInternalFrame(
            ApplicationContext aAppContext,
            FileListItemCache aFileListItemCache,
            GlobalThumbnailCache aGlobalThumbnailCache,
            CommandProvider aCommandProvider,
            CommandGroupModel aCommandGroupModel,
            ActionModel aGlobalActionModel,
            MediaInfoCache aMediaInfoCache,
            FileListViewContext aViewContext) throws CommandException
    {
        super(aCommandGroupModel, aAppContext, aCommandProvider, aGlobalActionModel);

        mFileListItemCache = aFileListItemCache;
        mMediaInfoCache = aMediaInfoCache;
        mViewContext = aViewContext;
        mThumbnailCache = new LocalThumbnailCache(aGlobalThumbnailCache);

        FocusedFileOperationListener fileOperationListener = new FocusedFileOperationListener();
        addDisposable(fileOperationListener);
        mAppContext.getFileOperation().addFileOperationListener(fileOperationListener);

        // Create file lists
        FileListSelectionModel listSelectionModel = new FileListSelectionModel();
        mFileList = new FileJList(aViewContext, aMediaInfoCache);
        mFileList.setTransferHandler(new FileListTransferHandler(mAppContext.getFileOperation()));
        mFileList.setDragEnabled(true);
        mFileThumbnailList = new ThumbnailJList(aViewContext, mThumbnailCache,
                mAppContext.getLibraryManager(), aMediaInfoCache);
        mFileThumbnailList.setTransferHandler(new FileListTransferHandler(mAppContext.getFileOperation()));
        mFileThumbnailList.setDragEnabled(true);
        mFileDetails = new DetailsJTable();
        mFileTaskContainer = new FileListTaskPaneContainer(aViewContext, mThumbnailCache, aAppContext, aMediaInfoCache);
        mFileList.setSelectionModel(listSelectionModel);
        mFileThumbnailList.setSelectionModel(listSelectionModel);
        mFileTaskContainer.setSelectionModel(listSelectionModel);
//        mFileDetails.setSelectionModel(listSelectionModel);
        mFileListSelectionManager = new FileListSelectionManager(mFileList);
        mFileFilterField = new JTextField();
        mFileFilter = new DocumentPatternFilter();
        mFileFilterField.getDocument().addDocumentListener(mFileFilter);
        mFileFilterComponent = createTagFilterComponent();

        mFileListSelectionManager.addListSelectionListener(new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent aEvent)
            {
                if (mFileListSelectionManager.getFilesSelectedCount() == 1)
                {
                    mViewContext.setFocusedItem(mFileListSelectionManager.getSelectedItems().get(0));
                }
                else if (mFileListSelectionManager.getFilesSelectedCount() == 0)
                {
                    mViewContext.setFocusedItem(null);
                }
            }
        });

        // Create splitter
        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT, null, null);
        splitPane.setOneTouchExpandable(false);
        splitPane.setDividerLocation(300);
        splitPane.setDividerSize(5);
        getContentPane().add(splitPane);

        // Popup menus
        mFileItemMenu = createMenu("filePopup", aGlobalActionModel);

        // File list mouse listener
/*        mFileList.addListSelectionListener(new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent aEvent)
            {
                int leadIndex = mFileList.getLeadSelectionIndex();
                if (leadIndex >= 0 && !(leadIndex >= mFileList.getModel().getSize()))
                {
                    FileListItem item = (FileListItem) mFileList.getModel().getElementAt(leadIndex);
                    setFocusedItem(item);
                }
            }
        });*/
        FileListMouseAdapter fileListMouseAdapter = new FileListMouseAdapter();
        mFileList.addMouseListener(fileListMouseAdapter);
        mFileList.addMouseMotionListener(fileListMouseAdapter);
        mFileThumbnailList.addMouseListener(fileListMouseAdapter);
        mFileThumbnailList.addMouseMotionListener(fileListMouseAdapter);
        setListViewMode(mViewContext.getListViewMode());

        // View context property listener
        ViewPropertyChangeListener propertyListener = new ViewPropertyChangeListener();
        mViewContext.addPropertyChangeListener(propertyListener);
        addPropertyChangeListener(propertyListener);

        // Internal frame listener (activated & closed)
        addInternalFrameListener(new InternalFrameHandler());

        // Library listener
        RefreshLibraryEventListener libraryListener = new RefreshLibraryEventListener();
        addDisposable(libraryListener);
        aAppContext.getLibraryManager().addLibraryEventListener(libraryListener);
    }

    protected JMenu createMenu(String aMenuId, ActionModel aActionModel)
    {
        try
        {
            ActionModel actionModel = aActionModel != null ? aActionModel : mGlobalActionModel;
            return CommandWidgetFactory.buildMenu(mCommandProvider.getMenuItem(aMenuId), actionModel, mCommandGroupModel);
        }
        catch (CommandException e)
        {
            LOGGER.error("Unable to build menu " + aMenuId, e);
            return null;
        }
    }

    protected JPopupMenu createFileItemPopupMenu(FileItem aFileItem)
    {
        if (aFileItem != null && !aFileItem.isDirectory())
        {
            return mFileItemMenu.getPopupMenu();
        }
        return null;
    }

    private Component createTagFilterComponent() throws CommandException
    {
        CommandProvider commandProvider = getCommandProvider();
        CommandItem toolbarItem = commandProvider.getToolBarItem("fileFilterToolbar");
        CommandGroupModel commandGroupModel = getCommandGroupModel();

        JToolBar toolbar = CommandWidgetFactory.buildToolBar(toolbarItem, getGlobalActionModel(),
                commandGroupModel);

        toolbar.add(mFileFilterField);
        toolbar.setFloatable(false);

        return toolbar;
    }

    protected String createFilterValue(Collection<String> aValues)
    {
        StringBuilder builder = new StringBuilder();
        for (String tag : aValues)
        {
            if (builder.length() > 0)
            {
                builder.append("|");
            }
            builder.append(tag);
        }

        return builder.toString();
    }

    public final FileListViewContext getViewContext()
    {
        return mViewContext;
    }

    protected FileListSelectionManager getFileListSelectionManager()
    {
        return mFileListSelectionManager;
    }

    public LocalThumbnailCache getThumbnailCache()
    {
        return mThumbnailCache;
    }

    public FileListItemCache getFileListItemCache()
    {
        return mFileListItemCache;
    }

    public MediaInfoCache getMediaInfoCache()
    {
        return mMediaInfoCache;
    }

    protected void setFocusedItem(FileListItem aItem)
    {
        if (!ObjectUtils.equals(getViewContext().getFocusedItem(), aItem))
        {
            getViewContext().setFocusedItem(aItem);
            mFileThumbnailList.repaint();
        }
    }

    protected void setFileFilterText(String aText)
    {
        if (mFileFilterField.getText().equals(aText))
        {
            requestFileListFocus();
            return;
        }

        FileListSelectionManager selectionManager = mViewContext.getFileListSelectionManager();
        List<FileItem> selectedItems = selectionManager.getSelectedItems();

        mFileFilterField.setText(aText);

        selectionManager.clearSelectedFiles();
        selectionManager.addSelectedItems(selectedItems);
        requestFileListFocus();
    }

    protected void setFileListModel(ListModel aModel, ObjectFilter... aFilters)
    {
        if (aFilters != null && aFilters.length > 0)
        {
            ObjectFilter[] filters = new ObjectFilter[aFilters.length + 1];
            filters[0] = mFileFilter;
            System.arraycopy(aFilters, 0, filters, 1, aFilters.length);
            aModel = ListModels.filteredModel(aModel, new CompositeObjectFilter(filters));
        }
        else
        {
            aModel = ListModels.filteredModel(aModel, mFileFilter);
        }

        List<File> selectedFiles = mFileListSelectionManager.getSelectedFiles();
        mFileList.clearSelection();
        mFileList.setModel(aModel);
        mFileThumbnailList.setModel(aModel);
        mFileTaskContainer.setModel(aModel);
        mFileListSelectionManager.addSelectedFiles(selectedFiles);
//        mFileDetails.setModel(new DetailsTableModel(aModel));
        mThumbnailCache.activate();
    }

    protected void requestFileListFocus()
    {
        // WARNING! This is a hack! Can break if components composition changes!
        JSplitPane splitPane = ((javax.swing.JSplitPane) getContentPane().getComponent(0));
        int dividerLocation = splitPane.getDividerLocation();
        ((JScrollPane) splitPane.getRightComponent()).getViewport().getView().requestFocus();
    }

    private void setListViewMode(ListViewMode aViewMode)
    {
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setColumnHeaderView(mFileFilterComponent);
        switch (aViewMode)
        {
            case DETAILS:
//                scrollPane.setViewportView(mFileDetails);
//                break;
                scrollPane.setViewportView(mFileTaskContainer);
                mCommandGroupModel.setSelected("viewDetails", true);
                break;
            case THUMBNAILS:
                scrollPane.setViewportView(mFileThumbnailList);
                mCommandGroupModel.setSelected("viewThumbnails", true);
                break;
            case LIST:
                scrollPane.setViewportView(mFileList);
                mCommandGroupModel.setSelected("viewList", true);
                break;
        }

        LookAndFeel.installColors(scrollPane.getViewport(), "Table.background", "Table.foreground");

        JSplitPane splitPane = ((javax.swing.JSplitPane) getContentPane().getComponent(0));
        int dividerLocation = splitPane.getDividerLocation();
        splitPane.setRightComponent(scrollPane);
        splitPane.setDividerLocation(dividerLocation);
        scrollPane.getViewport().getView().requestFocus();
    }

    public Action createAction(String aId)
    {
        if ("fileRebuildThumbnail".equals(aId))
        {
            return new AbstractAction()
            {
                public void actionPerformed(ActionEvent aEvent)
                {
                    FileListSelectionManager selectionManager = mViewContext.getFileListSelectionManager();
                    List<FileItem> selectedItems = selectionManager.getSelectedItems();
                    for (FileItem item : selectedItems)
                    {
                        mThumbnailCache.regenerate(item);
                    }
                }
            };
        }

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
                    setFileFilterText(createFilterValue(tags));
                }
            };
        }

        if ("fileFilterSelectedFileGroups".equals(aId))
        {
            return new AbstractAction()
            {
                public void actionPerformed(ActionEvent aEvent)
                {
                    FileListSelectionManager selectionManager = mViewContext.getFileListSelectionManager();
                    List<FileItem> selectedItems = selectionManager.getSelectedItems();
                    List<String> series = new ArrayList<String>(selectedItems.size());
                    for (FileItem item : selectedItems)
                    {
                        series.add(Series.getSeriesName(item.getFile().getName()));
                    }
                    setFileFilterText(createFilterValue(series));
                }
            };
        }

        if ("fileFilterReset".equals(aId))
        {
            return new AbstractAction()
            {
                public void actionPerformed(ActionEvent aEvent)
                {
                    setFileFilterText("");
                }
            };
        }

        return null;
    }

    private class FileListMouseAdapter extends MouseAdapter
    {
        public void mousePressed(MouseEvent aEvent)
        {
            JList fileList = ((JList) aEvent.getComponent());
            int index = fileList.locationToIndex(aEvent.getPoint());
            if (index >= 0 && fileList.getCellBounds(index, index).contains(aEvent.getPoint()))
            {
                boolean shiftOrCtrlPressed = (aEvent.getModifiersEx() &
                        (MouseEvent.CTRL_DOWN_MASK | MouseEvent.SHIFT_DOWN_MASK)) != 0;

                if (SwingUtilities.isLeftMouseButton(aEvent) ||
                        (SwingUtilities.isRightMouseButton(aEvent) && !shiftOrCtrlPressed))
                {
                    FileListItem item = (FileListItem) fileList.getModel().getElementAt(index);
                    setFocusedItem(item);
                }


                if (SwingUtilities.isRightMouseButton(aEvent) && !shiftOrCtrlPressed && !fileList.isSelectedIndex(index))
                {
                    fileList.setSelectionInterval(index, index);
                }

            }
        }

        public void mouseClicked(MouseEvent aEvent)
        {
            if (SwingUtilities.isLeftMouseButton(aEvent) && aEvent.getClickCount() == 2)
            {
                JList fileList = ((JList) aEvent.getComponent());
                int index = fileList.locationToIndex(aEvent.getPoint());
                if (index >= 0 && fileList.getCellBounds(index, index).contains(aEvent.getPoint()))
                {
                    FileListItem item = (FileListItem) fileList.getModel().getElementAt(index);
                    setFocusedItem(item);
                    mGlobalActionModel.getAction("fileOpen").actionPerformed(new ActionEvent(this, 0, "fileOpen"));
                }
            }
        }

        public void mouseReleased(MouseEvent aEvent)
        {
            if (aEvent.isPopupTrigger() && Math.min(aEvent.getX(), aEvent.getY()) >= 0)
            {
                FileItem focusedItem = getViewContext().getFocusedItem();

                // Display proper popup menu
                JPopupMenu popupMenu = createFileItemPopupMenu(focusedItem);
                if (popupMenu != null)
                {
                    popupMenu.setLightWeightPopupEnabled(true);
                    popupMenu.show(aEvent.getComponent(), aEvent.getX(), aEvent.getY());
                }
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // LibraryEventListener interface

    private class RefreshLibraryEventListener implements LibraryEventListener, Disposable
    {
        public void libraryAdded(LibraryEvent aEvent)
        {
            revalidate();
        }

        public void libraryRemoved(LibraryEvent aEvent)
        {
            revalidate();
        }

        public void libraryLoaded(LibraryEvent aEvent)
        {
        }

        public void libraryUnloaded(LibraryEvent aEvent)
        {
        }

        public void dispose()
        {
            getAppContext().getLibraryManager().removeLibraryEventListener(this);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // FileOperationListener interface

    private class FocusedFileOperationListener implements FileOperationListener, Disposable
    {
        public void fileRenamed(FileOperationEvent aEvent)
        {
            FileItem focusedItem = mViewContext.getFocusedItem();
            if (focusedItem != null)
            {
                if (aEvent.getSourceFile().equals(focusedItem.getFile()))
                {
                    FileListItem item = mFileListItemCache.getItem(aEvent.getDestinationFile(), focusedItem.isDirectory());
                    mViewContext.setFocusedItem(item);
                }
            }
        }

        public void fileCopied(FileOperationEvent aEvent)
        {
        }

        public void fileMoved(FileOperationEvent aEvent)
        {
            fileDeleted(aEvent);
        }

        public void fileDeleted(FileOperationEvent aEvent)
        {
            FileItem focusedItem = mViewContext.getFocusedItem();
            if (focusedItem != null)
            {
                if (aEvent.getSourceFile().equals(focusedItem.getFile()))
                {
                    mViewContext.setFocusedItem(null);
                }
            }
        }

        public void dispose()
        {
            mAppContext.getFileOperation().removeFileOperationListener(this);
        }
    }


    private class ViewPropertyChangeListener implements PropertyChangeListener
    {
        public void propertyChange(PropertyChangeEvent aEvent)
        {
            String propertyName = aEvent.getPropertyName();
            if (TITLE_PROPERTY.equals(propertyName))
            {
                mThumbnailCache.setCacheName((String) aEvent.getNewValue());
            }
            else if (FileListViewContext.LISTVIEW_MODE.equals(propertyName))
            {
                setListViewMode((ListViewMode) aEvent.getNewValue());
            }
        }
    }

    private class InternalFrameHandler extends InternalFrameAdapter
    {
        public void internalFrameActivated(InternalFrameEvent aEvent)
        {
            mThumbnailCache.activate();
        }

        public void internalFrameDeactivated(InternalFrameEvent aEvent)
        {

        }

        public void internalFrameClosed(InternalFrameEvent aEvent)
        {
            mThumbnailCache.dispose();
            mFileList.dispose();
            mFileThumbnailList.dispose();
            mFileDetails.dispose();
            AbstractFileListViewInternalFrame.this.removeInternalFrameListener(this);
        }
    }
}
