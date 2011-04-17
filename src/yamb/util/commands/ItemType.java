package yamb.util.commands;

/**
 * This class encapsulate a command item type.
 *
 * @author Manuel Laflamme
 * @since 2001
 */
public class ItemType
{
    private final String mType;

    /**
     * Construct a new command item type object with the given type name.
     */
    public ItemType(String type)
    {
        mType = type;
    }
    /**
     *
     */
    public String toString()
    {
        return mType;
    }

}