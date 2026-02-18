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

import com.fasterxml.jackson.core.JsonToken;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ConcurrentHashMap;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.xml.deser.FromXmlParser;
import java.util.ArrayList;
import java.util.ArrayDeque;
import javax.xml.stream.XMLResolver;

/**
 * A ResourceBundle that is loaded from an XML document. The document must conform to the DTD
 * provided in this module's resources (dev/javai18n/core/properties.dtd) or from
 * https://java.sun.com/dtd/properties.dtd. The DTD provided with this module is a super set of
 * the latter.
 */
public class XMLResourceBundle  extends AttributeCollectionResourceBundle
{
    /**
     * The set of possible context states that may be encountered while parsing the XML document.
     */
    protected enum ContextType
    {
        /** The root properties element. */
        PROPERTIES,
        /** An entry element containing a key-value pair. */
        ENTRY,
        /** An object element being parsed. */
        OBJECT,
        /** An array element being parsed. */
        ARRAY,
        /** An item within an array. */
        ITEM,
        /** A type attribute specifying the class name. */
        TYPE,
        /** A key attribute specifying the entry name. */
        KEY,
        /** A string value within an element. */
        STRING_VALUE,
        /** The DTD version attribute (ignored). */
        VERSION,
        /** The DTD encoding attribute (ignored). */
        ENCODING
    }

    /**
     * The full context for the parsing operation. Parsing starts and ends in the PROPERTIES state and progresses
     * through the other states as the document is parsed. The ParseContext is maintained in a Stack, each element
     * of which contains the context type, the name of the field being parsed, and the object being parsed - the
     * String value, {@code ArrayList<Object>} of array elements or the AttributeCollection object.
     *
     * @param type   the context type.
     * @param key    the key or field name for the entry that is being parsed.
     * @param object the context-specific data object.
     */
    protected record ParseContext(ContextType type, String key, Object object) {}

