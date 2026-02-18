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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A ResourceBundle that is loaded from a JSON document.
 */
public class JsonResourceBundle  extends AttributeCollectionResourceBundle
{
    /**
     * The set of possible context states that may be encountered while parsing the JSON document.
     */
    protected enum ContextType
    {
        /** The root context at the start and end of parsing. */
        ROOT,
        /** A JSON field name has been encountered. */
        FIELD,
        /** An array is being parsed. */
        ARRAY,
        /** An object is being parsed. */
        OBJECT
    }

    /**
     * The full context for the parsing operation. Parsing starts and ends in the ROOT state and progresses
     * through the other states as the document is parsed. The ParseContext is maintained in a Stack and
     * the object contained in the ParseContext is either the name of the field being parsed, the {@code ArrayList<Object>}
     * of array elements being parsed, or the AttributeCollection object being parsed.
     *
     * @param type   the context type.
     * @param object the context-specific data object.
     */
    protected record ParseContext(ContextType type, Object object) {}

    /** The JsonFactory used to create JsonParser instances. */
    private static final JsonFactory JSON_FACTORY = new JsonFactory();

    /**
     * Constructs a JsonResourceBundle given an InputStream that provides the JSON document.
     *
     * @param stream An InputStream that provides the JSON document containing resource keys and values.
     * @throws IOException if the stream cannot be read.
     */
    public JsonResourceBundle(InputStream stream) throws IOException
    {
        props = new ConcurrentHashMap<>();
        try (JsonParser parser = JSON_FACTORY.createParser(stream))
        {
            // First token should be START_OBJECT, consume it
            if (parser.nextToken() != JsonToken.START_OBJECT) return;
            ArrayDeque<ParseContext> ctx = new ArrayDeque<>();
            ctx.push(new ParseContext(ContextType.ROOT, null));
            while (parser.nextToken() != null)
            {
                JsonToken token = parser.currentToken();
                ContextType currentCtx = ctx.peek().type;
                if (token == JsonToken.FIELD_NAME)
                {
                    if ((ContextType.FIELD == currentCtx || ContextType.ARRAY == currentCtx))
                    {
                        throw new IOException("JSON format error - found a field name following a '[' or field name");
                    }
                    ctx.push(new ParseContext(ContextType.FIELD, parser.getText()));
                    continue;
                }
                if (token == JsonToken.VALUE_STRING)
                {
                    String value = parser.getText();
                    if (ContextType.ARRAY == currentCtx)
                    {
                        ArrayList<Object> list = (ArrayList<Object>) ctx.peek().object;
                        list.add(value);
                    }
                    else
                    {
                        String name = (String) ctx.pop().object;
                        currentCtx = ctx.peek().type;
                        if (ContextType.ROOT == currentCtx)
                        {
                            props.put(name, value);
                        }
                        else
                        {
                            AttributeCollection coll = (AttributeCollection) ctx.peek().object;
                            coll.setAttribute(name, value);
                        }
                    }
                    continue;
                }
                if (token == JsonToken.VALUE_NUMBER_INT)
                {
                    int value = parser.getIntValue();
                    if (ContextType.ARRAY == currentCtx)
                    {
                        ArrayList<Object> list = (ArrayList<Object>) ctx.peek().object;
                        list.add(value);
                    }
                    else
                    {
                        String name = (String) ctx.pop().object;
                        currentCtx = ctx.peek().type;
                        if (ContextType.ROOT == currentCtx)
                        {
                            props.put(name, value);
                        }
                        else
                        {
                            AttributeCollection coll = (AttributeCollection) ctx.peek().object;
                            coll.setAttribute(name, value);
                        }
                    }
                    continue;
                }
                if (token == JsonToken.VALUE_NUMBER_FLOAT)
                {
                    double value = parser.getDoubleValue();
                    if (ContextType.ARRAY == currentCtx)
                    {
                        ArrayList<Object> list = (ArrayList<Object>) ctx.peek().object;
                        list.add(value);
                    }
                    else
                    {
                        String name = (String) ctx.pop().object;
                        currentCtx = ctx.peek().type;
                        if (ContextType.ROOT == currentCtx)
                        {
                            props.put(name, value);
                        }
                        else
                        {
                            AttributeCollection coll = (AttributeCollection) ctx.peek().object;
                            coll.setAttribute(name, value);
                        }
                    }
                    continue;
                }
                if (token == JsonToken.VALUE_FALSE ||
                    token == JsonToken.VALUE_TRUE)
                {
                    boolean value = parser.getBooleanValue();
                    if (ContextType.ARRAY == currentCtx)
                    {
                        ArrayList<Object> list = (ArrayList<Object>) ctx.peek().object;
                        list.add(value);
                    }
                    else
                    {
                        String name = (String) ctx.pop().object;
                        currentCtx = ctx.peek().type;
                        if (ContextType.ROOT == currentCtx)
                        {
                            props.put(name, value);
                        }
                        else
                        {
                            AttributeCollection coll = (AttributeCollection) ctx.peek().object;
                            coll.setAttribute(name, value);
                        }
                    }
                    continue;
                }
                if (token == JsonToken.VALUE_NULL)
                {
                    if (ContextType.ARRAY == currentCtx)
                    {
                        ArrayList<Object> list = (ArrayList<Object>) ctx.peek().object;
                        list.add(null);
                    }
                    else
                    {
                        String name = (String) ctx.pop().object;
                        currentCtx = ctx.peek().type;
                        if (ContextType.ROOT == currentCtx)
                        {
                            props.put(name, null);
                        }
                        else
                        {
                            AttributeCollection coll = (AttributeCollection) ctx.peek().object;
                            coll.setAttribute(name, null);
                        }
                    }
                    continue;
                }
                if (token == JsonToken.START_ARRAY)
                {
                    ArrayList<Object> arrayList = new ArrayList<>();
                    ctx.push(new ParseContext(ContextType.ARRAY, arrayList));
                    continue;
                }
                if (token == JsonToken.START_OBJECT)
                {
                    token = parser.nextToken();
                    // Expect the first token to be a field with the name "type"
                    if (token != JsonToken.FIELD_NAME)
                    {
                        throw new IOException("JSON format error - a field name was not the first item in the json object");
                    }
                    String fieldName = parser.getText();
                    if (!"type".equals(fieldName))
                    {
                        throw new IOException("JSON format error - the type field was not the first field in the json object");
                    }
                    token = parser.nextToken();
                    if (token != JsonToken.VALUE_STRING)
                    {
                        throw new IOException("JSON format error - A value was not found for the type field");
                    }
                    String className = parser.getText();
                    AttributeCollection coll = constructAttributeCollectionObject(className);
                    ctx.push(new ParseContext(ContextType.OBJECT, coll));
                    continue;
                }
                if (token == JsonToken.END_ARRAY)
                {
                    if (currentCtx != ContextType.ARRAY)
                    {
                        throw new IOException("JSON format error - Found ']' without matching '['");
                    }
                    Object[] list = convertToArray((ArrayList<Object>) ctx.pop().object);
                    currentCtx = ctx.peek().type;
                    if (null == currentCtx)
                    {
                        throw new IOException("JSON format error - Unexpected content after ']' ");
                    }
                    else switch (currentCtx)
                    {
                        case ARRAY ->
                        {
                            ArrayList<Object> owner = (ArrayList<Object>) ctx.peek().object;
                            owner.add(list);
                        }
                        case FIELD ->
                        {
                            String name = (String) ctx.pop().object;
                            currentCtx = ctx.peek().type;
                            if (ContextType.OBJECT == currentCtx)
                            {
                                AttributeCollection coll = (AttributeCollection) ctx.peek().object;
                                coll.setAttribute(name, list);
                            }
                            else
                            {
                                props.put(name, list);
                            }
                        }
                        default -> throw new IOException("JSON format error - unexpected content after ']'");
                    }
                    continue;
                }
                if (token == JsonToken.END_OBJECT)
                {
                    if (currentCtx == ContextType.ROOT)
                    {
                        continue;
                    }
                    if (currentCtx != ContextType.OBJECT)
                    {
                        throw new IOException("JSON format error - found '}' without matching '{'");
                    }
                    AttributeCollection coll = (AttributeCollection) ctx.pop().object;
                    currentCtx = ctx.peek().type;
                    if (null == currentCtx)
                    {
                        throw new IOException("JSON format error - unexpected content after '}'");
                    }
                    else switch (currentCtx)
                    {
                        case ARRAY ->
                        {
                            ArrayList<Object> owner = (ArrayList<Object>) ctx.peek().object;
                            owner.add(coll);
                        }
                        case FIELD ->
                        {
                            String name = (String) ctx.pop().object;
                            currentCtx = ctx.peek().type;
                            if (ContextType.OBJECT == currentCtx)
                            {
                                AttributeCollection owner = (AttributeCollection) ctx.peek().object;
                                owner.setAttribute(name, coll);
                            }
                            else
                            {
                                props.put(name, coll);
                            }
                        }
                        default -> throw new IOException("Unexpected content after '}'");
                    }
                }
            }
        }
        if (props.isEmpty())
        {
            throw new IOException("Failed to parse any properties from the specified stream");
        }
    }

}
