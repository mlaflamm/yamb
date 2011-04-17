package yamb.util.swing;

import yamb.util.Disposable;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JList;

/**
 * @author manuel.laflamme
 * @since 7-Feb-2008
 */

public class AutoDisposeModelJList extends JList implements Disposable
{
    public AutoDisposeModelJList()
    {
        addPropertyChangeListener("model", new PropertyChangeListener()
        {
            public void propertyChange(PropertyChangeEvent aEvent)
            {
                if (aEvent.getOldValue() instanceof Disposable)
                {
                    ((Disposable) aEvent.getOldValue()).dispose();
                }
            }
        });
    }

    public void dispose()
    {
        if (getModel() instanceof Disposable)
        {
            ((Disposable) getModel()).dispose();
        }
    }
}
