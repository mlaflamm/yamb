package yamb.app.fileitem.filelist;

import yamb.app.fileitem.FileItem;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;


/**
 * @author manuel.laflamme
 * @since 11-Feb-2008
 */
public class FileListSelectionManager //implements ListSelectionListener//, ActiveContext
{
    private final JList mFileList;

    public FileListSelectionManager(JList aFileList)
    {
        mFileList = aFileList;
    }

    public void addListSelectionListener(ListSelectionListener aListener)
    {
        mFileList.addListSelectionListener(aListener);
    }

    public void removeListSelectionListener(ListSelectionListener aListener)
    {
        mFileList.removeListSelectionListener(aListener);
    }

    public void addModelPropertyChangeListener(PropertyChangeListener aListener)
    {
        mFileList.addPropertyChangeListener("model", aListener);
    }

    public void removeModelPropertyChangeListener(PropertyChangeListener aListener)
    {
        mFileList.removePropertyChangeListener("model", aListener);
    }

    public boolean isSelectionEmpty()
    {
        ListSelectionModel selectionModel = mFileList.getSelectionModel();
        return selectionModel.isSelectionEmpty();
    }

    public int getFilesSelectedCount()
    {
        ListSelectionModel selectionModel = mFileList.getSelectionModel();
        int minIndex = selectionModel.getMinSelectionIndex();
        int maxIndex = selectionModel.getMaxSelectionIndex();

        if (selectionModel.isSelectionEmpty())
        {
            return 0;
        }

        int selectedCount = 0;
        for (int i = minIndex; i <= maxIndex; i++)
        {
            if (selectionModel.isSelectedIndex(i))
            {
                selectedCount++;
            }
        }

        return selectedCount;
    }

    public int getFilesTotalCount()
    {
        return mFileList.getModel().getSize();
    }

    public void selectAllFiles()
    {
        ListSelectionModel selectionModel = mFileList.getSelectionModel();
        ListModel model = mFileList.getModel();
        selectionModel.setSelectionInterval(0, model.getSize());
    }

    public List<File> getSelectedFiles()
    {
        ArrayList<File> files = new ArrayList<File>();
        ListSelectionModel selectionModel = mFileList.getSelectionModel();
        if (selectionModel.isSelectionEmpty())
        {
            return files;
        }

        ListModel listModel = mFileList.getModel();

        int min = selectionModel.getMinSelectionIndex();
        int max = selectionModel.getMaxSelectionIndex();
        for (int i = min; i <= max; i++)
        {
            if (selectionModel.isSelectedIndex(i))
            {
                FileItem item = (FileItem) listModel.getElementAt(i);
                files.add(item.getFile());
            }
        }

        return files;
    }

    public List<FileItem> getSelectedItems()
    {
        ArrayList<FileItem> files = new ArrayList<FileItem>();
        ListSelectionModel selectionModel = mFileList.getSelectionModel();
        if (selectionModel.isSelectionEmpty())
        {
            return files;
        }

        ListModel listModel = mFileList.getModel();

        int min = selectionModel.getMinSelectionIndex();
        int max = selectionModel.getMaxSelectionIndex();
        for (int i = min; i <= max; i++)
        {
            if (selectionModel.isSelectedIndex(i))
            {
                FileItem item = (FileItem) listModel.getElementAt(i);
                files.add(item);
            }
        }

        return files;
    }

    public void addSelectedFiles(Collection<File> aFiles)
    {
        if (aFiles.isEmpty())
        {
            return;
        }

        Set<File> files = new HashSet<File>(aFiles);
        ListSelectionModel selectionModel = mFileList.getSelectionModel();
        ListModel listModel = mFileList.getModel();

        for (int i = 0; i < listModel.getSize(); i++)
        {
            FileItem item = (FileItem) listModel.getElementAt(i);
            if (files.contains(item.getFile()))
            {
                selectionModel.addSelectionInterval(i, i);
                files.remove(item.getFile());
                if (files.isEmpty())
                {
                    break;
                }
            }
        }
    }

    public void addSelectedItems(Collection<FileItem> aItems)
    {
        if (aItems.isEmpty())
        {
            return;
        }

        Set<FileItem> items = new HashSet<FileItem>(aItems);
        ListSelectionModel selectionModel = mFileList.getSelectionModel();
        ListModel listModel = mFileList.getModel();

        for (int i = 0; i < listModel.getSize(); i++)
        {
            FileItem item = (FileItem) listModel.getElementAt(i);
            if (items.contains(item))
            {
                selectionModel.addSelectionInterval(i, i);
                items.remove(item);
                if (items.isEmpty())
                {
                    break;
                }
            }
        }
    }

    public void removeSelectedFiles(List<File> aFiles)
    {
        // No file to remove
        if (aFiles.isEmpty())
        {
            return;
        }

        // No selection
        ListSelectionModel selectionModel = mFileList.getSelectionModel();
        if (selectionModel.isSelectionEmpty())
        {
            return;
        }

        Set<File> files = new HashSet<File>(aFiles);
        ListModel listModel = mFileList.getModel();

        int min = selectionModel.getMinSelectionIndex();
        int max = selectionModel.getMaxSelectionIndex();
        for (int i = min; i <= max; i++)
        {
            if (selectionModel.isSelectedIndex(i))
            {
                FileItem item = (FileItem) listModel.getElementAt(i);
                if (files.contains(item.getFile()))
                {
                    selectionModel.removeSelectionInterval(i, i);
                    files.remove(item.getFile());
                    if (files.isEmpty())
                    {
                        break;
                    }
                }
            }
        }
    }

    public void clearSelectedFiles()
    {
        ListSelectionModel selectionModel = mFileList.getSelectionModel();
        selectionModel.clearSelection();
    }
}
