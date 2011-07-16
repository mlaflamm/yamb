package yamb.app.view;

import yamb.app.fileitem.FileItem;
import yamb.util.Disposable;
import yamb.util.media.VideoInfo;
import yamb.util.media.Videos;
import yamb.util.media.mediainfo.cache.MediaInfoCache;
import yamb.util.media.mediainfo.cache.MediaInfoListener;
import yamb.util.media.mediainfo.cache.VideoInfoEvent;
import org.apache.log4j.Logger;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import javax.swing.JTextArea;

/**
 * @author manuel.laflamme
 * @since Apr 14, 2008
 */
public class FilePropertiesPanel extends JTextArea implements PropertyChangeListener, MediaInfoListener, Disposable
{
    private static final Logger LOGGER = Logger.getLogger(FilePropertiesPanel.class);

    private final FileListViewContext mViewContext;
    private final MediaInfoCache mMediaInfoCache;

    public FilePropertiesPanel(FileListViewContext aViewContext, MediaInfoCache aMediaInfoCache)
    {
        mViewContext = aViewContext;
        mMediaInfoCache = aMediaInfoCache;

        setEditable(false);
        mViewContext.addPropertyChangeListener(this);
        mMediaInfoCache.addMediaInfoEventListener(this);
    }

    private void handleMediaInfo(File aMediaFile, VideoInfo aMediaInfo)
    {
        setText(Videos.getVideoDetailsText(aMediaFile, aMediaInfo));
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    // Disposable interface

    public void dispose()
    {
        mViewContext.removePropertyChangeListener(this);
        mMediaInfoCache.removeMediaInfoEventListener(this);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    // PropertyChangeListener interface

    public void propertyChange(PropertyChangeEvent aEvent)
    {
        if (FileListViewContext.FOCUSED_ITEM.equals(aEvent.getPropertyName()))
        {
            FileItem focusedItem = mViewContext.getFocusedItem();
            if (focusedItem != null && !focusedItem.isDirectory() &&
                    Videos.isVideoFile(focusedItem.getFile()))
            {
                VideoInfo mediaInfo = mMediaInfoCache.getCachedMediaInfo(focusedItem.getFile());
                handleMediaInfo(focusedItem.getFile(), mediaInfo);
            }
            else
            {
                setText(null);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    // MediaInfoListener interface

    public void mediaInfoUpdated(VideoInfoEvent aEvent)
    {
        FileItem focusedItem = mViewContext.getFocusedItem();
//        LOGGER.debug("mediaInfoUpdated - focused="+focusedItem+", event=" + aEvent);
        if (focusedItem != null && focusedItem.getFile().equals(aEvent.getMediaFile()))
        {
//            LOGGER.debug("mediaInfoUpdated - handled");
            handleMediaInfo(aEvent.getMediaFile(), aEvent.getMediaInfo());
        }
//        else
//        {
//            setText(null);
//        }
    }
}
