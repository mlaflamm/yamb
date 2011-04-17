package yamb.util.swing;

import junit.framework.TestCase;

import javax.swing.DefaultListModel;
import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

/**
 * @author manuel.laflamme
 * @since Feb 7, 2008
 */
public class TestFilteredListModel extends TestCase
{
    protected DefaultListModel createDefaultListModel(Object[] aValues)
    {
        DefaultListModel model = new DefaultListModel();
        for (Object value : aValues)
        {
            model.addElement(value);
        }
        return model;
    }

    protected ListModel createFilteredListModel(DefaultListModel aDefaultModel, ObjectFilter aFilter)
    {
        return new FilteredListModel(aDefaultModel, aFilter);
    }

    private void assertEquals(ListModel aExpected, ListModel aActual)
    {
        assertEquals("size", aExpected.getSize(), aActual.getSize());
        for (int i = 0; i < aExpected.getSize(); i++)
        {
            assertEquals("" + i, aExpected.getElementAt(i), aActual.getElementAt(i));
        }
    }

    public void testFilterAcceptAll() throws Exception
    {
        ObjectFilter filter = new AbstractObjectFilter()
        {
            public boolean acceptAll()
            {
                return true;
            }

            public boolean accept(Object aObject)
            {
                return true;
            }
        };

        DefaultListModel defaultModel = createDefaultListModel(new Object[]{1, 2, 3, 4, 5, 6});
        ListModel filteredModel = createFilteredListModel(defaultModel, filter);

        assertEquals(defaultModel, filteredModel);
    }

    public void testFilterAcceptAllRemove() throws Exception
    {
        ObjectFilter filter = new AbstractObjectFilter()
        {
            public boolean acceptAll()
            {
                return true;
            }

            public boolean accept(Object aObject)
            {
                return true;
            }
        };

        DefaultListModel defaultModel = createDefaultListModel(new Object[]{1, 2, 3, 4, 5, 6});
        ListModel filteredModel = createFilteredListModel(defaultModel, filter);

        assertEquals(defaultModel, filteredModel);

        while (defaultModel.size() > 0)
        {
            defaultModel.remove(0);
        }

        assertEquals(defaultModel, filteredModel);
    }

    public void testFilterAcceptOdd() throws Exception
    {
        ObjectFilter filter = new AbstractObjectFilter()
        {
            public boolean acceptAll()
            {
                return false;
            }

            public boolean accept(Object aObject)
            {
                return ((Integer) aObject) % 2 == 1;
            }
        };

        DefaultListModel defaultModel = createDefaultListModel(new Object[]{1, 2, 3, 4, 5, 6});
        ListModel filteredModel = createFilteredListModel(defaultModel, filter);

        assertEquals("size", 3, filteredModel.getSize());
        for (int i = 0; i < 3; i++)
        {
            int actualValue = (Integer) filteredModel.getElementAt(i);
            int expectedValue = i * 2 + 1;
            assertEquals("" + i, expectedValue, actualValue);
        }
    }

    public void testRegexFilterAcceptAllChange() throws Exception
    {
        DefaultListModel defaultModel = createDefaultListModel(new Object[]{"a", "aa", "ab", "ba", "bb"});
        RegexPaternFilter filter = RegexPaternFilter.createCaseInsensitiveLiteralFilter("a");
        filter.setAcceptAll(true);
        ListModel filteredModel = createFilteredListModel(defaultModel, filter);
        ListModelRecorder modelRecorder = new ListModelRecorder(filteredModel);
        filteredModel.addListDataListener(modelRecorder);

        assertEquals(defaultModel, filteredModel);
        assertEquals(defaultModel, modelRecorder);

        filter.setRegex("b");

        assertEquals(filteredModel, modelRecorder);
        assertEquals("size b", 3, filteredModel.getSize());
        assertEquals("ab", filteredModel.getElementAt(0));
        assertEquals("ba", filteredModel.getElementAt(1));
        assertEquals("bb", filteredModel.getElementAt(2));

        filter.setAcceptAll(true);

        assertEquals(defaultModel, filteredModel);
        assertEquals(defaultModel, modelRecorder);
    }

    public void testRegexFilterChangeAndElementRemoved() throws Exception
    {
        DefaultListModel defaultModel = createDefaultListModel(new Object[]{"a", "aa", "ab", "ba", "bb"});
        RegexPaternFilter filter = RegexPaternFilter.createCaseInsensitiveLiteralFilter("a");
        ListModel filteredModel = createFilteredListModel(defaultModel, filter);
        ListModelRecorder modelRecorder = new ListModelRecorder(filteredModel);
        filteredModel.addListDataListener(modelRecorder);

        assertEquals(filteredModel, modelRecorder);
        assertEquals("size a", 4, filteredModel.getSize());
        assertEquals("a", filteredModel.getElementAt(0));
        assertEquals("aa", filteredModel.getElementAt(1));
        assertEquals("ab", filteredModel.getElementAt(2));
        assertEquals("ba", filteredModel.getElementAt(3));

        filter.setRegex("b");

        assertEquals(filteredModel, modelRecorder);
        assertEquals("size b", 3, filteredModel.getSize());
        assertEquals("ab", filteredModel.getElementAt(0));
        assertEquals("ba", filteredModel.getElementAt(1));
        assertEquals("bb", filteredModel.getElementAt(2));
    }

