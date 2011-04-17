package yamb.util.event;

import java.lang.reflect.Method;
import java.util.EventListener;
import java.util.EventObject;

public class ReflectionEventDispatch implements EventDispatch
{
    /////////////////////////////////////////////////////////////////////////////
    // EventDispatch interface

    public void dispatchEvent(String eventName, EventListener listener, EventObject event)
        throws EventDispatchException
    {
        // retrieve the event method to invoke from the listener
        Method method = getEventMethod(eventName, listener.getClass(), event);
        if ( method == null )
            throw new NoSuchEventException(eventName);

        // invoke the event method
        try
        {
            Object[] arguments = new Object[] {event};
            method.invoke(listener, arguments);
        }
        catch(IllegalAccessException e)
        {
            throw new EventDispatchException(e);
        }
        catch(java.lang.reflect.InvocationTargetException e)
        {
            throw new EventListenerTargetException(e);
        }
    }

    /////////////////////////////////////////////////////////////////////////////
    // ReflectionEventDispatch class

    protected Method getEventMethod(String eventName, Class listener, EventObject event)
    {
        Method method = null;
        Class[] listenerInterfaces = listener.getInterfaces();

        for ( int i=0; i<listenerInterfaces.length; i++ )
        {
            try
            {
                Method[] methods = listenerInterfaces[i].getMethods();
                String name = listenerInterfaces[i].getName();
                Class[] paramTypes = new Class[] {event.getClass()};
                method = listenerInterfaces[i].getMethod(eventName, paramTypes);
            }
            catch (NoSuchMethodException e)
            {
            }
        }

        // check in all super class if one of them implements the rigth listener
        Class superClass = listener.getSuperclass();
        String superClassName = superClass.getName();
        if (method == null && !(superClassName.equals("java.lang.Object")))
        {
            method = getEventMethod(eventName, superClass, event);
        }
        return method;
    }
}