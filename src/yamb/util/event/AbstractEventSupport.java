package yamb.util.event;

import java.util.Collection;
import java.util.EventListener;
import java.util.EventObject;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * This is an abstract utility class that can be used as a basis to implement multi-cast
 * event dispatching. This class provides an abstract implementation to add/remove
 * event listeners and to fire events. The subclass must provide public specialized methods
 * use to perform these operation. This abstract class can also be used to implement
 * property like event support.
 *
 * @author Manuel Laflamme
 * @since 2001
 * @see EventDispatch
 */
public abstract class AbstractEventSupport
{
    private final EventDispatch mEventDispatcher;
    private Collection mListeners = null;
    private Map mChildren = null;

    /**
     * Defines a new AbstractEventSupport object with the specified event dispatcher.
     * The AbstractEventSupport object doesn't explicitly know the type of event
     * and listener it manipulates so the event dispatcher is used as a proxy to explicitly
     * call the event method on listeners.
     *
     * @param handler
     */
    protected AbstractEventSupport(EventDispatch handler)
    {
        mEventDispatcher = handler;
    }
    /**
     * Add a listener to the listener list. The listener is registered for all property.
     *
     * @param listener The key that identify the property to listen on
     */
    protected void addEventListener(Object listener)
    {
        if ( listener != null )
        {
            if ( mListeners == null )
            {
                mListeners = new LinkedList();
//                mListeners = new ReferenceCollection(new LinkedList(),
//                    ReferenceFactory.WEAK);
            }
            mListeners.add(listener);
        }
    }
    /**
     * Remove a listener from the listener list. This removes a listener that was
     * registered for all properties.
     *
     * @param listener The listener to be removed.
     */
    protected void removeEventListener(Object listener)
    {
        if ( mListeners != null )
        {
            mListeners.remove(listener);
        }
    }
    /**
     * Add a listener for a specific property. The listener will be invoked only when
     * a call on {@link #fireEvent(Object, String, EventObject)} specify that property.
     *
     * @param key The key that identify the property to listen on
     * @param listener The listener to be added
     */
    protected void addEventListener(Object key, Object listener)
    {
        if (mChildren == null)
        {
            mChildren = new HashMap();
        }

        AbstractEventSupport child = (AbstractEventSupport) mChildren.get(key);
        if (child == null)
        {
            child = createEventSupport(mEventDispatcher);
            mChildren.put(key, child);
        }
        child.addEventListener(listener);
    }
    /**
     * Remove a lisitener for a specific property.
     *
     * @param key The key that identify the property that was listened on
     * @param listener
     */
    protected void removeEventListener(Object key, Object listener)
    {
        if (mChildren == null)
        {
            return;
        }

        AbstractEventSupport child = (AbstractEventSupport) mChildren.get(key);
        if (child == null)
        {
            return;
        }

        child.removeEventListener(listener);
    }
    /**
     * Return <code>true</code> if contains at least one listener.
     */
    public boolean hasListeners()
    {
        if (mListeners != null && !mListeners.isEmpty())
        {
            return true;
        }

        return false;
    }
    /**
     * Return <code>true</code> if contains at least one listener for the
     * specified key.
     */
    public boolean hasListeners(Object key)
    {
        if (mChildren == null)
        {
            return false;
        }

        AbstractEventSupport child = (AbstractEventSupport) mChildren.get(key);
        if (child == null)
        {
            return false;
        }

        return child.hasListeners();
    }
    /**
     * Report a bound event to any listeners registered for the specified property.
     *
     * @param key The property reporting the event.
     * @param eventName The event method to invoke on listeners.
     * @param event The event object send to listeners.
     * @exception NoSuchEventException
     */
    protected void fireEvent(Object key, String eventName, EventObject event)
        throws EventDispatchException
    {
        fireEvent(eventName, event);

        if (mChildren != null)
        {
            AbstractEventSupport child = (AbstractEventSupport) mChildren.get(key);
            if (child != null)
                child.fireEvent(eventName, event);
        }
    }

    /**
     * Send an event to any listeners registered for all property.
     *
     * @param eventName The event method to invoke on listeners.
     * @param event The event object send to listeners.
     * @exception NoSuchEventException
     */
    protected void fireEvent(String eventName, EventObject event)
        throws EventDispatchException
    {
        if (mListeners != null)
        {
            Object[] targets = mListeners.toArray();
            for (int i = 0; i < targets.length; i++)
            {
                dispatchEvent(eventName, targets[i], event);
            }
        }
    }
    /**
     * Invoke the specified event method with this event object on this listener.
     * This method is called internally by AbstractEventSupport for each registered
     * listener invoked.
     *
     * @param eventName The event method invoked.
     * @param listener The invoked listener.
     * @param event The event object send.
     * @exception NoSuchEventException
     */
    protected void dispatchEvent(String eventName, Object listener, EventObject event)
        throws EventDispatchException
    {
        // override this method if listener is not an implementation of EventListener
        mEventDispatcher.dispatchEvent(eventName, (EventListener)listener, event);
    }
    /**
     * Create a new AbtractEventSupport subclass with the specified event dispatcher. This method
     * is called internally by AbstractEventSupport to support property like event and must be
     * implemented by subclass.
     *
     * @param handler
     */
    protected abstract AbstractEventSupport createEventSupport(EventDispatch handler);

}