    public void testRegexFilterChangeAndElementAdded() throws Exception
    {
        DefaultListModel defaultModel = createDefaultListModel(new Object[]{"a", "aa", "ab", "ba", "bb"});
        RegexPaternFilter filter = RegexPaternFilter.createCaseInsensitiveLiteralFilter("b");
        ListModel filteredModel = createFilteredListModel(defaultModel, filter);
        ListModelRecorder modelRecorder = new ListModelRecorder(filteredModel);
        filteredModel.addListDataListener(modelRecorder);

        assertEquals(filteredModel, modelRecorder);
        assertEquals("size b", 3, filteredModel.getSize());
        assertEquals("ab", filteredModel.getElementAt(0));
        assertEquals("ba", filteredModel.getElementAt(1));
        assertEquals("bb", filteredModel.getElementAt(2));

        filter.setRegex("a");

        assertEquals("size a", 4, filteredModel.getSize());
        assertEquals("a", filteredModel.getElementAt(0));
        assertEquals("aa", filteredModel.getElementAt(1));
        assertEquals("ab", filteredModel.getElementAt(2));
        assertEquals("ba", filteredModel.getElementAt(3));
        assertEquals(filteredModel, modelRecorder);
    }

    public void testRegexFilterChangeAndSameSize() throws Exception
    {
        DefaultListModel defaultModel = createDefaultListModel(new Object[]{"a", "aa", "ab", "ba", "bb", "bc"});
        RegexPaternFilter filter = RegexPaternFilter.createCaseInsensitiveLiteralFilter("a");
        ListModel filteredModel = createFilteredListModel(defaultModel, filter);
        ListModelRecorder modelRecorder = new ListModelRecorder(filteredModel);
        filteredModel.addListDataListener(modelRecorder);

        assertEquals(filteredModel, modelRecorder);
        assertEquals("size a", 4, filteredModel.getSize());
        assertEquals("a", filteredModel.getElementAt(0));
        assertEquals("aa", filteredModel.getElementAt(1));
        assertEquals("ab", filteredModel.getElementAt(2));
        assertEquals("ba", filteredModel.getElementAt(3));

        filter.setRegex("b");

        assertEquals(filteredModel, modelRecorder);
        assertEquals("size b", 4, filteredModel.getSize());
        assertEquals("ab", filteredModel.getElementAt(0));
        assertEquals("ba", filteredModel.getElementAt(1));
        assertEquals("bb", filteredModel.getElementAt(2));
        assertEquals("bc", filteredModel.getElementAt(3));
    }

    public void testRegexFilterChangeAndAllRemoved() throws Exception
    {
        DefaultListModel defaultModel = createDefaultListModel(new Object[]{"a", "aa", "ab", "ba", "bb", "bc"});
        RegexPaternFilter filter = RegexPaternFilter.createCaseInsensitiveLiteralFilter("a");
        ListModel filteredModel = createFilteredListModel(defaultModel, filter);
        ListModelRecorder modelRecorder = new ListModelRecorder(filteredModel);
        filteredModel.addListDataListener(modelRecorder);

        assertEquals(filteredModel, modelRecorder);
        assertEquals("size a", 4, filteredModel.getSize());
        assertEquals("a", filteredModel.getElementAt(0));
        assertEquals("aa", filteredModel.getElementAt(1));
        assertEquals("ab", filteredModel.getElementAt(2));
        assertEquals("ba", filteredModel.getElementAt(3));

        filter.setRegex("z");

        assertEquals(filteredModel, modelRecorder);
        assertEquals("size z", 0, filteredModel.getSize());
    }

    public void testRegexFilterChangeAndAllAdded() throws Exception
    {
        DefaultListModel defaultModel = createDefaultListModel(new Object[]{"a", "aa", "ab", "ba", "bb", "bc"});
        RegexPaternFilter filter = RegexPaternFilter.createCaseInsensitiveLiteralFilter("z");
        ListModel filteredModel = createFilteredListModel(defaultModel, filter);
        ListModelRecorder modelRecorder = new ListModelRecorder(filteredModel);
        filteredModel.addListDataListener(modelRecorder);

        assertEquals(filteredModel, modelRecorder);
        assertEquals("size z", 0, filteredModel.getSize());

        filter.setRegex("a");

        assertEquals(filteredModel, modelRecorder);
        assertEquals("size a", 4, filteredModel.getSize());
        assertEquals("a", filteredModel.getElementAt(0));
        assertEquals("aa", filteredModel.getElementAt(1));
        assertEquals("ab", filteredModel.getElementAt(2));
        assertEquals("ba", filteredModel.getElementAt(3));
    }

    public void testRegexFilterAcceptAll() throws Exception
    {
        DefaultListModel defaultModel = createDefaultListModel(new Object[]{"a", "aa", "ab", "ba", "bb", "bc"});
        RegexPaternFilter filter = RegexPaternFilter.createCaseInsensitiveLiteralFilter("");
        ListModel filteredModel = createFilteredListModel(defaultModel, filter);

        assertEquals("size", 6, filteredModel.getSize());
        assertEquals(defaultModel, filteredModel);
    }


    public void testRegexFilterAcceptNone() throws Exception
    {
        DefaultListModel defaultModel = createDefaultListModel(new Object[]{"a", "aa", "ab", "ba", "bb", "bc"});
        RegexPaternFilter filter = RegexPaternFilter.createCaseInsensitiveLiteralFilter("z");
        ListModel filteredModel = createFilteredListModel(defaultModel, filter);

        assertEquals("size z", 0, filteredModel.getSize());
    }

