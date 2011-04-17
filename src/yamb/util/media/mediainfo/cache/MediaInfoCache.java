package yamb.util.media.mediainfo.cache;

import yamb.util.event.SwingSafeEventSupport;
import yamb.util.media.Videos;
import yamb.util.media.mediainfo.MediaInfo;
import org.apache.commons.collections.map.LRUMap;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.Map;

/**
 * @author manuel.laflamme
 * @since Apr 14, 2008
 */
public class MediaInfoCache
{
    private static final Logger LOGGER = Logger.getLogger(MediaInfoCache.class);

    private final SwingSafeEventSupport mEventSupport = new SwingSafeEventSupport();

    private Map<File, MediaInfo> mLruCache = new LRUMap(16);
    private File mFileToProcess;

    public MediaInfoCache()
    {
        Thread loadThread = new Thread(new MediaInfoProcessor(), "MediaInfo-Load");
        loadThread.setDaemon(true);
        loadThread.setPriority(loadThread.getPriority() / 2);
        loadThread.start();
    }

    /**
     * Returns specified file cached MediaInfo or null if not in the cache.
     * If the MediaInfo is not found in the cache, the cache will load the information in a
     * background thread and notify the listeners.
     */
    public MediaInfo getCachedMediaInfo(File aMediaFile)
    {
        synchronized(mLruCache)
        {
            MediaInfo mediaInfo = mLruCache.get(aMediaFile);
            if (mediaInfo != null)
            {
                return mediaInfo;
            }

            mFileToProcess = aMediaFile;
            mLruCache.notifyAll();
        }

        return null;
    }

    private void fireMediaInfoUpdated(File aMediaFile, MediaInfo aMediaInfo)
    {
        synchronized (mEventSupport)
        {
            mEventSupport.fireEvent("mediaInfoUpdated", new MediaInfoEvent(this, aMediaFile, aMediaInfo));
        }
    }

    public void addMediaInfoEventListener(MediaInfoListener aListener)
    {
            mEventSupport.addEventListener(aListener);
    }

    public void removeMediaInfoEventListener(MediaInfoListener aListener)
    {
            mEventSupport.removeEventListener(aListener);
    }

    private class MediaInfoProcessor implements Runnable
    {
        public void run()
        {
            try
            {
                while (true)
                {
                    File fileToProcess = null;
                    synchronized(mLruCache)
                    {
                        mLruCache.wait();
                        if (mFileToProcess != null)
                        {
                            fileToProcess = mFileToProcess;
                            mFileToProcess = null;
                        }
                    }

                    if (fileToProcess != null && fileToProcess.exists() && fileToProcess.isFile() &&
                            Videos.isVideoFile(fileToProcess))
                    {
                        MediaInfo mediaInfo = MediaInfo.createVideoInfo(fileToProcess);

                        synchronized(mLruCache)
                        {
                            mLruCache.put(fileToProcess, mediaInfo);
                        }

                        fireMediaInfoUpdated(fileToProcess, mediaInfo);
                    }
                }
            }
            catch (InterruptedException e)
            {
                LOGGER.fatal("Unhandled InterruptedException", e);
            }
        }
    }
}
