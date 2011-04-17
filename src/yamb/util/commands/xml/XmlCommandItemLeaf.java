package yamb.util.commands.xml;

import yamb.util.commands.CommandException;
import yamb.util.commands.ItemType;
import yamb.util.commands.Mnemonics;
import yamb.util.xml.XPathAPI;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ResourceBundle;
import javax.swing.Icon;
import javax.swing.KeyStroke;

/**
 * @author Manuel Laflamme
 * @since 2001
 */

public class XmlCommandItemLeaf extends AbstractXmlCommandItem
{
    protected Element mElemCommand = null;
    protected final String mRef;
    protected String mGroupName = null;
    protected int mGroupSize = 0;

    public XmlCommandItemLeaf(ItemType type, Element elemItem, Element elemCommands,
            ResourceBundle bundle) throws CommandException
    {
        super(type, elemItem, bundle);

        mRef = getNodeAttributeValue(elemItem, "ref");

        try
        {
            if (mRef != null)
            {
                mElemCommand = (Element) XPathAPI.selectSingleNode(elemCommands, "command[@id = '" + mRef + "']");
                mGroupName = getNodeAttributeValue(mElemCommand, "group");
                if (mGroupName != null)
                {
                    NodeList groupNodeList = XPathAPI.selectNodeList(elemCommands, "command[@group = '" + mGroupName + "']");
                    mGroupSize = groupNodeList.getLength();
                }
            }
        }
        catch (org.xml.sax.SAXException e)
        {
            throw new CommandException(e);
        }
    }

    protected String getCommandAttributeValue(String attribute)
    {
        String value = getNodeAttributeValue(mElemCommand, attribute);

        if (value == null && mBundle != null)
        {
            value = mBundle.getObject(getId() + "_" + attribute).toString();
        }

        return value;
    }

    /**
     *
     */
    protected boolean isTooltipEnabled()
    {
        return true;
    }

    /**
     *
     */
    private boolean isIconEnabled()
    {
        String displayIcon = getNodeAttributeValue(mElemItem, "displayIcon");
        if ((displayIcon == null && getType().equals(TOOLBAR_ITEM)) ||
                (displayIcon != null && displayIcon.equals("true")))
        {
            return true;
        }

        return false;
    }

    ////////////////////////////////////////////////////////////////////////////
    // CommandItem interface

    /**
     *
     */
    public String getId()
    {
        return mRef;
    }

    /**
     *
     */
    public String getDescription()
    {
        return getCommandAttributeValue("description");
    }

    /**
     *
     */
    public KeyStroke getAccelerator()
    {
        String value = getNodeAttributeValue(mElemCommand, "accelerator");
        if (value != null)
        {
            return KeyStroke.getKeyStroke(value);
        }
        return null;
    }

    /**
     *
     */
    public Icon getIcon()
    {
        if (!isIconEnabled())
        {
            return null;
        }

        String filename = getCommandAttributeValue("icon");
        if (filename == null)
        {
            return null;
        }

        java.net.URL location = this.getClass().getClassLoader().getResource(filename);
        return new javax.swing.ImageIcon(location);
    }

    /**
     *
     */
    public String getToolTipText()
    {
        if (isTooltipEnabled())
        {
            String text = getCommandAttributeValue("text");
            if (text != null)
            {
                text = Mnemonics.normalize(text);
            }
            return text;
        }

        return null;
    }


    public String getGroupName()
    {
        return mGroupName;
    }

    public int getGroupSize()
    {
        return mGroupSize;
    }
}