package yamb.util.media.thumbnail.video.dsj;

import yamb.util.media.thumbnail.video.AbstractVideoThumbnailGenerator;
import yamb.util.media.thumbnail.video.VideoThumbnailGenerator;
import de.humatic.dsj.DSEnvironment;
import de.humatic.dsj.DSFiltergraph;
import de.humatic.dsj.DSJException;
import org.apache.log4j.Logger;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;

/**
 * @author manuel.laflamme
 * @since 23-Feb-2008
 */
public class DsjVideoThumbnailGenerator extends AbstractVideoThumbnailGenerator implements VideoThumbnailGenerator, PropertyChangeListener
{
    private static final Logger LOGGER = Logger.getLogger(DsjVideoThumbnailGenerator.class);

    static
    {
        try
        {
            DSEnvironment.setDLLPath(new File(System.getProperty("dsj.path", ""), "dsj.dll").getCanonicalPath());
        }
        catch (IOException e)
        {
            LOGGER.fatal("Cannot set DSJ dll path", e);
        }
    }

    public Image generateThumbnailImage(File aInputFile, int aPosition) throws IOException
    {
        try
        {
            DSFiltergraph movie = DSFiltergraph.createDSFiltergraph(aInputFile.getAbsolutePath(),
                    DSFiltergraph.RENDER_NATIVE | DSFiltergraph.INIT_PAUSED, this);

            try
            {
                int duration = movie.getDuration();
                movie.setTimeValue((int) (duration * (aPosition / 100.0f)));
                BufferedImage image = movie.getImage();
                return image;
            }
            finally
            {
                movie.dispose();
            }
        }
        catch (DSJException e)
        {
            IOException ioe = new IOException();
            ioe.initCause(e);
            throw ioe;
        }
    }

    public void propertyChange(PropertyChangeEvent aEvent)
    {
//        System.err.println(aEvent.getPropertyName() + " : " + aEvent.getOldValue() + " - " + aEvent.getNewValue());
    }
}