    public void testModelEmptyChangeFilter() throws Exception
    {
        DefaultListModel defaultModel = createDefaultListModel(new Object[]{});
        RegexPaternFilter filter = RegexPaternFilter.createCaseInsensitiveLiteralFilter("a");
        ListModel filteredModel = createFilteredListModel(defaultModel, filter);
        ListModelRecorder modelRecorder = new ListModelRecorder(filteredModel);
        filteredModel.addListDataListener(modelRecorder);

        assertEquals(filteredModel, modelRecorder);
        assertEquals("size a", 0, filteredModel.getSize());

        filter.setRegex("b");

        assertEquals("size b", 0, filteredModel.getSize());
        assertEquals(filteredModel, modelRecorder);
    }

    public void testModelEmptyAddNotAcceptedElement() throws Exception
    {
        DefaultListModel defaultModel = createDefaultListModel(new Object[]{});
        RegexPaternFilter filter = RegexPaternFilter.createCaseInsensitiveLiteralFilter("a");
        ListModel filteredModel = createFilteredListModel(defaultModel, filter);
        ListModelRecorder modelRecorder = new ListModelRecorder(filteredModel);
        filteredModel.addListDataListener(modelRecorder);

        assertEquals(filteredModel, modelRecorder);
        assertEquals("size a", 0, filteredModel.getSize());

        defaultModel.addElement("z");
        assertEquals("size add z", 0, filteredModel.getSize());
        assertEquals(filteredModel, modelRecorder);
    }

    public void testModelEmptyAddAcceptedElement() throws Exception
    {
        DefaultListModel defaultModel = createDefaultListModel(new Object[]{});
        RegexPaternFilter filter = RegexPaternFilter.createCaseInsensitiveLiteralFilter("a");
        ListModel filteredModel = createFilteredListModel(defaultModel, filter);
        ListModelRecorder modelRecorder = new ListModelRecorder(filteredModel);
        filteredModel.addListDataListener(modelRecorder);

        assertEquals(filteredModel, modelRecorder);
        assertEquals("size a", 0, filteredModel.getSize());

        defaultModel.addElement("a");
        assertEquals("size add a", 1, filteredModel.getSize());
        assertEquals("a", filteredModel.getElementAt(0));
        assertEquals(filteredModel, modelRecorder);
    }

    public void testRemoveFirstAcceptedElement() throws Exception
    {
        DefaultListModel defaultModel = createDefaultListModel(new Object[]{"a", "b", "ab", "bb", "ba", "bc"});
        RegexPaternFilter filter = RegexPaternFilter.createCaseInsensitiveLiteralFilter("a");
        ListModel filteredModel = createFilteredListModel(defaultModel, filter);
        ListModelRecorder modelRecorder = new ListModelRecorder(filteredModel);
        filteredModel.addListDataListener(modelRecorder);

        assertEquals(filteredModel, modelRecorder);
        assertEquals("size a", 3, filteredModel.getSize());
        assertEquals("a", filteredModel.getElementAt(0));
        assertEquals("ab", filteredModel.getElementAt(1));
        assertEquals("ba", filteredModel.getElementAt(2));

        defaultModel.removeElement("a");

        assertEquals("size a", 2, filteredModel.getSize());
        assertEquals("ab", filteredModel.getElementAt(0));
        assertEquals("ba", filteredModel.getElementAt(1));
        assertEquals(filteredModel, modelRecorder);
    }

    public void testRemoveMiddleAcceptedElement() throws Exception
    {
        DefaultListModel defaultModel = createDefaultListModel(new Object[]{"a", "b", "ab", "bb", "ba", "bc"});
        RegexPaternFilter filter = RegexPaternFilter.createCaseInsensitiveLiteralFilter("a");
        ListModel filteredModel = createFilteredListModel(defaultModel, filter);
        ListModelRecorder modelRecorder = new ListModelRecorder(filteredModel);
        filteredModel.addListDataListener(modelRecorder);

        assertEquals(filteredModel, modelRecorder);
        assertEquals("size a", 3, filteredModel.getSize());
        assertEquals("a", filteredModel.getElementAt(0));
        assertEquals("ab", filteredModel.getElementAt(1));
        assertEquals("ba", filteredModel.getElementAt(2));

        defaultModel.removeElement("ab");

        assertEquals("size a", 2, filteredModel.getSize());
        assertEquals("a", filteredModel.getElementAt(0));
        assertEquals("ba", filteredModel.getElementAt(1));
        assertEquals(filteredModel, modelRecorder);
    }

    public void testRemoveBeginRangeAcceptAll() throws Exception
    {
        DefaultListModel defaultModel = createDefaultListModel(new Object[]{"a", "b", "ab", "bb", "ba", "bc"});
        RegexPaternFilter filter = RegexPaternFilter.createCaseInsensitiveLiteralFilter("a");
        ListModel filteredModel = createFilteredListModel(defaultModel, filter);
        ListModelRecorder modelRecorder = new ListModelRecorder(filteredModel);
        filteredModel.addListDataListener(modelRecorder);

        assertEquals("size a", 3, filteredModel.getSize());
        filter.setAcceptAll(true);
        assertEquals("acceptAll", true, filter.acceptAll());
        assertEquals(filteredModel, modelRecorder);
        assertEquals("size acceptAll", 6, filteredModel.getSize());

        defaultModel.removeRange(0, 2);

        assertEquals("size removeRange", 3, filteredModel.getSize());
        assertEquals("bb", filteredModel.getElementAt(0));
        assertEquals("ba", filteredModel.getElementAt(1));
        assertEquals("bc", filteredModel.getElementAt(2));
        assertEquals(filteredModel, modelRecorder);
    }

