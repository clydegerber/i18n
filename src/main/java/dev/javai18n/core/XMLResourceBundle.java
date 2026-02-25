/*
 * Copyright 2026 Clyde Gerber
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.javai18n.core;

import static dev.javai18n.core.LocalizableLogger.I18N_LOGGER;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * A ResourceBundle that is loaded from an XML document. The document must conform to the DTD
 * provided in this module's resources (dev/javai18n/core/properties.dtd) or from
 * https://java.sun.com/dtd/properties.dtd. The DTD provided with this module is a super set of
 * the latter.
 */
public class XMLResourceBundle  extends AttributeCollectionResourceBundle
{
    /**
     * An EntityResolver that resolves the properties DTD from this module's resources (dev/javai18n/core/properties.dtd)
     * or from https://java.sun.com/dtd/properties.dtd.
     */
    protected static final class PropertiesDtdResolver implements EntityResolver
    {
        /** Singleton instance of the resolver. */
        protected static final PropertiesDtdResolver RESOLVER = new PropertiesDtdResolver();

        /** Constructs a PropertiesDtdResolver. */
        PropertiesDtdResolver() {}
        private static final String SUN_DTD_HTTPS = "https://java.sun.com/dtd/properties.dtd";
        private static final String SUN_DTD_HTTP = "http://java.sun.com/dtd/properties.dtd";
        private static final int TIMEOUT_MS = 5000;

        /** The Sun properties DTD. */
        private static volatile byte[] cachedSunDtd;

        @Override
        public InputSource resolveEntity(String publicID, String systemID)
                throws SAXException, IOException
        {
            if (null == systemID)
            {
                throw new SAXException("Resolution of external entity blocked: null systemID");
            }
            if (systemID.endsWith("properties.dtd"))
            {
                InputStream dtdStream = PropertiesDtdResolver.class.getModule()
                        .getResourceAsStream("dev/javai18n/core/properties.dtd");
                if (null == dtdStream)
                {
                    throw new SAXException("Local properties DTD not found");
                }
                return new InputSource(dtdStream);
            }
            if (systemID.equals(SUN_DTD_HTTP) || systemID.equals(SUN_DTD_HTTPS))
            {
                byte[] dtd = cachedSunDtd;
                if (null == dtd)
                {
                    try
                    {
                        URL dtdUrl = URI.create(SUN_DTD_HTTPS).toURL();
                        URLConnection conn = dtdUrl.openConnection();
                        conn.setConnectTimeout(TIMEOUT_MS);
                        conn.setReadTimeout(TIMEOUT_MS);
                        try (InputStream dtdStream = conn.getInputStream())
                        {
                            dtd = dtdStream.readAllBytes();
                            cachedSunDtd = dtd;
                        }
                    }
                    catch (IOException ex)
                    {
                        throw new SAXException("Failed to fetch Sun properties DTD: " + ex.getMessage(), ex);
                    }
                }
                return new InputSource(new ByteArrayInputStream(dtd));
            }
            throw new SAXException("Resolution of external entity blocked: " + systemID);
        }
    }

    /** The cached DocumentBuilderFactory configured for parsing property XML documents. */
    private static final DocumentBuilderFactory DOC_BUILDER_FACTORY = createDocumentBuilderFactory();

    /** The ErrorHandler that logs warnings and throws on errors and fatal errors. */
    private static final ErrorHandler ERROR_HANDLER = new ErrorHandler()
    {
        @Override
        public void warning(SAXParseException e) throws SAXException
        {
            I18N_LOGGER.log(System.Logger.Level.WARNING, "xml.parse.warning", e.getMessage(), e);
        }
        @Override
        public void error(SAXParseException e) throws SAXException { throw e; }
        @Override
        public void fatalError(SAXParseException e) throws SAXException { throw e; }
    };

    private static DocumentBuilderFactory createDocumentBuilderFactory()
    {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setValidating(true);
        return dbf;
    }

    /**
     * Creates a new DocumentBuilder configured for parsing property XML documents.
     *
     * @return a configured DocumentBuilder.
     * @throws IOException if the DocumentBuilder cannot be created.
     */
    private static DocumentBuilder createDocumentBuilder() throws IOException
    {
        try
        {
            DocumentBuilder db = DOC_BUILDER_FACTORY.newDocumentBuilder();
            db.setEntityResolver(PropertiesDtdResolver.RESOLVER);
            db.setErrorHandler(ERROR_HANDLER);
            return db;
        }
        catch (ParserConfigurationException e)
        {
            throw new IOException("Failed to create XML parser", e);
        }
    }

