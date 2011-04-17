package yamb.app.fileitem.folders;

import yamb.util.Disposable;

import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.DropMode;
import javax.swing.InputMap;
import javax.swing.JTree;
import javax.swing.KeyStroke;

public class FolderJTree extends JTree implements Disposable
{
    public FolderJTree()
    {
//        super(new FolderTreeModel());
        setRootVisible(false);
        setExpandsSelectedPaths(true);
        setDropMode(DropMode.ON);

        addPropertyChangeListener("model", new PropertyChangeListener()
        {
            public void propertyChange(PropertyChangeEvent aEvent)
            {
                if (aEvent.getOldValue() instanceof Disposable)
                {
                    ((Disposable) aEvent.getOldValue()).dispose();
                }
            }
        });

        // Ensure that copy, paste and rename accelerators are not trap by the tree
        InputMap im = getInputMap();
        KeyStroke ctrlC = KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_MASK);
        KeyStroke ctrlV = KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_MASK);
        KeyStroke f2 = KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0);
        String actionKey = "none";
        im.put(ctrlC, actionKey);
        im.put(ctrlV, actionKey);
        im.put(f2, actionKey);
    }

    public void dispose()
    {
        if (getModel() instanceof Disposable)
        {
            ((Disposable) getModel()).dispose();
        }
    }
}