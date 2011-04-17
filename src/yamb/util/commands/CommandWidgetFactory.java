package yamb.util.commands;

import java.util.HashMap;
import java.util.Map;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;


public class CommandWidgetFactory
{
    /**
     *
     */
    public static JMenuBar buildMenuBar(CommandItem aItem, ActionModel aModel)
            throws CommandException
    {
        return buildMenuBar(aItem, aModel, null, null);
    }

    public static JMenuBar buildMenuBar(CommandItem aItem, ActionModel aModel, CommandGroupModel aGroupModel)
            throws CommandException
    {
        return buildMenuBar(aItem, aModel, aGroupModel, null);
    }

    /**
     *
     */
    private static void initMenu(JMenu aMenu, CommandItem aItem) throws CommandException
    {
        aMenu.setText(aItem.getText());
        KeyStroke stroke = aItem.getAccelerator();
        if (stroke != null)
        {
            aMenu.setAccelerator(aItem.getAccelerator());
        }
        aMenu.setIcon(aItem.getIcon());
        aMenu.setToolTipText(aItem.getToolTipText());
        aMenu.setMnemonic(aItem.getMnemonic());
    }

    /**
     *
     */
    public static JMenuBar buildMenuBar(CommandItem aItem, ActionModel aModel, CommandGroupModel aGroupModel, Map<String, JMenu> aMenuMap)
            throws CommandException
    {
        // Create a new empty menubar
        JMenuBar menubar = new JMenuBar();

        CommandItem[] items = aItem.getChildItems();
        for (int i = 0; i < items.length; i++)
        {
            // A menubar only contains submenus as direct child
            if (items[i].getType() == CommandItem.MENU)
            {
                JMenu submenu = null;
                if (aMenuMap == null)
                {
                    submenu = buildMenu(items[i], aModel, aGroupModel, aMenuMap);
                }
                else
                {
                    String id = items[i].getId();
                    if (id != null && aMenuMap.containsKey(id))
                    {
                        submenu = (JMenu) aMenuMap.get(items[i].getId());
                        initMenu(submenu, items[i]);
                    }
                    else
                    {
                        submenu = buildMenu(items[i], aModel, aGroupModel, aMenuMap);
                        aMenuMap.put(id, submenu);
                    }
                }
                menubar.add(submenu);
            }
            else
            {
                throw new CommandException("Illegal command item type '" + items[i].getType() + "'");
            }
        }

        return menubar;
    }

    /**
     *
     */
    public static JMenu buildMenu(CommandItem aItem, ActionModel aModel)
            throws CommandException
    {
        return buildMenu(aItem, aModel, null, null);
    }

    public static JMenu buildMenu(CommandItem aItem, ActionModel aModel, CommandGroupModel aGroupModel)
            throws CommandException
    {
        return buildMenu(aItem, aModel, aGroupModel, null);
    }

