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
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
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
     * An EntityResolver that resolves the properties DTD. Recognized system IDs are:
     * <ul>
     *   <li>{@code http://java.sun.com/dtd/properties.dtd} and
     *       {@code https://java.sun.com/dtd/properties.dtd} — fetched from the
     *       Sun/Oracle server (with caching and redirect following). If the fetch
     *       fails, falls back to the local module resource.</li>
     *   <li>{@code properties.dtd} and {@code dev/javai18n/core/properties.dtd} — resolved
     *       to the local module resource, which is a superset of the Sun DTD.</li>
     * </ul>
     * All other system IDs are blocked to prevent XXE attacks.
     */
    protected static final class PropertiesDtdResolver implements EntityResolver
    {
        /** Singleton instance of the resolver. */
        protected static final PropertiesDtdResolver RESOLVER = new PropertiesDtdResolver();

        /** Constructs a PropertiesDtdResolver. */
        PropertiesDtdResolver() {}
        private static final String SUN_DTD_HTTPS = "https://java.sun.com/dtd/properties.dtd";
        private static final String SUN_DTD_HTTP = "http://java.sun.com/dtd/properties.dtd";
        private static final String LOCAL_DTD_PATH = "dev/javai18n/core/properties.dtd";
        private static final String LOCAL_DTD_NAME = "properties.dtd";
        private static final int TIMEOUT_MS = 5000;
        private static final int MAX_REDIRECTS = 5;

        /** The Sun properties DTD, cached after first successful fetch. */
        private static volatile byte[] cachedSunDtd;

        @Override
        public InputSource resolveEntity(String publicID, String systemID)
                throws SAXException, IOException
        {
            if (null == systemID)
            {
                throw new SAXException("Resolution of external entity blocked: null systemID");
            }
            if (systemID.equals(SUN_DTD_HTTP) || systemID.equals(SUN_DTD_HTTPS))
            {
                return resolveSunDtd();
            }
            if (isLocalPropertiesDtd(systemID))
            {
                return resolveLocalDtd();
            }
            throw new SAXException("Resolution of external entity blocked: " + systemID);
        }

        /**
         * Fetches the Sun properties DTD from the network, following redirects.
         * Falls back to the local DTD on any failure.
         *
         * @return an InputSource for the Sun DTD, or the local DTD as fallback.
         * @throws SAXException if neither the Sun DTD nor the local DTD can be loaded.
         */
        private static InputSource resolveSunDtd() throws SAXException, IOException
        {
            byte[] dtd = cachedSunDtd;
            if (null != dtd)
            {
                return new InputSource(new ByteArrayInputStream(dtd));
            }
            try
            {
                dtd = fetchWithRedirects(SUN_DTD_HTTPS);
                cachedSunDtd = dtd;
                return new InputSource(new ByteArrayInputStream(dtd));
            }
            catch (IOException ex)
            {
                I18N_LOGGER.log(System.Logger.Level.DEBUG, "resource.bundle.load.error",
                        ex.getClass().getName(), SUN_DTD_HTTPS, "", ex);
                return resolveLocalDtd();
            }
        }

        /**
         * Fetches content from a URL, manually following redirects across
         * hosts and protocols (which {@code HttpURLConnection} does not do
         * by default).
         */
        private static byte[] fetchWithRedirects(String urlString) throws IOException
        {
            for (int i = 0; i < MAX_REDIRECTS; i++)
            {
                URL url = URI.create(urlString).toURL();
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(TIMEOUT_MS);
                conn.setReadTimeout(TIMEOUT_MS);
                conn.setInstanceFollowRedirects(false);
                int status = conn.getResponseCode();
                if (status == HttpURLConnection.HTTP_OK)
                {
                    try (InputStream stream = conn.getInputStream())
                    {
                        return stream.readAllBytes();
                    }
                }
                if (status == HttpURLConnection.HTTP_MOVED_PERM ||
                    status == HttpURLConnection.HTTP_MOVED_TEMP ||
                    status == HttpURLConnection.HTTP_SEE_OTHER ||
                    status == 307 || status == 308)
                {
                    String location = conn.getHeaderField("Location");
                    if (null == location)
                    {
                        throw new IOException("Redirect with no Location header from " + urlString);
                    }
                    urlString = location;
                    continue;
                }
                throw new IOException("HTTP " + status + " from " + urlString);
            }
            throw new IOException("Too many redirects fetching Sun properties DTD");
        }

        /**
         * Resolves the local properties DTD from the module's resources.
         *
         * @return an InputSource for the local DTD.
         * @throws SAXException if the local DTD resource cannot be found.
         */
        private static InputSource resolveLocalDtd() throws SAXException, IOException
        {
            InputStream dtdStream = PropertiesDtdResolver.class.getModule()
                    .getResourceAsStream(LOCAL_DTD_PATH);
            if (null == dtdStream)
            {
                throw new SAXException("Local properties DTD not found");
            }
            return new InputSource(dtdStream);
        }

        /**
         * Returns {@code true} if the system ID identifies the local properties DTD.
         * Matches the relative forms ({@code "properties.dtd"} and
         * {@code "dev/javai18n/core/properties.dtd"}) as well as absolute {@code file://}
         * URIs that the DOM parser produces when resolving relative system IDs.
         */
        private static boolean isLocalPropertiesDtd(String systemID)
        {
            if (systemID.equals(LOCAL_DTD_NAME) || systemID.equals(LOCAL_DTD_PATH))
            {
                return true;
            }
            if (systemID.startsWith("file:"))
            {
                return systemID.endsWith("/" + LOCAL_DTD_NAME) ||
                       systemID.endsWith("/" + LOCAL_DTD_PATH);
            }
            return false;
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
