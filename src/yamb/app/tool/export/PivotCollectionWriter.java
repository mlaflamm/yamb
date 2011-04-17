package yamb.app.tool.export;

import yamb.app.fileitem.filelist.thumbnail.GlobalThumbnailCache;
import yamb.app.tag.TagManager;
import yamb.app.tag.series.Series;
import yamb.util.xml.XmlWriter;
import org.apache.commons.io.FilenameUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * @author manuel.laflamme
 * @since Dec 11, 2009
 */
public class PivotCollectionWriter
{
    public PivotCollectionWriter()
    {
    }

    public static void write(File aOutCxmlFile, TagManager aTagManager,
            File aInDzcFile, GlobalThumbnailCache aThumbnailCache) throws IOException
    {
        FileWriter writer = new FileWriter(aOutCxmlFile);
        XmlWriter xmlWriter = new XmlWriter(writer);

        Map<String, String> aThumbnailIndexMap = readDzc(aInDzcFile);

        try
        {
            //<?xml  version="1.0"?>
            //<Collection Name="Hello World Collection" SchemaVersion="1.0"
            xmlWriter.writeElement("Collection").writeAttribute("Name", "Yamb Collection").writeAttribute("SchemaVersion", "1.0");
            // xmlns="http://schemas.microsoft.com/collection/metadata/2009"
            xmlWriter.writeAttribute("xmlns", "http://schemas.microsoft.com/collection/metadata/2009");
            //xmlns:p="http://schemas.microsoft.com/livelabs/pivot/collection/2009"
            xmlWriter.writeAttribute("xmlns:p", "http://schemas.microsoft.com/livelabs/pivot/collection/2009");
            //xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
            xmlWriter.writeAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
            //xmlns:xsd="http://www.w3.org/2001/XMLSchema">
            xmlWriter.writeAttribute("xmlns:xsd", "http://www.w3.org/2001/XMLSchema");
            //  <FacetCategories>
            xmlWriter.writeElement("FacetCategories");
            //    <FacetCategory Name="Tag" Type="String"/>
            xmlWriter.writeElement("FacetCategory").writeAttribute("Name", "Tag").writeAttribute("Type", "String").endElement();
            //    <FacetCategory Name="Group" Type="String"/>
            xmlWriter.writeElement("FacetCategory").writeAttribute("Name", "Group").writeAttribute("Type", "String").endElement();
            //  </FacetCategories>
            xmlWriter.endElement();

            //  <Items ImgBase="helloworld.dzc">
            xmlWriter.writeElement("Items").writeAttribute("ImgBase", aInDzcFile.getAbsolutePath());

            List<File> taggedFiles = aTagManager.getTaggedFiles();
            for (int i = 0; i < taggedFiles.size(); i++)
            {
                File taggedFile = taggedFiles.get(i);
                String absoluteFilePath = taggedFile.getAbsolutePath();
                String fileName = taggedFile.getName();

                if (fileName.contains("ü") || fileName.contains("ö") || fileName.contains("ß") || fileName.contains("à") || fileName.contains("é") || fileName.contains("ä"))
                {
                    continue;
                }

                File thumbnailFile = aThumbnailCache.getThumbnailFile(taggedFile);
                String thumbnailFileName = thumbnailFile.getName().toLowerCase();
                String imageIndex = aThumbnailIndexMap.get(thumbnailFileName);
//                System.out.println(imageIndex +", "+thumbnailFileName);

                xmlWriter.writeElement("Item").writeAttribute("Id", "" + i).writeAttribute("Href", absoluteFilePath).writeAttribute("Name", fileName);
                if (imageIndex != null)
                {
                    xmlWriter.writeAttribute("Img", "#" + imageIndex);
                }
                //      <Description> This is the only item in the collection.</Description>
                xmlWriter.writeElementWithText("Description", fileName);
                //      <Facets>
                xmlWriter.writeElement("Facets");
                //        <Facet Name="Group">
                xmlWriter.writeElement("Facet").writeAttribute("Name", "Group");
                //         <String Value="110 Percent Natural"/>
                xmlWriter.writeElement("String").writeAttribute("Value", Series.getSeriesName(fileName)).endElement();
                //        </Facet>
                xmlWriter.endElement();

                //        <Facet Name="Tag">
                xmlWriter.writeElement("Facet").writeAttribute("Name", "Tag");
                List<String> tags = aTagManager.getFileTags(taggedFile);
                for (String tag : tags)
                {
                    //         <String Value="Nicara"/>
                    xmlWriter.writeElement("String").writeAttribute("Value", tag).endElement();
                }
                //        </Facet>
                xmlWriter.endElement();
                //      </Facets>
                xmlWriter.endElement();
                //    </Item>
                xmlWriter.endElement();
            }
            //  </Items>
            xmlWriter.endElement();
            //</Collection>
            xmlWriter.endElement();
            xmlWriter.close();
        }
        finally
        {
            writer.close();
        }
    }

    public static Map<String, String> readDzc(File aInDzcFile) throws IOException
    {
        Map<String, String> thumbnailMap = new HashMap<String, String>();
        try
        {
//            Document document = new Document(new FileInputStream(aInDzcFile));
//            Elements elements = document.getElement("Collection").getElement("Items").getElements("I");
//
//            File parentFile = aInDzcFile.getParentFile();
//
//            while (elements.hasMoreElements())
//            {
//                Element element = (Element) elements.nextElement();
//                String id = element.getAttributeValue("Id");
//                String source = element.getAttributeValue("Source");
//
//                File thumbnailFile = new File(parentFile, source);
//                thumbnailMap.put(thumbnailFile, id);
//
//                System.out.println(thumbnailFile.getAbsolutePath());
//            }

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(aInDzcFile);
            doc.getDocumentElement().normalize();
            NodeList nodeLst = doc.getElementsByTagName("I");

            for (int s = 0; s < nodeLst.getLength(); s++)
            {

                Node fstNode = nodeLst.item(s);

                if (fstNode.getNodeType() == Node.ELEMENT_NODE)
                {
                    Element fstElmnt = (Element) fstNode;
                    String id = fstElmnt.getAttribute("Id");
                    String source = URLDecoder.decode(fstElmnt.getAttribute("Source"), "utf-8");

                    String thumbnailFileName = FilenameUtils.removeExtension(FilenameUtils.getName(source)) + ".jpg";
                    thumbnailMap.put(thumbnailFileName, id);

                    System.out.println(id + ", " + thumbnailFileName);
                }
            }
        }
        catch (ParserConfigurationException e)
        {
            throw new IOException(e);
        }
//        catch (ParseException e)
//        {
//            throw new IOException(e);
//        }
        catch (SAXException e)
        {
            throw new IOException(e);
        }
        return thumbnailMap;

    }
}
