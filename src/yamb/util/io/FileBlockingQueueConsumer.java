package yamb.util.io;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;

/**
 * @author manuel.laflamme
 * @since Feb 4, 2008
 */
public abstract class FileBlockingQueueConsumer implements Runnable
{
    private static final Logger LOGGER = Logger.getLogger(FileBlockingQueueConsumer.class);
    private final BlockingQueue<File> mQueue;

    public FileBlockingQueueConsumer(BlockingQueue<File> aQueue)
    {
        mQueue = aQueue;
    }

    public boolean isEmpty()
    {
        return mQueue.isEmpty();
    }

    public void run()
    {
        while (true)
        {
            File takenFile = null;
            try
            {
                takenFile = mQueue.take();
                consume(takenFile);
            }
            catch (InterruptedException e)
            {
                LOGGER.fatal("Unhandled InterruptedException", e);
                break;
            }
            catch (Exception e)
            {
                LOGGER.error("Unhandled error while consuming '" + takenFile.getName() + "'", e);
            }
        }
    }

    protected abstract void consume(File aFile) throws IOException;
}
