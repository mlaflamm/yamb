package yamb.app;

import yamb.app.fileitem.FileIconType;
import yamb.app.fileitem.filelist.FileListItem;
import yamb.app.fileitem.filelist.FileListItemCache;
import yamb.app.fileitem.filelist.thumbnail.LocalThumbnailCache;
import yamb.app.tag.Tags;
import yamb.util.media.Videos;
import org.apache.commons.io.FileUtils;
import se.datadosen.component.RiverLayout;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

/**
 * @author manuel.laflamme
 * @since 2-Mar-2008
 */
public class PropertiesDialog
{
    private static final DateFormat DATE_FORMATER = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG);

    private final LocalThumbnailCache mThumbnailCache;
    private final FileListItemCache mListItemCache;

    public PropertiesDialog(LocalThumbnailCache aThumbnailCache, FileListItemCache aListItemCache)
    {
        mThumbnailCache = aThumbnailCache;
        mListItemCache = aListItemCache;
    }

    public void show(List<File> aFiles)
    {
        JComponent component;
        if (aFiles.size() == 1)
        {
            FileListItem item = mListItemCache.getItem(aFiles.get(0), true);
            component = createFilePanel(item);
        }
        else
        {
            JTabbedPane tabbedPane = new JTabbedPane();
            tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
            for (File file : aFiles)
            {
                FileListItem item = mListItemCache.getItem(file, true);
                tabbedPane.addTab(item.toString(), item.getIcon(FileIconType.SMALL), createFilePanel(item));
            }
            component = tabbedPane;
        }

        JOptionPane optionPane = new JOptionPane(component, JOptionPane.PLAIN_MESSAGE/*, JOptionPane.OK_OPTION*/);
        JDialog dialog = optionPane.createDialog(null, "Properties");
        dialog.setVisible(true);
    }

    JComponent createFilePanel(FileListItem aItem)
    {
        File file = aItem.getFile();
        String fileName = file.getName();
        boolean directory = file.isDirectory();
//        FileListItem item = mListItemCache.getItem(aFile, directory);

        JPanel panel = new JPanel(new RiverLayout());
        Color background = panel.getBackground();

        // File name
        panel.add("left vtop", new JLabel(aItem.getIcon(FileIconType.SMALL)));
        panel.add("tab hfill", new PropertyField(fileName, background));

        // Thumbnail
        if (!directory)
        {
            panel.add("left vtop", new JLabel());
            panel.add("tab hfill", new JLabel(mThumbnailCache.getThumbnail(aItem)));
        }

        panel.add("br vtop", new JLabel());
        panel.add("tab hfill", new JSeparator());

        // Location
        panel.add("br vtop", new JLabel("Location:"));
        panel.add("tab hfill", new PropertyField(file.getParentFile().getAbsolutePath(), background));

        // Tags
        if (!directory)
        {
            panel.add("br vtop", new JLabel("Tags:"));
            panel.add("tab hfill vfill", new PropertyField(getTags(file), background));
        }

        // Modified
        panel.add("br vtop", new JLabel("Modified:"));
        panel.add("tab hfill", new PropertyField(DATE_FORMATER.format(new Date(file.lastModified())), background));

        // Size
        if (!directory)
        {
            panel.add("br vtop", new JLabel("Size:"));
            panel.add("tab hfill", new PropertyField(FileUtils.byteCountToDisplaySize(file.length()), background));
        }

        panel.add("br vtop", new JLabel());
        panel.add("tab hfill", new JSeparator());

        if (!directory && Videos.isVideoFile(file))
        {

        }

        return panel;
    }

    private class PropertyField extends JTextField
    {
        public PropertyField(String aText, Color aBackgroundColor)
        {
            super(aText);
            setEditable(false);
            setBackground(aBackgroundColor);
            setForeground(Color.black);
            setBorder(new EmptyBorder(0, 0, 0, 0));
        }
    }

    private String getTags(File aFile)
    {
        String[] fileTags = Tags.getTagsFromFileName(aFile.getName());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(out), true);
        for (String tag : fileTags)
        {
            writer.println(tag);
        }
        writer.flush();
        return out.toString();
    }
}
