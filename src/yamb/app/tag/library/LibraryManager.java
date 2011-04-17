package yamb.app.tag.library;

import java.io.File;
import java.util.List;

/**
 * @author manuel.laflamme
 * @since Aug 27, 2008
 */
public interface LibraryManager
{
    /**
     * Returns list of library root diectories
     */
    List<File> getLibraryRoots();

    int getLibraryCount();

    int getLoadedLibraryCount();

    boolean isLibraryRoot(File aLibraryRoot);

    LibraryState getLibraryState(File aLibraryRoot);

    boolean addLibrary(File aLibraryRoot);

    boolean removeLibrary(File aLibraryRoot);
    
    void loadLibrary(File aLibraryRoot);

    void unloadLibrary(List<File> aLibraryRoots);

    void rebuildLibrary(File aLibraryRoot);

    /**
     * Rebuild the library containg the specified file.
     * @param aFile A file contained by a library
     */
    void rebuildContainingLibrary(File aFile);

    void addLibraryEventListener(LibraryEventListener aListener);

    void removeLibraryEventListener(LibraryEventListener aListener);

    public void addLibraryProcessingEventListener(LibraryProcessingListener aListener);

    public void removeLibraryProcessingEventListener(LibraryProcessingListener aListener);

}
