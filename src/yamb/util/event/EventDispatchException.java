package yamb.util.event;

public class EventDispatchException extends RuntimeException
{
    public EventDispatchException()
    {
        super();
    }

    public EventDispatchException(String msg)
    {
        super(msg);
    }

    public EventDispatchException(Throwable e)
    {
        super(e);
    }

    public EventDispatchException(String msg, Throwable e)
    {
        super(msg, e);
    }

}