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

import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ResourceBundle;
import dev.javai18n.core.JsonResourceBundle;
import dev.javai18n.core.AttributeCollectionResourceBundle;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for JsonResourceBundle.
 */
public class TestJsonResourceBundle
{
    @BeforeAll
    public static void registerTypes()
    {
        AttributeCollectionResourceBundle.registerAttributeCollectionPackage(
            SimpleAttributeCollection.class.getPackageName());
    }

    @Test
    public void testValidInput()
    {
        Module module = this.getClass().getModule();
        InputStream stream = assertDoesNotThrow(() -> module.getResourceAsStream("dev/javai18n/core/test/JsonPropertiesBundle.json"));
        try
        {
            JsonResourceBundle jsonBundle = new JsonResourceBundle(stream);
            assertEquals(jsonBundle.getString("key1"), "value1");
            assertEquals(jsonBundle.getString("key2"), "value2");
            String [] array = jsonBundle.getStringArray("key3");
            assertNotNull(array);
            assertEquals(3, array.length);
            assertEquals("value3A", array[0]);
            assertEquals("value3B", array[1]);
            assertEquals("value3C", array[2]);
            SimpleAttributeCollection coll = (SimpleAttributeCollection) jsonBundle.getObject("key4");
            SimpleAttributeCollection expectedColl = new SimpleAttributeCollection("My name", "My value");
            assertEquals(coll, expectedColl);
            assertEquals(jsonBundle.getString("key5"), "value5");
            Object [] objArray = (Object[]) jsonBundle.getObject("key6");
            assertEquals(3, objArray.length);
            SimpleAttributeCollection collA = new SimpleAttributeCollection("My nameA", "My valueA");
            SimpleAttributeCollection collB = new SimpleAttributeCollection("My nameB", "My valueB");
            SimpleAttributeCollection collC = new SimpleAttributeCollection("My nameC", "My valueC");
            SimpleAttributeCollection objA = (SimpleAttributeCollection) objArray[0];
            SimpleAttributeCollection objB = (SimpleAttributeCollection) objArray[1];
            SimpleAttributeCollection objC = (SimpleAttributeCollection) objArray[2];
            assertEquals(collA, objA);
            assertEquals(collB, objB);
            assertEquals(collC, objC);
        } catch (IOException ex)
        {
            System.getLogger(TestJsonResourceBundle.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
    }

    @Test
    public void testInvalidInput()
    {
        {
            InputStream inputStream = new ByteArrayInputStream("}".getBytes());
            Exception e = assertThrows(IOException.class, ()->{ new JsonResourceBundle(inputStream); },
                "Exception not thrown");
            assertEquals("Unexpected close marker '}': no open Object to close\n" +
                " at [Source: REDACTED (`StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION` disabled); line: 1, column: 1]",
                e.getMessage());
        }
        {
            InputStream inputStream = new ByteArrayInputStream("{".getBytes());
            Exception e = assertThrows(IOException.class, ()->{ new JsonResourceBundle(inputStream); },
                "Exception not thrown");
            assertEquals("Unexpected end-of-input: expected close marker for Object (start marker at [Source: REDACTED " +
                "(`StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION` disabled); line: 1, column: 1])\n" +
                " at [Source: REDACTED (`StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION` disabled); line: 1, column: 2]",
                e.getMessage());
        }
        {
            InputStream inputStream = new ByteArrayInputStream("{}".getBytes());
            Exception e = assertThrows(IOException.class, ()->{ new JsonResourceBundle(inputStream); },
                "Exception not thrown");
            assertEquals("Failed to parse any properties from the specified stream",
                e.getMessage());
        }
        {
            InputStream inputStream = new ByteArrayInputStream("{[]}".getBytes());
            Exception e = assertThrows(IOException.class, ()->{ new JsonResourceBundle(inputStream); },
                "Exception not thrown");
            assertEquals("Unexpected character ('[' (code 91)): was expecting double-quote to start field name\n" +
                " at [Source: REDACTED (`StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION` disabled); line: 1, column: 2]",
                e.getMessage());
        }
        {
            InputStream inputStream = new ByteArrayInputStream("{{}}".getBytes());
            Exception e = assertThrows(IOException.class, ()->{ new JsonResourceBundle(inputStream); },
                "Exception not thrown");
            assertEquals("Unexpected character ('{' (code 123)): was expecting double-quote to start field name\n" +
                " at [Source: REDACTED (`StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION` disabled); line: 1, column: 2]",
                e.getMessage());
        }
        {
            InputStream inputStream = new ByteArrayInputStream(
                "{\"key1\": {\"name\": \"My name\", \"value\": \"My value\"}}".getBytes());
            Exception e = assertThrows(IOException.class, ()->{ new JsonResourceBundle(inputStream); },
                "Exception not thrown");
            assertEquals("JSON format error - the type field was not the first field in the json object",
                e.getMessage());
        }
        {
            InputStream inputStream = new ByteArrayInputStream(
                "{\"key1\": {\"type\": \"foo\", \"name\": \"My name\", \"value\": \"My value\"}}".getBytes());
            Exception e = assertThrows(IOException.class, ()->{ new JsonResourceBundle(inputStream); },
                "Exception not thrown");
            assertEquals("Class foo is not in a registered AttributeCollection package",
                e.getMessage());
        }
    }

    @Test
    public void testNestedObject()
    {
        String input = "{\"key1\": {\"type\": \"dev.javai18n.core.test.NestedAttributeCollection\", " +
                       "\"name\": \"My name\", \"value\": \"My value\", \"coll\":" +
                       "{\"type\": \"dev.javai18n.core.test.SimpleAttributeCollection\", \"name\": \"foo\", " +
                       "\"value\": \"bar\"}}}";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        JsonResourceBundle jBundle = assertDoesNotThrow(()->new JsonResourceBundle(inputStream));
        SimpleAttributeCollection expected = new SimpleAttributeCollection("foo", "bar");
        NestedAttributeCollection nested = (NestedAttributeCollection) jBundle.getObject("key1");
        SimpleAttributeCollection actual = nested.coll;
        assertEquals(expected, actual);
    }

    @Test
    public void testNestedArray()
    {
        String input = "{\"key1\": [[\"abc\", \"123\"], [\"xyz\", \"321\"]]}";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        JsonResourceBundle jBundle = assertDoesNotThrow(()->new JsonResourceBundle(inputStream));
        Object[] array = (Object[]) jBundle.getObject("key1");
        assertEquals(2, array.length);
        String[] sarray = (String[]) array[0];
        assertEquals(2, sarray.length);
        assertEquals("123", sarray[1]);
    }

   @Test
    public void testNumericValues()
    {
        String input = "{\"key1\": {\"type\": \"dev.javai18n.core.test.NumericValueAttributeCollection\", " +
                       "\"intValue\": 5," +
                       "\"doubleValue\": 2.5}}";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        JsonResourceBundle jBundle = assertDoesNotThrow(()->new JsonResourceBundle(inputStream));
        NumericValueAttributeCollection coll = (NumericValueAttributeCollection) jBundle.getObject("key1");
        NumericValueAttributeCollection expected = new NumericValueAttributeCollection("", 5, 2.5, false);
        assertEquals(expected, coll);
    }

   @Test
    public void testBooleanValues()
    {
        String input = "{\"key1\": {\"type\": \"dev.javai18n.core.test.NumericValueAttributeCollection\", " +
                       "\"booleanValue\": true}}";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        JsonResourceBundle jBundle = assertDoesNotThrow(()->new JsonResourceBundle(inputStream));
        NumericValueAttributeCollection coll = (NumericValueAttributeCollection) jBundle.getObject("key1");
        NumericValueAttributeCollection expected = new NumericValueAttributeCollection("", 0, 0, true);
        assertEquals(expected, coll);
    }

   @Test
    public void testNullValues()
    {
        String input = "{\"key1\": {\"type\": \"dev.javai18n.core.test.NumericValueAttributeCollection\", " +
                       "\"stringValue\": null}}";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        JsonResourceBundle jBundle = assertDoesNotThrow(()->new JsonResourceBundle(inputStream));
        NumericValueAttributeCollection coll = (NumericValueAttributeCollection) jBundle.getObject("key1");
        NumericValueAttributeCollection expected = new NumericValueAttributeCollection(null, 0, 0, false);
        assertEquals(expected, coll);
    }

    @Test
    public void testObjectWithArrayField()
    {
        String input = "{\"My Object\": {\"type\": \"dev.javai18n.core.test.AttributeCollectionWithStringArray\"," +
                       "\"name\": \"My Name\", \"value\": \"My value\", \"values\": [\"foo\", \"bar\", \"baz\"]}}";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        JsonResourceBundle jBundle = assertDoesNotThrow(()->new JsonResourceBundle(inputStream));
        AttributeCollectionWithStringArray coll = (AttributeCollectionWithStringArray) jBundle.getObject("My Object");
        String[] array = coll.getValues();
        assertEquals(3, array.length);
    }

    @Test
    public void testEmptyJsonFileDoesNotHideNonEmptyPropertiesFile()
    {
        ResourceBundle rb = ResourceBundle.getBundle("dev.javai18n.core.test.EmptyJsonNonEmptyProperties");
        assertEquals("Value for key1 from EmptyJsonNonEmptyPropertiesBundle.properties", rb.getString("key1"));
    }

    @Test
    public void testPropertiesFileNoSuffix()
    {
        ResourceBundle rb = ResourceBundle.getBundle("dev.javai18n.core.test.PropertiesFileNoSuffix");
        assertEquals("Value for key1 from PropertiesFileNoSuffix.properties", rb.getString("key1"));
    }

    @Test
    public void testClassFileNoSuffix()
    {
        ResourceBundle rb = ResourceBundle.getBundle("dev.javai18n.core.test.ClassBundleNoSuffix");
        assertEquals("Value for key1 from ClassBundleNoSuffix for root locale.", rb.getString("key1"));
    }

    @Test
    public void testFromGetBundle()
    {
        JsonResourceBundle jsonBundle = (JsonResourceBundle) ResourceBundle.getBundle("dev.javai18n.core.test.JsonProperties");
        assertEquals(jsonBundle.getString("key1"), "value1");
        assertEquals(jsonBundle.getString("key2"), "value2");
        String [] array = jsonBundle.getStringArray("key3");
        assertNotNull(array);
        assertEquals(3, array.length);
        assertEquals("value3A", array[0]);
        assertEquals("value3B", array[1]);
        assertEquals("value3C", array[2]);
        SimpleAttributeCollection coll = (SimpleAttributeCollection) jsonBundle.getObject("key4");
        SimpleAttributeCollection expectedColl = new SimpleAttributeCollection("My name", "My value");
        assertEquals(coll, expectedColl);
        assertEquals(jsonBundle.getString("key5"), "value5");
        Object [] objArray = (Object[]) jsonBundle.getObject("key6");
        assertEquals(3, objArray.length);
        SimpleAttributeCollection collA = new SimpleAttributeCollection("My nameA", "My valueA");
        SimpleAttributeCollection collB = new SimpleAttributeCollection("My nameB", "My valueB");
        SimpleAttributeCollection collC = new SimpleAttributeCollection("My nameC", "My valueC");
        SimpleAttributeCollection objA = (SimpleAttributeCollection) objArray[0];
        SimpleAttributeCollection objB = (SimpleAttributeCollection) objArray[1];
        SimpleAttributeCollection objC = (SimpleAttributeCollection) objArray[2];
        assertEquals(collA, objA);
        assertEquals(collB, objB);
        assertEquals(collC, objC);
    }
}
