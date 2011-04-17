package yamb.app.tag.series;

import yamb.util.swing.AutoDisposeModelJList;

import javax.swing.DropMode;
import javax.swing.JList;
import javax.swing.ListSelectionModel;

/**
 * @author manuel.laflamme
 * @since Aug 8, 2008
 */
public class SeriesJList extends AutoDisposeModelJList
{
    public SeriesJList()
    {
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setLayoutOrientation(JList.VERTICAL);
        setVisibleRowCount(-1);
        setDropMode(DropMode.ON);
    }
}