    public void testRemoveMiddleRangeAcceptAll() throws Exception
    {
        DefaultListModel defaultModel = createDefaultListModel(new Object[]{"a", "b", "ab", "bb", "ba", "bc"});
        RegexPaternFilter filter = RegexPaternFilter.createCaseInsensitiveLiteralFilter("a");
        ListModel filteredModel = createFilteredListModel(defaultModel, filter);
        ListModelRecorder modelRecorder = new ListModelRecorder(filteredModel);
        filteredModel.addListDataListener(modelRecorder);

        assertEquals("size a", 3, filteredModel.getSize());
        filter.setAcceptAll(true);
        assertEquals("acceptAll", true, filter.acceptAll());
        assertEquals(filteredModel, modelRecorder);
        assertEquals("size acceptAll", 6, filteredModel.getSize());

        defaultModel.removeRange(2, 3);

        assertEquals("size removeRange", 4, filteredModel.getSize());
        assertEquals("a", filteredModel.getElementAt(0));
        assertEquals("b", filteredModel.getElementAt(1));
        assertEquals("ba", filteredModel.getElementAt(2));
        assertEquals("bc", filteredModel.getElementAt(3));
        assertEquals(filteredModel, modelRecorder);
    }

    public void testRemoveMiddleRange() throws Exception
    {
        DefaultListModel defaultModel = createDefaultListModel(new Object[]{"a", "b", "ab", "bb", "ba", "bc", "ca"});
        RegexPaternFilter filter = RegexPaternFilter.createCaseInsensitiveLiteralFilter("a");
        ListModel filteredModel = createFilteredListModel(defaultModel, filter);
        ListModelRecorder modelRecorder = new ListModelRecorder(filteredModel);
        filteredModel.addListDataListener(modelRecorder);

        assertEquals("size a", 4, filteredModel.getSize());
        assertEquals("a", filteredModel.getElementAt(0));
        assertEquals("ab", filteredModel.getElementAt(1));
        assertEquals("ba", filteredModel.getElementAt(2));
        assertEquals("ca", filteredModel.getElementAt(3));

        defaultModel.removeRange(1, 5);

        assertEquals("size removeRange", 2, filteredModel.getSize());
        assertEquals("a", filteredModel.getElementAt(0));
        assertEquals("ca", filteredModel.getElementAt(1));
        assertEquals(filteredModel, modelRecorder);
    }

    public void testRemoveEndRangeAcceptAll() throws Exception
    {
        DefaultListModel defaultModel = createDefaultListModel(new Object[]{"a", "b", "ab", "bb", "ba", "bc"});
        RegexPaternFilter filter = RegexPaternFilter.createCaseInsensitiveLiteralFilter("a");
        ListModel filteredModel = createFilteredListModel(defaultModel, filter);
        ListModelRecorder modelRecorder = new ListModelRecorder(filteredModel);
        filteredModel.addListDataListener(modelRecorder);

        assertEquals("size a", 3, filteredModel.getSize());
        filter.setAcceptAll(true);
        assertEquals("acceptAll", true, filter.acceptAll());
        assertEquals(filteredModel, modelRecorder);
        assertEquals("size acceptAll", 6, filteredModel.getSize());

        defaultModel.removeRange(3, 5);

        assertEquals("size removeRange", 3, filteredModel.getSize());
        assertEquals("a", filteredModel.getElementAt(0));
        assertEquals("b", filteredModel.getElementAt(1));
        assertEquals("ab", filteredModel.getElementAt(2));
        assertEquals(filteredModel, modelRecorder);
    }

    public void testRemoveLastAcceptedElement() throws Exception
    {
        DefaultListModel defaultModel = createDefaultListModel(new Object[]{"a", "b", "ab", "bb", "ba", "bc"});
        RegexPaternFilter filter = RegexPaternFilter.createCaseInsensitiveLiteralFilter("a");
        ListModel filteredModel = createFilteredListModel(defaultModel, filter);
        ListModelRecorder modelRecorder = new ListModelRecorder(filteredModel);
        filteredModel.addListDataListener(modelRecorder);

        assertEquals(filteredModel, modelRecorder);
        assertEquals("size a", 3, filteredModel.getSize());
        assertEquals("a", filteredModel.getElementAt(0));
        assertEquals("ab", filteredModel.getElementAt(1));
        assertEquals("ba", filteredModel.getElementAt(2));

        defaultModel.removeElement("ba");

        assertEquals("size a", 2, filteredModel.getSize());
        assertEquals("a", filteredModel.getElementAt(0));
        assertEquals("ab", filteredModel.getElementAt(1));
        assertEquals(filteredModel, modelRecorder);
    }

