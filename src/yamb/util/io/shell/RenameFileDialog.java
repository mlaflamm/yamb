package yamb.util.io.shell;

import org.apache.commons.io.FilenameUtils;
import se.datadosen.component.RiverLayout;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * @author manuel.laflamme
 * @since 12-Feb-2008
 */
public class RenameFileDialog //extends JDialog
{
    private JTextField mNameField;

    public RenameFileDialog()
    {
    }

    /**
     * Returns true if OK button selected
     */
    public void show(File aFile, FileOperation aFileOperation)
    {
        String fileName = aFile.getName();
        boolean directory = aFile.isDirectory();

        String baseName = directory ? fileName : FilenameUtils.getBaseName(fileName);
        String extension = directory ? "" : FilenameUtils.getExtension(fileName);
        mNameField = new JTextField(baseName, 40);
        JTextField extField = new JTextField(extension, 4);

        JPanel panel = new JPanel(new RiverLayout());
        panel.add("left vtop", new JLabel("New Name:"));
        panel.add("br hfill", mNameField);
        if (!directory)
        {
            panel.add("tab", new JLabel(" "));
            panel.add("tab", new JLabel("."));
            panel.add("tab", extField);
        }

        JOptionPane optionPane = new JOptionPane(panel, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
        JDialog dialog = optionPane.createDialog(null, "Rename File");
//        dialog.pack();
        dialog.addWindowListener(new WindowAdapter()
        {
            public void windowOpened(WindowEvent e)
            {
                mNameField.requestFocus();
            }
        });

        while (true)
        {
            dialog.setVisible(true);
            Object value = optionPane.getValue();
            if (value != null && value.equals(JOptionPane.OK_OPTION))
            {
                String newName = directory ? mNameField.getText() :
                        mNameField.getText() + "." + extField.getText();
                try
                {
                    if (!aFileOperation.renameFile(aFile, newName))
                    {
                        continue;
                    }
                }
                catch (IOException e)
                {
                    JOptionPane.showMessageDialog(null, e.getMessage(), "Rename Error", JOptionPane.ERROR_MESSAGE);
                }
            }
            break;
        }
    }
}
