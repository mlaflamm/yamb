package yamb.app.tag;

import java.io.File;
import java.util.List;

/**
 * @author manuel.laflamme
 * @since Apr 11, 2008
 */
public interface TagManager
{
    /**
     * Returns all possible tags
     */
    List<String> getTags();

    int getTagsCount();

    List<String> getRecentsTags();

    int getRecentTagsCount();

    boolean isRecentTag(String aTag);

    /**
     * Returns the list of tags related to the specified file.
     */
    List<String> getFileTags(File aFile);

    /**
     * Returns the list of favorite tags
     */
    List<String> getFavoriteTags();

    int getFavoriteTagsCount();

    void addFavoriteTags(List<String> aFavoriteTags);

    /**
     * Returns true if the specified tag is in the favorite tag list.
     */
    boolean isFavoriteTag(String aTag);

    /**
     * Returns list of files related to the specified tag.
     */
    List<File> getFiles(String aTag);

    /**
     * Returns list of files related to the specified tags.
     */
    List<File> getFiles(List<String> aTags, TagSetMode aSetMode);

    void addTagEventListener(TagEventListener aListener);

    void removeTagEventListener(TagEventListener aListener);

    List<File> getTaggedFiles();

    int getTaggedFilesCount();

    void addTaggedFileEventListener(TaggedFileEventListener aListener);

    void removeTaggedFileEventListener(TaggedFileEventListener aListener);

//    void start();
}
