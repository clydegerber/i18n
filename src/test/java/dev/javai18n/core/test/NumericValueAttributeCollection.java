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
public class NumericValueAttributeCollection implements AttributeCollection
{
    String stringValue = "";
    int intValue = 0;
    double doubleValue = 0.0;
    boolean booleanValue = false;

    public NumericValueAttributeCollection() {}

    public NumericValueAttributeCollection(String stringValue, int intValue,double doubleValue, boolean booleanValue)
    {
        this.stringValue = stringValue;
        this.intValue = intValue;
        this.doubleValue = doubleValue;
        this.booleanValue = booleanValue;
    }

    @Override
    public void setAttribute(String name, Object value)
    {
        if ("stringValue".equals(name))
        {
            this.stringValue = (String) value;
            return;
        }
        if ("intValue".equals(name))
        {
            if (value instanceof String s)
            {
                this.intValue = Integer.parseInt(s);
                return;
            }
            this.intValue = (int) value;
            return;
        }
        if ("doubleValue".equals(name))
        {
            if (value instanceof String s)
            {
                this.doubleValue = Double.parseDouble(s);
                return;
            }
            this.doubleValue = (double) value;
            return;
        }
        if ("booleanValue".equals(name))
        {
            if (value instanceof String s)
            {
                this.booleanValue = Boolean.parseBoolean(s);
                return;
            }
            this.booleanValue = (boolean) value;
            return;
        }
        throw new RuntimeException("Unknown attribute: " + name);
    }

    @Override
    public String toString()
    {
        return "[stringValue: " + stringValue + " intValue: " + intValue + " doubleValue: " + doubleValue + " booleanValue: " + booleanValue + "]";
    }

    @Override
    public boolean equals(Object other)
    {
        if (!(other instanceof NumericValueAttributeCollection)) return false;
        NumericValueAttributeCollection otherColl = (NumericValueAttributeCollection) other;
        return (Objects.equals(this.stringValue, otherColl.stringValue) &&
                (this.intValue == otherColl.intValue) &&
                (this.doubleValue == otherColl.doubleValue) &&
                (this.booleanValue == otherColl.booleanValue));
    }

    @Override
    public int hashCode()
    {
        int hash = 6;
        hash = 15 * hash + Objects.hashCode(this.stringValue);
        hash = 15 * hash + Integer.hashCode(this.intValue);
        hash = 15 * hash + Double.hashCode(this.doubleValue);
        hash = 15 * hash + Boolean.hashCode(this.booleanValue);
        return hash;
    }
}
