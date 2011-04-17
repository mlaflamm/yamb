package yamb.app.tag.library;

import yamb.util.swing.AutoDisposeModelJList;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Arrays;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

/**
 * @author manuel.laflamme
 * @since Apr 12, 2008
 */
public class LibraryJList extends AutoDisposeModelJList
{
    private final LibraryManager mTagManager;

    public LibraryJList(LibraryManager aTagManager)
    {
        super();

        setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        setCellRenderer(new LibraryCheckboxListCellRenderer(aTagManager));
//        setCellRenderer(new LibraryListCellRenderer(aTagManager));
//        setLayoutOrientation(JList.HORIZONTAL_WRAP);
        setVisibleRowCount(-1);
        mTagManager = aTagManager;

        // Mouse listener
        addMouseListener(new MouseAdapter()
        {
            public void mouseClicked(MouseEvent aEvent)
            {
                if ((SwingUtilities.isLeftMouseButton(aEvent) && aEvent.getClickCount() == 2) ||
                        SwingUtilities.isMiddleMouseButton(aEvent))
                {
                    int index = locationToIndex(aEvent.getPoint());
                    if (index >= 0 && getCellBounds(index, index).contains(aEvent.getPoint()))
                    {
                        File library = (File) getModel().getElementAt(index);
                        if (mTagManager.getLibraryState(library) != LibraryState.UNLOADED)
                        {
                            mTagManager.unloadLibrary(Arrays.asList(new File[]{library}));
                        }
                        else
                        {
//                            mTagManager.rebuildLibrary(library);
                            mTagManager.loadLibrary(library);
                        }
                    }
                }
            }
        });
    }

}
