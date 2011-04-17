package yamb.app.tag.library;

import yamb.util.xml.XmlUtil;

import java.awt.Color;
import java.awt.Component;
import java.io.File;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

/**
 * @author manuel.laflamme
 * @since Aug 11, 2008
 */
public class LibraryListCellRenderer extends DefaultListCellRenderer
{
    private final LibraryManager mTagManager;

    public LibraryListCellRenderer(LibraryManager aTagManager)
    {
        mTagManager = aTagManager;
    }

    public Component getListCellRendererComponent(JList aJList,
            Object aValue,
            int aIndex,
            boolean aIsSelected,
            boolean aCellHasFocus)
    {
        File library = (File) aValue;
        setToolTipText(library.getAbsolutePath());

        String stringValue = library.getName();

        // todo: ensure that loaded selected colors match with the list colors...
        stringValue = XmlUtil.escapeXml(stringValue);
        boolean isLoaded = mTagManager.getLibraryState(library) != LibraryState.UNLOADED;
        if (isLoaded)
        {
            if (aIsSelected)
            {
                stringValue = "<html><font color='#ffffff'><b>" + stringValue + "</b></font>";
            }
            else
            {
                stringValue = "<html><font color='#0000ff'><b>" + stringValue + "</b></font>";
            }
        }

        super.getListCellRendererComponent(aJList, stringValue, aIndex, aIsSelected, aCellHasFocus);
        if (aIsSelected)
        {
            setForeground(aJList.getSelectionForeground());
            setBackground(isLoaded ? Color.BLUE : aJList.getSelectionBackground());
        }
        else
        {
            setForeground(isLoaded ? Color.BLUE : aJList.getForeground());
            setBackground(aJList.getBackground());
        }

        return this;
    }

}
