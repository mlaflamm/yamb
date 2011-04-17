package yamb.util.commands;

/**
 * @author Manuel Laflamme
 * @since 2001
 */
public interface CommandProvider
{
    public CommandItem getMenuBarItem(String aId) throws CommandException;

    public CommandItem getMenuItem(String aId) throws CommandException;

    public CommandItem getToolBarItem(String aId) throws CommandException;
}