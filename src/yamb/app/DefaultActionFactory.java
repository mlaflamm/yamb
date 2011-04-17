package yamb.app;

import yamb.app.tool.export.CsvTagStatWriter;
import yamb.app.fileitem.FileItem;
import yamb.app.fileitem.filelist.FileListSelectionManager;
import yamb.app.fileitem.folders.FolderTreeItem;
import yamb.app.tag.TagSetMode;
import yamb.app.tag.Tags;
import yamb.app.tag.library.LibraryManager;
import yamb.app.tag.library.LibraryState;
import yamb.app.tag.series.Series;
import yamb.app.view.FileListViewContext;
import yamb.app.view.ListViewMode;
import yamb.app.view.ViewContext;
import yamb.app.view.explorer.ActiveFolder;
import yamb.app.view.explorer.ExplorerViewContext;
import yamb.app.view.tag.TagViewContext;
import yamb.util.commands.ActionFactory;
import yamb.util.io.Files;
import yamb.util.io.shell.FileSelection;
import yamb.util.io.shell.RenameFileDialog;
import org.apache.log4j.Logger;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;

public class DefaultActionFactory implements ActionFactory
{
    private static final Logger LOGGER = Logger.getLogger(DefaultActionFactory.class);

    private final ApplicationContext mAppContext;

    public DefaultActionFactory(ApplicationContext aAppContext)
    {
        mAppContext = aAppContext;
    }

    public Action createAction(String aId)
    {
        // File exit
        if (aId.equals("exit"))
        {
            return new Exit();
        }        // File exit

        if (aId.equals("exportTagStats"))
        {
            return new ExportTagStats();
        }


        // Explorer view show Folders
        if ("explorerShowFolders".equals(aId))
        {
            return new ExplorerViewShowFolders();
        }

        // View details
        if (aId.equals("viewDetails"))
        {
            return new ChangeListViewMode(ListViewMode.DETAILS);
        }

        // View list
        if (aId.equals("viewList"))
        {
            return new ChangeListViewMode(ListViewMode.LIST);
        }

        // Thumbnail list
        if (aId.equals("viewThumbnails"))
        {
            return new ChangeListViewMode(ListViewMode.THUMBNAILS);
        }

        // Tag set mode
        if (aId.equals("tagSetAnd"))
        {
            return new ChangeTagSetMode(TagSetMode.AND);
        }
        if (aId.equals("tagSetOr"))
        {
            return new ChangeTagSetMode(TagSetMode.OR);
        }

        if (aId.equals("tagRecentOnly"))
        {
            return new ShowRecentOnly();
        }

        // File and folder operation

        if (aId.equals("fileOpen"))
        {
            return new FileOpen();
        }

        if (aId.equals("fileOpenContainingFolderInExplorer"))
        {
            return new FileOpenContainingFolderInExplorer();
        }

        if ("folderOpenRecursive".equals(aId))
        {
            return new FolderOpenRecursive();
        }

        if ("folderAddLibrary".equals(aId))
        {
            return new FolderAddLibrary();
        }

        if ("folderRemoveLibrary".equals(aId))
        {
            return new FolderRemoveLibrary();
        }

        if ("folderRebuildLibrary".equals(aId))
        {
            return new FolderRebuildLibrary();
        }

        if ("fileOpenExplorerView".equals(aId))
        {
            return new FileOpenExplorerView();
        }

        if ("fileOpenTagView".equals(aId))
        {
            return new FileOpenTagView();
        }

        if ("fileOpenGroupView".equals(aId))
        {
            return new FileOpenSeriesView();
        }

        if ("openStatView".equals(aId))
        {
            return new OpenStatView();
        }

        if (aId.equals("fileCut"))
        {
            return new FileCut();
        }

        if (aId.equals("fileCopy"))
        {
            return new FileCopy();
        }

        if (aId.equals("filePaste"))
        {
            return new FilePaste();
        }

        if (aId.equals("fileDelete"))
        {
            return new FileDelete();
        }

        if (aId.equals("fileRename"))
        {
            return new FileRename();
        }

        if (aId.equals("libraryLoadAll"))
        {
            return new LibraryLoadAll();
        }

        if (aId.equals("libraryUnloadAll"))
        {
            return new LibraryUnloadAll();
        }

        if (aId.equals("libraryInvertLoaded"))
        {
            return new LibraryInvertLoaded();
        }

        if (aId.equals("libraryRebuildAll"))
        {
            return new LibraryRebuildAll();
        }

        if (aId.equals("libraryRebuildLoaded"))
        {
            return new LibraryRebuildLoaded();
        }

        if (aId.equals("fileRebuildLibrary"))
        {
            return new FileRebuildLibrary();
        }

        return new ViewActionProxy(aId);
    }

