package yamb.app.tool.export;

import yamb.app.tag.TagCategory;
import yamb.app.tag.TagContext;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

/**
 * @author manuel.laflamme
 * @since Apr 27, 2010
 */
public class TagMklinkBatchWriter
{
    private static final Logger LOGGER = Logger.getLogger(CsvTagStatWriter.class);

    public static void write(TagContext aTagContext, File aFile)
    {
        List<String> tags = aTagContext.getTags(TagCategory.ACTIVE);

        try
        {
            PrintStream out = new PrintStream(new FileOutputStream(aFile));
            try
            {
                for (String tag : tags)
                {
                    out.println("mkdir \"" + tag + "\"");
                    out.println("cd \"" + tag + "\"");

                    List<File> files = aTagContext.getFiles(tag);
                    for (File file : files)
                    {
                        String name = file.getName();
                        String pathName = file.getAbsolutePath();
                        out.println("mklink \"" + name + "\" \"" + pathName + "\"");
                    }

                    out.println("cd ..");
                    out.println();
                }
                out.flush();
            }
            finally
            {
                out.close();
            }
        }
        catch (IOException e)
        {
            LOGGER.warn("Export error", e);
        }
    }

}
