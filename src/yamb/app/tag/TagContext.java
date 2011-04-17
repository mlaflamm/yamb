package yamb.app.tag;

import java.io.File;
import java.util.List;

/**
 * @author manuel.laflamme
 * @since Aug 6, 2008
 */
public interface TagContext
{
    List<String> getTags(TagCategory aCategory);
    int getTagsCount(TagCategory aCategory);
    boolean isCategory(String aTagName, TagCategory aCategory);

    void addTagEventListener(TagEventListener aListener);
    void removeTagEventListener(TagEventListener aListener);

    // Active tags management
    void addActiveTags(List<String> aTagNames);
    void removeActiveTags(List<String> aTagNames);


    /**
     * Returns the list of tags related to the specified file.
     */
    List<String> getFileTags(File aFile);

    /**
     * Returns list of files related to the specified tag.
     */
    List<File> getFiles(String aTag);

    /**
     * Returns list of files related to the specified tags.
     */
    List<File> getFiles(List<String> aTags, TagSetMode aSetMode);
}