    private class ViewActionProxy extends javax.swing.AbstractAction
    {
        private final String mId;

        public ViewActionProxy(String aId)
        {
            mId = aId;
        }

        public void actionPerformed(ActionEvent aEvent)
        {
            ViewContext viewContext = mAppContext.getActiveViewContext();
            if (viewContext != null)
            {
                Action action = viewContext.getActionModel().getAction(mId);
                if (action != null)
                {
                    action.actionPerformed(aEvent);
                }
            }
        }

/*        public boolean isEnabled()
        {
            ViewContext viewContext = mAppContext.getActiveViewContext();
            if (viewContext != null)
            {
                Action action = viewContext.getActionModel().getAction(mId);
                if (action != null)
                {
                    return action.isEnabled();
                }
            }

            return false;
        }*/
    }

    private abstract class AbstractFocusedItemAction extends AbstractAction
    {
/*
        public boolean isEnabled()
        {
//            enabled = false;
            ViewContext viewContext = mAppContext.getActiveViewContext();
            if (viewContext != null)
            {
                FileItem focusedItem = viewContext.getFocusedItem();
                return focusedItem != null;
            }
            return false;
        }
*/
    }

    private abstract class AbstractFileSelectionAction extends AbstractAction
    {
/*
        public boolean isEnabled()
        {
            ViewContext viewContext = mAppContext.getActiveViewContext();
            if (viewContext != null)
            {
                return !viewContext.getFileListSelectionManager().isSelectionEmpty();
            }
            return false;
        }
*/
    }

    private class LibraryLoadAll extends javax.swing.AbstractAction
    {
        public void actionPerformed(ActionEvent aEvent)
        {
            LibraryManager libraryManager = mAppContext.getLibraryManager();
            List<File> libraryRoots = libraryManager.getLibraryRoots();
            for (File libraryRoot : libraryRoots)
            {
                libraryManager.loadLibrary(libraryRoot);
            }
        }
    }

    private class LibraryUnloadAll extends javax.swing.AbstractAction
    {
        public void actionPerformed(ActionEvent aEvent)
        {
            LibraryManager libraryManager = mAppContext.getLibraryManager();
            libraryManager.unloadLibrary(libraryManager.getLibraryRoots());
        }
    }

    private class LibraryInvertLoaded extends javax.swing.AbstractAction
    {
        public void actionPerformed(ActionEvent aEvent)
        {
            LibraryManager libraryManager = mAppContext.getLibraryManager();
            List<File> allLibraries = libraryManager.getLibraryRoots();
            List<File> librariesToUnload = new ArrayList<File>();
            for (File library : allLibraries)
            {
                if (libraryManager.getLibraryState(library) != LibraryState.UNLOADED)
                {
                    librariesToUnload.add(library);
                }
                else
                {
                    libraryManager.loadLibrary(library);
                }
            }

            libraryManager.unloadLibrary(librariesToUnload);
        }
    }

    private class LibraryRebuildAll extends javax.swing.AbstractAction
    {
        public void actionPerformed(ActionEvent aEvent)
        {
            LibraryManager libraryManager = mAppContext.getLibraryManager();
            List<File> libraryRoots = libraryManager.getLibraryRoots();
            for (File libraryRoot : libraryRoots)
            {
                libraryManager.rebuildLibrary(libraryRoot);
            }
        }
    }

