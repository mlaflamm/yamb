package yamb.app.fileitem.filelist;

import yamb.app.view.FileListViewContext;
import yamb.util.media.Videos;
import yamb.util.media.mediainfo.MediaInfo;
import yamb.util.media.mediainfo.cache.MediaInfoCache;
import yamb.util.swing.AutoDisposeModelJList;

import java.awt.event.MouseEvent;
import javax.swing.DropMode;
import javax.swing.JList;
import javax.swing.ListSelectionModel;

public class FileJList extends AutoDisposeModelJList
{
    private final MediaInfoCache mMediaInfoCache;

    public FileJList(FileListViewContext aViewContext, MediaInfoCache aMediaInfoCache)
    {
        mMediaInfoCache = aMediaInfoCache;
        setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        setCellRenderer(new FileListRenderer(aViewContext));
        setLayoutOrientation(JList.VERTICAL_WRAP);
//        setLayoutOrientation(JList.HORIZONTAL_WRAP);
        setVisibleRowCount(-1);
        setDropMode(DropMode.ON);
    }

    public String getToolTipText(MouseEvent aEvent)
    {
        int index = locationToIndex(aEvent.getPoint());
        if (index >= 0 && getCellBounds(index, index).contains(aEvent.getPoint()))
        {
            FileListItem fileItem = (FileListItem) getModel().getElementAt(index);
            MediaInfo mediaInfo = mMediaInfoCache.getCachedMediaInfo(fileItem.getFile());
            return Videos.getVideoDetailsHtml(fileItem.getFile(), mediaInfo);
        }
        return null;
    }
}