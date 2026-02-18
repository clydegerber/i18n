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
import javax.xml.stream.XMLStreamException;
import dev.javai18n.core.XMLResourceBundle;
import dev.javai18n.core.AttributeCollectionResourceBundle;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for XMLResourceBundle.
 */
public class TestXmlResourceBundle
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
        InputStream stream = assertDoesNotThrow(() -> module.getResourceAsStream("dev/javai18n/core/test/LocalizableSub3Bundle_fr.xml"));
        try
        {
            XMLResourceBundle xmlBundle = new XMLResourceBundle(stream);
            assertEquals(xmlBundle.getString("key2"), "Value for key2 from LocalizableSub3Bundle_fr.xml.");
        } catch (IOException | XMLStreamException ex)
        {
            System.getLogger(TestXmlResourceBundle.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
    }

    @Test
    public void testInvalidInput()
    {
        {
            InputStream inputStream = new ByteArrayInputStream(
                      ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                       "<!DOCTYPE properties SYSTEM \"properties.dtd\">" +
                       "</properties>").getBytes());
            Exception e = assertThrows(IOException.class, ()->{ new XMLResourceBundle(inputStream); },
                "Exception not thrown");
            assertEquals("Unexpected character combination '</' in prolog.\n at [row,col {unknown-source}]: [1,85]",
                e.getMessage());
        }
        {
            InputStream inputStream = new ByteArrayInputStream(
                      ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                       "<!DOCTYPE properties SYSTEM \"properties.dtd\">" +
                       "<properties>").getBytes());
            Exception e = assertThrows(IOException.class, ()->{ new XMLResourceBundle(inputStream); },
                "Exception not thrown");
            assertEquals("Unexpected EOF; was expecting a close tag for element <properties>\n" +
                         " at [row,col {unknown-source}]: [1,95]\n" +
                         " at [Source: (com.ctc.wstx.sr.ValidatingStreamReader); line: 1, column: 96]",
                e.getMessage());
        }
        {
            InputStream inputStream = new ByteArrayInputStream(
                      ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                       "<!DOCTYPE properties SYSTEM \"properties.dtd\">" +
                       "<properties>" +
                       "</properties>").getBytes());
            Exception e = assertThrows(IOException.class, ()->{ new XMLResourceBundle(inputStream); },
                "Exception not thrown");
            assertEquals("Failed to parse any properties from the specified stream",
                e.getMessage());
        }
        {
            InputStream inputStream = new ByteArrayInputStream(
                      ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                       "<!DOCTYPE properties SYSTEM \"properties.dtd\">" +
                       "<properties>" +
                           "<array>" +
                           "</array>" +
                       "</properties>").getBytes());
            Exception e = assertThrows(IOException.class, ()->{ new XMLResourceBundle(inputStream); },
                "Exception not thrown");
            assertEquals("XML format error - array element found in unexpected location",
                e.getMessage());
        }
        {
            InputStream inputStream = new ByteArrayInputStream(
                      ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                       "<!DOCTYPE properties SYSTEM \"properties.dtd\">" +
                       "<properties>" +
                           "<properties>" +
                           "</properties>" +
                       "</properties>").getBytes());
            Exception e = assertThrows(IOException.class, ()->{ new XMLResourceBundle(inputStream); },
                "Exception not thrown");
            assertEquals("XML format error - unknown element or attribute found:properties",
                e.getMessage());
        }
        {
            InputStream inputStream = new ByteArrayInputStream(
                      ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                       "<!DOCTYPE properties SYSTEM \"properties.dtd\">" +
                       "<properties>" +
                           "<entry key='key1'>" +
                               "<object>" +
                                   "<entry key='name'>My name</entry>" +
                                   "<entry key='value'>My value</entry>" +
                               "</object>" +
                           "</entry>" +
                       "</properties>").getBytes());
            Exception e = assertThrows(IOException.class, ()->{ new XMLResourceBundle(inputStream); },
                "Exception not thrown");
            assertEquals("XML format error - type attribute for object is missing.",
                e.getMessage());
        }
        {
            InputStream inputStream = new ByteArrayInputStream(
                      ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                       "<!DOCTYPE properties SYSTEM \"properties.dtd\">" +
                       "<properties>" +
                           "<entry key='key1'>" +
                               "<object type='foo'>" +
                                   "<entry key='name'>My name</entry>" +
                                   "<entry key='value'>My value</entry>" +
                               "</object>" +
                           "</entry>" +
                       "</properties>").getBytes());
            Exception e = assertThrows(IOException.class, ()->{ new XMLResourceBundle(inputStream); },
                "Exception not thrown");
            assertEquals("Class foo is not in a registered AttributeCollection package",
                e.getMessage());
        }
    }

    @Test
    public void testNestedObject()
    {
        String input = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                       "<!DOCTYPE properties SYSTEM \"properties.dtd\">" +
                       "<properties>" +
                           "<entry key='key1'>" +
                               "<object type='dev.javai18n.core.test.NestedAttributeCollection'>" +
                                   "<entry key='name'>My name</entry>" +
                                   "<entry key='value'>My value</entry>" +
                                   "<entry key='coll'>" +
                                       "<object type='dev.javai18n.core.test.SimpleAttributeCollection'>" +
                                           "<entry key='name'>foo</entry>" +
                                           "<entry key='value'>bar</entry>" +
                                       "</object>" +
                                   "</entry>" +
                               "</object>" +
                           "</entry>" +
                       "</properties>";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        XMLResourceBundle xmlBundle = assertDoesNotThrow(()->new XMLResourceBundle(inputStream));
        SimpleAttributeCollection expected = new SimpleAttributeCollection("foo", "bar");
        NestedAttributeCollection nested = (NestedAttributeCollection) xmlBundle.getObject("key1");
        SimpleAttributeCollection actual = nested.coll;
        assertEquals(expected, actual);
    }

    @Test
    public void testNestedArray()
    {
        String input = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                       "<!DOCTYPE properties SYSTEM \"properties.dtd\">" +
                       "<properties>" +
                           "<entry key='key1'>" +
                               "<array>" +
                                   "<array>" +
                                       "<item>abc</item>" +
                                       "<item>123</item>" +
                                   "</array>" +
                                   "<array>" +
                                       "<item>xyz</item>" +
                                       "<item>321</item>" +
                                   "</array>" +
                               "</array>" +
                           "</entry>" +
                       "</properties>";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        XMLResourceBundle xmlBundle = assertDoesNotThrow(()->new XMLResourceBundle(inputStream));
        Object[] array = (Object[]) xmlBundle.getObject("key1");
        assertEquals(2, array.length);
        String[] sarray = (String[]) array[0];
        assertEquals(2, sarray.length);
        assertEquals("123", sarray[1]);
    }

   @Test
    public void testNumericValues()
    {
        String input = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                       "<!DOCTYPE properties SYSTEM \"properties.dtd\">" +
                       "<properties>" +
                           "<entry key='key1'>" +
                               "<object type='dev.javai18n.core.test.NumericValueAttributeCollection'>" +
                                   "<entry key='intValue'>5</entry>" +
                                   "<entry key='doubleValue'>2.5</entry>" +
                               "</object>" +
                           "</entry>" +
                       "</properties>";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        XMLResourceBundle xmlBundle = assertDoesNotThrow(()->new XMLResourceBundle(inputStream));
        NumericValueAttributeCollection coll = (NumericValueAttributeCollection) xmlBundle.getObject("key1");
        NumericValueAttributeCollection expected = new NumericValueAttributeCollection("", 5, 2.5, false);
        assertEquals(expected, coll);
    }

   @Test
    public void testBooleanValues()
    {
        String input = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                       "<!DOCTYPE properties SYSTEM \"properties.dtd\">" +
                       "<properties>" +
                           "<entry key='key1'>" +
                               "<object type='dev.javai18n.core.test.NumericValueAttributeCollection'>" +
                                   "<entry key='booleanValue'>true</entry>" +
                               "</object>" +
                           "</entry>" +
                       "</properties>";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        XMLResourceBundle xmlBundle = assertDoesNotThrow(()->new XMLResourceBundle(inputStream));
        NumericValueAttributeCollection coll = (NumericValueAttributeCollection) xmlBundle.getObject("key1");
        NumericValueAttributeCollection expected = new NumericValueAttributeCollection("", 0, 0, true);
        assertEquals(expected, coll);
    }

    /**
     * Test the behavior of an XML document with no string, object or array for an entry element.
     */
   @Test
    public void testNullValues()
    {
        String input = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                       "<!DOCTYPE properties SYSTEM \"properties.dtd\">" +
                       "<properties>" +
                           "<entry key='key1'>" +
                               "<object type='dev.javai18n.core.test.NumericValueAttributeCollection'>" +
                                   "<entry key='stringValue'/>" +
                               "</object>" +
                           "</entry>" +
                       "</properties>";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        XMLResourceBundle xmlBundle = assertDoesNotThrow(()->new XMLResourceBundle(inputStream));
        NumericValueAttributeCollection coll = (NumericValueAttributeCollection) xmlBundle.getObject("key1");
        NumericValueAttributeCollection expected = new NumericValueAttributeCollection(null, 0, 0, false);
        assertEquals(expected, coll);
    }

    @Test
    public void testObjectWithArrayField()
    {
        String input = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                       "<!DOCTYPE properties SYSTEM \"properties.dtd\">" +
                       "<properties>" +
                           "<entry key='My Object'>" +
                               "<object type='dev.javai18n.core.test.AttributeCollectionWithStringArray'>" +
                                   "<entry key='name'>My name</entry>" +
                                   "<entry key='value'>My value</entry>" +
                                   "<entry key='values'>" +
                                       "<array>" +
                                           "<item>foo</item>" +
                                           "<item>bar</item>" +
                                           "<item>baz</item>" +
                                       "</array>" +
                                   "</entry>" +
                               "</object>" +
                           "</entry>" +
                       "</properties>";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        XMLResourceBundle xmlBundle = assertDoesNotThrow(()->new XMLResourceBundle(inputStream));
        AttributeCollectionWithStringArray coll = (AttributeCollectionWithStringArray) xmlBundle.getObject("My Object");
        String[] array = coll.getValues();
        assertEquals(3, array.length);
    }

    @Test
    public void testEmptyXmlFileDoesNotHideNonEmptyPropertiesFile()
    {
        ResourceBundle rb = ResourceBundle.getBundle("dev.javai18n.core.test.EmptyXmlNonEmptyProperties");
        assertEquals("Value for key1 from EmptyXmlNonEmptyPropertiesBundle.properties", rb.getString("key1"));
    }

    @Test
    public void testFromGetBundle()
    {
        XMLResourceBundle xmlBundle = (XMLResourceBundle) ResourceBundle.getBundle("dev.javai18n.core.test.XmlProperties");
        assertEquals(xmlBundle.getString("key1"), "value1");
        assertEquals(xmlBundle.getString("key2"), "value2");
        String [] array = xmlBundle.getStringArray("key3");
        assertNotNull(array);
        assertEquals(3, array.length);
        assertEquals("value3A", array[0]);
        assertEquals("value3B", array[1]);
        assertEquals("value3C", array[2]);
        SimpleAttributeCollection coll = (SimpleAttributeCollection) xmlBundle.getObject("key4");
        SimpleAttributeCollection expectedColl = new SimpleAttributeCollection("My name", "My value");
        assertEquals(coll, expectedColl);
        assertEquals(xmlBundle.getString("key5"), "value5");
        Object [] objArray = (Object[]) xmlBundle.getObject("key6");
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