    private class LibraryRebuildLoaded extends javax.swing.AbstractAction
    {
        public void actionPerformed(ActionEvent aEvent)
        {
            LibraryManager libraryManager = mAppContext.getLibraryManager();
            List<File> libraryRoots = libraryManager.getLibraryRoots();
            for (File libraryRoot : libraryRoots)
            {
                if (libraryManager.getLibraryState(libraryRoot) != LibraryState.UNLOADED)
                {
                    libraryManager.rebuildLibrary(libraryRoot);
                }
            }
        }
    }

    private class FileRebuildLibrary extends javax.swing.AbstractAction
    {
        public void actionPerformed(ActionEvent aEvent)
        {
            if (mAppContext.getActiveViewContext() instanceof FileListViewContext)
            {
                LibraryManager libraryManager = mAppContext.getLibraryManager();
                FileListViewContext viewContext = (FileListViewContext) mAppContext.getActiveViewContext();
                FileListSelectionManager selectionManager = viewContext.getFileListSelectionManager();
                List<File> selectedFiles = selectionManager.getSelectedFiles();
                for (File file : selectedFiles)
                {
                    libraryManager.rebuildContainingLibrary(file);
                }
            }
        }
    }

    private class Exit extends javax.swing.AbstractAction
    {
        public void actionPerformed(java.awt.event.ActionEvent aEvent)
        {
            System.exit(0);
        }
    }

    private class ExplorerViewShowFolders extends javax.swing.AbstractAction
    {
        public void actionPerformed(java.awt.event.ActionEvent aEvent)
        {
            if (mAppContext.getActiveViewContext() instanceof ExplorerViewContext)
            {
                ExplorerViewContext viewContext = (ExplorerViewContext) mAppContext.getActiveViewContext();
                viewContext.setShowFolders(!viewContext.getShowFolders());
            }
        }
    }

    private class ChangeListViewMode extends javax.swing.AbstractAction
    {
        private final ListViewMode mMode;

        public ChangeListViewMode(ListViewMode aMode)
        {
            mMode = aMode;
        }

        /**
         *
         */
        public void actionPerformed(java.awt.event.ActionEvent aEvent)
        {
            if (mAppContext.getActiveViewContext() instanceof FileListViewContext)
            {
                FileListViewContext viewContext = (FileListViewContext) mAppContext.getActiveViewContext();
                viewContext.setListViewMode(mMode);
            }
        }
    }

    private class ChangeTagSetMode extends javax.swing.AbstractAction
    {
        private final TagSetMode mMode;

        public ChangeTagSetMode(TagSetMode aMode)
        {
            mMode = aMode;
        }

        public void actionPerformed(java.awt.event.ActionEvent aEvent)
        {
            if (mAppContext.getActiveViewContext() instanceof TagViewContext)
            {
                TagViewContext viewContext = (TagViewContext) mAppContext.getActiveViewContext();
                viewContext.setTagSetMode(mMode);
            }
        }

    }

    private class ShowRecentOnly extends javax.swing.AbstractAction
    {
        public void actionPerformed(ActionEvent aEvent)
        {
            System.err.println("RecentOnly: " + aEvent);
            if (mAppContext.getActiveViewContext() instanceof TagViewContext)
            {
                TagViewContext viewContext = (TagViewContext) mAppContext.getActiveViewContext();
                viewContext.setDisplaysRecentOnly(!viewContext.displaysRecentOnly());
            }
        }
    }

    private class FolderOpenRecursive extends AbstractFocusedItemAction
    {
        public void actionPerformed(ActionEvent aEvent)
        {
            if (mAppContext.getActiveViewContext() instanceof ExplorerViewContext)
            {
                ExplorerViewContext viewContext = (ExplorerViewContext) mAppContext.getActiveViewContext();
                viewContext.setActiveFolder(new ActiveFolder(viewContext.getFocusedItem().getFile(), true));
            }
        }
    }

    private class FolderAddLibrary extends AbstractFocusedItemAction
    {
        public void actionPerformed(ActionEvent aEvent)
        {
            if (mAppContext.getActiveViewContext() instanceof FileListViewContext)
            {
                FileListViewContext viewContext = (FileListViewContext) mAppContext.getActiveViewContext();
                FileItem focusedItem = viewContext.getFocusedItem();
                if (focusedItem != null)
                {
                    mAppContext.getLibraryManager().addLibrary(focusedItem.getFile());
                }
            }
        }
    }

