package yamb.util.media.thumbnail.video;

import yamb.util.media.Images;
import org.apache.log4j.Logger;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author manuel.laflamme
 * @since Dec 1, 2010
 */
public class ExternalProcessVideoThumbnailGenerator extends AbstractTempFileThumbnailGenerator
{
    private static final Logger LOGGER = Logger.getLogger(ExternalProcessVideoThumbnailGenerator.class);

    private Process mProcess = null;
    private BufferedReader mReader = null;
    private final Class<? extends VideoThumbnailGenerator> mGeneratorClass;

    public ExternalProcessVideoThumbnailGenerator(File aTempDirectory, Class<? extends VideoThumbnailGenerator> aGeneratorClass) throws IOException
    {
        super(aTempDirectory);
        mGeneratorClass = aGeneratorClass;

        Runtime.getRuntime().addShutdownHook(new Thread()
        {
            @Override
            public void run()
            {
                if (mProcess != null)
                {
                    mProcess.destroy();
                    mProcess = null;
                    mReader = null;
                }
            }
        });
    }

    @Override
    public File generateThumbnailFile(File aInputVideoFile, int aPosition) throws IOException
    {
        File tempFile = getThumbnailTempFile(aInputVideoFile);

        try
        {
            if (mProcess == null)
            {
                LOGGER.info("Starting external thumbnail generator process");
                mProcess = startProcess(mGeneratorClass, "dsj.path", "java.library.path");
                mReader = new BufferedReader(new InputStreamReader(mProcess.getInputStream()));
                String line = mReader.readLine();
                LOGGER.info("< " + line);
                if (!"ready".equals(line))
                {
                    String message = "Error starting external thumbnail generator process. Status: " + line;
                    LOGGER.error(message);
                    throw new IOException(message);
                }
            }

            PrintWriter out = new PrintWriter(mProcess.getOutputStream());

            StringBuffer buffer = new StringBuffer();
            buffer.append(aInputVideoFile.getAbsolutePath());
            buffer.append("|");
            buffer.append(String.valueOf(aPosition));
            buffer.append("|");
            buffer.append("20");
            buffer.append("|");
            buffer.append(tempFile.getAbsolutePath());

            LOGGER.info("> " + buffer);

            out.println(buffer);
            out.flush();

            String line = mReader.readLine();
            LOGGER.info("< " + line);
            if (!"ok".equals(line))
            {
                LOGGER.error("Error generating thumbnail and terminating process. Status: " + line);
//                out.println("exit");
//                out.flush();

                mProcess.destroy();
                mProcess = null;
                mReader = null;

//                LOGGER.info("Process terminated.");
                return null;
            }
        }
        catch (Exception e)
        {
            if (mProcess != null)
            {
                try
                {
                    LOGGER.warn("Process error, destroying process!", e);
                    mProcess.destroy();
                }
                finally
                {
                    mProcess = null;
                    mReader = null;
                }
            }
        }

        return tempFile;
    }

    public Process startProcess(Class aClass, String... aExportedProperties) throws IOException,
            InterruptedException
    {
        String javaHome = System.getProperty("java.home");
        String javaBin = javaHome +
                File.separator + "bin" +
                File.separator + "java";
        String classpath = System.getProperty("java.class.path");
//        String dsjpath = System.getProperty("dsj.path");
        String className = ExternalProcessVideoThumbnailGenerator.class.getCanonicalName();

//        ProcessBuilder builder = new ProcessBuilder(
//                javaBin, "\"-Ddsj.path=" + new File(dsjpath).getCanonicalPath() + "\"", "-cp", classpath, className, aClass.getName());
        ProcessBuilder builder = new ProcessBuilder();
        builder.directory(new File(".").getCanonicalFile());
        builder.command().add(javaBin);
        for (String property : aExportedProperties)
        {
            builder.command().add("\"-D" + property + "=" + System.getProperty(property) + "\"");
        }
//        builder.command("\"-Ddsj.path=" + new File(dsjpath).getCanonicalPath() + "\"");
        builder.command().addAll(Arrays.asList("-cp", classpath, className, aClass.getName()));

        return builder.start();
    }

    public static void main(String[] aArgs) throws Exception
    {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(System.out);
        System.setOut(System.err); // Stupid hack because Fobs4JMF is writing stuff in System.out

        final ExecutorService executorService = Executors.newCachedThreadPool(Executors.defaultThreadFactory());
        final VideoThumbnailGenerator thumbnailGenerator = (VideoThumbnailGenerator) Class.forName(aArgs[0]).newInstance();

        out.println("ready");
        out.flush();

        try
        {
            String line = in.readLine();
            while (line != null && !"exit".equals(line))
            {
                String[] parts = line.split("\\|");
                if (parts.length < 4)
                {
                    out.println("error");
                    out.flush();
                }

                final File inputVideoFile = new File(parts[0]); // input video file
                final int position = Integer.parseInt(parts[1]); // position
                final int timeout = Integer.parseInt(parts[2]); // timeout
                final String outputImagePath = parts[3]; // output image file

                Future<BufferedImage> future = executorService.submit(new Callable<BufferedImage>()
                {
                    @Override
                    public BufferedImage call() throws Exception
                    {
                        return (BufferedImage) thumbnailGenerator.generateThumbnailImage(inputVideoFile, position);
                    }
                });
                BufferedImage image = future.get(timeout, TimeUnit.SECONDS);
                if (image != null && Images.toJpegFile(image, outputImagePath))
                {
                    out.println("ok");
                    out.flush();
                }
                else
                {
                    out.println("error");
                    out.flush();
                }

                line = in.readLine();
            }
        }
        catch (Exception e)
        {
            out.println(e.toString());
            out.flush();
            throw e;
        }
    }
}
