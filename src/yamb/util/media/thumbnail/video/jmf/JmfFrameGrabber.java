package yamb.util.media.thumbnail.video.jmf;

import yamb.util.Disposable;
import org.apache.log4j.Logger;

import java.awt.Dimension;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import javax.media.Buffer;
import javax.media.Codec;
import javax.media.ConfigureCompleteEvent;
import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.Format;
import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.NoProcessorException;
import javax.media.PrefetchCompleteEvent;
import javax.media.Processor;
import javax.media.RealizeCompleteEvent;
import javax.media.ResourceUnavailableEvent;
import javax.media.StopEvent;
import javax.media.Time;
import javax.media.UnsupportedPlugInException;
import javax.media.control.FramePositioningControl;
import javax.media.control.TrackControl;
import javax.media.format.RGBFormat;
import javax.media.format.VideoFormat;
import javax.media.util.BufferToImage;

/**
 * Sample program to access individual video frames by using a
 * "pass-thru" codec. The codec is inserted into the data flow
 * path. As data pass through this codec, a callback is invoked
 * for each frame of video data.
 */
public class JmfFrameGrabber implements ControllerListener, Disposable
{
    private static final Logger LOGGER = Logger.getLogger(JmfFrameGrabber.class);

    private final Object mWaitSync = new Object();

    private Processor mProcessor;
    private boolean mStateTransitionOk = true;

    private Image mImage = null;

    /**
     * Load specified media file, create a processor and use that processor
     * to take an image at the specified media position.
     *
     * @param aPosition The approximate position in the video of the thumbnail image. This a value between 0 to 100
     *                  representing a percentage of the total playtime.
     */
    public JmfFrameGrabber(File aMediaFile, int aPosition) throws IOException
    {
        try
        {
            String url = "file:" + aMediaFile.getAbsolutePath();
            MediaLocator locator = new MediaLocator(url);
            mProcessor = Manager.createProcessor(locator);
        }
        catch (NoProcessorException e)
        {
            throw new IOException("Failed to create a processor from the given url: " + e);
        }

        boolean initialized = false;
        try
        {
            mProcessor.addControllerListener(this);

            // Put the Processor into configured state.
            mProcessor.configure();
            if (!waitForState(Processor.Configured))
            {
                throw new IOException("Failed to configure the processor.");
            }

            // So I can use it as a player.
            mProcessor.setContentDescriptor(null);

            // Obtain the track controls.
            TrackControl trackControls[] = mProcessor.getTrackControls();
            if (trackControls == null)
            {
                throw new IOException("Failed to obtain track controls from the processor.");
            }

            // Search for the video track.
            TrackControl videoTrack = null;
            for (TrackControl trackControl : trackControls)
            {
                if (trackControl.getFormat() instanceof VideoFormat)
                {
                    videoTrack = trackControl;
                }
                else
                {
                    trackControl.setEnabled(false);
                }
            }
            if (videoTrack == null)
            {
                throw new IOException("The input media does not contain a video track.");
            }

            VideoFormat format = (VideoFormat) videoTrack.getFormat();
            Dimension videoSize = format.getSize();
//            System.err.println("Video format: " + format);

            // Instantiate and set the frame access codec to the data flow path.
            try
            {
                Codec codec[] = {new PostAccessCodec(videoSize)};
                videoTrack.setCodecChain(codec);
            }
            catch (UnsupportedPlugInException e)
            {
                IOException ioe = new IOException("The process does not support effects");
                ioe.initCause(e);
                throw ioe;
            }

            // create a frame positioner
            FramePositioningControl positioningControl = (FramePositioningControl)
                    mProcessor.getControl("javax.media.control.FramePositioningControl");
            if (positioningControl == null)
            {
                throw new IOException("Frame positioning control could not be created");
            }

            // Realize the processor.
            mProcessor.prefetch();
            if (!waitForState(Processor.Prefetched))
            {
                throw new IOException("Failed to realise the processor.");
            }

            // Compute target frame position
            Float frameRate = format.getFrameRate();
            Time duration = mProcessor.getDuration();
            int seekPosition = (int) (duration.getSeconds() * frameRate * (aPosition / 100.0f));
//            System.out.println("Duration=" + duration.getSeconds() + "s, Frame rate=" + frameRate + ", 10%= " + seekPosition);
            synchronized (mWaitSync)
            {
                mImage = null;
                int seekedFrame = positioningControl.skip(seekPosition);
//                System.out.println("Seeked up to frame " + seekedFrame);
            }
            initialized = true;
        }
        finally
        {
            if (!initialized)
            {
                dispose();
            }
        }

    }

    public Image getImage()
    {
        synchronized (mWaitSync)
        {
            try
            {
                while (mImage == null)
                {
                    mWaitSync.wait(100);
                }
            }
            catch (Exception e)
            {
            }
        }

        return mImage;
    }

    public void dispose()
    {
        mProcessor.stop();
    }

    /**
     * Block until the processor has transitioned to the given state.
     * Return false if the transition failed.
     */
    private boolean waitForState(int aState)
    {
        synchronized (mWaitSync)
        {
            try
            {
                while (mProcessor.getState() != aState && mStateTransitionOk)
                {
                    mWaitSync.wait();
                }
            }
            catch (Exception e)
            {
            }
        }
        return mStateTransitionOk;
    }

