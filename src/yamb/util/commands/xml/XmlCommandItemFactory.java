package yamb.util.commands.xml;

import yamb.util.commands.CommandException;
import yamb.util.commands.CommandItem;
import org.w3c.dom.Element;

import java.util.ResourceBundle;

/**
 * @author Manuel Laflamme
 * @since 2001
 */
class XmlCommandItemFactory
{
    /**
     *
     */
    static CommandItem createCommandItem(Element elemItem,
            Element elemCommands, ResourceBundle bundle) throws CommandException
    {
        String typeName = elemItem.getNodeName();

        if (typeName.equals(CommandItem.MENU_ITEM.toString()))
        {
            return new XmlCommandMenuItem(elemItem, elemCommands, bundle);
        }

        if (typeName.equals(CommandItem.SEPARATOR.toString()))
        {
            return new XmlCommandItemLeaf(CommandItem.SEPARATOR, elemItem,
                    elemCommands, bundle);
        }

        if (typeName.equals(CommandItem.MENU.toString()))
        {
            return new XmlCommandMenu(elemItem, elemCommands, bundle);
        }

        if (typeName.equals(CommandItem.TOOLBAR_ITEM.toString()))
        {
            return new XmlCommandItemLeaf(CommandItem.TOOLBAR_ITEM, elemItem,
                    elemCommands, bundle);
        }

        if (typeName.equals(CommandItem.TOOLBAR.toString()))
        {
            return new XmlCommandItemNode(CommandItem.TOOLBAR, elemItem,
                    elemCommands, bundle);
        }

        if (typeName.equals(CommandItem.MENUBAR.toString()))
        {
            return new XmlCommandItemNode(CommandItem.MENUBAR, elemItem,
                    elemCommands, bundle);
        }

        throw new IllegalArgumentException("Unknown item type '" + typeName + "'");
    }
}