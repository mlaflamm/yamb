package yamb.util.event;

import java.util.EventListener;
import java.util.EventObject;

public interface EventDispatch
{
    public void dispatchEvent(String eventName, EventListener listener,
        EventObject event) throws EventDispatchException;
}