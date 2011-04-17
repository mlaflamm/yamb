package yamb.util.commands;

import java.util.HashMap;
import java.util.Map;
import javax.swing.Action;

/**
 * @author Manuel Laflamme
 * @since 2001
 */
public class DefaultActionModel implements ActionModel
{
    protected final Map<String, Action> mActionMap = new HashMap<String, Action>();
    protected final ActionFactory mActionFactory;

    /**
     *
     */
    public DefaultActionModel(ActionFactory aActionFactory)
    {
        mActionFactory = aActionFactory;
    }

    /////////////////////////////////////////////////////////////////////////////
    //  ActionModel interface

    /**
     *
     */
    public Action getAction(String aId)
    {
        if (mActionMap.containsKey(aId))
        {
            return (Action) mActionMap.get(aId);
        }
        Action action = getActionFactory().createAction(aId);
        mActionMap.put(aId, action);

        return action;
    }

    /**
     *
     */
    protected ActionFactory getActionFactory()
    {
        return mActionFactory;
    }
}