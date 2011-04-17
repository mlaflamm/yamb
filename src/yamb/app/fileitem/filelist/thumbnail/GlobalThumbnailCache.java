package yamb.app.fileitem.filelist.thumbnail;

import yamb.app.fileitem.FileIconType;
import yamb.app.fileitem.FileItem;
import yamb.app.fileitem.filelist.FileListItem;
import yamb.app.fileitem.filelist.FileListItemCache;
import yamb.util.io.FileBlockingQueueConsumer;
import yamb.util.io.Files;
import yamb.util.io.shell.FileOperation;
import yamb.util.io.shell.FileOperationEvent;
import yamb.util.io.shell.FileOperationListener;
import yamb.util.media.Images;
import yamb.util.media.thumbnail.ThumbnailGenerator;
import yamb.util.media.thumbnail.ThumbnailGeneratorRegistry;
import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.log4j.Logger;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.zip.CRC32;
import javax.swing.Icon;


/**
 * @author manuel.laflamme
 * @since 1-Feb-2008
 */
public class GlobalThumbnailCache extends AbstractThumbnailCache
{
    private static final Logger LOGGER = Logger.getLogger(GlobalThumbnailCache.class);

    private final ThumbnailGeneratorRegistry mThumbnailGeneratorRegistry;
    private final File mCacheDirectory;
    private final int mThumbnailWidth;
    private final int mThumbnailHeight;
    private final FileListItemCache mFileListItemCache;
    private final Map<String, Icon> mFileTypeCache = new HashMap<String, Icon>();

    private final BlockingQueue<File> mLoadFileQueue = new LinkedBlockingQueue<File>();
    private final BlockingQueue<File> mUpdateFileQueue = new LinkedBlockingQueue<File>();
//    private final Map<File, Icon> mFileCache = new HashMap<File, Icon>();
    private final Map<LocalThumbnailCache, List<File>> mCache = new HashMap<LocalThumbnailCache, List<File>>();
    private final Map<File, Icon> mLRUCache = new LRUMap(64);

    private final BufferedImage mOverlayImage;

    private LocalThumbnailCache mActiveLocalCache;

    public GlobalThumbnailCache(ThumbnailGeneratorRegistry aThumbnailGeneratorRegistry, File aCacheDirectory,
            int aThumbnailWidth, int aThumbnailHeight, FileListItemCache aFileListItemCache, FileOperation aFileOperation) throws IOException
    {
        mThumbnailGeneratorRegistry = aThumbnailGeneratorRegistry;
        mThumbnailWidth = aThumbnailWidth;
        mThumbnailHeight = aThumbnailHeight;
        mFileListItemCache = aFileListItemCache;
        mCacheDirectory = aCacheDirectory;
        FileUtils.forceMkdir(aCacheDirectory);

        aFileOperation.addFileOperationListener(new FileOperationHandler());
        mOverlayImage = createProcessingOverlay(aThumbnailWidth, aThumbnailHeight);

        Thread loadThread = new Thread(new LoadQueueConsumer(mLoadFileQueue), "Thumb-Load");
        loadThread.setDaemon(true);
        loadThread.setPriority(loadThread.getPriority() / 2);
        loadThread.start();

        Thread thumbThread = new Thread(new GeneratorQueueConsumer(mUpdateFileQueue), "Thumb-Create");
        thumbThread.setDaemon(true);
//        thumbThread.setPriority(thumbThread.getPriority() * 3 / 4);
        thumbThread.start();
    }

