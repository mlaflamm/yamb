package yamb.util.xml;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

/**
 * this is a wrapper class around org.apache.xpath.XPathAPI
 * it rethrows TransformerException in SAXException
 */
public class XPathAPI
{
    private static final XPathFactory FACTORY = XPathFactory.newInstance();

    public static Node selectSingleNode(Node contextNode, String str)
            throws SAXException
    {
        try
        {
            synchronized (FACTORY)
            {
                return (Node) FACTORY.newXPath().compile(str).evaluate(contextNode, XPathConstants.NODE);
            }
        }
        catch (XPathExpressionException e)
        {
            throw new SAXException(e);
        }
    }

    /**
     *
     */
    public static NodeList selectNodeList(Node contextNode, String str)
            throws SAXException
    {
        try
        {
            synchronized (FACTORY)
            {
                return (NodeList) FACTORY.newXPath().compile(str).evaluate(contextNode, XPathConstants.NODESET);
            }
        }
        catch (XPathExpressionException e)
        {
            throw new SAXException(e);
        }
    }

}
