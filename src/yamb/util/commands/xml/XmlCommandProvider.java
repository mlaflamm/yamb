package yamb.util.commands.xml;

import yamb.util.commands.CommandException;
import yamb.util.commands.CommandItem;
import yamb.util.commands.CommandProvider;
import yamb.util.commands.ItemType;
import yamb.util.xml.XPathAPI;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import java.io.InputStream;
import java.util.ResourceBundle;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * @author Manuel Laflamme
 * @since 2001
 */
public class XmlCommandProvider implements CommandProvider
{
    private static final String VALIDATION_FEATURE =
            "http://xml.org/sax/features/validation";

    private final Element mElemCommands;
    private final ResourceBundle mBundle;

    public XmlCommandProvider(InputStream stream) throws CommandException
    {
        this(stream, null);
    }

    public XmlCommandProvider(InputStream stream, ResourceBundle bundle)
            throws CommandException
    {
        mBundle = bundle;

        try
        {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true); // never forget this!
            factory.setValidating(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setEntityResolver(new DtdResolver());
            mElemCommands = builder.parse(new InputSource(stream)).getDocumentElement();
        }
        catch (java.io.IOException e)
        {
            throw new CommandException(e);
        }
        catch (org.xml.sax.SAXException e)
        {
            throw new CommandException(e);
        }
        catch (ParserConfigurationException e)
        {
            throw new CommandException(e);
        }
    }

    /**
     *
     */
    private CommandItem createItem(ItemType type, String id) throws CommandException
    {
        CommandItem item = null;
        try
        {
            Element elemItem = (Element) XPathAPI.selectSingleNode(mElemCommands, type + "[@id = '" + id + "']");
            if (elemItem == null)
            {
                throw new CommandException("Unknown " + type + " '" + id + "'");
            }
            item = XmlCommandItemFactory.createCommandItem(elemItem,
                    mElemCommands, mBundle);
        }
        catch (org.xml.sax.SAXException e)
        {
            throw new CommandException(e);
        }
        return item;
    }

    /////////////////////////////////////////////////////////////////////////////
    // CommandProvider interface

    /**
     *
     */
    public CommandItem getMenuBarItem(String id) throws CommandException
    {
        return createItem(CommandItem.MENUBAR, id);
    }

    /**
     *
     */
    public CommandItem getMenuItem(String id) throws CommandException
    {
        return createItem(CommandItem.MENU, id);
    }

    /**
     *
     */
    public CommandItem getToolBarItem(String id) throws CommandException
    {
        return createItem(CommandItem.TOOLBAR, id);
    }

}