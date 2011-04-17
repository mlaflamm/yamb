package yamb.app.tag;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author manuel.laflamme
 * @since 2-Feb-2008
 */
public class TagFileItem
{
    private final String mTagName;
    private final Set<File> mFileSet = new TreeSet<File>();

    public TagFileItem(String aTagName)
    {
        mTagName = aTagName;
    }

    public void add(File aFile)
    {
        mFileSet.add(aFile);
    }

    public List<File> getFiles()
    {
        return new ArrayList<File>(mFileSet);
    }

    public String toString()
    {
        return mTagName;
    }
}
