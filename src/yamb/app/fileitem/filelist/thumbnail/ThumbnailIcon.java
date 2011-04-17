package yamb.app.fileitem.filelist.thumbnail;

import java.awt.AlphaComposite;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * @author manuel.laflamme
 * @since Aug 28, 2008
 */
public class ThumbnailIcon implements Icon
{
    private final Icon mIcon;
    private final int mThumbnailWidth;
    private final int mThumbnailHeight;

    private BufferedImage mProcessingOverlayImage;
    private Image mLibraryOverlayImage;

    public ThumbnailIcon(Icon aIcon, int aThumbnailWidth, int aThumbnailHeight)
    {
        if (aIcon == null)
        {
            throw new NullPointerException();
        }
        mIcon = aIcon;
        mThumbnailWidth = aThumbnailWidth;
        mThumbnailHeight = aThumbnailHeight;
    }

    public ThumbnailIcon(ThumbnailIcon aIcon)
    {
        if (aIcon == null)
        {
            throw new NullPointerException();
        }
        mIcon = aIcon.mIcon;
        mThumbnailWidth = aIcon.mThumbnailWidth;
        mThumbnailHeight = aIcon.mThumbnailHeight;
    }

    public ThumbnailIcon(Image aImage, int aThumbnailWidth, int aThumbnailHeight)
    {
        if (aImage == null)
        {
            throw new NullPointerException();
        }
        mIcon = new ImageIcon(aImage);
        mThumbnailWidth = aThumbnailWidth;
        mThumbnailHeight = aThumbnailHeight;
    }

    public void setProcessingOverlayImage(BufferedImage aProcessingOverlayImage)
    {
        mProcessingOverlayImage = aProcessingOverlayImage;
    }

    public void setLibraryOverlayImage(Image aLibraryOverlayImage)
    {
        mLibraryOverlayImage = aLibraryOverlayImage;
    }

    public void paintIcon(Component aComponent, Graphics aGraphics, final int aX, final int aY)
    {
        int x = aX + (getIconWidth() - mIcon.getIconWidth()) / 2;
        int y = aY + (getIconHeight() - mIcon.getIconHeight()) / 2;
        mIcon.paintIcon(aComponent, aGraphics, x, y);

        if (mLibraryOverlayImage != null)
        {
            int libY = y + mIcon.getIconHeight() - mLibraryOverlayImage.getHeight(aComponent);
            aGraphics.drawImage(mLibraryOverlayImage, x, libY, aComponent);
        }

        if (mProcessingOverlayImage != null)
        {
            ((Graphics2D) aGraphics).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
            aGraphics.drawImage(mProcessingOverlayImage, 0, 0, aComponent);
        }
    }

    public int getIconWidth()
    {
        return mThumbnailWidth;
    }

    public int getIconHeight()
    {
        return mThumbnailHeight;
    }
}
