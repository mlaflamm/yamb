package yamb.app.fileitem.filelist.thumbnail;

import yamb.app.fileitem.filelist.FileListItem;
import yamb.app.tag.library.LibraryManager;
import yamb.app.view.FileListViewContext;
import yamb.util.io.Files;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JList;

/**
 * @author manuel.laflamme
 * @since 31-Jan-2008
 */
public class FileThumbnailCellRenderer extends DefaultListCellRenderer
{
    private final FileListViewContext mViewContext;
    private final LocalThumbnailCache mThumbnailCache;
    private final LibraryManager mLibraryManager;
    private final Dimension mCellSize;

    private final ImageIcon mLibraryIcon = new ImageIcon(getClass().getClassLoader().getResource("yamb/images/library.gif"));

    public FileThumbnailCellRenderer(FileListViewContext aViewContext, LocalThumbnailCache aThumbnailCache, LibraryManager aLibraryManager)
    {
        mViewContext = aViewContext;
        mThumbnailCache = aThumbnailCache;
        mLibraryManager = aLibraryManager;
        mCellSize = new Dimension(
                mThumbnailCache.getThumbnailWidth() + 5,
                mThumbnailCache.getThumbnailHeight() + 15);
    }

    public Component getListCellRendererComponent(JList aList, Object aValue, int aIndex, boolean aIsSelected, boolean aCellHasFocus)
    {
        aCellHasFocus = aValue.equals(mViewContext.getFocusedItem());
        Component renderer = super.getListCellRendererComponent(aList, aValue, aIndex, aIsSelected,
                aCellHasFocus);

        FileListItem fileItem = (FileListItem) aValue;
        ThumbnailIcon thumbnail = (ThumbnailIcon)mThumbnailCache.getThumbnail(fileItem);
        if (fileItem.isDirectory() && mLibraryManager.isLibraryRoot(fileItem.getFile()))
        {
            thumbnail.setLibraryOverlayImage(mLibraryIcon.getImage());
        }
        else
        {
            thumbnail.setLibraryOverlayImage(null);
        }
        setIcon(thumbnail);

        boolean isRecent = Files.isRecent(fileItem.getFile());
        if (aIsSelected)
        {
            renderer.setForeground(aList.getSelectionForeground());
            renderer.setBackground(isRecent ? Color.RED : aList.getSelectionBackground());
        }
        else
        {
            renderer.setForeground(isRecent ? Color.RED : aList.getForeground());
            renderer.setBackground(aList.getBackground());
        }

        setSize(mCellSize);
        setPreferredSize(mCellSize);
        setMaximumSize(mCellSize);
        setMinimumSize(mCellSize);

        setHorizontalAlignment(CENTER);
        setVerticalAlignment(CENTER);
        setHorizontalTextPosition(CENTER);
        setVerticalTextPosition(BOTTOM);

        return this;
    }

    public Dimension getFixedCellSize()
    {
        return new Dimension(mCellSize);
    }
}
