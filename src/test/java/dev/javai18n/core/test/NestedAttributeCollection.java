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

/**
 * A slightly more complex AttributeCollection that extends SimpleAttributeCollection and adds an embedded
 * SimpleAttributeCollection.
 */
public class NestedAttributeCollection extends SimpleAttributeCollection
{
    SimpleAttributeCollection coll;

    public NestedAttributeCollection() {}

    public NestedAttributeCollection(String name, String value)
    {
        this.name = name;
        this.value = value;
        coll = new SimpleAttributeCollection(name, value);
    }

    @Override
    public void setAttribute(String name, Object value)
    {
        if ("name".equals(name) || "value".equals(name))
        {
            super.setAttribute(name, value);
            return;
        }
        if ("coll".equals(name))
        {
            this.coll = (SimpleAttributeCollection) value;
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
        if (!(other instanceof NestedAttributeCollection)) return false;
        NestedAttributeCollection otherColl = (NestedAttributeCollection) other;
        return Objects.equals(this.name, otherColl.name)   &&
               Objects.equals(this.value, otherColl.value) &&
               Objects.equals(this.coll, otherColl.coll);
    }

    @Override
    public int hashCode()
    {
        int hash = super.hashCode();
        hash = 43 * hash + Objects.hashCode(this.coll);
        return hash;
    }
}
