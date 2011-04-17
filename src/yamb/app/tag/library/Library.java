package yamb.app.tag.library;

import yamb.app.tag.Tags;
import yamb.util.io.Files;
import yamb.util.media.VideoFileFilter;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author manuel.laflamme
 * @since Apr 11, 2008
 */
public class Library
{
    private static final Logger LOGGER = Logger.getLogger(Library.class);
    static final Library EMPTY_LIBRARY = new Library();

    private final Map<String, Set<File>> mTagFileCache = new HashMap<String, Set<File>>();
    private final Set<File> mTaggedFileCache = new HashSet<File>();

    private Library()
    {
    }

    public Set<String> getTags()
    {
        return mTagFileCache.keySet();
    }

    public Set<File> getFiles(String aTag)
    {
        return mTagFileCache.get(aTag);
    }

    public Set<File> getTaggedFiles()
    {
        return mTaggedFileCache;
    }

    public boolean storeLibrary(File aLibraryFile)
    {
        SortedSet<File> allFiles = new TreeSet<File>();
        for (Set<File> files : mTagFileCache.values())
        {
            allFiles.addAll(files);
        }

        try
        {
            PrintStream out = new PrintStream(new FileOutputStream(aLibraryFile));
            try
            {
                for (File file : allFiles)
                {
                    out.println(file.getAbsolutePath());
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
            LOGGER.error("Error writing libary: " + aLibraryFile, e);
            return false;
        }

        return true;
    }

    static Library loadLibrary(File aLibraryFile) throws IOException
    {
        Library library = new Library();
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(aLibraryFile)));
        try
        {
            String line;
            while ((line = reader.readLine()) != null)
            {
                line = line.trim();
                if (line.length() > 0)
                {
                    library.processFile(new File(line));
                }
            }
        }
        finally
        {
            reader.close();
        }

        return library;
    }

    static Library createLibrary(File aLibraryDirectory, LibraryProcessingEventSupport aEventSupport) //throws IOException
    {
        Library library = new Library();

        LOGGER.debug("Create library '" + aLibraryDirectory.getAbsolutePath() + "'");
        processDirectory(aLibraryDirectory, library, aEventSupport);
        aEventSupport.fireLibraryProcessing(null);
        return library;
    }

    private static void processDirectory(File aDirectory, Library aLibrary, LibraryProcessingEventSupport aEventSupport) //throws IOException
    {

        // Do not process if specified file is not a directory or does not exist
        if (!aDirectory.exists() || !aDirectory.isDirectory())
        {
            return;
        }

        aEventSupport.fireLibraryProcessing(aDirectory);

        File[] children = Files.getChildren(aDirectory, new VideoFileFilter(true));

        // Process directory
        LOGGER.debug("Processing: '" + aDirectory.getAbsolutePath() + "'");
        List<File> subdirectories = new ArrayList<File>();

        for (File child : children)
        {
            // Process subdirectories later
            if (child.isDirectory())
            {
                subdirectories.add(child);
            }
            else
            {
                // Update tag cache
                aLibrary.processFile(child);
            }
        }

        // Process subdirectories
        for (File subdirectory : subdirectories)
        {
            processDirectory(subdirectory, aLibrary, aEventSupport);
        }
    }

    static String[] getTags(File aFile)
    {
        String name = aFile.getName();
        if (name.indexOf("mol_") != -1)
        {
            return new String[0];
        }

        return Tags.getTagsFromFileName(name);
    }

    boolean processFile(File aFile)
    {
        String[] tags = getTags(aFile);
        if (tags.length > 0)
        {
            mTaggedFileCache.add(aFile);

            for (String tag : tags)
            {
                Set<File> files = mTagFileCache.get(tag);
                if (files == null)
                {
                    files = new HashSet<File>();
                    mTagFileCache.put(tag, files);
                }
                files.add(aFile);
            }
        }

        return tags.length > 0;
    }

    boolean removeFile(File aFile)
    {

        if (mTaggedFileCache.remove(aFile))
        {
            String[] tags = getTags(aFile);
            if (tags.length > 0)
            {
                for (String tag : tags)
                {
                    Set<File> files = mTagFileCache.get(tag);
                    if (files != null)
                    {
                        files.remove(aFile);
                        if (files.isEmpty())
                        {
                            mTagFileCache.remove(tag);
                        }
                    }
                }
            }

            return true;
        }

        return false;
    }
}
