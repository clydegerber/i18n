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

import java.util.Arrays;
import java.util.Objects;

/**
 * A slightly more complex AttributeCollection that extends SimpleAttributeCollection and adds a
 * String array field.
 */
public class AttributeCollectionWithStringArray extends SimpleAttributeCollection
{
    String[] values;

    public AttributeCollectionWithStringArray() {}

    public AttributeCollectionWithStringArray(String name, String value)
    {
        this.name = name;
        this.value = value;
    }

    public String[] getValues()
    {
        return values;
    }

    @Override
    public void setAttribute(String name, Object value)
    {
        if (name.equals("name") || name.equals("value"))
        {
            super.setAttribute(name, value);
            return;
        }
        if (name.equals("values"))
        {
            this.values = (String[]) value;
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
        if (!(other instanceof AttributeCollectionWithStringArray)) return false;
        AttributeCollectionWithStringArray otherArr = (AttributeCollectionWithStringArray) other;
        return Objects.equals(this.name, otherArr.name)   &&
               Objects.equals(this.value, otherArr.value) &&
               Arrays.equals(this.values, otherArr.values);
    }

    @Override
    public int hashCode()
    {
        int hash = super.hashCode();
        hash = 43 * hash + Arrays.hashCode(this.values);
        return hash;
    }
}
