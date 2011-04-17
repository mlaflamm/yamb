package yamb.app;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JInternalFrame;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

/**
 * @author manuel.laflamme
 * @since 9-Aug-2008
 */
public class InternalFrameSelectorTabbedPane extends JTabbedPane implements InternalFrameListener, PropertyChangeListener, ChangeListener
{
    private final List<JInternalFrame> mFrameList = new ArrayList<JInternalFrame>();

    public InternalFrameSelectorTabbedPane()
    {
        setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        getModel().addChangeListener(this);
        addMouseListener(new MouseAdapter()
        {
            public void mouseClicked(MouseEvent aEvent)
            {
                if (SwingUtilities.isMiddleMouseButton(aEvent) ||
                        (SwingUtilities.isLeftMouseButton(aEvent) && aEvent.getClickCount() == 2))
                {
//                JList fileList = ((JList) aEvent.getComponent());
                    int index = indexAtLocation(aEvent.getX(), aEvent.getY());
                    if (index >= 0)
                    {
                        try
                        {
                            mFrameList.get(index).setClosed(true);
//                            mFrameList.remove(index);
                        }
                        catch (PropertyVetoException e)
                        {
                            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                        }
                    }
                }
            }
        });
    }

    private int getFrameIndex(JInternalFrame aFrame)
    {
        for (int i = 0; i < mFrameList.size(); i++)
        {
            JInternalFrame frame = mFrameList.get(i);
            if (aFrame == frame)
            {
                return i;
            }

        }

        return -1;
    }

    private String getFrameTitle(JInternalFrame aFrame)
    {
        String title = aFrame.getTitle();
        if (title.length() > 50)
        {
            title = title.substring(0, 47) + "...";
        }

        return title;
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    // ChangeListener interface
    //

    public void stateChanged(ChangeEvent aEvent)
    {
        int index = getModel().getSelectedIndex();
        if (index >= 0)
        {
            try
            {
                mFrameList.get(index).setSelected(true);
            }
            catch (PropertyVetoException e)
            {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    // PropertyChangeListener interface
    //

    public void propertyChange(PropertyChangeEvent aEvent)
    {
        JInternalFrame frame = (JInternalFrame) aEvent.getSource();
        int frameIndex = getFrameIndex(frame);
        setTitleAt(frameIndex, getFrameTitle(frame));
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    // InternalFrameListener interface

    public void internalFrameOpened(InternalFrameEvent e)
    {
        JInternalFrame frame = e.getInternalFrame();
        mFrameList.add(frame);
        add(getFrameTitle(frame), null);
        setIconAt(getFrameIndex(frame), frame.getFrameIcon());
        frame.addPropertyChangeListener(JInternalFrame.TITLE_PROPERTY, this);
    }

    public void internalFrameClosing(InternalFrameEvent e)
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void internalFrameClosed(InternalFrameEvent e)
    {
        JInternalFrame frame = e.getInternalFrame();
        int frameIndex = getFrameIndex(frame);
        mFrameList.remove(frameIndex);
        remove(frameIndex);
        frame.removePropertyChangeListener(JInternalFrame.TITLE_PROPERTY, this);
        frame.removeInternalFrameListener(this);
    }

    public void internalFrameIconified(InternalFrameEvent e)
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void internalFrameDeiconified(InternalFrameEvent e)
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void internalFrameActivated(InternalFrameEvent e)
    {
        JInternalFrame frame = e.getInternalFrame();
        int frameIndex = getFrameIndex(frame);
        setSelectedIndex(frameIndex);
    }

    public void internalFrameDeactivated(InternalFrameEvent e)
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

}
