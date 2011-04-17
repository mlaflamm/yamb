package yamb.util.commands;

import javax.swing.Action;

/**
 * @author Manuel Laflamme
 * @since 2001
 */
public class CompositeActionFactory implements ActionFactory
{
    private final ActionFactory[] mFactories;

    public CompositeActionFactory(ActionFactory... aFactories)
    {
        mFactories = aFactories;
    }

    /////////////////////////////////////////////////////////////////////////////
    // IActionFactory Interface

    /**
     *
     */
    public Action createAction(String id)
    {
        for (int i = 0; i < mFactories.length; i++)
        {
            Action action = mFactories[i].createAction(id);
            if (action != null)
            {
                return action;
            }
        }

        return null;
    }
}