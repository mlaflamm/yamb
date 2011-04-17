package yamb.util.commands.xml;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * @author Manuel Laflamme
 * @since 2001
 */
public class DtdResolver implements EntityResolver
{
    /*
     * Return an empty ByteArrayInputStream as the source.
     * If you know how to get an input source to the actual
     * DTD pointed to by systemId, return an InputSource
     * pointing to it
     */
    public InputSource resolveEntity(String publicId, String systemId)
    {
        InputStream in = getClass().getClassLoader().getResourceAsStream(
                "fnj3/util/commands/dom/commands.dtd");
        if (in != null)
        {
            return (new InputSource(in));
        }
        else
        {
            // disable dtd loading
            return (new InputSource(new ByteArrayInputStream("".getBytes())));
        }
    }
}