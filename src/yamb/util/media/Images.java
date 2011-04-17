package yamb.util.media;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;
import org.apache.commons.io.FilenameUtils;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Images
{
    private static final List<String> IMAGE_EXTENSIONS = Collections.unmodifiableList(new ArrayList<String>(
            Arrays.asList(new String[]{
                    "jpeg",
                    "jpg",
                    "gif",
//                    "bmp",
//                    "pcx",
                    "png",
            })));

    public static boolean isImageFile(File aFile)
    {
        return IMAGE_EXTENSIONS.contains(FilenameUtils.getExtension(aFile.getName()));
    }

    public static List<String> getImageExtensions()
    {
        return IMAGE_EXTENSIONS;
    }

    static public Image read(String aImageFile)
    {
        try
        {
            Image image = Toolkit.getDefaultToolkit().getImage(aImageFile);
            MediaTracker mediaTracker = new MediaTracker(new Container());
            mediaTracker.addImage(image, 0);
            mediaTracker.waitForID(0);
            return image;
        }
        catch (InterruptedException e)
        {
            return null;
        }
    }

    static public boolean toJpegFile(BufferedImage aSourceImage, String aTargetFile)
    {
        try
        {
            // save thumbnail image to OUTFILE
            BufferedOutputStream out = new BufferedOutputStream(new
                    FileOutputStream(aTargetFile));
            JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
            JPEGEncodeParam param = encoder.
                    getDefaultJPEGEncodeParam(aSourceImage);
//        int quality = Integer.parseInt(args[4]);
//        quality = Math.max(0, Math.min(quality, 100));
            param.setQuality((float) 75 / 100.0f, false);
            encoder.setJPEGEncodeParam(param);
            encoder.encode(aSourceImage);
            out.close();
            return true;
        }
        catch (IOException e)
        {
            return false;
        }
    }

    static public BufferedImage rescale(Image aSourceImage, int aTargetWidth, int aTargetHeight)
    {
        // determine thumbnail size from WIDTH and HEIGHT
        int thumbWidth = aTargetWidth;
        int thumbHeight = aTargetHeight;
        double thumbRatio = (double) thumbWidth / (double) thumbHeight;
        int imageWidth = aSourceImage.getWidth(null);
        int imageHeight = aSourceImage.getHeight(null);
        double imageRatio = (double) imageWidth / (double) imageHeight;
        if (thumbRatio < imageRatio)
        {
            thumbHeight = (int) (thumbWidth / imageRatio);
        }
        else
        {
            thumbWidth = (int) (thumbHeight * imageRatio);
        }
        // draw original image to thumbnail image object and
        // scale it to the new size on-the-fly
        BufferedImage thumbImage = new BufferedImage(thumbWidth,
                thumbHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = thumbImage.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
        graphics2D.drawImage(aSourceImage, 0, 0, thumbWidth, thumbHeight, Color.WHITE, null);

        return thumbImage;
    }
}
