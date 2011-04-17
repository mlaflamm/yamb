package yamb.app;

import yamb.app.fileitem.FileItem;
import yamb.app.tag.TagCategory;
import yamb.app.tag.TagEvent;
import yamb.app.tag.TagEventListener;
import yamb.app.tag.TagManager;
import yamb.app.tag.library.LibraryEvent;
import yamb.app.tag.library.LibraryEventListener;
import yamb.app.tag.library.LibraryManager;
import yamb.app.tag.library.LibraryProcessingEvent;
import yamb.app.tag.library.LibraryProcessingListener;
import yamb.app.view.FileListViewContext;
import yamb.app.view.tag.TagViewContext;
import yamb.util.io.Files;
import org.apache.log4j.Logger;

import java.awt.BorderLayout;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * @author Manuel Laflamme
 * @since Feb 27, 2006
 */
public class StatusPanel extends JPanel implements ChangeListener, ListSelectionListener, PropertyChangeListener,
        /*ListDataListener,*/ TagEventListener, LibraryEventListener, LibraryProcessingListener
{
    private static final Logger LOGGER = Logger.getLogger(StatusPanel.class);
    private final ApplicationContext mAppContext;

    private final JLabel mStatusLabel;
    private final JLabel mStatsLabel;
    private final JLabel mLibProcessingLabel;

    private int mCachedSelectedFileCount = -1;
//    private List<FileListItem> mCachedSelectedItems = null;
    private final BlockingQueue<List<FileItem>> mSelectedFileSizeQueue = new LinkedBlockingQueue<List<FileItem>>();
    private String mCachedSelectedItemsSize = "";
    private ImageIcon mLibProcessingIcon;
    private long mLastLibProcessingMsgTimestamp = 0;

    public StatusPanel(ApplicationContext aAppContext)
    {
        mAppContext = aAppContext;

        mStatusLabel = new JLabel();
        mStatsLabel = new JLabel();
//        mLibProcessingIcon = new ImageIcon(getClass().getClassLoader().getResource("yamb/images/working3.gif"));
        mLibProcessingLabel = new JLabel();
        mStatsLabel.setOpaque(true);

        setLayout(new BorderLayout());
        add(mLibProcessingLabel, BorderLayout.WEST);
        add(mStatusLabel, BorderLayout.CENTER);
        add(mStatsLabel, BorderLayout.EAST);

        // Register to active view events
        aAppContext.addPropertyChangeListener(ApplicationContext.ACTIVE_VIEWCONTEXT, this);
        TagManager tagManager = aAppContext.getTagManager();
        LibraryManager libraryManager = mAppContext.getLibraryManager();
        tagManager.addTagEventListener(this);
        libraryManager.addLibraryEventListener(this);
        libraryManager.addLibraryProcessingEventListener(this);

        // Menu listener
        MenuSelectionManager manager = MenuSelectionManager.defaultManager();
        manager.addChangeListener(this);

        Thread loadThread = new Thread(new SelectedFileSizeProcessor(mSelectedFileSizeQueue), "FileSize-Process");
        loadThread.setDaemon(true);
        loadThread.setPriority(loadThread.getPriority() / 3);
        loadThread.start();

        updateStats();
    }

    public void updateStatus(String aText)
    {
        mStatusLabel.setText(aText);
    }

    public void updateStats()
    {
        StringBuilder builder = new StringBuilder(100);

        builder.append("Series: ");
        builder.append(countToString(getSeriesCount()));
        builder.append("    ");

        builder.append("Libs: ");
        builder.append(countToString(getLoadedLibraryCount()));
        builder.append("/");
        builder.append(countToString(getLibraryCount()));
        builder.append("    ");

        builder.append("Tags: ");
        builder.append(countToString(getTagsCount(TagCategory.ACTIVE)));
        builder.append("/");
        builder.append(countToString(getTagsCount(TagCategory.RECENT)));
        builder.append("/");
        builder.append(countToString(getTagsCount(TagCategory.FAVORITE)));
        builder.append("/");
        builder.append(countToString(getTagsCount(TagCategory.GLOBAL)));
        builder.append("    ");

        builder.append("Files: ");
        builder.append(countToString(getFilesSelectedCount()));
        builder.append("/");
        builder.append(countToString(getFilesTotalCount()));
        builder.append("/");
        builder.append(countToString(getTaggedFilesCount()));
        builder.append("    ");

        builder.append(mCachedSelectedItemsSize);
//        builder.append("    ");
        mStatsLabel.setText(builder.toString());
    }

    private int getLibraryCount()
    {
        LibraryManager libraryManager = mAppContext.getLibraryManager();
        return libraryManager.getLibraryCount();
    }

    private int getLoadedLibraryCount()
    {
        TagManager tagManager = mAppContext.getTagManager();
        if (tagManager instanceof LibraryManager)
        {
            return ((LibraryManager) tagManager).getLoadedLibraryCount();
        }
        return -1;
    }

    private String countToString(int aCount)
    {
        return aCount < 0 ? "-" : String.valueOf(aCount);
    }

    private int getSeriesCount()
    {
        return mAppContext.getSeriesManager().getSeriesNameCount();
    }

    private int getFilesSelectedCount()
    {
        if (mCachedSelectedFileCount == -1 && mAppContext.getActiveViewContext() instanceof FileListViewContext)
        {
            FileListViewContext viewContext = (FileListViewContext) mAppContext.getActiveViewContext();
//            mCachedSelectedFileCount = viewContext.getFileListSelectionManager().getFilesSelectedCount();

            List<FileItem> selectedItems = viewContext.getFileListSelectionManager().getSelectedItems();
            mCachedSelectedFileCount = selectedItems.size();
            mSelectedFileSizeQueue.offer(selectedItems);
        }
        return mCachedSelectedFileCount;
    }

    private int getFilesTotalCount()
    {
        int totalCount = -1;
        if (mAppContext.getActiveViewContext() instanceof FileListViewContext)
        {
            FileListViewContext viewContext = (FileListViewContext) mAppContext.getActiveViewContext();
            totalCount = viewContext.getFileListSelectionManager().getFilesTotalCount();
        }
        return totalCount;
    }

    private int getFilesNewCount()
    {
        // todo
        int totalCount = -1;
//        if (mAppContext.getActiveViewContext() instanceof FileListViewContext)
//        {
//            FileListViewContext viewContext = (FileListViewContext) mAppContext.getActiveViewContext();
//            totalCount = viewContext.getFileListSelectionManager().getFilesTotalCount();
//        }
        return totalCount;
    }

    private int getTagsCount(TagCategory aCategory)
    {
        if (aCategory == TagCategory.GLOBAL)
        {
            return mAppContext.getTagManager().getTagsCount();
        }

        if (mAppContext.getActiveViewContext() instanceof TagViewContext)
        {
            return ((TagViewContext) mAppContext.getActiveViewContext()).getTagContext().getTagsCount(aCategory);
        }

        return -1;
    }

    private int getTaggedFilesCount()
    {
        return mAppContext.getTagManager().getTaggedFilesCount();
    }

    ////////////////////////////////////////////////////////////////////////////
    // TagEventListener interface

    public void tagAdded(TagEvent aEvent)
    {
        updateStats();
    }

    public void tagRemoved(TagEvent aEvent)
    {
        updateStats();
    }

    public void libraryAdded(LibraryEvent aEvent)
    {
        updateStats();
    }

    ////////////////////////////////////////////////////////////////////////////
    // LibraryEventListener interface

    public void libraryRemoved(LibraryEvent aEvent)
    {
        updateStats();
    }

    public void libraryLoaded(LibraryEvent aEvent)
    {
        updateStats();
    }

    public void libraryUnloaded(LibraryEvent aEvent)
    {
        updateStats();
    }

    public void libraryProcessing(LibraryProcessingEvent aEvent)
    {
        if (aEvent.getProcessingFolder() != null)
        {
            long now = System.currentTimeMillis();
            if (now - mLastLibProcessingMsgTimestamp > 500)
            {
                mLastLibProcessingMsgTimestamp = now;
                updateStatus("Processing '" + aEvent.getLibraryRoot().getName() + "' : " + aEvent.getProcessingFolder().getAbsolutePath());
            }
        }
        else
        {
            updateStatus(null);
            mLastLibProcessingMsgTimestamp = 0;
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // ListSelectionListener interface

    public void valueChanged(ListSelectionEvent aEvent)
    {
        mCachedSelectedFileCount = -1;
        updateStats();
    }

/*
    // todo: update stats when file list count changes 

    ////////////////////////////////////////////////////////////////////////////
    // Interface ListDataListener

    public void contentsChanged(ListDataEvent aEvent)
    {
        updateStats();
    }

    public void intervalAdded(ListDataEvent aEvent
    )
    {
        updateStats();
    }

    public void intervalRemoved(ListDataEvent aEvent)
    {
        updateStats();
    }
*/

    ////////////////////////////////////////////////////////////////////////////
    // Interface PropertyChangeListener

    public void propertyChange(PropertyChangeEvent aEvent)
    {
        if (ApplicationContext.ACTIVE_VIEWCONTEXT.equals(aEvent.getPropertyName()))
        {
            mCachedSelectedFileCount = -1;

            if (aEvent.getOldValue() instanceof FileListViewContext)
            {
                FileListViewContext oldValue = (FileListViewContext) aEvent.getOldValue();
                oldValue.getFileListSelectionManager().removeListSelectionListener(this);
                oldValue.getFileListSelectionManager().removeModelPropertyChangeListener(this);
                if (oldValue instanceof TagViewContext)
                {
                    ((TagViewContext) oldValue).getTagContext().removeTagEventListener(this);
                }
            }

            if (aEvent.getNewValue() instanceof FileListViewContext)
            {
                FileListViewContext newValue = (FileListViewContext) aEvent.getNewValue();
                newValue.getFileListSelectionManager().addListSelectionListener(this);
                newValue.getFileListSelectionManager().addModelPropertyChangeListener(this);
                if (newValue instanceof TagViewContext)
                {
                    ((TagViewContext) newValue).getTagContext().addTagEventListener(this);
                }
            }

            updateStats();
        }
        else if ("model".equals(aEvent.getPropertyName()))
        {
            mCachedSelectedFileCount = -1;
            updateStats();
        }
        else
        {
            System.out.println("Unsupported property: " + aEvent);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Interface ChangeListener

    public void stateChanged(ChangeEvent aEvent)
    {
        MenuSelectionManager manager = MenuSelectionManager.defaultManager();
        MenuElement[] elements = manager.getSelectedPath();
        try
        {
            MenuElement element = elements[elements.length - 1];
            Component component = element.getComponent();

            if (component instanceof JMenuItem)
            {
                JMenuItem menu = (JMenuItem) component;
                Action action = menu.getAction();
                if (action != null)
                {
                    String hint = (String) action.getValue(Action.LONG_DESCRIPTION);
                    if (hint != null)
                    {
                        updateStatus(hint);
                    }
                    else
                    {
                        updateStatus(null);
                    }
                }
                else
                {
                    updateStatus(null);
                }
            }

            // todo: move this to command widget factory
            if (component instanceof MenuElement)
            {
                MenuElement menu = (MenuElement) component;
                MenuElement[] subElements = menu.getSubElements();
//                System.out.println(menu + " --------------> " + Arrays.asList(subElements));

                for (MenuElement subElement : subElements)
                {
                    if (subElement instanceof AbstractButton)
                    {
                        Action action = ((AbstractButton) subElement).getAction();
                        if (action != null)
                        {
                            action.setEnabled(action.isEnabled());
                        }
                    }
                }
            }

        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            updateStatus(null);
        }
    }

    private class SelectedFileSizeProcessor implements Runnable
    {
        private final BlockingQueue<List<FileItem>> mQueue;

        public SelectedFileSizeProcessor(BlockingQueue<List<FileItem>> aQueue)
        {
            mQueue = aQueue;
        }

        public void run()
        {
            while (true)
            {
                try
                {
                    List<FileItem> items = mQueue.take();

                    // Only process the last item list from the queue. If queue is not empty, skip to next list.
                    if (!mQueue.isEmpty())
                    {
                        continue;
                    }

                    long size = 0;
                    for (int i = 0; i < items.size(); i++)
                    {
                        FileItem item = items.get(i);
                        size += item.getSize();

                        // Stop processing every x items and update the displayed size statistics with what been
                        // computed so far
                        if (i > 0 && i % 50 == 0)
                        {
                            // Only process the last item list from the queue.
                            // If queue is not empty, stop processing now and skip to next list.
                            if (!mQueue.isEmpty())
                            {
                                mCachedSelectedItemsSize = "";
                                delayedUpdateStats();
                                break;
                            }
                            else
                            {
                                mCachedSelectedItemsSize = "+" + Files.byteCountToDisplaySize(size);
                                delayedUpdateStats();
                            }
                        }
                    }

                    mCachedSelectedItemsSize = items.size() > 0 ? Files.byteCountToDisplaySize(size) : "";
                    delayedUpdateStats();
                }
                catch (InterruptedException e)
                {
                    LOGGER.fatal("Unhandled InterruptedException", e);
                    break;
                }
                catch (Exception e)
                {
                    LOGGER.error("Unhandled error", e);
                }
            }
        }

        private void delayedUpdateStats()
        {
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    updateStats();
                }
            });
        }
    }

}