package yamb.util.commands;

import javax.swing.Action;

/**
 * @author Manuel Laflamme
 * @since 2001
 */
public interface ActionFactory
{
    public Action createAction(String aId);
}