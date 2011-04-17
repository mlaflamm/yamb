import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;
import de.humatic.dsj.DSFiltergraph;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

class DsjTester implements PropertyChangeListener
{
    public void getScreenshots(String filePath)
    {
        File f = new File(filePath);

        DSFiltergraph movie =
                DSFiltergraph.createDSFiltergraph(f.getAbsolutePath(),
                        DSFiltergraph.RENDER_NATIVE, this);
        movie.pause();

        int timing = 10000;

        int nb_screenshots = movie.getDuration() / timing;
        for (int i = 1; i < nb_screenshots; i++)
        {
            movie.setTimeValue(i * timing);

            try
            {
                BufferedImage src_image = movie.getImage();

                saveImage(src_image, f.getAbsolutePath() + "." + i + ".jpg");
            }
            catch (de.humatic.dsj.DSJException e)
            {
                System.err.println("Screenshot error at " +
                        (i * timing) + " [" + e + "]");
            }
        }

        movie.dispose();
    }

    public BufferedImage
            getBufferedImage(Image __image,
            int __type_image)
    {
        BufferedImage buffered_image = new
                BufferedImage(__image.getWidth(null), __image.getHeight(null),
                __type_image);

        Graphics g = buffered_image.createGraphics();
        g.drawImage(__image, 0, 0, null);

        g.dispose();
        __image.flush();
        __image = null;
        return (buffered_image);
    }

    public void
            saveImage(Image __image,
            String __filename)
    {
        BufferedImage buff = getBufferedImage(__image,
                BufferedImage.TYPE_INT_RGB);

        try
        {
            BufferedOutputStream out = new
                    BufferedOutputStream(new FileOutputStream(__filename));
            JPEGImageEncoder encoder =
                    JPEGCodec.createJPEGEncoder(out);
            JPEGEncodeParam param =
                    encoder.getDefaultJPEGEncodeParam(buff);
            param.setQuality((float) 75 / 100.0f, false);
            encoder.setJPEGEncodeParam(param);
            encoder.encode(buff);
            out.flush();
            out = null;
        }
        catch (java.io.IOException e)
        {
            System.err.println("Error while saving image : " + e);
            e.printStackTrace();
        }
    }

    public void
            propertyChange(PropertyChangeEvent pe)
    {
    }

    public static void
            main(String __args[])
    {
//        DSEnvironment.setSetupPath("C:/Program Files/Java/jre1.6.0_02/lib/ext/x86/dsj.xml");

        DsjTester t = new DsjTester();
        t.getScreenshots(__args[0]);
    }
}