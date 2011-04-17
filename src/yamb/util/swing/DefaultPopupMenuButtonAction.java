package yamb.util.swing;

import de.cismet.tools.gui.JPopupMenuButton;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JPopupMenu;

/**
 * @author manuel.laflamme
 * @since Aug 15, 2008
 */
public class DefaultPopupMenuButtonAction extends AbstractAction
{
    private final JPopupMenuButton mMenuButton;
    private final JPopupMenu mPopupMenu;

    public DefaultPopupMenuButtonAction(JPopupMenuButton aMenuButton, JPopupMenu aPopupMenu)
    {
        mMenuButton = aMenuButton;
        mPopupMenu = aPopupMenu;
        mMenuButton.setPopupMenu(aPopupMenu);
    }

    public void actionPerformed(ActionEvent e)
    {
        mPopupMenu.setVisible(true);
        mPopupMenu.show(mMenuButton,0, mMenuButton.getHeight());
    }
}
