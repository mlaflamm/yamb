package yamb.util.event;

import java.util.EventObject;
import javax.swing.SwingUtilities;

/**
 * @author manuel.laflamme
 * @since Aug 5, 2008
 */
public class SwingSafeEventSupport extends GenericEventSupport
{
    public void fireEvent(Object aKey, String aEventName, EventObject aEvent) throws EventDispatchException
    {
        if (SwingUtilities.isEventDispatchThread())
        {
            super.fireEvent(aKey, aEventName, aEvent);
        }
        else
        {
            SwingUtilities.invokeLater(new DelayedEventInvoker(aKey, aEventName, aEvent));
        }
    }

    public void fireEvent(String aEventName, EventObject aEvent) throws EventDispatchException
    {
        if (SwingUtilities.isEventDispatchThread())
        {
            super.fireEvent(aEventName, aEvent);
        }
        else
        {
            SwingUtilities.invokeLater(new DelayedEventInvoker(aEventName, aEvent));
        }
    }

    private class DelayedEventInvoker implements Runnable
    {
        private final Object mKey;
        private final String mEventName;
        private final EventObject mEvent;

        public DelayedEventInvoker(Object aKey, String aEventName, EventObject aEvent)
        {
            mKey = aKey;
            mEventName = aEventName;
            mEvent = aEvent;
        }

        public DelayedEventInvoker(String aEventName, EventObject aEvent)
        {
            mKey = null;
            mEventName = aEventName;
            mEvent = aEvent;
        }

        public void run()
        {
            if (mKey != null)
            {
                fireEvent(mKey, mEventName, mEvent);
            }
            else
            {
                fireEvent(mEventName, mEvent);
            }
        }
    }
}
