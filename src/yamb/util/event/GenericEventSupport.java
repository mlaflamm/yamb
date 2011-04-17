package yamb.util.event;

import java.util.EventListener;
import java.util.EventObject;

public class GenericEventSupport extends AbstractEventSupport
{
    public GenericEventSupport()
    {
        super(new ReflectionEventDispatch());
    }

    public GenericEventSupport(EventDispatch aHandler)
    {
        super(aHandler);
    }

    public void addEventListener(EventListener aListener)
    {
        super.addEventListener(aListener);
    }

    public void removeEventListener(EventListener aListener)
    {
        super.removeEventListener(aListener);
    }

    public void addEventListener(Object aKey, EventListener aListener)
    {
        super.addEventListener(aKey, aListener);
    }

    public void removeEventListener(Object aKey, EventListener aListener)
    {
        super.removeEventListener(aKey, aListener);
    }

    public void fireEvent(Object aKey, String aEventName, EventObject aEvent)
        throws EventDispatchException
    {
        super.fireEvent(aKey, aEventName, aEvent);
    }

    public void fireEvent(String aEventName, EventObject aEvent)
        throws EventDispatchException
    {
        super.fireEvent(aEventName, aEvent);
    }


    /////////////////////////////////////////////////////////////////////////////
    // AbstractEventSupport class

    protected AbstractEventSupport createEventSupport(EventDispatch aHandler)
    {
        return new GenericEventSupport(aHandler);
    }
}