    public void testRemoveAllAcceptedElement() throws Exception
    {
        DefaultListModel defaultModel = createDefaultListModel(new Object[]{"a", "b", "ab", "bb", "ba", "bc"});
        RegexPaternFilter filter = RegexPaternFilter.createCaseInsensitiveLiteralFilter("a");
        ListModel filteredModel = createFilteredListModel(defaultModel, filter);
        ListModelRecorder modelRecorder = new ListModelRecorder(filteredModel);
        filteredModel.addListDataListener(modelRecorder);

        assertEquals(filteredModel, modelRecorder);
        assertEquals("size a", 3, filteredModel.getSize());
        assertEquals("a", filteredModel.getElementAt(0));
        assertEquals("ab", filteredModel.getElementAt(1));
        assertEquals("ba", filteredModel.getElementAt(2));

        defaultModel.removeElement("ab");
        defaultModel.removeElement("ba");
        defaultModel.removeElement("a");

        assertEquals("size a", 0, filteredModel.getSize());
        assertEquals(filteredModel, modelRecorder);
    }

    public void testRemoveFirstNonAcceptedElement() throws Exception
    {
        DefaultListModel defaultModel = createDefaultListModel(new Object[]{"a", "b", "ab", "bb", "ba", "bc"});
        RegexPaternFilter filter = RegexPaternFilter.createCaseInsensitiveLiteralFilter("a");
        ListModel filteredModel = createFilteredListModel(defaultModel, filter);
        ListModelRecorder modelRecorder = new ListModelRecorder(filteredModel);
        filteredModel.addListDataListener(modelRecorder);

        assertEquals(filteredModel, modelRecorder);
        assertEquals("size a", 3, filteredModel.getSize());
        assertEquals("a", filteredModel.getElementAt(0));
        assertEquals("ab", filteredModel.getElementAt(1));
        assertEquals("ba", filteredModel.getElementAt(2));

        defaultModel.removeElement("b");

        assertEquals("size a", 3, filteredModel.getSize());
        assertEquals("a", filteredModel.getElementAt(0));
        assertEquals("ab", filteredModel.getElementAt(1));
        assertEquals("ba", filteredModel.getElementAt(2));
        assertEquals(filteredModel, modelRecorder);
    }

    public void testRemoveMiddleNonAcceptedElement() throws Exception
    {
        DefaultListModel defaultModel = createDefaultListModel(new Object[]{"a", "b", "ab", "bb", "ba", "bc"});
        RegexPaternFilter filter = RegexPaternFilter.createCaseInsensitiveLiteralFilter("a");
        ListModel filteredModel = createFilteredListModel(defaultModel, filter);
        ListModelRecorder modelRecorder = new ListModelRecorder(filteredModel);
        filteredModel.addListDataListener(modelRecorder);

        assertEquals(filteredModel, modelRecorder);
        assertEquals("size a", 3, filteredModel.getSize());
        assertEquals("a", filteredModel.getElementAt(0));
        assertEquals("ab", filteredModel.getElementAt(1));
        assertEquals("ba", filteredModel.getElementAt(2));

        defaultModel.removeElement("bb");

        assertEquals("size a", 3, filteredModel.getSize());
        assertEquals("a", filteredModel.getElementAt(0));
        assertEquals("ab", filteredModel.getElementAt(1));
        assertEquals("ba", filteredModel.getElementAt(2));
        assertEquals(filteredModel, modelRecorder);

    }

    public void testRemoveLastNonAcceptedElement() throws Exception
    {
        DefaultListModel defaultModel = createDefaultListModel(new Object[]{"a", "b", "ab", "bb", "ba", "bc"});
        RegexPaternFilter filter = RegexPaternFilter.createCaseInsensitiveLiteralFilter("a");
        ListModel filteredModel = createFilteredListModel(defaultModel, filter);
        ListModelRecorder modelRecorder = new ListModelRecorder(filteredModel);
        filteredModel.addListDataListener(modelRecorder);

        assertEquals(filteredModel, modelRecorder);
        assertEquals("size a", 3, filteredModel.getSize());
        assertEquals("a", filteredModel.getElementAt(0));
        assertEquals("ab", filteredModel.getElementAt(1));
        assertEquals("ba", filteredModel.getElementAt(2));

        defaultModel.removeElement("bc");

        assertEquals("size a", 3, filteredModel.getSize());
        assertEquals("a", filteredModel.getElementAt(0));
        assertEquals("ab", filteredModel.getElementAt(1));
        assertEquals("ba", filteredModel.getElementAt(2));
        assertEquals(filteredModel, modelRecorder);
    }

    public void testAddBeginingAcceptedElement() throws Exception
    {
        DefaultListModel defaultModel = createDefaultListModel(new Object[]{"a", "b", "ab", "bb", "ba", "bc"});
        RegexPaternFilter filter = RegexPaternFilter.createCaseInsensitiveLiteralFilter("a");
        ListModel filteredModel = createFilteredListModel(defaultModel, filter);
        ListModelRecorder modelRecorder = new ListModelRecorder(filteredModel);
        filteredModel.addListDataListener(modelRecorder);

        assertEquals(filteredModel, modelRecorder);
        assertEquals("size a", 3, filteredModel.getSize());
        assertEquals("a", filteredModel.getElementAt(0));
        assertEquals("ab", filteredModel.getElementAt(1));
        assertEquals("ba", filteredModel.getElementAt(2));

        defaultModel.insertElementAt("aa", 0);

        System.out.println("Recorder: " + modelRecorder);
        System.out.println("Filtered: " + filteredModel);

        assertEquals(filteredModel, modelRecorder);
        assertEquals("size a", 4, filteredModel.getSize());
        assertEquals("aa", filteredModel.getElementAt(0));
        assertEquals("a", filteredModel.getElementAt(1));
        assertEquals("ab", filteredModel.getElementAt(2));
        assertEquals("ba", filteredModel.getElementAt(3));
    }

