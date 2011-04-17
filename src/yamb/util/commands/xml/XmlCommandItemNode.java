package yamb.util.commands.xml;

import yamb.util.commands.CommandException;
import yamb.util.commands.CommandItem;
import yamb.util.commands.ItemType;
import yamb.util.xml.XPathAPI;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ResourceBundle;

/**
 * @author Manuel Laflamme
 * @since 2001
 */
public class XmlCommandItemNode extends AbstractXmlCommandItem
{
    final protected Element mElemCommands;
    final protected String mId;

    public XmlCommandItemNode(ItemType type, Element elemItem, Element elemCommands, ResourceBundle bundle)
    {
        super(type, elemItem, bundle);
        mId = getNodeAttributeValue(elemItem, "id");
        mElemCommands = elemCommands;
    }

    /**
     *
     */
    public String getId()
    {
        return mId;
    }

    /**
     *
     */
    public CommandItem[] getChildItems() throws CommandException
    {
        CommandItem[] items = null;

        try
        {
            NodeList nodeList = XPathAPI.selectNodeList(mElemItem, "*");

            items = new CommandItem[nodeList.getLength()];
            for (int i = 0; i < items.length; i++)
            {
                items[i] = XmlCommandItemFactory.createCommandItem(
                        (Element) nodeList.item(i), mElemCommands, mBundle);
            }
        }
        catch (org.xml.sax.SAXException e)
        {
            throw new CommandException(e);
        }

        return items;
    }
}