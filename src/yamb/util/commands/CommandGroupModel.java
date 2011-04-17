package yamb.util.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractButton;
import javax.swing.JToggleButton;

/**
 * @author manuel.laflamme
 * @since Feb 17, 2008
 */
public class CommandGroupModel
{
    private final Map<String, List<JToggleButton.ToggleButtonModel>> mItemGroupMapping =
            new HashMap<String, List<JToggleButton.ToggleButtonModel>>();

    public void addGroupItem(String aId, AbstractButton aWidget)
    {
        List<JToggleButton.ToggleButtonModel> toggleModels = mItemGroupMapping.get(aId);
        if (toggleModels == null)
        {
            toggleModels = new ArrayList<JToggleButton.ToggleButtonModel>();
            mItemGroupMapping.put(aId, toggleModels);
        }

        if (aWidget.getModel() instanceof JToggleButton.ToggleButtonModel)
        {
            JToggleButton.ToggleButtonModel toggleModel = (JToggleButton.ToggleButtonModel) aWidget.getModel();
            toggleModels.add(toggleModel);
        }
    }

    public void setSelected(String aId, boolean aSelected)
    {
        List<JToggleButton.ToggleButtonModel> toggleModels = mItemGroupMapping.get(aId);
        if (toggleModels != null)
        {
            for (JToggleButton.ToggleButtonModel toggleModel : toggleModels)
            {
                toggleModel.setSelected(aSelected);
            }
        }
    }
}