    public void testAddMiddleAcceptedElement() throws Exception
    {
        DefaultListModel defaultModel = createDefaultListModel(new Object[]{"a", "b", "ab", "bb", "ba", "bc"});
        RegexPaternFilter filter = RegexPaternFilter.createCaseInsensitiveLiteralFilter("a");
        ListModel filteredModel = createFilteredListModel(defaultModel, filter);
        ListModelRecorder modelRecorder = new ListModelRecorder(filteredModel);
        filteredModel.addListDataListener(modelRecorder);

        assertEquals(filteredModel, modelRecorder);
        assertEquals("size a", 3, filteredModel.getSize());
        assertEquals("a", filteredModel.getElementAt(0));
        assertEquals("ab", filteredModel.getElementAt(1));
        assertEquals("ba", filteredModel.getElementAt(2));

        defaultModel.insertElementAt("aa", 2);

        System.out.println("Recorder: " + modelRecorder);
        System.out.println("Filtered: " + filteredModel);

        assertEquals("size a", 4, filteredModel.getSize());
        assertEquals("a", filteredModel.getElementAt(0));
        assertEquals("aa", filteredModel.getElementAt(1));
        assertEquals("ab", filteredModel.getElementAt(2));
        assertEquals("ba", filteredModel.getElementAt(3));
        assertEquals(filteredModel, modelRecorder);
    }

    public void testAddEndAcceptedElement() throws Exception
    {
        DefaultListModel defaultModel = createDefaultListModel(new Object[]{"a", "b", "ab", "bb", "ba", "bc"});
        RegexPaternFilter filter = RegexPaternFilter.createCaseInsensitiveLiteralFilter("a");
        ListModel filteredModel = createFilteredListModel(defaultModel, filter);
        ListModelRecorder modelRecorder = new ListModelRecorder(filteredModel);
        filteredModel.addListDataListener(modelRecorder);

        assertEquals(filteredModel, modelRecorder);
        assertEquals("size a", 3, filteredModel.getSize());
        assertEquals("a", filteredModel.getElementAt(0));
        assertEquals("ab", filteredModel.getElementAt(1));
        assertEquals("ba", filteredModel.getElementAt(2));

        defaultModel.addElement("aa");

        assertEquals("size a", 4, filteredModel.getSize());
        assertEquals("a", filteredModel.getElementAt(0));
        assertEquals("ab", filteredModel.getElementAt(1));
        assertEquals("ba", filteredModel.getElementAt(2));
        assertEquals("aa", filteredModel.getElementAt(3));
        assertEquals(filteredModel, modelRecorder);
    }

    public void testAddBeginingNonAcceptedElement() throws Exception
    {
        DefaultListModel defaultModel = createDefaultListModel(new Object[]{"a", "b", "ab", "bb", "ba", "bc"});
        RegexPaternFilter filter = RegexPaternFilter.createCaseInsensitiveLiteralFilter("a");
        ListModel filteredModel = createFilteredListModel(defaultModel, filter);
        ListModelRecorder modelRecorder = new ListModelRecorder(filteredModel);
        filteredModel.addListDataListener(modelRecorder);

        assertEquals(filteredModel, modelRecorder);
        assertEquals("size a", 3, filteredModel.getSize());
        assertEquals("a", filteredModel.getElementAt(0));
        assertEquals("ab", filteredModel.getElementAt(1));
        assertEquals("ba", filteredModel.getElementAt(2));

        defaultModel.insertElementAt("c", 0);

        System.out.println("Recorder: " + modelRecorder);
        System.out.println("Filtered: " + filteredModel);

        assertEquals("size a", 3, filteredModel.getSize());
        assertEquals("a", filteredModel.getElementAt(0));
        assertEquals("ab", filteredModel.getElementAt(1));
        assertEquals("ba", filteredModel.getElementAt(2));
        assertEquals(filteredModel, modelRecorder);
    }

    public void testAddMiddleNonAcceptedElement() throws Exception
    {
        DefaultListModel defaultModel = createDefaultListModel(new Object[]{"a", "b", "ab", "bb", "ba", "bc"});
        RegexPaternFilter filter = RegexPaternFilter.createCaseInsensitiveLiteralFilter("a");
        ListModel filteredModel = createFilteredListModel(defaultModel, filter);
        ListModelRecorder modelRecorder = new ListModelRecorder(filteredModel);
        filteredModel.addListDataListener(modelRecorder);

        assertEquals(filteredModel, modelRecorder);
        assertEquals("size a", 3, filteredModel.getSize());
        assertEquals("a", filteredModel.getElementAt(0));
        assertEquals("ab", filteredModel.getElementAt(1));
        assertEquals("ba", filteredModel.getElementAt(2));

        defaultModel.insertElementAt("c", 3);

        assertEquals("size a", 3, filteredModel.getSize());
        assertEquals("a", filteredModel.getElementAt(0));
        assertEquals("ab", filteredModel.getElementAt(1));
        assertEquals("ba", filteredModel.getElementAt(2));
        assertEquals(filteredModel, modelRecorder);

    }

