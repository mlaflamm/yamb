package yamb.app.view.tag;

import yamb.app.tag.TagContext;
import yamb.app.tag.TagSetMode;
import yamb.app.view.FileListViewContext;

/**
 * @author manuel.laflamme
 * @since 5-Aug-2008
 */
public interface TagViewContext extends FileListViewContext
{
    String TAGSET_MODE = "yamb.tagViewContext.tagsetMode";
    String RECENT_ONLY = "yamb.tagViewContext.recentOnly";

    public TagContext getTagContext();

    public TagSetMode getTagSetMode();

    public void setTagSetMode(TagSetMode aTagSetMode);

    public boolean displaysRecentOnly();

    public void setDisplaysRecentOnly(boolean aRecentOnly);
}
