package yamb.util.commands.xml;

import yamb.util.commands.CommandException;
import yamb.util.commands.CommandItem;
import org.w3c.dom.Element;

import java.util.ResourceBundle;

/**
 * @author Manuel Laflamme
 * @since 2001
 */
public class XmlCommandMenu extends XmlCommandItemNode
{
    public XmlCommandMenu(Element elemItem, Element elemCommands, ResourceBundle bundle)
            throws CommandException
    {
        super(CommandItem.MENU, elemItem, elemCommands, bundle);
    }

    ////////////////////////////////////////////////////////////////////////////
    // AbstractXmlCommandItem class

    /**
     *
     */
    protected String getItemText()
    {
        String text = super.getItemText();
        if (text != null)
        {
            return text;
        }

        return getId();
    }
}