    /**
     * An XMLResolver that resolves the properties DTD from this module's resources (dev/javai18n/core/properties.dtd)
     * or from https://java.sun.com/dtd/properties.dtd.
     */
    protected static final class PropertiesDtdResolver implements XMLResolver
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
        public Object resolveEntity(String publicID, String systemID, String baseURI, String namespace)
                throws XMLStreamException
        {
            if (null == systemID)
            {
                throw new XMLStreamException("Resolution of external entity blocked: null systemID");
            }
            if (systemID.equals("properties.dtd") || systemID.equals("dev/javai18n/core/properties.dtd"))
            {
                try (InputStream dtdStream = PropertiesDtdResolver.class.getModule()
                        .getResourceAsStream("dev/javai18n/core/properties.dtd"))
                {
                    if (null == dtdStream)
                    {
                        throw new XMLStreamException("Local properties DTD not found");
                    }
                    return new ByteArrayInputStream(dtdStream.readAllBytes());
                }
                catch (IOException ex)
                {
                    throw new XMLStreamException(ex.getMessage(), ex);
                }
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
                        throw new XMLStreamException("Failed to fetch Sun properties DTD: " + ex.getMessage(), ex);
                    }
                }
                return new ByteArrayInputStream(dtd);
            }
            throw new XMLStreamException("Resolution of external entity blocked: " + systemID);
        }
    }

    /** The XMLInputFactory used to create XMLStreamReader instances. */
    private static final XMLInputFactory XML_INPUT_FACTORY = createXmlInputFactory();

    /** The XmlFactory used to create FromXmlParser instances. */
    private static final XmlFactory XML_FACTORY = new XmlFactory();

    private static XMLInputFactory createXmlInputFactory()
    {
        XMLInputFactory f = XMLInputFactory.newInstance();
        f.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        f.setXMLResolver(PropertiesDtdResolver.RESOLVER);
        return f;
    }

    /**
     * Constructs an XMLResourceBundle given an InputStream that provides the XML document.
     *
     * @param stream An InputStream that provides the XML document containing resource keys and values.
     * @throws IOException if the stream cannot be read.
     * @throws XMLStreamException if the stream does not contain a valid XML document.
     */
    public XMLResourceBundle(InputStream stream) throws IOException, XMLStreamException
    {
        props = new ConcurrentHashMap<>();
        XMLStreamReader xmlReader = XML_INPUT_FACTORY.createXMLStreamReader(stream, "UTF-8");
        try (FromXmlParser parser = XML_FACTORY.createParser(xmlReader))
        {
            ArrayDeque<ParseContext> ctx = new ArrayDeque<>();
            //Jackson XML parser skips the root element, so start in the PROPERTIES context
            ctx.push(new ParseContext(ContextType.PROPERTIES, null, null));
            while (parser.nextToken() != null)
            {
                JsonToken token = parser.currentToken();
                ContextType currentCtx = ctx.peek().type;
                if (token == JsonToken.FIELD_NAME)
                {
                    String xmlName = parser.getText();
                    // Ignore version in dtd
                    if ("version".equals(xmlName))
                    {
                        ctx.push(new ParseContext(ContextType.VERSION, null, null));
                        continue;
                    }
                    // Ignore encoding in dtd
                    if ("encoding".equals(xmlName))
                    {
                        ctx.push(new ParseContext(ContextType.ENCODING, null, null));
                        continue;
                    }
                    if ("entry".equals(xmlName))
                    {
                        if (ContextType.PROPERTIES != currentCtx &&
                            ContextType.OBJECT != currentCtx)
                        {
                            throw new IOException("XML format error - entry element found in unexpected location");
                        }
                        ctx.push(new ParseContext(ContextType.ENTRY, null, null));
                        continue;
                    }
                    if ("object".equals(xmlName))
                    {
                        if (ContextType.ENTRY != currentCtx &&
                            ContextType.ARRAY != currentCtx)
                        {
                            throw new IOException("XML format error - object element found in unexpected location");
                        }
                        ctx.push(new ParseContext(ContextType.OBJECT, ctx.peek().key, null));
                        continue;
                    }
                    if ("array".equals(xmlName))
                    {
                        if (ContextType.ENTRY != currentCtx &&
                            ContextType.ARRAY != currentCtx)
                        {
                            throw new IOException("XML format error - array element found in unexpected location");
                        }
                        ArrayList<Object> arrayList = new ArrayList<>();
                        ctx.push(new ParseContext(ContextType.ARRAY, null, arrayList));
                        continue;
                    }
                    if ("item".equals(xmlName))
                    {
                        if (ContextType.ARRAY != currentCtx)
                        {
                            throw new IOException("XML format error - item element found in unexpected location");
                        }
                        ctx.push(new ParseContext(ContextType.ITEM, null, null));
                        continue;
                    }
                    if ("type".equals(xmlName))
                    {
                        if (ContextType.OBJECT != currentCtx)
                        {
                            throw new IOException("XML format error - type attribute found in unexpected location");
                        }
                        if (ctx.peek().object != null)
                        {
                            throw new IOException("XML format error - duplicate type attribute found for object");
                        }
                        ctx.push(new ParseContext(ContextType.TYPE, null, null));
                        continue;
                    }
                    if ("key".equals(xmlName))
                    {
                        if (ContextType.ENTRY != currentCtx)
                        {
                            throw new IOException("XML format error - key attribute found in unexpected location");
                        }
                        if (ctx.peek().object != null)
                        {
                            throw new IOException("XML format error - duplicate key attribute found for object");
                        }
                        ctx.push(new ParseContext(ContextType.KEY, null, null));
                        continue;
                    }
                    if ("".equals(xmlName))
                    {
                        ctx.push(new ParseContext(ContextType.STRING_VALUE, null, null));
                        continue;
                    }
                    throw new IOException("XML format error - unknown element or attribute found:" + xmlName);
                }
                if (token == JsonToken.VALUE_STRING)
                {
                    if (ContextType.VERSION == currentCtx ||
                        ContextType.ENCODING == currentCtx)
                    {
                        ctx.pop();
                        continue;
                    }
                    boolean elementText = false;
                    if (ContextType.STRING_VALUE == currentCtx)
                    {
                        elementText = true;
                        ctx.pop();
                        currentCtx = ctx.peek().type;
                    }
                    String value = parser.getText();
                    String key = null;
                    switch (currentCtx)
                    {
                        case KEY ->
                        {
                            ctx.pop();
                            currentCtx = ctx.pop().type;
                            if (ContextType.ENTRY != currentCtx)
                            {
                                throw new IOException("XML format error - key attribute found in unexpected location");
                            }
                            ctx.push(new ParseContext(ContextType.ENTRY, value, null));
                        }
                        case TYPE ->
                        {
                            ctx.pop();
                            key = ctx.peek().key;
                            currentCtx = ctx.pop().type;
                            if (ContextType.OBJECT != currentCtx)
                            {
                                throw new IOException("XML format error - type attribute found in unexpected location");
                            }
                            AttributeCollection coll = constructAttributeCollectionObject(value);
                            ctx.push(new ParseContext(ContextType.OBJECT, key, coll));
                        }
                        case ITEM ->
                        {
                            ctx.pop();
                            currentCtx = ctx.peek().type;
                            if (ContextType.ARRAY != currentCtx)
                            {
                                throw new IOException("XML format error - item attribute found in unexpected location");
                            }
                            ArrayList<Object> list = (ArrayList<Object>) ctx.peek().object;
                            list.add(value);
                        }
                        case ENTRY ->
                        {
                            key = (String) ctx.pop().key;
                            currentCtx = ctx.peek().type;
                            switch (currentCtx)
                            {
                                case PROPERTIES ->
                                {
                                    props.put(key, value);
                                }
                                case OBJECT ->
                                {
                                    AttributeCollection coll = (AttributeCollection) ctx.peek().object;
                                    if (null == coll)
                                    {
                                        throw new IOException("XML format error - type attribute for object is missing.");
                                    }
                                    coll.setAttribute(key, value);
                                }
                            }
                        }
                        default -> throw new IOException("XML format error - string value found in unexpected context");
                    }
                    if (elementText)
                    {
                        ctx.push(new ParseContext(ContextType.ENTRY, key, value));
                        continue;
                    }
                    continue;
                }
                if (token == JsonToken.VALUE_NUMBER_INT   ||
                    token == JsonToken.VALUE_NUMBER_FLOAT ||
                    token == JsonToken.VALUE_FALSE        ||
                    token == JsonToken.VALUE_TRUE         ||
                    token == JsonToken.VALUE_NULL         ||
                    token == JsonToken.START_ARRAY        ||
                    token == JsonToken.END_ARRAY)
                {
                    throw new IOException("XML format error - unexpected token found: " + token);
                }
                if (token == JsonToken.START_OBJECT)
                {
                    continue;
                }
                if (token == JsonToken.END_OBJECT)
                {
                    if (ContextType.STRING_VALUE == currentCtx)
                    {
                        ctx.pop();
                        continue;
                    }
                    if (ContextType.ENTRY == currentCtx)
                    {
                        Object obj = ctx.peek().object;
                        String key = ctx.pop().key;
                        currentCtx = ctx.peek().type;
                        if (null == obj)
                        {
                            switch (currentCtx)
                            {
                                case PROPERTIES ->
                                {
                                    props.put(key, null);
                                }
                                case OBJECT ->
                                {
                                    AttributeCollection coll = (AttributeCollection) ctx.peek().object;
                                    if (null == coll)
                                    {
                                        throw new IOException("XML format error - type attribute for object is missing.");
                                    }
                                    coll.setAttribute(key, null);
                                }
                            }
                        }
                        continue;
                    }
                    if (ContextType.OBJECT == currentCtx)
                    {
                        AttributeCollection coll = (AttributeCollection) ctx.pop().object;
                        currentCtx = ctx.peek().type;
                        switch (currentCtx)
                        {
                            case ARRAY ->
                            {
                                ArrayList<Object> owner = (ArrayList<Object>) ctx.peek().object;
                                owner.add(coll);
                            }
                            case ENTRY ->
                            {
                                String key = (String) ctx.pop().key;
                                currentCtx = ctx.peek().type;
                                if (null == currentCtx)
                                {
                                    throw new IOException("XML format error - element close in unexpected context");
                                }
                                switch (currentCtx)
                                {
                                    case OBJECT ->
                                    {
                                        AttributeCollection owner = (AttributeCollection) ctx.peek().object;
                                        owner.setAttribute(key, coll);
                                    }
                                    case PROPERTIES -> props.put(key, coll);
                                    default -> throw new IOException("XML format error - element close in unexpected context");
                                }
                            }
                            default -> throw new IOException("Unexpected content after element close");
                        }
                        continue;
                    }
                    if (ContextType.ARRAY == currentCtx)
                    {
                        Object[] array = convertToArray((ArrayList<Object>) ctx.pop().object);
                        currentCtx = ctx.peek().type;
                        if (null == currentCtx)
                        {
                            throw new IOException("XML format error - unexpected content after element close");
                        }
                        else switch (currentCtx)
                        {
                            case ARRAY ->
                            {
                                ArrayList<Object> owner = (ArrayList<Object>) ctx.peek().object;
                                owner.add(array);
                            }
                            case ENTRY ->
                            {
                                String key = (String) ctx.pop().key;
                                currentCtx = ctx.peek().type;
                                if (null == currentCtx)
                                {
                                    throw new IOException("XML format error - element close in unexpected context");
                                }
                                switch (currentCtx)
                                {
                                    case OBJECT ->
                                    {
                                        AttributeCollection owner = (AttributeCollection) ctx.peek().object;
                                        owner.setAttribute(key, array);
                                    }
                                    case PROPERTIES -> props.put(key, array);
                                    default -> throw new IOException("XML format error - element close in unexpected context");
                                }
                            }
                            default -> throw new IOException("Unexpected content after element close");
                        }
                    }
                }
            }
        }
        finally
        {
            xmlReader.close();
        }
        if (props.isEmpty())
        {
            throw new IOException("Failed to parse any properties from the specified stream");
        }
    }
}
