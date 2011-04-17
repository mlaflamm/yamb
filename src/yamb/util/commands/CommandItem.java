package yamb.util.commands;

import javax.swing.Icon;
import javax.swing.KeyStroke;

public interface CommandItem
{
//    final public ItemType UNKNOWN_ITEM    = new ItemType("unknown");
    public final ItemType SEPARATOR = new ItemType("separator");
    public final ItemType MENUBAR = new ItemType("menubar");
    public final ItemType MENU = new ItemType("menu");
    public final ItemType MENU_ITEM = new ItemType("menuItem");
    public final ItemType TOOLBAR = new ItemType("toolbar");
    public final ItemType TOOLBAR_ITEM = new ItemType("toolbarItem");

    public ItemType getType();

    public String getId();

    public String getText();

    public int getMnemonic();

    public Icon getIcon();

    public String getToolTipText();

    public String getDescription();

    public KeyStroke getAccelerator();

    public String getGroupName();

    public int getGroupSize();

    public CommandItem[] getChildItems() throws CommandException;
//	public boolean hasChildItems();

}