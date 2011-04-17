package yamb.util.swing;

import javax.swing.ListModel;

/**
 * @author manuel.laflamme
 * @since 9-Aug-2008
 */

public class ListModels
{
    public static ListModel filteredModel(ListModel aModel, ObjectFilter aFilter)
    {
        return new AdaptiveFilteredListModel(aModel, aFilter);
//        return new BuggyFilteredListModel(aModel, aFilter);
//        return new SimpleFilteredListModel(aModel, aFilter);
    }
}