    public void testAddEndNonAcceptedElement() throws Exception
    {
        DefaultListModel defaultModel = createDefaultListModel(new Object[]{"a", "b", "ab", "bb", "ba", "bc"});
        RegexPaternFilter filter = RegexPaternFilter.createCaseInsensitiveLiteralFilter("a");
        ListModel filteredModel = createFilteredListModel(defaultModel, filter);
        ListModelRecorder modelRecorder = new ListModelRecorder(filteredModel);
        filteredModel.addListDataListener(modelRecorder);

        assertEquals(filteredModel, modelRecorder);
        assertEquals("size a", 3, filteredModel.getSize());
        assertEquals("a", filteredModel.getElementAt(0));
        assertEquals("ab", filteredModel.getElementAt(1));
        assertEquals("ba", filteredModel.getElementAt(2));

        defaultModel.addElement("c");

        assertEquals("size a", 3, filteredModel.getSize());
        assertEquals("a", filteredModel.getElementAt(0));
        assertEquals("ab", filteredModel.getElementAt(1));
        assertEquals("ba", filteredModel.getElementAt(2));
        assertEquals(filteredModel, modelRecorder);

    }

    public void testChangeFirstBecomeAcceptedElement() throws Exception
    {
        DefaultListModel defaultModel = createDefaultListModel(new Object[]{"bc", "a", "b", "ab", "bb"});
        RegexPaternFilter filter = RegexPaternFilter.createCaseInsensitiveLiteralFilter("a");
        ListModel filteredModel = createFilteredListModel(defaultModel, filter);
        ListModelRecorder modelRecorder = new ListModelRecorder(filteredModel);
        filteredModel.addListDataListener(modelRecorder);

        assertEquals(filteredModel, modelRecorder);
        assertEquals("size a", 2, filteredModel.getSize());
        assertEquals("a", filteredModel.getElementAt(0));
        assertEquals("ab", filteredModel.getElementAt(1));

        defaultModel.setElementAt("aa", 0);

        assertEquals("size a", 3, filteredModel.getSize());
        assertEquals("aa", filteredModel.getElementAt(0));
        assertEquals("a", filteredModel.getElementAt(1));
        assertEquals("ab", filteredModel.getElementAt(2));
        assertEquals(filteredModel, modelRecorder);

    }

    public void testChangeMidlleBecomeAcceptedElement() throws Exception
    {
        DefaultListModel defaultModel = createDefaultListModel(new Object[]{"a", "b", "bb", "bbb", "ba"});
        RegexPaternFilter filter = RegexPaternFilter.createCaseInsensitiveLiteralFilter("a");
        ListModel filteredModel = createFilteredListModel(defaultModel, filter);
        ListModelRecorder modelRecorder = new ListModelRecorder(filteredModel);
        filteredModel.addListDataListener(modelRecorder);

        assertEquals(filteredModel, modelRecorder);
        assertEquals("size a", 2, filteredModel.getSize());
        assertEquals("a", filteredModel.getElementAt(0));
        assertEquals("ba", filteredModel.getElementAt(1));

        defaultModel.setElementAt("aa", 2);

        assertEquals("size a", 3, filteredModel.getSize());
        assertEquals("a", filteredModel.getElementAt(0));
        assertEquals("aa", filteredModel.getElementAt(1));
        assertEquals("ba", filteredModel.getElementAt(2));
        assertEquals(filteredModel, modelRecorder);
    }

    public void testChangeEndBecomeAcceptedElement() throws Exception
    {
        DefaultListModel defaultModel = createDefaultListModel(new Object[]{"a", "b", "ab", "bb"});
        RegexPaternFilter filter = RegexPaternFilter.createCaseInsensitiveLiteralFilter("a");
        ListModel filteredModel = createFilteredListModel(defaultModel, filter);
        ListModelRecorder modelRecorder = new ListModelRecorder(filteredModel);
        filteredModel.addListDataListener(modelRecorder);

        assertEquals(filteredModel, modelRecorder);
        assertEquals("size a", 2, filteredModel.getSize());
        assertEquals("a", filteredModel.getElementAt(0));
        assertEquals("ab", filteredModel.getElementAt(1));

        defaultModel.setElementAt("aa", 3);

        assertEquals("size a", 3, filteredModel.getSize());
        assertEquals("a", filteredModel.getElementAt(0));
        assertEquals("ab", filteredModel.getElementAt(1));
        assertEquals("aa", filteredModel.getElementAt(2));
        assertEquals(filteredModel, modelRecorder);
    }

    public void testChangeFirstBecomeNonAcceptedElement() throws Exception
    {
        DefaultListModel defaultModel = createDefaultListModel(new Object[]{"a", "b", "ab", "bb", "ba"});
        RegexPaternFilter filter = RegexPaternFilter.createCaseInsensitiveLiteralFilter("a");
        ListModel filteredModel = createFilteredListModel(defaultModel, filter);
        ListModelRecorder modelRecorder = new ListModelRecorder(filteredModel);
        filteredModel.addListDataListener(modelRecorder);

        assertEquals(filteredModel, modelRecorder);
        assertEquals("size a", 3, filteredModel.getSize());
        assertEquals("a", filteredModel.getElementAt(0));
        assertEquals("ab", filteredModel.getElementAt(1));
        assertEquals("ba", filteredModel.getElementAt(2));

        defaultModel.setElementAt("c", 0);

        assertEquals("size a", 2, filteredModel.getSize());
        assertEquals("ab", filteredModel.getElementAt(0));
        assertEquals("ba", filteredModel.getElementAt(1));
        assertEquals(filteredModel, modelRecorder);
    }

