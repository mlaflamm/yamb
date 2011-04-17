package yamb.app.view.tag;

import yamb.app.tag.TagContext;
import yamb.app.tag.TagSetMode;
import yamb.app.view.AbstractFileListViewContext;

/**
 * @author manuel.laflamme
 * @since Aug 6, 2008
 */
public class DefaultTagViewContext extends AbstractFileListViewContext implements TagViewContext
{
    private final TagContext mTagContext;

    private TagSetMode mTagSetMode = TagSetMode.OR;
    private boolean mDisplayRecentOnly = false;

    public DefaultTagViewContext(TagContext aTagContext)
    {
        super();
        mTagContext = aTagContext;
    }

    public TagContext getTagContext()
    {
        return mTagContext;
    }

    public TagSetMode getTagSetMode()
    {
        return mTagSetMode;
    }

    public void setTagSetMode(TagSetMode aTagSetMode)
    {
        TagSetMode newValue = aTagSetMode;
        TagSetMode oldValue = mTagSetMode;

        mTagSetMode = aTagSetMode;

        // Notify listeners of list view mode change
        mPropertySupport.firePropertyChange(TAGSET_MODE, oldValue, newValue);
    }

    public boolean displaysRecentOnly()
    {
        return mDisplayRecentOnly;
    }

    public void setDisplaysRecentOnly(boolean aRecentOnly)
    {
        boolean oldValue = mDisplayRecentOnly;
        boolean newValue = aRecentOnly;

        mDisplayRecentOnly = aRecentOnly;

        // Notify listeners
        mPropertySupport.firePropertyChange(RECENT_ONLY, oldValue, newValue);

    }
}