    /**
     * Constructs an XMLResourceBundle given an InputStream that provides the XML document.
     *
     * @param stream An InputStream that provides the XML document containing resource keys and values.
     * @throws IOException if the stream cannot be read or parsed.
     */
    public XMLResourceBundle(InputStream stream) throws IOException
    {
        props = new ConcurrentHashMap<>();
        try
        {
            DocumentBuilder db = createDocumentBuilder();
            Document doc = db.parse(stream);
            Element root = doc.getDocumentElement();
            processEntries(root, null);
        }
        catch (SAXException e)
        {
            throw new IOException(e.getMessage(), e);
        }
        if (props.isEmpty())
        {
            throw new IOException("Failed to parse any properties from the specified stream");
        }
    }

    /**
     * Processes entry child elements of the given parent element, storing values either in
     * the props map (for root-level entries) or as attributes on the given AttributeCollection.
     *
     * @param parent       the parent element whose entry children to process.
     * @param parentObject the AttributeCollection to set attributes on, or null for root-level entries.
     * @throws IOException if an entry has an invalid structure.
     */
    private void processEntries(Element parent, AttributeCollection parentObject) throws IOException
    {
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++)
        {
            Node child = children.item(i);
            if (child.getNodeType() != Node.ELEMENT_NODE) continue;
            Element elem = (Element) child;
            if (!"entry".equals(elem.getTagName())) continue;
            String key = elem.getAttribute("key");
            Object value = parseEntryValue(elem);
            if (parentObject != null)
            {
                parentObject.setAttribute(key, value);
            }
            else
            {
                props.put(key, value);
            }
        }
    }

    /**
     * Parses the value of an entry element. The value is determined by the entry's child elements:
     * an object element produces an AttributeCollection, an array element produces an Object array,
     * text content produces a String, and an empty entry produces null.
     *
     * @param entry the entry element to parse.
     * @return the parsed value.
     * @throws IOException if the entry has an invalid structure.
     */
    private Object parseEntryValue(Element entry) throws IOException
    {
        NodeList children = entry.getChildNodes();
        for (int i = 0; i < children.getLength(); i++)
        {
            Node child = children.item(i);
            if (child.getNodeType() != Node.ELEMENT_NODE) continue;
            Element childElem = (Element) child;
            String tagName = childElem.getTagName();
            if ("object".equals(tagName))
            {
                return parseObject(childElem);
            }
            if ("array".equals(tagName))
            {
                return parseArray(childElem);
            }
        }
        String text = getDirectTextContent(entry);
        if (!text.isEmpty())
        {
            return text;
        }
        return null;
    }

    /**
     * Returns the direct text content of an element, excluding text from child elements.
     *
     * @param elem the element.
     * @return the concatenated text content of direct text and CDATA child nodes.
     */
    private static String getDirectTextContent(Element elem)
    {
        StringBuilder sb = new StringBuilder();
        NodeList children = elem.getChildNodes();
        for (int i = 0; i < children.getLength(); i++)
        {
            Node child = children.item(i);
            if (child.getNodeType() == Node.TEXT_NODE || child.getNodeType() == Node.CDATA_SECTION_NODE)
            {
                sb.append(child.getNodeValue());
            }
        }
        return sb.toString();
    }

    /**
     * Parses an object element into an AttributeCollection.
     *
     * @param objectElem the object element.
     * @return the constructed AttributeCollection.
     * @throws IOException if the type attribute is missing or the object cannot be constructed.
     */
    private AttributeCollection parseObject(Element objectElem) throws IOException
    {
        String type = objectElem.getAttribute("type");
        if (type == null || type.isEmpty())
        {
            throw new IOException("XML format error - type attribute for object is missing.");
        }
        AttributeCollection coll = constructAttributeCollectionObject(type);
        processEntries(objectElem, coll);
        return coll;
    }

    /**
     * Parses an array element into an Object array.
     *
     * @param arrayElem the array element.
     * @return the parsed array.
     * @throws IOException if the array has an invalid structure.
     */
    private Object[] parseArray(Element arrayElem) throws IOException
    {
        ArrayList<Object> list = new ArrayList<>();
        NodeList children = arrayElem.getChildNodes();
        for (int i = 0; i < children.getLength(); i++)
        {
            Node child = children.item(i);
            if (child.getNodeType() != Node.ELEMENT_NODE) continue;
            Element elem = (Element) child;
            String tagName = elem.getTagName();
            if ("item".equals(tagName))
            {
                list.add(getDirectTextContent(elem));
            }
            else if ("array".equals(tagName))
            {
                list.add(parseArray(elem));
            }
            else if ("object".equals(tagName))
            {
                list.add(parseObject(elem));
            }
        }
        return convertToArray(list);
    }
}