    private BufferedImage createProcessingOverlay(int aThumbnailWidth, int aThumbnailHeight)
    {
        BufferedImage overlayImage = new BufferedImage(aThumbnailWidth,
                aThumbnailHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = overlayImage.createGraphics();
        graphics2D.setColor(Color.YELLOW);

        for (int x = 1; x < aThumbnailWidth; x = x + 2)
        {
            graphics2D.drawLine(x, 0, x, aThumbnailHeight);
        }

        for (int y = 1; y < aThumbnailHeight; y = y + 2)
        {
            graphics2D.drawLine(0, y, aThumbnailWidth, y);
        }
        graphics2D.dispose();
        return overlayImage;
    }

    Icon getThumbnail(FileListItem aFileItem, LocalThumbnailCache aLocalCache)
    {
        // Lookup thumbnail from memory
        File file = aFileItem.getFile();
        synchronized (mCache)
        {
            Icon icon = null;
            for (LocalThumbnailCache localCache : mCache.keySet())
            {
                // Skip calling local cache
                if (!localCache.equals(aLocalCache))
                {
                    icon = localCache.getLocalThumbnail(aFileItem);
                    if (icon != null)
                    {
                        return icon;
                    }
                }
            }

            // Not found in active cache, verify LRU cache
            icon = mLRUCache.get(file);
            if (icon != null)
            {
                return icon;
            }
        }

        // Not in memory, queue loading of the thumbnail file
        mLoadFileQueue.add(file);

        // Until thumbnail gets loaded, fallback to file type thumbnail
        return getFileTypeThumbnail(aFileItem);
    }

    /**
     * Returns file type thumbnail.
     */
    private Icon getFileTypeThumbnail(FileListItem aFileItem)
    {
        // Directory, take shell icon
        if (aFileItem.isDirectory())
        {
            return new ThumbnailIcon(aFileItem.getIcon(FileIconType.LARGE), getThumbnailWidth(), getThumbnailHeight());
        }

        String extension = FilenameUtils.getExtension(aFileItem.toString());
        if (isFileTypeCacheExcluded(extension))
        {
            return new ThumbnailIcon(aFileItem.getIcon(FileIconType.LARGE), getThumbnailWidth(), getThumbnailHeight());
        }

        // Lookup file type thumbnail from memory
        synchronized (mCache)
        {
            Icon icon = mFileTypeCache.get(extension);
            if (icon != null)
            {
                return icon;
            }

            // Not in memory, generate thumbnail from shell icon
            icon = new ThumbnailIcon(aFileItem.getIcon(FileIconType.LARGE), getThumbnailWidth(), getThumbnailHeight());
            mFileTypeCache.put(extension, icon);

            return icon;
        }
    }

    private boolean isFileTypeCacheExcluded(String aExtension)
    {
        // todo : configurable exception list
        if (aExtension == null || aExtension.trim().isEmpty() || "exe".equalsIgnoreCase(aExtension))
        {
            return true;
        }
        return false;
    }

    /**
     * Clear the content of the thumbnail cache and update queue
     */
    void clear(LocalThumbnailCache aLocalCache)
    {
//        mLoadFileQueue.clear();

        synchronized (mCache)
        {
            if (aLocalCache.equals(mActiveLocalCache))
            {
                LOGGER.debug("Clear active local cache " + aLocalCache);
                mUpdateFileQueue.clear();
                mCache.remove(aLocalCache);

                // Activate another local cache
                setActiveLocalCache(null);
            }
            else
            {
                LOGGER.debug("Clear non-active local cache " + aLocalCache);
                mCache.remove(aLocalCache);
            }
        }
    }

    /**
     * Add files to the thumbnail update queue
     */
    void queueThumbnailUpdate(List<FileItem> aItems, LocalThumbnailCache aLocalCache)
    {
        synchronized (mCache)
        {
            // files from the active local cache are queued immediatly for processing
            if (aLocalCache.equals(mActiveLocalCache))
            {
                LOGGER.debug("Queue new files to active local cache " + aLocalCache + ", new=" + aItems.size() + ", old=" + mUpdateFileQueue.size());
                for (FileItem item : aItems)
                {
                    // Only process non directory items
                    if (!item.isDirectory())
                    {
                        mUpdateFileQueue.offer(item.getFile());
                    }
                }
            }
            // files for the other local caches are kept in a separate queue until they become active
            else
            {
                if (!mCache.containsKey(aLocalCache))
                {
                    mCache.put(aLocalCache, new ArrayList<File>());
                }

                // Only process non directory items
                List<File> queue = mCache.get(aLocalCache);
                LOGGER.debug("Queue new files to non-active local cache " + aLocalCache + ", new=" + aItems.size() + ", old=" + queue.size());
                for (FileItem item : aItems)
                {
                    if (!item.isDirectory())
                    {
                        queue.add(item.getFile());
                    }
                }
            }

            // No active cache, activate the queueing local cache
            if (mActiveLocalCache == null)
            {
                setActiveLocalCache(aLocalCache);
            }
        }
    }

    /**
     * Activate the specified local cache. Only thumbnails from the active local cache are generated. When the
     * specified local cache value is null, a new local cache is automatically selected for thumbnail generation
     * processing.
     */
    void setActiveLocalCache(LocalThumbnailCache aLocalCache)
    {
        synchronized (mCache)
        {
            if (aLocalCache != null && aLocalCache.equals(mActiveLocalCache))
            {
                LOGGER.debug("Activate local cache - skip " + aLocalCache);
                return;
            }

            // Keep pending generation queue from the active local cache
            if (mActiveLocalCache != null)
            {
                List<File> queue = new ArrayList<File>();
                mUpdateFileQueue.drainTo(queue);
                mCache.put(mActiveLocalCache, queue);
                LOGGER.debug("Activate local cache - deactivate current active first " + mActiveLocalCache + ", pending=" + queue.size());
                mActiveLocalCache = null;
            }

            // Activate the specified local cache if that cache queue is not empty
            if (aLocalCache != null)
            {
                List<File> queue = mCache.get(aLocalCache);
                LOGGER.debug("Activate local cache - activate " + aLocalCache + ", pending=" + (queue == null ? 0 : queue.size()));
                if (queue != null && queue.size() > 0)
                {
                    for (File file : queue)
                    {
                        mUpdateFileQueue.offer(file);
                    }
                    mActiveLocalCache = aLocalCache;
                    queue.clear();
                    return;
                }
            }

            // Activate the first local cache with a non empty queue to process
            for (Map.Entry<LocalThumbnailCache, List<File>> entry : mCache.entrySet())
            {
                List<File> queue = entry.getValue();
                LOGGER.debug("Activate local cache - search non empty " + entry);
                if (queue.size() > 0)
                {
                    LOGGER.debug("Activate local cache - search activate " + entry);
                    for (File file : queue)
                    {
                        mUpdateFileQueue.offer(file);
                    }
                    mActiveLocalCache = entry.getKey();
                    queue.clear();
                    return;
                }
            }
        }
    }

    int getThumbnailWidth()
    {
        return mThumbnailWidth;
    }

    int getThumbnailHeight()
    {
        return mThumbnailHeight;
    }

    public void regenerate(FileItem aFile, LocalThumbnailCache aLocalCache)
    {
        getThumbnailFile(aFile.getFile()).delete();
        queueThumbnailUpdate(Arrays.asList(new FileItem[]{aFile}), aLocalCache);
    }

    public File getThumbnailFile(File aInputFile)
    {
        CRC32 crc32 = new CRC32();
        crc32.update(aInputFile.getAbsolutePath().getBytes());
        return new File(getThumbnailDirectory(aInputFile), aInputFile.getName() + "." + Integer.toHexString((int) crc32.getValue()) + "." +
                mThumbnailWidth + "x" + mThumbnailHeight + ".jpg");
    }

    private File getThumbnailDirectory(File aInputFile)
    {
        String name = aInputFile.getName();
        String subdirName = (name.length() > 0) ? name.substring(0, 2).trim() : "";
        return new File(mCacheDirectory, subdirName);
    }

    public File getThumbnailCacheDirectory()
    {
        return mCacheDirectory;
    }

    private class LoadQueueConsumer extends FileBlockingQueueConsumer
    {
        public LoadQueueConsumer(BlockingQueue<File> aQueue)
        {
            super(aQueue);
        }

        protected void consume(File aFile) throws IOException
        {
            // Try to load from thumbnail file
            File thumbnailFile = getThumbnailFile(aFile);
            if (thumbnailFile.exists())
            {
                //                System.out.println("Load thumbnail: " + thumbnailFile);
                Image image = Images.read(thumbnailFile.getAbsolutePath());
                //                image = ThumbnailUtil.resizeImage(image, mThumbnailWidth, mThumbnailHeight);

                Icon icon = new ThumbnailIcon(image, getThumbnailWidth(), getThumbnailHeight());
                synchronized (mCache)
                {
                    mLRUCache.put(aFile, icon);
                }

                // Notifies listener that the specfied thumbnail been loaded
                fireThumbnailUpdated(aFile, icon);
            }
        }
    }

    private class GeneratorQueueConsumer extends FileBlockingQueueConsumer
    {
        public GeneratorQueueConsumer(BlockingQueue<File> aQueue)
        {
            super(aQueue);
        }

        protected void consume(File aInputFile) throws IOException
        {
            Icon icon = null;
            try
            {
                String extension = FilenameUtils.getExtension(aInputFile.getName());
                if (!mThumbnailGeneratorRegistry.hasGenerator(extension))
                {
                    return;
                }

                // Generate a new thumbnail file if it does not already exist
                File thumbnailFile = getThumbnailFile(aInputFile);
                if (!thumbnailFile.exists())
                {
                    icon = getThumbnail(mFileListItemCache.getItem(aInputFile, false), null);
                    ThumbnailIcon processingIcon = new ThumbnailIcon((ThumbnailIcon) icon);
                    processingIcon.setProcessingOverlayImage(mOverlayImage);
                    fireThumbnailUpdated(aInputFile, processingIcon);

                    long startGetFrameTime = System.currentTimeMillis();
                    Image image = null;
                    ThumbnailGenerator thumbnailGenerator = mThumbnailGeneratorRegistry.getGenerator(extension);
                    image = thumbnailGenerator.generateThumbnailImage(aInputFile);
//                    if (thumbnailGenerator instanceof VideoThumbnailGenerator)
//                    {
//                        VideoThumbnailGenerator generator = (VideoThumbnailGenerator) thumbnailGenerator;
//                        image = generator.generateThumbnailImage(aInputFile, 10);
//                    }
//                    else
//                    {
//                        LOGGER.warn("'" + aInputFile.getName() + "' no thumbnail generator.");
//                        return;
//                    }
                    long endGetFrameTime = System.currentTimeMillis();
                    long elapseTime = endGetFrameTime - startGetFrameTime;
                    LOGGER.debug("'" + aInputFile.getName() + "' thumbnail generated in " + elapseTime + "ms");

                    // Resize thumbnail image to thumbnail size
                    if (image != null)
                    {
                        if (image.getWidth(null) > getThumbnailWidth() || image.getHeight(null) > getThumbnailHeight())
                        {
                            BufferedImage bufferedImage = Images.rescale(image, mThumbnailWidth, mThumbnailHeight);
                            image.flush();
                            FileUtils.forceMkdir(thumbnailFile.getParentFile());
                            Images.toJpegFile(bufferedImage, thumbnailFile.getAbsolutePath());
                            image = bufferedImage;
                        }

                        icon = new ThumbnailIcon(image, getThumbnailWidth(), getThumbnailHeight());
                        synchronized (mCache)
                        {
                            mLRUCache.put(aInputFile, icon);
                        }
                    }
                }
            }
            finally
            {
                // Notifies listener that the specfied thumbnail been loaded
                if (icon != null)
                {
                    fireThumbnailUpdated(aInputFile, icon);
                }

                // If processing queue file is empty, activate a different local cache for further processing
                if (isEmpty())
                {
                    synchronized (mCache)
                    {
                        if (isEmpty())
                        {
                            LOGGER.debug("Post generation activate new since local cache queue empty " + mActiveLocalCache);
                            setActiveLocalCache(null);
                        }
                    }
                }
            }
        }
    }

    private class FileOperationHandler implements FileOperationListener
    {

        public void fileRenamed(FileOperationEvent aEvent)
        {
            fileDeleted(aEvent);
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
            File sourceFile = aEvent.getSourceFile();
            if (sourceFile.isDirectory())
            {
                File[] children = Files.getRecursiveChildren(sourceFile, FileFileFilter.FILE);
                for (File child : children)
                {
                    getThumbnailFile(child).delete();

                }
            }
            else
            {
                getThumbnailFile(sourceFile).delete();
            }
        }
    }
}
