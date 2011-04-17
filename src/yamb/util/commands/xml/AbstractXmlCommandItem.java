package yamb.util.commands.xml;

import yamb.util.commands.CommandException;
import yamb.util.commands.CommandItem;
import yamb.util.commands.ItemType;
import yamb.util.commands.Mnemonics;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.ResourceBundle;
import javax.swing.Icon;
import javax.swing.KeyStroke;

public abstract class AbstractXmlCommandItem implements CommandItem
{
    protected final Element mElemItem;
    protected final ItemType mType;
    protected final ResourceBundle mBundle;

    public AbstractXmlCommandItem(ItemType type, Element elemItem, ResourceBundle bundle)
    {
        mType = type;
        mElemItem = elemItem;
        mBundle = bundle;
    }

    /**
     *
     */
    protected String getItemText()
    {
        String text = getNodeAttributeValue(mElemItem, "text");
        if (mBundle != null && text != null && text.startsWith("$"))
        {
            text = text.substring(1, text.length());
            if (!text.startsWith("$"))
            {
                text = mBundle.getObject(text).toString();
            }
        }

        return text;
    }

    /**
     *
     */
    protected String getNodeAttributeValue(Node node, String attribute)
    {
        try
        {
            return node.getAttributes().getNamedItem(attribute).getNodeValue();
        }
        catch (java.lang.NullPointerException e)
        {
            return null;
        }
    }

    /////////////////////////////////////////////////////////////////////////////
    // CommandItem Interface

    /**
     *
     */
    public ItemType getType()
    {
        return mType;
    }

    /**
     *
     */
    public String getText()
    {
        String text = getItemText();
        if (text != null)
        {
            text = Mnemonics.normalize(text);
        }

        return text;
    }

    /**
     *
     */
    public KeyStroke getAccelerator()
    {
        return null;
    }

    /**
     *
     */
    public Icon getIcon()
    {
        return null;
    }

    /**
     *
     */
    public String getDescription()
    {
        return null;
    }

    /**
     *
     */
    public String getToolTipText()
    {
        return null;
    }

    /**
     *
     */
    public int getMnemonic()
    {
        String text = getItemText();
        if (text == null)
        {
            text = "";
        }
        return Mnemonics.parseMnemonic(text);
    }


    public String getGroupName()
    {
        return null;
    }

    public int getGroupSize()
    {
        return 0;
    }

    /**
     *
     */
    public CommandItem[] getChildItems() throws CommandException
    {
        throw new CommandException("Unsupported operation");
    }
}