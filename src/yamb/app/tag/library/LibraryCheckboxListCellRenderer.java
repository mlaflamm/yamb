package yamb.app.tag.library;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.io.File;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 * @author manuel.laflamme
 * @since Apr 12, 2008
 */
public class LibraryCheckboxListCellRenderer extends JComponent implements ListCellRenderer
{
    private final DefaultListCellRenderer mDefaultRenderer;
    private final JCheckBox mCheckbox;
    private final LibraryManager mTagManager;

    public LibraryCheckboxListCellRenderer(LibraryManager aTagManager)
    {
        setLayout(new BorderLayout());
        mDefaultRenderer = new DefaultListCellRenderer();
        mCheckbox = new JCheckBox();
        add(mCheckbox, BorderLayout.WEST);
        add(mDefaultRenderer, BorderLayout.CENTER);

        mTagManager = aTagManager;
    }

    public Component getListCellRendererComponent(
            JList aList,
            Object aValue,
            int aIndex,
            boolean aIsSelected,
            boolean aCellHasFocus)
    {
        File library = (File) aValue;
        setToolTipText(library.getAbsolutePath());

        Color foreground = aIsSelected ? aList.getSelectionForeground() : aList.getForeground();
        Color background = aIsSelected ? aList.getSelectionBackground() : aList.getBackground();

        String stringValue = library.getName();
        mDefaultRenderer.getListCellRendererComponent(aList, stringValue, aIndex,
                false, aCellHasFocus);
        mDefaultRenderer.setForeground(foreground);
        mDefaultRenderer.setBackground(background);

        mCheckbox.setSelected(mTagManager.getLibraryState(library) != LibraryState.UNLOADED);
        mCheckbox.setEnabled(mTagManager.getLibraryState(library) != LibraryState.LOADING);
        mCheckbox.setForeground(foreground);
        mCheckbox.setBackground(background);

        return this;
    }

}