    /**
     * Controller Listener.
     */
    public void controllerUpdate(ControllerEvent aEvent)
    {

        if (aEvent instanceof ConfigureCompleteEvent
                || aEvent instanceof RealizeCompleteEvent
                || aEvent instanceof PrefetchCompleteEvent)
        {
            synchronized (mWaitSync)
            {
                mStateTransitionOk = true;
                mWaitSync.notifyAll();
            }
        }
        else if (aEvent instanceof ResourceUnavailableEvent)
        {
            synchronized (mWaitSync)
            {
                mStateTransitionOk = false;
                mWaitSync.notifyAll();
            }
        }
        else if (aEvent instanceof StopEvent)
        {
            mProcessor.close();
        }
    }

    /**
     * ******************************************************
     * Inner class.
     * <p/>
     * A pass-through codec to access to individual frames.
     * *******************************************************
     */

    private class PreAccessCodec implements Codec
    {
        // We'll advertize as supporting all video formats.
        protected Format mSupportedIns[] = new Format[]{new VideoFormat(null)};

        // We'll advertize as supporting all video formats.
        protected Format mSupportedOuts[] = new Format[]{new VideoFormat(null)};

        protected Format mInput = null;
        protected Format mOutput = null;

        /**
         * Callback to access individual video frames.
         */
        void accessFrame(Buffer aFrame)
        {

            // For demo, we'll just print out the frame #, time &
            // data length.

/*            long t = (long) (aFrame.getTimeStamp() / 10000000f);
            JmfFrameGrabber.LOGGER.info("Pre: frame #: "
                    + aFrame.getSequenceNumber()
                    + ", time: "
                    + ((float) t) / 100f
                    + ", len: "
                    + aFrame.getLength());*/
        }

        /**
         * The code for a pass through codec.
         */
        public String getName()
        {
            return "Pre-Access Codec";
        }

        //these dont do anything
        public void open()
        {
        }

        public void close()
        {
        }

        public void reset()
        {
        }

        public Format[] getSupportedInputFormats()
        {
            return mSupportedIns;
        }

        public Format[] getSupportedOutputFormats(Format aIn)
        {
            if (aIn == null)
            {
                return mSupportedOuts;
            }
            else
            {
                // If an input format is given, we use that input format
                // as the output since we are not modifying the bit stream
                // at all.
                Format outs[] = new Format[1];
                outs[0] = aIn;
                return outs;
            }
        }

        public Format setInputFormat(Format aFormat)
        {
            mInput = aFormat;
            return mInput;
        }

        public Format setOutputFormat(Format aFormat)
        {
            mOutput = aFormat;
            return mOutput;
        }

        public int process(Buffer aIn, Buffer aOut)
        {

            // This is the "Callback" to access individual frames.
            accessFrame(aIn);

            // Swap the data between the input & output.
            Object data = aIn.getData();
            aIn.setData(aOut.getData());
            aOut.setData(data);

            // Copy the input attributes to the output
            aOut.setFlags(aIn.getFlags() | Buffer.FLAG_NO_SYNC);
            aOut.setFormat(aIn.getFormat());
            aOut.setLength(aIn.getLength());
            aOut.setOffset(aIn.getOffset());

            return BUFFER_PROCESSED_OK;
        }

        public Object[] getControls()
        {
            return new Object[0];
        }

        public Object getControl(String aType)
        {
            return null;
        }
    }

    private class PostAccessCodec extends JmfFrameGrabber.PreAccessCodec
    {
        private Dimension mSize;

        // We'll advertize as supporting all video formats.
        public PostAccessCodec(Dimension aSize)
        {
            mSupportedIns = new Format[]{new RGBFormat()};
            mSize = aSize;
        }

        /**
         * Callback to access individual video frames.
         */
        void accessFrame(Buffer aFrame)
        {
            // For demo, we'll just print out the frame #, time &
            // data length.
/*            long t = (long) (aFrame.getTimeStamp() / 10000000f);
            JmfFrameGrabber.LOGGER.info("Post: frame #: "
                    + aFrame.getSequenceNumber()
                    + ", time: "
                    + ((float) t) / 100f
                    + ", len: "
                    + aFrame.getLength());*/

            BufferToImage stopBuffer = new BufferToImage((VideoFormat) aFrame.getFormat());
            synchronized (mWaitSync)
            {
                mImage = stopBuffer.createImage(aFrame);
                mWaitSync.notifyAll();
            }

            /*   try {
                BufferedImage outImage = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_RGB);
                Graphics og = outImage.getGraphics();
                og.drawImage(stopImage, 0, 0, size.width, size.height, null);
                //prepareImage(outImage,rheight,rheight, null);
                Iterator writers = ImageIO.getImageWritersByFormatName("jpg");
                ImageWriter writer = (ImageWriter) writers.next();

                //Once an ImageWriter has been obtained, its destination must be set to an ImageOutputStream:
                File f = new File(frame.getSequenceNumber() + ".jpg");
                ImageOutputStream ios = ImageIO.createImageOutputStream(f);
                writer.setOutput(ios);

                //Finally, the image may be written to the output stream:
                //BufferedImage bi;
                //writer.write(imagebi);
                writer.write(outImage);
                ios.close();
            } catch (IOException e) {
                System.out.println("Error :" + e);
            }*/
        }

        public String getName()
        {
            return "Post-Access Codec";
        }
    }
}
