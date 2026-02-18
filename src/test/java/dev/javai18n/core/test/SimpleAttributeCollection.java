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

package dev.javai18n.core.test;

import java.util.Objects;
import dev.javai18n.core.AttributeCollection;

/**
 * A simple AttributeCollection object with name and value attributes.
 */
public class SimpleAttributeCollection implements AttributeCollection
{
    String name;

    String value;

    public SimpleAttributeCollection() {}

    public SimpleAttributeCollection(String name, String value)
    {
        this.name = name;
        this.value = value;
    }

    @Override
    public void setAttribute(String name, Object value)
    {
        if ("name".equals(name))
        {
            this.name = (String) value;
            return;
        }
        if ("value".equals(name))
        {
            this.value = (String) value;
            return;
        }
        throw new RuntimeException("Unknown attribute: " + name);
    }

    @Override
    public String toString()
    {
        return "[name: " + name + " value: " + value + "]";
    }

    @Override
    public boolean equals(Object other)
    {
        if (!(other instanceof SimpleAttributeCollection)) return false;
        SimpleAttributeCollection otherColl = (SimpleAttributeCollection) other;
        return Objects.equals(this.name, otherColl.name) && Objects.equals(this.value, otherColl.value);
    }

    @Override
    public int hashCode()
    {
        int hash = 3;
        hash = 11 * hash + Objects.hashCode(this.name);
        hash = 11 * hash + Objects.hashCode(this.value);
        return hash;
    }
}
