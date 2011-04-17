package yamb.util.event;

public class NoSuchEventException extends EventDispatchException
{
    public NoSuchEventException()
    {
        super();
    }

    public NoSuchEventException(String s)
    {
        super(s);
    }
}