package app.scan.util;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

public class XmlUtil {

    public static Document parseString(String xml) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        return dbFactory.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
    }

    public static String evaluateXPath(String expression, Document document) throws XPathExpressionException {
        XPathFactory factory = XPathFactory.newInstance();
        XPath xPath = factory.newXPath();
        NodeList results = (NodeList) xPath.evaluate(expression, document, XPathConstants.NODESET);
        return results.item(0).getTextContent();
    }

}
