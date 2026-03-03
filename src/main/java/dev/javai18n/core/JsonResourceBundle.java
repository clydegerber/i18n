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
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A {@link ResourceBundle} loaded from a JSON document.
 *
 * <h2>Document structure</h2>
 * <p>The root of the JSON document must be a JSON object. Each field of the root object becomes
 * a bundle entry whose key is the field name. Values may be:</p>
 * <ul>
 *   <li><b>String</b> — retrieved via {@link ResourceBundle#getString(String)}</li>
 *   <li><b>Number</b> — stored as {@code int}, {@code double}, or {@code float}; retrieved via
 *       {@link ResourceBundle#getObject(String)}</li>
 *   <li><b>Boolean</b> — stored as {@code boolean}; retrieved via
 *       {@link ResourceBundle#getObject(String)}</li>
 *   <li><b>Array</b> — stored as {@code String[]} when all elements are strings, otherwise as
 *       {@code Object[]}; retrieved via {@link ResourceBundle#getStringArray(String)} or
 *       {@link ResourceBundle#getObject(String)}</li>
 *   <li><b>Object</b> — deserialized into an {@link AttributeCollection}; retrieved via
 *       {@link ResourceBundle#getObject(String)}</li>
 * </ul>
 *
 * <h2>String-only bundle</h2>
 * <p>A bundle containing string entries and a string array:</p>
 * <pre>{@code
 * {
 *   "greeting": "Hello",
 *   "farewell": "Goodbye",
 *   "weekdays": ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday"]
 * }
 * }</pre>
 *
 * <h2>Bundle containing typed objects</h2>
 * <p>A JSON object value is deserialized into an {@link AttributeCollection}. The object must
 * contain a {@code "type"} field whose value is the fully qualified class name of the target
 * type. The remaining fields are passed to the instance via
 * {@link AttributeCollection#setAttribute(String, Object)}, one call per field.</p>
 *
 * <p>Given the following {@code AttributeCollection} implementation:</p>
 * <pre>{@code
 * package com.example;
 *
 * public class ButtonProps implements AttributeCollection {
 *     private String label;
 *     private String tooltip;
 *
 *     public ButtonProps() {}  // public no-arg constructor required
 *
 *     public String getLabel()   { return label; }
 *     public String getTooltip() { return tooltip; }
 *
 *     @Override
 *     public void setAttribute(String name, Object value) {
 *         switch (name) {
 *             case "label"   -> label   = (String) value;
 *             case "tooltip" -> tooltip = (String) value;
 *         }
 *     }
 * }
 * }</pre>
 *
 * <p>…a bundle that includes a string entry and an object entry for it looks like:</p>
 * <pre>{@code
 * {
 *   "title": "File Explorer",
 *   "okButton": {
 *     "type": "com.example.ButtonProps",
 *     "label": "OK",
 *     "tooltip": "Confirm the selection"
 *   }
 * }
 * }</pre>
 *
 * <p>The object is then retrieved and cast:</p>
 * <pre>{@code
 * ButtonProps ok = (ButtonProps) bundle.getObject("okButton");
 * }</pre>
 *
 * <h3>Package registration</h3>
 * <p>As a security measure, only classes whose package has been explicitly registered may be
 * instantiated. Attempting to deserialize an object whose class belongs to an unregistered
 * package throws {@link java.io.IOException}. Packages are registered once — typically at
 * application or module startup — before any bundle containing objects in that package is
 * loaded:</p>
 * <pre>{@code
 * AttributeCollectionResourceBundle.registerAttributeCollectionPackage("com.example");
 * }</pre>
 *
 * <h3>No-arg constructor requirement</h3>
 * <p>The target class must have a {@code public} no-argument constructor. The bundle loader
 * instantiates the object via that constructor and then populates it by calling
 * {@link AttributeCollection#setAttribute(String, Object)} for each non-{@code "type"} field
 * in the JSON object.</p>
 *
 * @see XMLResourceBundle
 * @see AttributeCollection
 * @see AttributeCollectionResourceBundle#registerAttributeCollectionPackage(String)
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
            if (!root.isObject())
            {
                throw new IOException("JSON root is not an object");
            }
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
                throw new IOException("JSON format error - object is missing a 'type' field or type is not a string");
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
