package yamb.app.tag;

import yamb.util.xml.XmlUtil;

import java.awt.Color;
import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

/**
 * @author manuel.laflamme
 * @since 4-Feb-2008
 */
public class TagListCellRenderer extends DefaultListCellRenderer
{
    private final TagContext mTagContext;

    public TagListCellRenderer(TagContext aTagContext)
    {
        mTagContext = aTagContext;
    }

    public Component getListCellRendererComponent(JList aJList, Object aValue, int aIndex,
            boolean aIsSelected, boolean aCellHasFocus)
    {

        String tagName = aValue.toString();
        if (mTagContext.isCategory(tagName, TagCategory.ACTIVE))
        {
            tagName = XmlUtil.escapeXml(tagName);
            tagName = "<html><font color='#ff0000'><i>" + tagName + "</i></font>";
        }

        Component renderer = super.getListCellRendererComponent(aJList, tagName, aIndex, aIsSelected, aCellHasFocus);

        boolean isFavorite = mTagContext.isCategory(aValue.toString(), TagCategory.FAVORITE);
        if (aIsSelected)
        {
            renderer.setForeground(aJList.getSelectionForeground());
            renderer.setBackground(isFavorite ? Color.RED : aJList.getSelectionBackground());
        }
        else
        {
            renderer.setForeground(isFavorite ? Color.RED : aJList.getForeground());
            renderer.setBackground(aJList.getBackground());
        }

        return this;

    }
}
