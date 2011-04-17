package yamb.util.media.thumbnail.video;

import org.apache.log4j.Logger;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * @author manuel.laflamme
 * @since 24-Feb-2008
 */
public class FallbackVideoThumbnailGenerator extends AbstractVideoThumbnailGenerator implements VideoThumbnailGenerator
{
    private static final Logger LOGGER = Logger.getLogger(FallbackVideoThumbnailGenerator.class);
    private final VideoThumbnailGenerator[] mGenerators;
    private final ExecutorService mExecutorService = Executors.newCachedThreadPool(new ThreadFactory()
    {
        public Thread newThread(Runnable r)
        {
            Thread thread = Executors.defaultThreadFactory().newThread(r);
            thread.setPriority(Thread.currentThread().getPriority());
            return thread;
        }
    });

    public FallbackVideoThumbnailGenerator(VideoThumbnailGenerator... aGenerators)
    {
        mGenerators = aGenerators.clone();
    }

    public Image generateThumbnailImage(File aInputVideoFile, int aPosition) throws IOException
    {
        for (VideoThumbnailGenerator generator : mGenerators)
        {
            LOGGER.info("Generate thumbnail for '" + aInputVideoFile.getName() + "' using " + generator.getClass().getName());
            try
            {
                Future<Image> future = mExecutorService.submit(new CallableGenerator(generator, aInputVideoFile, aPosition));
                Image image = future.get(120, TimeUnit.SECONDS);
                if (image != null)
                {
                    return image;
                }
            }
            catch (Exception e)
            {
                LOGGER.error("Unable to generate thumbnail for '" + aInputVideoFile.getName() + "' and falling back to next generator", e);
            }
        }
        LOGGER.error("Thumbnail not generated '" + aInputVideoFile.getName() + "'");
        return null;
    }

    class CallableGenerator implements Callable<Image>
    {
        private final VideoThumbnailGenerator mGenerator;
        private final File mInputVideoFile;
        private final int mPosition;

        public CallableGenerator(VideoThumbnailGenerator aGenerator, File aInputVideoFile, int aPosition)
        {
            mGenerator = aGenerator;
            mInputVideoFile = aInputVideoFile;
            mPosition = aPosition;
        }

        public Image call() throws Exception
        {
            return mGenerator.generateThumbnailImage(mInputVideoFile, mPosition);
        }
    }
}
