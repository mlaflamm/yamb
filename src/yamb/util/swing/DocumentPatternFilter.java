package yamb.util.swing;

import java.util.regex.Pattern;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

/**
 * @author manuel.laflamme
 * @since 7-Feb-2008
 */

public class DocumentPatternFilter extends RegexPaternFilter implements DocumentListener
{
    public DocumentPatternFilter()
    {
        super("", Pattern.CASE_INSENSITIVE);
        setAcceptAll(true);
    }

    private void setRegex(DocumentEvent aEvent)
    {
        Document document = aEvent.getDocument();
        try
        {
            String text = document.getText(0, document.getLength());
            boolean acceptAll = text.isEmpty();
            if (acceptAll)
            {
                setAcceptAll(acceptAll);
            }
            else
            {
                setRegex(text.replaceAll(" ", ".*"));
            }
        }
        catch (BadLocationException e)
        {
            throw new IllegalStateException(e);
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////
    // DocumentListener interface

    public void insertUpdate(DocumentEvent aEvent)
    {
        setRegex(aEvent);
    }

    public void removeUpdate(DocumentEvent aEvent)
    {
        setRegex(aEvent);
    }

    public void changedUpdate(DocumentEvent aEvent)
    {
        setRegex(aEvent);
    }
}
