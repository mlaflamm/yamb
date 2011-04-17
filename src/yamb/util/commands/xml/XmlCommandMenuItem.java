package yamb.util.commands.xml;

import yamb.util.commands.CommandException;
import yamb.util.commands.CommandItem;
import org.w3c.dom.Element;

import java.util.ResourceBundle;

/**
 * @author Manuel Laflamme
 * @since 2001
 */
public class XmlCommandMenuItem extends XmlCommandItemLeaf
{

    public XmlCommandMenuItem(Element elemItem, Element elemCommands, ResourceBundle bundle)
            throws CommandException
    {
        super(CommandItem.MENU_ITEM, elemItem, elemCommands, bundle);
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

        text = getCommandAttributeValue("text");
        if (text != null)
        {
            return text;
        }

        return getId();
    }
//    /**
//     *
//     */
//    protected boolean isTooltipEnabled()
//    {
//        return false;
//    }

    ////////////////////////////////////////////////////////////////////////////
    // CommandItem interface

    /**
     *
     */
    public String getToolTipText()
    {
        return null;
//        return getDescription();
    }

}