    private class FolderRemoveLibrary extends AbstractFocusedItemAction
    {
        public void actionPerformed(ActionEvent aEvent)
        {
            if (mAppContext.getActiveViewContext() instanceof FileListViewContext)
            {
                FileListViewContext viewContext = (FileListViewContext) mAppContext.getActiveViewContext();
                FileItem focusedItem = viewContext.getFocusedItem();
                if (focusedItem != null)
                {
                    mAppContext.getLibraryManager().removeLibrary(focusedItem.getFile());
                }
            }
        }
    }

    private class FolderRebuildLibrary extends AbstractFocusedItemAction
    {
        public void actionPerformed(ActionEvent aEvent)
        {
            if (mAppContext.getActiveViewContext() instanceof FileListViewContext)
            {
                FileListViewContext viewContext = (FileListViewContext) mAppContext.getActiveViewContext();
                FileItem focusedItem = viewContext.getFocusedItem();
                if (focusedItem != null)
                {
                    File file = focusedItem.getFile();
                    LibraryManager libraryManager = mAppContext.getLibraryManager();
                    if (libraryManager.isLibraryRoot(file))
                    {
                        libraryManager.rebuildLibrary(file);
                    }
                }
            }
        }
    }

    private class FileOpen extends AbstractFocusedItemAction
    {
        public void actionPerformed(ActionEvent aEvent)
        {
            if (mAppContext.getActiveViewContext() instanceof FileListViewContext)
            {
                FileListViewContext viewContext = (FileListViewContext) mAppContext.getActiveViewContext();

                FileItem focusedItem = viewContext.getFocusedItem();
                if (focusedItem != null)
                {
                    File file = focusedItem.getFile();
                    if (file.isDirectory())
                    {
                        if (viewContext instanceof ExplorerViewContext)
                        {
                            ((ExplorerViewContext) viewContext).setActiveFolder(new ActiveFolder(file));
                        }
                    }
                    else
                    {
                        Files.shellExecute(file);
                    }
                }
            }
        }
    }

    private class FileOpenContainingFolderInExplorer extends AbstractFocusedItemAction
    {
        public void actionPerformed(ActionEvent aEvent)
        {
            if (mAppContext.getActiveViewContext() instanceof FileListViewContext)
            {
                FileListViewContext viewContext = (FileListViewContext) mAppContext.getActiveViewContext();

                FileItem focusedItem = viewContext.getFocusedItem();
                if (focusedItem != null)
                {
                    File file = focusedItem.getFile();
                    {
                        Files.shellExecute(file.getParentFile());
                    }
                }
            }
        }
    }

    private class FileOpenExplorerView extends AbstractAction
    {
        public void actionPerformed(ActionEvent aEvent)
        {
            // todo : remove this hard coded hack to select the default folder with some config
            File[] defaultFolders = new File[]{
                    new File("C:\\"),
                    new File("C:\\Projects\\_data"),
                    new File("Z:\\"),
            };

            ActiveFolder activeFolder = null;
            for (File folder : defaultFolders)
            {
                if (folder.exists())
                {
                    activeFolder = new ActiveFolder(folder);
                }
            }

            FileItem focusedItem = null;
            boolean showFolders = true;
            if (mAppContext.getActiveViewContext() instanceof FileListViewContext)
            {
                FileListViewContext viewContext = (FileListViewContext) mAppContext.getActiveViewContext();

                focusedItem = viewContext.getFocusedItem();
                if (focusedItem != null)
                {
                    if (focusedItem.isDirectory())
                    {
                        activeFolder = new ActiveFolder(focusedItem.getFile());
                        showFolders = false;
                    }
                    else
                    {
                        File parent = focusedItem.getFile().getParentFile();
                        activeFolder = new ActiveFolder(parent);
                        showFolders = false;
                    }
                }
            }

            ExplorerViewContext viewContext =
                    (ExplorerViewContext) mAppContext.getViewFactory().createExploreView(activeFolder);
            viewContext.setShowFolders(showFolders);
            if (focusedItem != null)
            {
                ArrayList<File> files = new ArrayList<File>();
                files.add(focusedItem.getFile());
                viewContext.getFileListSelectionManager().addSelectedFiles(files);
            }
        }
    }

