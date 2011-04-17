package yamb.app.tag;

import yamb.util.swing.AutoDisposeModelJList;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.SwingUtilities;

/**
 * @author manuel.laflamme
 * @since 2-Feb-2008
 */
public class TagJList extends AutoDisposeModelJList
{
    private final TagContext mTagContext;

    public TagJList(TagContext aTagContext)
    {
        super();

//        setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        setCellRenderer(new TagCheckboxListCellRenderer(aTagContext));
//        setLayoutOrientation(JList.HORIZONTAL_WRAP);
        setVisibleRowCount(-1);
        mTagContext = aTagContext;

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
                        String tagName = getModel().getElementAt(index).toString();
//                        System.out.println(new Timestamp(System.currentTimeMillis()) +
//                                " TagJList.mouseClicked: " + tagName);

                        // Active
                        if (mTagContext.isCategory(tagName, TagCategory.ACTIVE))
                        {
                            ArrayList<String> tags = new ArrayList<String>();
                            tags.add(tagName);
                            mTagContext.removeActiveTags(tags);
                        }
                        else
                        {
                            ArrayList<String> tags = new ArrayList<String>();
                            tags.add(tagName);
                            mTagContext.addActiveTags(tags);
                        }
                    }
                }
            }
        });
    }
}
