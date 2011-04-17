package yamb.app.view.explorer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import javax.swing.JTextField;

/**
 * @author manuel.laflamme
 * @since Feb 16, 2008
 */
public class ActiveFolderTextField extends JTextField implements PropertyChangeListener, ActionListener
{
    private final ExplorerViewContext mViewContext;

    public ActiveFolderTextField(ExplorerViewContext aViewContext)
    {
        mViewContext = aViewContext;
        setText(mViewContext.getActiveFolder().getAbsolutePath());

        mViewContext.addPropertyChangeListener(this);
        addActionListener(this);
    }

    public void actionPerformed(ActionEvent aEvent)
    {
        File activeFolder = new File(getText());
        if (activeFolder.exists() && activeFolder.isDirectory())
        {
            mViewContext.setActiveFolder(new ActiveFolder(activeFolder));
        }
    }

    public void propertyChange(PropertyChangeEvent aEvent)
    {
        String propertyName = aEvent.getPropertyName();
        if (ExplorerViewContext.ACTIVE_FOLDER.equals(propertyName))
        {
            ActiveFolder activeFolder = (ActiveFolder) aEvent.getNewValue();
            setText(activeFolder.getAbsolutePath());
        }
    }
}
