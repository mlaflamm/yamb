package yamb.util.event;

import java.lang.reflect.InvocationTargetException;

public class EventListenerTargetException extends EventDispatchException
{
    /**
     *
     */
    public EventListenerTargetException(InvocationTargetException e)
    {
        super(e.getTargetException());
    }
    /**
     *
     */
    public Throwable getTargetException()
    {
        return super.getCause();
    }
}