    private class FileOpenTagView extends AbstractAction
    {
        public void actionPerformed(ActionEvent aEvent)
        {
            List<String> activeTags = new ArrayList<String>();
            if (mAppContext.getActiveViewContext() instanceof FileListViewContext)
            {
                FileListViewContext viewContext = (FileListViewContext) mAppContext.getActiveViewContext();
                List<File> selectedFiles = viewContext.getFileListSelectionManager().getSelectedFiles();
                for (File file : selectedFiles)
                {
                    activeTags.addAll(Arrays.asList(Tags.getTagsFromFileName(file.getName())));
                }
            }
            mAppContext.getViewFactory().createTagView(activeTags);
        }
    }

    private class FileOpenSeriesView extends AbstractAction
    {
        public void actionPerformed(ActionEvent aEvent)
        {
            FileItem focusedItem = null;
            String seriesName = null;
            if (mAppContext.getActiveViewContext() instanceof FileListViewContext)
            {
                FileListViewContext viewContext = (FileListViewContext) mAppContext.getActiveViewContext();

                focusedItem = viewContext.getFocusedItem();
                if (focusedItem != null && !focusedItem.isDirectory())
                {
                    File file = focusedItem.getFile();
                    seriesName = Series.getSeriesName(file.getName());
                }
            }

            FileListViewContext viewContext = (FileListViewContext) mAppContext.getViewFactory().createSeriesView(seriesName);
            if (focusedItem != null)
            {
                ArrayList<File> files = new ArrayList<File>();
                files.add(focusedItem.getFile());
                viewContext.getFileListSelectionManager().addSelectedFiles(files);
            }
        }
    }

    private class OpenStatView extends AbstractAction
    {
        public void actionPerformed(ActionEvent aEvent)
        {
            mAppContext.getViewFactory().createStatisticsView();
        }
    }

    private class FileCut extends AbstractFileSelectionAction
    {
        public void actionPerformed(ActionEvent aEvent)
        {
            if (mAppContext.getActiveViewContext() instanceof FileListViewContext)
            {
                FileListViewContext viewContext = (FileListViewContext) mAppContext.getActiveViewContext();
                FileSelection fileSelection = null;
                FileItem focusedItem = viewContext.getFocusedItem();
                if (focusedItem instanceof FolderTreeItem)
                {
                    fileSelection = new FileSelection(focusedItem.getFile(), true);
                }
                else
                {
                    FileListSelectionManager selectionManager = viewContext.getFileListSelectionManager();
                    if (!selectionManager.isSelectionEmpty())
                    {
                        List<File> selectedFiles = selectionManager.getSelectedFiles();
                        fileSelection = new FileSelection(selectedFiles, true);
                    }
                }

                if (fileSelection != null)
                {
                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    clipboard.setContents(fileSelection, fileSelection);
                }
            }
        }
    }

    private class FileCopy extends AbstractFileSelectionAction
    {
        public void actionPerformed(ActionEvent aEvent)
        {
            if (mAppContext.getActiveViewContext() instanceof FileListViewContext)
            {
                FileListViewContext viewContext = (FileListViewContext) mAppContext.getActiveViewContext();
                FileSelection fileSelection = null;
                FileItem focusedItem = viewContext.getFocusedItem();
                if (focusedItem instanceof FolderTreeItem)
                {
                    fileSelection = new FileSelection(focusedItem.getFile(), false);
                }
                else
                {
                    FileListSelectionManager selectionManager = viewContext.getFileListSelectionManager();
                    if (!selectionManager.isSelectionEmpty())
                    {
                        List<File> selectedFiles = selectionManager.getSelectedFiles();
                        fileSelection = new FileSelection(selectedFiles, false);
                    }
                }

                if (fileSelection != null)
                {
                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    clipboard.setContents(fileSelection, fileSelection);
                }
            }
        }
    }

