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

/**
 * An interface for objects that allow setting of named attributes. Used in deserializing
 * {@link JsonResourceBundle}s and {@link XMLResourceBundle}s.
 *
 * <p>As a security measure, only classes whose package has been explicitly registered may be
 * instantiated from resource files. The implementing class's package must be registered before
 * loading any bundle that references it:</p>
 * <pre>{@code
 * AttributeCollectionResourceBundle.registerAttributeCollectionPackage("com.example");
 * }</pre>
 *
 * @see AttributeCollectionResourceBundle#registerAttributeCollectionPackage(String)
 */
public interface AttributeCollection
{
    /**
     * Sets the value of the named attribute.
     *
     * @param attributeName  the name of the attribute to set.
     * @param attributeValue the value to assign to the attribute.
     */
    public void setAttribute(String attributeName, Object attributeValue);
}
