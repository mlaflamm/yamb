package yamb.util.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * @author manuel.laflamme
 * @since 27-Aug-2008
 */
// todo: rename, not a set anymore
public class TextDataSet
{
    private List<String> mLines = new ArrayList<String>();

    public TextDataSet(InputStream aInputStream) throws IOException
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(aInputStream));
        try
        {
            String line;
            while ((line = reader.readLine()) != null)
            {
                mLines.add(line);
            }
        }
        finally
        {
            reader.close();
        }
    }

    public void write(OutputStream aOutputStream) throws IOException
    {
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(aOutputStream));
        try
        {
            for (String line : mLines)
            {
                writer.println(line);
            }
            writer.flush();
        }
        finally
        {
            writer.close();
        }
    }

    public List<String> getValues()
    {
        List<String> values = new ArrayList<String>();

        for (String line : mLines)
        {
            line = cleanLine(line);
            if (line.length() > 0)
            {
                values.add(line);
            }
        }

        return values;
    }

    private String cleanLine(String line)
    {
        int index = line.indexOf("#");
        if (index > -1)
        {
            line = line.substring(0, index);
        }

        line = line.trim();
        return line;
    }

    //    public boolean contains(String aValue)
//    {
//        for (String line : mLines)
//        {
//            line = cleanLine(line);
//            if (aValue.equals(line))
//            {
//                return true;
//            }
//        }
//
//        return false;
//    }
//
    public boolean add(String aValue)
    {
//        if (contains(aValue))
//        {
//            return false;
//        }
//
        mLines.add(aValue);
        return true;
    }

    /**
     * Remove specified value from dataset. Remove the first item found. Search backward from the end of the collection.
     */
    public boolean remove(String aValue)
    {
//        for (Iterator<String> it = mLines.iterator(); it.hasNext();)
//        {
        ListIterator<String> it = mLines.listIterator(mLines.size());
        while (it.hasPrevious())
        {
            String line = it.previous();
            line = cleanLine(line);
            if (aValue.equals(line))
            {
                it.remove();
                return true;
            }
        }

        return false;
    }
}
