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

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import dev.javai18n.core.AssociativeResourceBundleLocator;
import dev.javai18n.core.ResourceStreamLoader;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the AssociativeResourceBundleLocator class.
 */
public class TestResourceBundleLocator
{
    /**
     * Tests that invalid constructor arguments throw the expected exceptions.
     */
    @Test
    public void testCtor()
    {
        Exception e = assertThrows(NullPointerException.class, ()->{ new AssociativeResourceBundleLocator(null); },
                                  "Exception not thrown");
        assertEquals("suffix is null", e.getMessage());
    }

    /**
     * Tests that the list returned from getFormats contains the expected entries.
     */
    @Test
    public void testFormats()
    {
        AssociativeResourceBundleLocator locator = new AssociativeResourceBundleLocator("Bundle");
        List<String> formats = locator.getFormats();
        assertEquals(formats.get(0), "java.class");
        assertEquals(formats.get(1), "json");
        assertEquals(formats.get(2), "xml");
        assertEquals(formats.get(3), "java.properties");
        assertEquals(formats.size(), 4);
    }

    /**
     * Tests invalid arguments to newBundle()
     */
    @Test
    public void testGetBundleArgs()
    {
        AssociativeResourceBundleLocator locator = new AssociativeResourceBundleLocator("Bundle");
        Exception e = assertThrows(NullPointerException.class,
                ()->{ locator.newBundle(null, null, null, null, true); },
                "Exception not thrown");
        assertEquals("baseName is null", e.getMessage());
        e = assertThrows(NullPointerException.class,
                ()->{ locator.newBundle("dev.javai18n.core.test.LocalizableSuper", null, null, null, true); },
                "Exception not thrown");
        assertEquals("locale is null", e.getMessage());
        e = assertThrows(NullPointerException.class,
                ()->{ locator.newBundle("dev.javai18n.core.test.LocalizableSuper", Locale.ROOT, null, null, true); },
                "Exception not thrown");
        assertEquals("format is null", e.getMessage());
        e = assertThrows(NullPointerException.class,
                ()->{ locator.newBundle("dev.javai18n.core.test.LocalizableSuper", Locale.ROOT, "java.class", null, true); },
                "Exception not thrown");
        assertEquals("loader is null", e.getMessage());
        e = assertThrows(IllegalArgumentException.class,
                ()->{ locator.newBundle("dev.javai18n.core.test.LocalizableSuper", Locale.ROOT, "foo", new ResourceStreamLoader(this.getClass().getClassLoader()), true); },
                "Exception not thrown");
        assertEquals("unknown format: foo", e.getMessage());
    }

    /**
     * Tests locating a class-based ResourceBundle
     */
    @Test
    public void testLocateClassBasedBundle()
    {
        AssociativeResourceBundleLocator locator = new AssociativeResourceBundleLocator("Bundle");
        ResourceBundle rb = assertDoesNotThrow(() ->
            locator.newBundle("dev.javai18n.core.test.LocalizableSuper", Locale.ROOT, "java.class",
                new ResourceStreamLoader(this.getClass().getClassLoader()), true));
        assertNotNull(rb);
        assertEquals("Value for key1 from LocalizableSuperBundle for root locale.", rb.getString("key1"));
    }

    /**
     * Tests locating an xml-based ResourceBundle
     */
    @Test
    public void testLocateXmlBasedBundle()
    {
        AssociativeResourceBundleLocator locator = new AssociativeResourceBundleLocator("Bundle");
        ResourceBundle rb = assertDoesNotThrow(() ->
            locator.newBundle("dev.javai18n.core.test.LocalizableSub3", Locale.FRENCH, "xml",
                new ResourceStreamLoader(this.getClass().getModule()), true));
        assertNotNull(rb);
        assertEquals("Value for key2 from LocalizableSub3Bundle_fr.xml.", rb.getString("key2"));
    }

    /**
     * Tests locating a properties-based ResourceBundle
     */
    @Test
    public void testLocatePropertiesBasedBundle()
    {
        AssociativeResourceBundleLocator locator = new AssociativeResourceBundleLocator("Bundle");
        ResourceBundle rb = assertDoesNotThrow(() ->
            locator.newBundle("dev.javai18n.core.test.LocalizableSub3", Locale.ROOT, "java.properties",
                new ResourceStreamLoader(this.getClass().getModule()), true));
        assertNotNull(rb);
        assertEquals("Value for key2 from LocalizableSub3Bundle.properties for root locale.", rb.getString("key2"));
    }
}
