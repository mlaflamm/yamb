package yamb.app.fileitem.filelist.thumbnail;

import yamb.app.fileitem.FileItem;
import yamb.app.tag.library.LibraryManager;
import yamb.app.view.FileListViewContext;
import yamb.util.media.VideoInfo;
import yamb.util.media.Videos;
import yamb.util.media.mediainfo.cache.MediaInfoCache;
import yamb.util.swing.AutoDisposeModelJList;

import java.awt.Dimension;
import java.awt.event.MouseEvent;
import javax.swing.DropMode;
import javax.swing.JList;
import javax.swing.ListSelectionModel;

/**
 * @author manuel.laflamme
 * @since 31-Jan-2008
 */
public class ThumbnailJList extends AutoDisposeModelJList
{
    private final MediaInfoCache mMediaInfoCache;

    public ThumbnailJList(FileListViewContext aViewContext, LocalThumbnailCache aThumbnailCache,
            LibraryManager aLibraryManager, MediaInfoCache aMediaInfoCache)
    {
        mMediaInfoCache = aMediaInfoCache;
        setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        setLayoutOrientation(JList.HORIZONTAL_WRAP);
        setVisibleRowCount(-1);

        // Setup cell renderer
        FileThumbnailCellRenderer cellRenderer = new FileThumbnailCellRenderer(
                aViewContext, aThumbnailCache, aLibraryManager);
        setCellRenderer(cellRenderer);
        Dimension fixedCellSize = cellRenderer.getFixedCellSize();
        setFixedCellHeight(fixedCellSize.height);
        setFixedCellWidth(fixedCellSize.width);
        setDropMode(DropMode.ON);
    }

    public String getToolTipText(MouseEvent aEvent)
    {
        int index = locationToIndex(aEvent.getPoint());
        if (index >= 0 && getCellBounds(index, index).contains(aEvent.getPoint()))
        {
            FileItem fileItem = (FileItem) getModel().getElementAt(index);
            VideoInfo mediaInfo = mMediaInfoCache.getCachedMediaInfo(fileItem.getFile());
            return Videos.getVideoDetailsHtml(fileItem.getFile(), mediaInfo);
        }
        return null;
    }
}
