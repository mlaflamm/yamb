package yamb.app.tag;

import yamb.util.xml.XmlUtil;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 * @author manuel.laflamme
 * @since 6-Feb-2008
 */
public class TagCheckboxListCellRenderer extends JComponent implements ListCellRenderer
{
    private final DefaultListCellRenderer mDefaultRenderer;
    private final JCheckBox mCheckbox;
    private final TagContext mTagContext;

    public TagCheckboxListCellRenderer(TagContext aTagContext)
    {
        setLayout(new BorderLayout());
        mDefaultRenderer = new DefaultListCellRenderer();
        mCheckbox = new JCheckBox();
        add(mCheckbox, BorderLayout.WEST);
        add(mDefaultRenderer, BorderLayout.CENTER);

        mTagContext = aTagContext;
    }

    public Component getListCellRendererComponent(JList aList,
            Object aValue,
            int aIndex,
            boolean aIsSelected,
            boolean aCellHasFocus)
    {
        boolean isActive = mTagContext.isCategory(aValue.toString(), TagCategory.ACTIVE);
        boolean isFavorite = mTagContext.isCategory(aValue.toString(), TagCategory.FAVORITE);
        boolean isRecent = mTagContext.isCategory(aValue.toString(), TagCategory.RECENT);

        String stringValue = aValue.toString();
        if (isRecent)
        {
            stringValue = XmlUtil.escapeXml(stringValue);
            if (isFavorite)
            {
                stringValue = "<html><font color='#0000ff'><i>" + stringValue + "</i></font>";
            }
            else
            {
                stringValue = "<html><i>" + stringValue + "</i>";
            }
        }

        Color foreground = isFavorite ? Color.BLUE : aList.getForeground();
        Color background = aList.getBackground();
        if (aIsSelected)
        {
            foreground = aList.getSelectionForeground();
            background = isFavorite ? Color.BLUE :aList.getSelectionBackground();
        }

        mDefaultRenderer.getListCellRendererComponent(aList, stringValue, aIndex,
                false, aCellHasFocus);
        mDefaultRenderer.setForeground(foreground);
        mDefaultRenderer.setBackground(background);

        mCheckbox.setSelected(isActive);
        mCheckbox.setForeground(foreground);
        mCheckbox.setBackground(background);

        return this;
    }
}
