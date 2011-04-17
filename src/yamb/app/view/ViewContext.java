package yamb.app.view;

import yamb.util.commands.ActionModel;

import java.beans.PropertyChangeListener;

/**
 * @author manuel.laflamme
 * @since 11-Feb-2008
 */
public interface ViewContext
{
    public ActionModel getActionModel();

    public void addPropertyChangeListener(PropertyChangeListener aListener);

    public void addPropertyChangeListener(String aPropertyName,
            PropertyChangeListener aListener);

    public void removePropertyChangeListener(PropertyChangeListener aListener);

    public void removePropertyChangeListener(String aPropertyName,
            PropertyChangeListener aListener);
}
