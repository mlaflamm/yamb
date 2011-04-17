package yamb.util.io.shell;

import yamb.util.event.SwingSafeEventSupport;
import com.sun.jna.examples.win32.Shell32;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author manuel.laflamme
 * @since 12-Feb-2008
 */
public class FileOperation
{
    private final SwingSafeEventSupport mEventSupport = new SwingSafeEventSupport();

    public boolean renameFile(File aSource, String aTargetName) throws IOException
    {
        File target = new File(aSource.getParent(), aTargetName);

        // A hack when only want to change the casing of the file
        if (target.equals(aSource) && !target.getName().equals(aSource.getName()))
        {
            File tempFile = new File(aSource.getParent(), aTargetName + ".rename");
            if (!tempFile.exists())
            {
                if (!renameFile(aSource, tempFile))
                {
                    return false;
                }
                if (!renameFile(tempFile, target))
                {
                    return false;
                }
            }
        }
        // Normal rename
        else if (!renameFile(aSource, target))
        {
            return false;
        }

        // If we are here, rename been successful
        fireFileRenamed(aSource, target);
        return true;
    }

    private boolean renameFile(File aSource, File aTarget) throws IOException
    {
        Shell32.SHFILEOPSTRUCT struct = new Shell32.SHFILEOPSTRUCT();
        struct.wFunc = Shell32.FO_RENAME;
        struct.pFrom = struct.encodePaths(toPaths(aSource));
        struct.pTo = struct.encodePaths(toPaths(aTarget));
//        struct.lpszProgressTitle = "Paste";

        Shell32 shell32 = Shell32.INSTANCE;
        int result = shell32.SHFileOperation(struct);
        return result == 0 && !struct.fAnyOperationsAborted;

    }

    public void moveFiles(List<File> aFiles, File aTarget) throws IOException
    {
        Thread loadThread = new Thread(new MoveFiles(aFiles, aTarget), "FileOp-Move");
        loadThread.setDaemon(true);
        loadThread.start();
    }

    private class MoveFiles implements Runnable
    {
        private final Shell32.SHFILEOPSTRUCT mFileOpStruct;
        private final List<File> mFiles;
        private final File mTarget;

        public MoveFiles(List<File> aFiles, File aTarget) throws IOException
        {
            mFiles = aFiles;
            mTarget = aTarget;

            mFileOpStruct = new Shell32.SHFILEOPSTRUCT();
            mFileOpStruct.wFunc = Shell32.FO_MOVE;
            mFileOpStruct.pFrom = mFileOpStruct.encodePaths(toPaths(aFiles));
            mFileOpStruct.pTo = mFileOpStruct.encodePaths(toPaths(aTarget));
        }

        public void run()
        {
            Shell32 shell32 = Shell32.INSTANCE;
            shell32.SHFileOperation(mFileOpStruct);
            for (File file : mFiles)
            {
                if (!file.exists())
                {
                    fireFileMoved(file, mTarget);
                }
            }
        }
    }

    public void copyFiles(List<File> aFiles, File aTarget) throws IOException
    {
        Thread loadThread = new Thread(new CopyFiles(aFiles, aTarget), "FileOp-Copy");
        loadThread.setDaemon(true);
        loadThread.start();
    }

    private class CopyFiles implements Runnable
    {
        private final Shell32.SHFILEOPSTRUCT mFileOpStruct;
        private final List<File> mFiles;
        private final File mTarget;
        private final List<File> mExistingFiles;

        public CopyFiles(List<File> aFiles, File aTarget) throws IOException
        {
            mFiles = aFiles;
            mTarget = aTarget;

            mExistingFiles = new ArrayList<File>();
            for (File file : aFiles)
            {
                if (new File(aTarget, file.getName()).exists())
                {
                    mExistingFiles.add(file);
                }
            }

            mFileOpStruct = new Shell32.SHFILEOPSTRUCT();
            mFileOpStruct.wFunc = Shell32.FO_COPY;
            mFileOpStruct.pFrom = mFileOpStruct.encodePaths(toPaths(aFiles));
            mFileOpStruct.pTo = mFileOpStruct.encodePaths(toPaths(aTarget));
        }

        public void run()
        {
            Shell32 shell32 = Shell32.INSTANCE;
            shell32.SHFileOperation(mFileOpStruct);
            for (File file : mFiles)
            {
                if (!mExistingFiles.contains(file) && new File(mTarget, file.getName()).exists())
                {
                    fireFileCopied(file, mTarget);
                }
            }
        }
    }

    public void deleteFiles(List<File> aFiles) throws IOException
    {
        Thread loadThread = new Thread(new DeleteFiles(aFiles), "FileOp-Delete");
        loadThread.setDaemon(true);
        loadThread.start();
    }

    private class DeleteFiles implements Runnable
    {
        private final Shell32.SHFILEOPSTRUCT mFileOpStruct;
        private final List<File> mFiles;


        public DeleteFiles(List<File> aFiles) throws IOException
        {
            mFiles = aFiles;

            mFileOpStruct = new Shell32.SHFILEOPSTRUCT();
            mFileOpStruct.wFunc = Shell32.FO_DELETE;
            mFileOpStruct.pFrom = mFileOpStruct.encodePaths(toPaths(aFiles));

        }

        public void run()
        {
            Shell32 shell32 = Shell32.INSTANCE;
            shell32.SHFileOperation(mFileOpStruct);
            for (File file : mFiles)
            {
                if (!file.exists())
                {
                    fireFileDeleted(file);
                }
            }
        }
    }

    private String[] toPaths(List<File> aFiles) throws IOException
    {
        String[] paths = new String[aFiles.size()];
        for (int i = 0; i < aFiles.size(); i++)
        {
            File file = aFiles.get(i);
            paths[i] = file.getCanonicalPath();
        }
        return paths;
    }

    private String[] toPaths(File aFile) throws IOException
    {
        return new String[]{aFile.getCanonicalPath()};
    }

    public void addFileOperationListener(FileOperationListener aListener)
    {
        mEventSupport.addEventListener(aListener);
    }

    public void removeFileOperationListener(FileOperationListener aListener)
    {
        mEventSupport.removeEventListener(aListener);
    }

    private void fireFileRenamed(File aSource, File aTarget)
    {
        mEventSupport.fireEvent("fileRenamed", new FileOperationEvent(this, aSource, aTarget));
    }

    private void fireFileMoved(File aSource, File aTarget)
    {
        mEventSupport.fireEvent("fileMoved", new FileOperationEvent(this, aSource, aTarget));
    }

    private void fireFileCopied(File aSource, File aTarget)
    {
        mEventSupport.fireEvent("fileCopied", new FileOperationEvent(this, aSource, aTarget));
    }

    public void fireFileDeleted(File aFile)
    {
        mEventSupport.fireEvent("fileDeleted", new FileOperationEvent(this, aFile, aFile));
    }
}