    public void testChangeMidleBecomeNonAcceptedElement() throws Exception
    {
        DefaultListModel defaultModel = createDefaultListModel(new Object[]{"a", "b", "ab", "bb", "ba"});
        RegexPaternFilter filter = RegexPaternFilter.createCaseInsensitiveLiteralFilter("a");
        ListModel filteredModel = createFilteredListModel(defaultModel, filter);
        ListModelRecorder modelRecorder = new ListModelRecorder(filteredModel);
        filteredModel.addListDataListener(modelRecorder);

        assertEquals(filteredModel, modelRecorder);
        assertEquals("size a", 3, filteredModel.getSize());
        assertEquals("a", filteredModel.getElementAt(0));
        assertEquals("ab", filteredModel.getElementAt(1));
        assertEquals("ba", filteredModel.getElementAt(2));

        defaultModel.setElementAt("c", 2);

        assertEquals("size a", 2, filteredModel.getSize());
        assertEquals("a", filteredModel.getElementAt(0));
        assertEquals("ba", filteredModel.getElementAt(1));
        assertEquals(filteredModel, modelRecorder);

    }

    public void testChangeEndBecomeNonAcceptedElement() throws Exception
    {
        DefaultListModel defaultModel = createDefaultListModel(new Object[]{"a", "b", "ab", "bb", "ba"});
        RegexPaternFilter filter = RegexPaternFilter.createCaseInsensitiveLiteralFilter("a");
        ListModel filteredModel = createFilteredListModel(defaultModel, filter);
        ListModelRecorder modelRecorder = new ListModelRecorder(filteredModel);
        filteredModel.addListDataListener(modelRecorder);

        assertEquals(filteredModel, modelRecorder);
        assertEquals("size a", 3, filteredModel.getSize());
        assertEquals("a", filteredModel.getElementAt(0));
        assertEquals("ab", filteredModel.getElementAt(1));
        assertEquals("ba", filteredModel.getElementAt(2));

        defaultModel.setElementAt("c", 4);

        assertEquals("size a", 2, filteredModel.getSize());
        assertEquals("a", filteredModel.getElementAt(0));
        assertEquals("ab", filteredModel.getElementAt(1));
        assertEquals(filteredModel, modelRecorder);
    }

    public void testChangeStayAcceptedElement() throws Exception
    {
        DefaultListModel defaultModel = createDefaultListModel(new Object[]{"a", "b", "ab", "bb", "ba"});
        RegexPaternFilter filter = RegexPaternFilter.createCaseInsensitiveLiteralFilter("a");
        ListModel filteredModel = createFilteredListModel(defaultModel, filter);
        ListModelRecorder modelRecorder = new ListModelRecorder(filteredModel);
        filteredModel.addListDataListener(modelRecorder);

        assertEquals(filteredModel, modelRecorder);
        assertEquals("size a", 3, filteredModel.getSize());
        assertEquals("a", filteredModel.getElementAt(0));
        assertEquals("ab", filteredModel.getElementAt(1));
        assertEquals("ba", filteredModel.getElementAt(2));

        defaultModel.setElementAt("aa", 2);

        assertEquals("size a", 3, filteredModel.getSize());
        assertEquals("a", filteredModel.getElementAt(0));
        assertEquals("aa", filteredModel.getElementAt(1));
        assertEquals("ba", filteredModel.getElementAt(2));
        assertEquals(filteredModel, modelRecorder);
    }

    public void testChangeStayNonAcceptedElement() throws Exception
    {
        DefaultListModel defaultModel = createDefaultListModel(new Object[]{"a", "b", "ab", "bb", "ba"});
        RegexPaternFilter filter = RegexPaternFilter.createCaseInsensitiveLiteralFilter("a");
        ListModel filteredModel = createFilteredListModel(defaultModel, filter);
        ListModelRecorder modelRecorder = new ListModelRecorder(filteredModel);
        filteredModel.addListDataListener(modelRecorder);

        assertEquals(filteredModel, modelRecorder);
        assertEquals("size a", 3, filteredModel.getSize());
        assertEquals("a", filteredModel.getElementAt(0));
        assertEquals("ab", filteredModel.getElementAt(1));
        assertEquals("ba", filteredModel.getElementAt(2));

        defaultModel.setElementAt("cc", 3);

        assertEquals("size a", 3, filteredModel.getSize());
        assertEquals("a", filteredModel.getElementAt(0));
        assertEquals("ab", filteredModel.getElementAt(1));
        assertEquals("ba", filteredModel.getElementAt(2));
        assertEquals(filteredModel, modelRecorder);
    }

    private static class ListModelRecorder extends DefaultListModel implements ListDataListener
    {
        public ListModelRecorder(ListModel aModel)
        {
            for (int i = 0; i < aModel.getSize(); i++)
            {
                addElement(aModel.getElementAt(i));
            }
        }

        public void intervalAdded(ListDataEvent e)
        {
            System.out.println(e);
            ListModel sourceModel = (ListModel) e.getSource();
            for (int i = e.getIndex0(); i <= e.getIndex1(); i++)
            {
                insertElementAt(sourceModel.getElementAt(i), i);
            }
        }

        public void intervalRemoved(ListDataEvent e)
        {
            System.out.println(e);
            removeRange(e.getIndex0(), e.getIndex1());
        }

        public void contentsChanged(ListDataEvent e)
        {
            System.out.println(e);
            ListModel sourceModel = (ListModel) e.getSource();
            for (int i = e.getIndex0(); i <= e.getIndex1(); i++)
            {
                setElementAt(sourceModel.getElementAt(i), i);
            }
        }
    }
}
