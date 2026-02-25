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

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.JsonNode;
import tools.jackson.core.JacksonException;
import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A ResourceBundle that is loaded from a JSON document.
 */
public class JsonResourceBundle  extends AttributeCollectionResourceBundle
{
    /** The ObjectMapper used to parse JSON documents. */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Constructs a JsonResourceBundle given an InputStream that provides the JSON document.
     *
     * @param stream An InputStream that provides the JSON document containing resource keys and values.
     * @throws IOException if the stream cannot be read.
     */
    public JsonResourceBundle(InputStream stream) throws IOException
    {
        props = new ConcurrentHashMap<>();
        try
        {
            JsonNode root = MAPPER.readTree(stream);
            if (!root.isObject()) return;
            for (Map.Entry<String, JsonNode> field : root.properties())
            {
                props.put(field.getKey(), convertNode(field.getValue()));
            }
        }
        catch (JacksonException e)
        {
            throw new IOException(e.getMessage(), e);
        }
        if (props.isEmpty())
        {
            throw new IOException("Failed to parse any properties from the specified stream");
        }
    }

    /**
     * Converts a JsonNode to the appropriate Java object.
     *
     * @param node the JsonNode to convert.
     * @return the converted Java object.
     * @throws IOException if the node contains an invalid structure.
     */
    private Object convertNode(JsonNode node) throws IOException
    {
        if (node.isString()) return node.stringValue();
        if (node.isInt()) return node.intValue();
        if (node.isDouble() || node.isFloat()) return node.doubleValue();
        if (node.isBoolean()) return node.booleanValue();
        if (node.isNull()) return null;
        if (node.isArray())
        {
            ArrayList<Object> list = new ArrayList<>();
            for (JsonNode element : node)
            {
                list.add(convertNode(element));
            }
            return convertToArray(list);
        }
        if (node.isObject())
        {
            JsonNode typeNode = node.get("type");
            if (typeNode == null || !typeNode.isString())
            {
                throw new IOException("JSON format error - the type field was not the first field in the json object");
            }
            String className = typeNode.stringValue();
            AttributeCollection coll = constructAttributeCollectionObject(className);
            for (Map.Entry<String, JsonNode> field : node.properties())
            {
                if ("type".equals(field.getKey())) continue;
                coll.setAttribute(field.getKey(), convertNode(field.getValue()));
            }
            return coll;
        }
        throw new IOException("JSON format error - unexpected node type: " + node.getNodeType());
    }

}
