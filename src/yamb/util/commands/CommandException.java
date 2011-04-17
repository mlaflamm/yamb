package yamb.util.commands;

/**
 * @author Manuel Laflamme
 * @since 2001
 */
public class CommandException extends Exception
{
    public CommandException()
    {
        super();
    }

    public CommandException(String msg)
    {
        super(msg);
    }

    public CommandException(Throwable e)
    {
        super(e);
    }

    public CommandException(String msg, Throwable e)
    {
        super(msg, e);
    }

}