    /**
     *
     */
    public static JMenu buildMenu(CommandItem aItem, ActionModel aModel, CommandGroupModel aGroupModel, Map<String, JMenu> aMenuMap)
            throws CommandException
    {
        Map<String, ButtonGroup> groups = new HashMap<String, ButtonGroup>();

        // Create the menu
        JMenu menu = new JMenu();
        initMenu(menu, aItem);

        CommandItem[] items = aItem.getChildItems();
        for (CommandItem item : items)
        {
            ItemType type = item.getType();

            // Separator
            if (type == CommandItem.SEPARATOR)
            {
                menu.add(new JSeparator());
            }

            // Submenu
            else if (type == CommandItem.MENU)
            {
                JMenu submenu = null;
                if (aMenuMap == null)
                {
                    submenu = buildMenu(item, aModel, aGroupModel, aMenuMap);
                }
                else
                {
                    String id = item.getId();
                    if (id != null && aMenuMap.containsKey(id))
                    {
                        submenu = (JMenu) aMenuMap.get(item.getId());
                        initMenu(submenu, item);
                    }
                    else
                    {
                        submenu = buildMenu(item, aModel, aGroupModel, aMenuMap);
                        aMenuMap.put(id, submenu);
                    }
                }
                menu.add(submenu);
            }

            // Menu item
            else if (type == CommandItem.MENU_ITEM)
            {
                if (item.getId() != null)
                {
                    JMenuItem menuItem = null;
                    String groupName = item.getGroupName();
                    if (groupName != null)
                    {
                        if (item.getGroupSize() > 1)
                        {
                            menuItem = new JRadioButtonMenuItem();
                        }
                        else
                        {
                            menuItem = new JCheckBoxMenuItem();
                        }

                        ButtonGroup buttonGroup = groups.get(groupName);
                        if (buttonGroup == null)
                        {
                            buttonGroup = new ButtonGroup();
                            groups.put(groupName, buttonGroup);
                        }
                        buttonGroup.add(menuItem);

                        if (aGroupModel != null)
                        {
                            aGroupModel.addGroupItem(item.getId(), menuItem);
                        }
                    }
                    else
                    {
                        menuItem = new JMenuItem();
                    }

                    Action action = aModel.getAction(item.getId());
                    if (action != null)
                    {
                        menuItem.setAction(action);
                        menuItem.setAccelerator(item.getAccelerator());
                        action.putValue(Action.LONG_DESCRIPTION,
                                item.getDescription());
                    }
                    else
                    {
                        menuItem.setEnabled(false);
                    }
                    menuItem.setText(item.getText());
                    menuItem.setIcon(item.getIcon());
                    menuItem.setToolTipText(item.getToolTipText());
                    menuItem.setMnemonic(item.getMnemonic());
                    menu.add(menuItem);
                }
            }
            else
            {
                throw new CommandException("Illegal command item type '" + type + "'");
            }

        }

        return menu;
    }

    /**
     *
     */
    public static JToolBar buildToolBar(CommandItem aItem, ActionModel aActionModel, CommandGroupModel aGroupModel)
            throws CommandException
    {
        return buildToolBar(aItem, aActionModel, aGroupModel, null);
    }

    public static JToolBar buildToolBar(CommandItem aItem, ActionModel aActionModel, CommandGroupModel aGroupModel,
            Map<String, AbstractButton> aItemMap) throws CommandException
    {
        Map<String, ButtonGroup> groups = new HashMap<String, ButtonGroup>();

        // Create a new empty toolbar
        JToolBar toolbar = new JToolBar();

        CommandItem[] items = aItem.getChildItems();
        for (CommandItem item : items)
        {
            ItemType type = item.getType();

            // Separator
            if (type == CommandItem.SEPARATOR)
            {
                toolbar.addSeparator();
            }

            // Toolbar button
            else if (type == CommandItem.TOOLBAR_ITEM)
            {
                if (item.getId() != null)
                {
                    AbstractButton button = null;
                    String id = item.getId();
                    String groupName = item.getGroupName();
                    if (groupName != null)
                    {
                        button = new JToggleButton();
                        ButtonGroup buttonGroup = groups.get(groupName);
                        if (buttonGroup == null)
                        {
                            buttonGroup = new ButtonGroup();
                            groups.put(groupName, buttonGroup);
                        }
                        buttonGroup.add(button);

                        if (aGroupModel != null)
                        {
                            aGroupModel.addGroupItem(id, button);
                        }
                    }
                    else
                    {
                        if (aItemMap != null)
                        {
                            button = aItemMap.get(id);
                            if (button == null)
                            {
                                button = new JButton();
                                aItemMap.put(id, button);
                            }
                        }
                        else
                        {
                            button = new JButton();
                        }
                    }

                    Action action = aActionModel.getAction(id);
                    if (action != null)
                    {
                        button.setAction(action);
//                            button.setAccelerator(childItems[i].getAccelerator());
                    }
                    else
                    {
                        button.setEnabled(false);
                    }
                    button.setText(item.getText());
                    button.setIcon(item.getIcon());
                    button.setToolTipText(item.getToolTipText());
                    toolbar.add(button);
                }
            }
            else
            {
                throw new CommandException("Illegal command item type '" + type + "'");
            }
        }

        return toolbar;
    }

}