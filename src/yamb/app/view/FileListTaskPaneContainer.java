package yamb.app.view;

import yamb.app.ApplicationContext;
import yamb.app.fileitem.FileItem;
import yamb.app.fileitem.filelist.thumbnail.LocalThumbnailCache;
import yamb.app.tag.series.Series;
import yamb.util.Disposable;
import yamb.util.media.mediainfo.cache.MediaInfoCache;
import org.jdesktop.swingx.JXTaskPaneContainer;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.AbstractListModel;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;

/**
 * @author manuel.laflamme
 * @since Apr 25, 2010
 */
public class FileListTaskPaneContainer extends /*JPanel*/JXTaskPaneContainer
{
    private final FileListViewContext mViewContext;
    private final LocalThumbnailCache mThumbnailCache;
    private final ApplicationContext mAppContext;
    private final MediaInfoCache mMediaInfoCache;

    private ListModel mListModel;
    private ListSelectionModel mListSelectionModel;

    public FileListTaskPaneContainer(FileListViewContext aViewContext, LocalThumbnailCache aThumbnailCache,
            ApplicationContext aAppContext, MediaInfoCache aMediaInfoCache)
    {
        mViewContext = aViewContext;
        mThumbnailCache = aThumbnailCache;
        mAppContext = aAppContext;
        mMediaInfoCache = aMediaInfoCache;
    }

    public void setModel(ListModel aListModel)
    {
        int count = getComponentCount();
        for (int i = 0; i < count; i++)
        {
            Component component = getComponent(i);
            if (component instanceof Disposable)
            {
                ((Disposable) component).dispose();
            }
        }
        removeAll();

        mListModel = aListModel;

        Map<String, List<FileItem>> groupByYear = new TreeMap<String, List<FileItem>>();
        for (int i = 0; i < aListModel.getSize(); i++)
        {
            FileItem item = (FileItem) aListModel.getElementAt(i);
            String year = Series.getFileYearByFileName(item.getFile());
            List<FileItem> list = groupByYear.get(year == null ? "" : year);
            if (list == null)
            {
                list = new ArrayList<FileItem>();
                groupByYear.put(year == null ? "" : year, list);
            }
            list.add(item);
        }

        for (final Map.Entry<String, List<FileItem>> entry : groupByYear.entrySet())
        {
            FileListTaskPane taskPane = new FileListTaskPane(mViewContext, mThumbnailCache, mAppContext, mMediaInfoCache);
            String year = "".equals(entry.getKey()) ? "Unknown" : entry.getKey();
            taskPane.setTitle(year + " (" + entry.getValue().size() + ")");
            taskPane.setModel(new AbstractListModel()
            {
                @Override
                public int getSize()
                {
                    return entry.getValue().size();
                }

                @Override
                public Object getElementAt(int index)
                {
                    return entry.getValue().get(index);
                }
            });
//            taskPane.setSelectionModel(mListSelectionModel);
            add(taskPane);
        }

        invalidate();
    }

    public void setSelectionModel(ListSelectionModel aSelectionModel)
    {
        mListSelectionModel = aSelectionModel;
    }

}
