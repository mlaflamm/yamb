package yamb.util.io;

import org.apache.commons.io.FileUtils;
import sun.awt.shell.ShellFolder;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


/**
 * @author Manuel Laflamme
 * @since Feb 25, 2006
 */
public class Files
{
    private static final DecimalFormat FILESIZE_FORMAT = new DecimalFormat("0.00");

    public static final Comparator<File> FILE_NAMECOMPARATOR = new Comparator<File>()
    {
        public int compare(File o1, File o2)
        {
            return o1.getName().compareToIgnoreCase(o2.getName());
        }
    };

    /**
     * Returns a human-readable version of the file size, where the input
     * represents a specific number of bytes.
     *
     * @param size  the number of bytes
     * @return a human-readable display value (includes units)
     */
    public static String byteCountToDisplaySize(long size) {
        String displaySize;

        if (size / FileUtils.ONE_GB > 0) {
            displaySize = FILESIZE_FORMAT.format((double)size / FileUtils.ONE_GB) + " GB";
        } else if (size / FileUtils.ONE_MB > 0) {
            displaySize = FILESIZE_FORMAT.format((double)size / FileUtils.ONE_MB) + " MB";
        } else if (size / FileUtils.ONE_KB > 0) {
            displaySize = FILESIZE_FORMAT.format((double)size / FileUtils.ONE_KB) + " KB";
        } else {
            displaySize = String.valueOf(size) + " bytes";
        }
        return displaySize;
    }

    public static void copyFile(File aInput, File aOutput) throws IOException
    {
        FileChannel sourceChannel = new
                FileInputStream(aInput).getChannel();
        FileChannel destinationChannel = new
                FileOutputStream(aOutput).getChannel();
        sourceChannel.transferTo(0, sourceChannel.size(), destinationChannel);
        // or
        //  destinationChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
        sourceChannel.close();
        destinationChannel.close();
    }

    public static boolean isRecent(File aFile)
    {
        String absolutePath = aFile.getAbsolutePath();
        int tempIndex = absolutePath.indexOf("\\(temp");
        if (tempIndex > 0)
        {
            tempIndex += 4;
        }

        int tempIndex2 = absolutePath.indexOf("1)\\(new", tempIndex);
        if (tempIndex2 > 0)
        {
            tempIndex += 7;
        }

        return absolutePath.endsWith("\\New") ||
                absolutePath.indexOf("\\New\\") != -1 ||
                absolutePath.indexOf("\\(", Math.max(0, tempIndex)) != -1;

    }

    public static boolean moveFileTo(File aSourceFile, File aDestFile) throws IOException
    {
        // Source and destination cannot be same
        if (aSourceFile.equals(aDestFile))
        {
            return false;
        }

        // Ensure parent dir exist
        FileUtils.forceMkdir(aDestFile.getParentFile());

        // destination does not exist, great rename
        if (!aDestFile.exists())
        {
            return aSourceFile.renameTo(aDestFile);
        }

        // destination exist and is a file, delete file then rename
        if (!aSourceFile.isDirectory())
        {
            aDestFile.delete();
            return aSourceFile.renameTo(aDestFile);
        }

        // destination is a directory, try move each child
        File[] children = aSourceFile.listFiles();
        for (File child : children)
        {
            File childDestFile = new File(aDestFile, child.getName());
            if (!moveFileTo(child, childDestFile))
            {
                return false;
            }
        }

        // all children moved, delete source directory
        aSourceFile.delete();

        return true;
    }

    public static void shellExecute(File aFile)
    {
        try
        {
            Runtime.getRuntime().exec("rundll32 SHELL32.DLL,ShellExec_RunDLL \"" + aFile.getAbsolutePath() + "\"");
        }
        catch (IOException e1)
        {
            IllegalStateException rte = new IllegalStateException(e1.toString());
            rte.initCause(e1);
            throw rte;
        }
    }

    public static boolean isChild(File aParent, File aChild)
    {
        String parentPath = aParent.getAbsolutePath();
        String childPath = aChild.getAbsolutePath();

        if (parentPath.length() >= childPath.length())
        {
            return false;
        }

        if (childPath.charAt(parentPath.length()) != File.separatorChar)
        {
            return false;
        }

        String childSubpath = childPath.substring(0, parentPath.length());
        return childSubpath.compareToIgnoreCase(parentPath) == 0;

    }

/*
    public static File[] getChildren(File aParent, FileFilter aFileFilter, boolean aRecursive)
    {
        if (aRecursive)
        {
            return getRecursiveChildren(aParent, aFileFilter);
        }
        return getChildren(aParent, aFileFilter);
    }
*/

    public static File[] getChildren(File aParent, FileFilter aFileFilter)
    {
        if (aParent.getParentFile() == null ||
                (aParent instanceof ShellFolder && !((ShellFolder) aParent).isFileSystem()))
        {
            File[] files = aParent.listFiles();
            List<File> children = new ArrayList<File>(files.length);
            for (File file : files)
            {
                if (aFileFilter.accept(file))
                {
                    children.add(file);
                }
            }
            return children.toArray(new File[0]);
        }

        return aParent.listFiles(aFileFilter);
    }

    public static File[] getRecursiveChildren(File aParent, FileFilter aFileFilter)
    {
        List<File> children = new ArrayList<File>();
        getRecursiveChildren(aParent, children, aFileFilter);
        return children.toArray(new File[0]);
    }

    private static void getRecursiveChildren(File aParent, List<File> aChildrenList, FileFilter aFileFilter)
    {
        File[] files = aParent.listFiles();
        for (File file : files)
        {
            if (file.isDirectory())
            {
                getRecursiveChildren(file, aChildrenList, aFileFilter);
            }

            if (aFileFilter.accept(file))
            {
                aChildrenList.add(file);
            }
        }
    }
}
