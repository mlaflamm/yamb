package yamb.app.view;

import yamb.app.ApplicationContext;
import yamb.app.fileitem.filelist.FileListTransferHandler;
import yamb.app.fileitem.filelist.thumbnail.LocalThumbnailCache;
import yamb.app.fileitem.filelist.thumbnail.ThumbnailJList;
import yamb.util.Disposable;
import yamb.util.media.mediainfo.cache.MediaInfoCache;
import org.jdesktop.swingx.JXTaskPane;

import javax.swing.ListModel;
import javax.swing.ListSelectionModel;

/**
 * @author manuel.laflamme
 * @since Apr 25, 2010
 */
public class FileListTaskPane extends JXTaskPane implements Disposable
{
    private ThumbnailJList mFileThumbnailList;

    public FileListTaskPane(FileListViewContext aViewContext, LocalThumbnailCache aThumbnailCache,
            ApplicationContext aAppContext, MediaInfoCache aMediaInfoCache)
    {
        mFileThumbnailList = new ThumbnailJList(aViewContext, aThumbnailCache, aAppContext.getLibraryManager(), aMediaInfoCache);
        mFileThumbnailList.setTransferHandler(new FileListTransferHandler(aAppContext.getFileOperation()));
        mFileThumbnailList.setDragEnabled(true);
        add(mFileThumbnailList);
    }

    public void setModel(ListModel aListModel)
    {
        mFileThumbnailList.setModel(aListModel);
    }

    public void setSelectionModel(ListSelectionModel aSelectionModel)
    {
        mFileThumbnailList.setSelectionModel(aSelectionModel);
    }

    @Override
    public void dispose()
    {
        mFileThumbnailList.dispose();
    }
}