    private class FilePaste extends AbstractFocusedItemAction
    {
        public void actionPerformed(ActionEvent aEvent)
        {
            try
            {
                if (mAppContext.getActiveViewContext() instanceof FileListViewContext)
                {
                    FileListViewContext viewContext = (FileListViewContext) mAppContext.getActiveViewContext();

                    FileItem focusedItem = viewContext.getFocusedItem();
                    if (focusedItem == null || !focusedItem.isDirectory())
                    {
                        return;
                    }

                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    if (clipboard.isDataFlavorAvailable(FileSelection.CUT_FILES_FLAVOR))
                    {
                        Transferable contents = clipboard.getContents(FileSelection.COPY_FILES_FLAVOR);
                        List<File> files = new ArrayList<File>((List) contents.getTransferData(FileSelection.COPY_FILES_FLAVOR));
                        mAppContext.getFileOperation().moveFiles(files, focusedItem.getFile());
                    }
                    else if (clipboard.isDataFlavorAvailable(FileSelection.COPY_FILES_FLAVOR))
                    {
                        Transferable contents = clipboard.getContents(FileSelection.COPY_FILES_FLAVOR);
                        List<File> files = new ArrayList<File>((List) contents.getTransferData(FileSelection.COPY_FILES_FLAVOR));
                        mAppContext.getFileOperation().copyFiles(files, focusedItem.getFile());
                    }
                }
            }
            catch (UnsupportedFlavorException e)
            {
                LOGGER.error("Paste error", e);
            }
            catch (IOException e)
            {
                LOGGER.warn("Paste error", e);
            }
        }
    }


    private class FileDelete extends AbstractFileSelectionAction
    {
        public void actionPerformed(java.awt.event.ActionEvent aEvent)
        {
            try
            {
                if (mAppContext.getActiveViewContext() instanceof FileListViewContext)
                {
                    FileListViewContext viewContext = (FileListViewContext) mAppContext.getActiveViewContext();
                    FileItem focusedItem = viewContext.getFocusedItem();
                    if (focusedItem instanceof FolderTreeItem)
                    {

                        mAppContext.getFileOperation().deleteFiles(Arrays.asList(new File[]{focusedItem.getFile()}));
                    }
                    else
                    {
                        FileListSelectionManager selectionManager = viewContext.getFileListSelectionManager();
                        if (!selectionManager.isSelectionEmpty())
                        {
                            List<File> selectedFiles = selectionManager.getSelectedFiles();
                            mAppContext.getFileOperation().deleteFiles(selectedFiles);
                        }
                    }
                }
            }
            catch (IOException e)
            {
                LOGGER.warn("Delete error", e);
            }
        }
    }

    private class FileRename extends AbstractFileSelectionAction
    {
        public void actionPerformed(java.awt.event.ActionEvent aEvent)
        {
            if (mAppContext.getActiveViewContext() instanceof FileListViewContext)
            {
                FileListViewContext viewContext = (FileListViewContext) mAppContext.getActiveViewContext();

                FileItem focusedItem = viewContext.getFocusedItem();
                if (focusedItem != null)
                {
                    new RenameFileDialog().show(focusedItem.getFile(), mAppContext.getFileOperation());
                }
            }
        }
    }

    private class ExportTagStats extends javax.swing.AbstractAction
    {
        public void actionPerformed(java.awt.event.ActionEvent aEvent)
        {

            JFileChooser chooser = new JFileChooser();
            chooser.setMultiSelectionEnabled(false);
//            chooser.setFileFilter(new FileNameExtensionFilter("Comma Separated Values File", "csv"));
            chooser.setSelectedFile(new File("tagstats.csv"));
//            chooser.setCurrentDirectory(mAppContext.getActiveFolder().getFile());
            int option = chooser.showSaveDialog(null);
            if (option == JFileChooser.APPROVE_OPTION)
            {
                CsvTagStatWriter.write(mAppContext.getTagManager(), chooser.getSelectedFile());
            }
        }
    }
}