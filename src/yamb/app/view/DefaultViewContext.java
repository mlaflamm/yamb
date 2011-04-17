package yamb.app.view;

import yamb.util.commands.ActionModel;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * @author manuel.laflamme
 * @since Aug 6, 2008
 */
public class DefaultViewContext implements ViewContext
{
    private ActionModel mActionModel;
    protected final PropertyChangeSupport mPropertySupport;

    public DefaultViewContext()
    {
        mPropertySupport = new PropertyChangeSupport(this);
    }

    public ActionModel getActionModel()
    {
        assert(mActionModel != null);
        return mActionModel;
    }

    // todo : fix this hack
    public void setActionModel(ActionModel aActionModel)
    {
        mActionModel = aActionModel;
    }

    public void addPropertyChangeListener(PropertyChangeListener aListener)
    {
        mPropertySupport.addPropertyChangeListener(aListener);
    }

    public void addPropertyChangeListener(String aPropertyName,
            PropertyChangeListener aListener)
    {
        mPropertySupport.addPropertyChangeListener(aPropertyName, aListener);
    }

    public void removePropertyChangeListener(PropertyChangeListener aListener)
    {
        mPropertySupport.removePropertyChangeListener(aListener);
    }

    public void removePropertyChangeListener(String aPropertyName,
            PropertyChangeListener aListener)
    {
        mPropertySupport.removePropertyChangeListener(aPropertyName, aListener);
    }
}
