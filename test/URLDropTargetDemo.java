import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.net.URL;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class URLDropTargetDemo extends JPanel
        implements DropTargetListener
{

    DropTarget dropTarget;
    JLabel dropHereLabel;
    JTextField statusField;
    static DataFlavor urlFlavor;

    static
    {
        try
        {
            urlFlavor =
                    new DataFlavor("application/x-java-url; class=java.net.URL");
        }
        catch (ClassNotFoundException cnfe)
        {
            cnfe.printStackTrace();
        }
    }

    public URLDropTargetDemo()
    {
        super(new BorderLayout());
        dropHereLabel = new JLabel("Drop here",
                SwingConstants.CENTER);
        dropHereLabel.setFont(getFont().deriveFont(Font.BOLD, 24.0f));
        add(dropHereLabel, BorderLayout.CENTER);
        // set up drop target stuff
        dropTarget = new DropTarget(dropHereLabel, this);
        statusField = new JTextField(30);
        statusField.setEditable(false);
        add(statusField, BorderLayout.SOUTH);
    }

    public static void main(String[] args)
    {
        JFrame frame = new JFrame("URL DropTarget Demo");
        URLDropTargetDemo demoPanel = new URLDropTargetDemo();
        frame.getContentPane().add(demoPanel);
        frame.pack();
        frame.setVisible(true);
    }

    // drop target listener events

    public void dragEnter(DropTargetDragEvent dtde)
    {
        System.out.println("dragEnter");
    }

    public void dragExit(DropTargetEvent dte)
    {
        System.out.println("dragExit");
    }

    public void dragOver(DropTargetDragEvent dtde)
    {
        System.out.println("dragOver");

    }

    public void drop(DropTargetDropEvent dtde)
    {
        System.out.println("drop");
        dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
        Transferable trans = dtde.getTransferable();
        dumpDataFlavors(trans);
        boolean gotData = false;
        // try for application/x-java-url flavor
        try
        {
            if (trans.isDataFlavorSupported(urlFlavor))
            {
                URL url = (URL) trans.getTransferData(urlFlavor);
                // System.out.println ("got a " + o.getClass().getName());
                statusField.setText(url.toString());
                statusField.setCaretPosition(0);
                gotData = true;
            }
            else if (trans.isDataFlavorSupported(DataFlavor.stringFlavor))
            {
                String s =
                        (String) trans.getTransferData(DataFlavor.stringFlavor);
                statusField.setText(s);
                gotData = true;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            System.out.println("gotData is " + gotData);
            dtde.dropComplete(gotData);
        }
    }

    public void dropActionChanged(DropTargetDragEvent dtde)
    {
        System.out.println("dropActionChanged");

    }

    private void dumpDataFlavors(Transferable trans)
    {
        System.out.println("Flavors:");
        DataFlavor[] flavors = trans.getTransferDataFlavors();
        for (int i = 0; i < flavors.length; i++)
        {
            System.out.println("*** " + i + ": " + flavors[i]);
        }
    }

}
