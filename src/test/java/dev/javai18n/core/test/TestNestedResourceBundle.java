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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import dev.javai18n.core.AssociativeResourceBundleControl;
import dev.javai18n.core.NestedResourceBundle;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the NestedResourceBundle class.
 */
public class TestNestedResourceBundle
{
    @Test
    void getFrenchResource()
    {
        LocalizableSuper s = new LocalizableSuper();
        NestedResourceBundle rootRB = assertDoesNotThrow(() -> (NestedResourceBundle) s.getResourceBundle());
        assertNotNull(rootRB);
        String root1 = rootRB.getString("key1");
        assertEquals("Value for key1 from LocalizableSuperBundle for root locale.", root1);
        String root2 = rootRB.getString("key2");
        assertEquals("Value for key2 from LocalizableSuperBundle for root locale.", root2);
        assertDoesNotThrow(() -> s.setBundleLocale(Locale.FRENCH));
        NestedResourceBundle frRB = assertDoesNotThrow(() -> (NestedResourceBundle) s.getResourceBundle());
        assertNotNull(frRB);
        String fr1 = frRB.getString("key1");
        assertEquals("Value for key1 from LocalizableSuperBundle_fr locale.", fr1);
        String fr2 = frRB.getString("key2");
        assertEquals("Value for key2 from LocalizableSuperBundle for root locale.", fr2);
    }

    @Test
    void getResourceForSubclassWithNoResourcesOfItsOwn()
    {
        LocalizableSub1  sub1 = new LocalizableSub1();
        NestedResourceBundle rootRB = assertDoesNotThrow(() -> (NestedResourceBundle) sub1.getResourceBundle());
        assertDoesNotThrow(() -> sub1.setBundleLocale(Locale.FRENCH));
        NestedResourceBundle frRB = assertDoesNotThrow(() -> (NestedResourceBundle) sub1.getResourceBundle());
        String root1 = rootRB.getString("key1");
        assertEquals("Value for key1 from LocalizableSuperBundle for root locale.", root1);
        String root2 = rootRB.getString("key2");
        assertEquals("Value for key2 from LocalizableSuperBundle for root locale.", root2);
        String fr1 = frRB.getString("key1");
        assertEquals("Value for key1 from LocalizableSuperBundle_fr locale.", fr1);
        String fr2 = frRB.getString("key2");
        assertEquals("Value for key2 from LocalizableSuperBundle for root locale.", fr2);
    }

    @Test
    void getResourceForSubclassWithResourcesOfItsOwn()
    {
        LocalizableSub2 sub2 = new LocalizableSub2();
        NestedResourceBundle rootRB = assertDoesNotThrow(() -> (NestedResourceBundle) sub2.getResourceBundle());
        assertDoesNotThrow(() -> sub2.setBundleLocale(Locale.FRENCH));
        NestedResourceBundle frRB = assertDoesNotThrow(() -> (NestedResourceBundle) sub2.getResourceBundle());
        String root1 = rootRB.getString("key1");
        assertEquals("Value for key1 from LocalizableSub2Bundle for root locale.", root1);
        String root2 = rootRB.getString("key2");
        assertEquals("Value for key2 from LocalizableSuperBundle for root locale.", root2);
        String fr1 = frRB.getString("key1");
        assertEquals("Value for key1 from LocalizableSub2Bundle_fr locale.", fr1);
        String fr2 = frRB.getString("key2");
        assertEquals("Value for key2 from LocalizableSuperBundle for root locale.", fr2);
    }

    @Test
    void getResourceFallbackToLanguageLocale()
    {
        LocalizableSub2  sub2 = new LocalizableSub2();
        assertDoesNotThrow(() -> sub2.setBundleLocale(Locale.of("fr","FR")));
        ResourceBundle frRB = assertDoesNotThrow(() -> sub2.getResourceBundle());
        String fr1 = frRB.getString("key1");
        assertEquals("Value for key1 from LocalizableSub2Bundle_fr locale.", fr1);
        String fr2 = frRB.getString("key2");
        assertEquals("Value for key2 from LocalizableSuperBundle for root locale.", fr2);
    }

    @Test
    void getKeys()
    {
        HashSet<String> expectedKeys = new HashSet<>(Arrays.asList("key1", "key2", "key3"));
        LocalizableSub2  sub2 = new LocalizableSub2();
        assertDoesNotThrow(() -> sub2.setBundleLocale(Locale.of("fr","FR")));
        ResourceBundle frRB = assertDoesNotThrow(() -> sub2.getResourceBundle());
        Enumeration<String> e = frRB.getKeys();
        int count = 0;
        while (e.hasMoreElements())
        {
            ++count;
            String elem = e.nextElement();
            assertTrue(expectedKeys.contains(elem));
        }
        assertEquals(3, count);
    }

    @Test
    void getBaseBundleName()
    {
        LocalizableSub2  sub2 = new LocalizableSub2();
        assertDoesNotThrow(() -> sub2.setBundleLocale(Locale.of("fr","FR")));
        NestedResourceBundle frRB = assertDoesNotThrow(() -> (NestedResourceBundle) sub2.getResourceBundle());
        assertNotNull(frRB);
        assertEquals("dev.javai18n.core.test.LocalizableSub2", frRB.getBaseBundleName());
    }

    /**
     * Test the locale found when no delegate exists that matches the locale.
     */
    @Test
    void getLocale()
    {
        LocalizableSub2  sub2 = new LocalizableSub2();
        Locale locale = Locale.of("fr", "FR");
        assertDoesNotThrow(() -> sub2.setBundleLocale(locale));
        NestedResourceBundle frRB = assertDoesNotThrow(() -> (NestedResourceBundle) sub2.getResourceBundle());
        assertEquals(Locale.FRENCH, frRB.getLocale());
    }

    /**
     * Tests that null arguments to newBundle() throw an exception.
     */
    @Test
    void nullArgs()
    {
        AssociativeResourceBundleControl ctrl = new AssociativeResourceBundleControl();
        Exception e = assertThrows(NullPointerException.class, ()->{ ctrl.newBundle(null, null, null, null, false); },
                                  "Exception not thrown");
        assertEquals("baseName is null", e.getMessage());
        e = assertThrows(NullPointerException.class, ()->{ ctrl.newBundle("foo", null, null, null, false); },
                         "Exception not thrown");
        assertEquals("locale is null", e.getMessage());
        e = assertThrows(NullPointerException.class, ()->{ ctrl.newBundle("foo", Locale.FRENCH, null, null, false); },
                         "Exception not thrown");
        assertEquals("format is null", e.getMessage());
        e = assertThrows(NullPointerException.class, ()->{ ctrl.newBundle("foo", Locale.FRENCH, "foo", null, false); },
                         "Exception not thrown");
        assertEquals("loader.getResourceClassLoader returns null", e.getMessage());
    }

    /**
     * Tests that calling newBundle with an unexpected format throws an IllegalArgumentException.
     */
    @Test
    void illegalFormat()
    {
        AssociativeResourceBundleControl ctrl = new AssociativeResourceBundleControl();
        Exception e = assertThrows(IllegalArgumentException.class, ()->{ ctrl.newBundle("foo", Locale.FRENCH, "foo", getClass().getClassLoader(), false); },
                                  "Exception not thrown");
        assertEquals("unknown format: foo", e.getMessage());
    }

    /**
     * Tests that attempting to load a ResourceBundle from a class that can't be
     * cast to the ResourceBundle class raises a ClassCastException.
     */
    @Test
    void classCastExceptionRaised()
    {
        AssociativeResourceBundleControl ctrl = new AssociativeResourceBundleControl();

        Exception e = assertThrows(ClassCastException.class, ()->{ ctrl.newBundle(
                "dev.javai18n.core.test.LocalizableSuper2", Locale.ROOT, "java.class",
                getClass().getClassLoader(), false); },
                "Exception not thrown");
        assertEquals("dev.javai18n.core.test.LocalizableSuper2Bundle cannot be cast to ResourceBundle", e.getMessage());
    }

    /**
     * Tests that a null key passed to getObject() raises a NullPointerException.
     */
    @Test
    void nullKeyRaisesNullPointerException()
    {
        LocalizableSub2  sub2 = new LocalizableSub2();
        assertDoesNotThrow(() -> sub2.setBundleLocale(Locale.of("fr","FR")));
        ResourceBundle frRB = assertDoesNotThrow(() -> sub2.getResourceBundle());
        assertNotNull(frRB);
        Exception e = assertThrows(NullPointerException.class, ()->{ frRB.getObject(null); },
                                  "Exception not thrown");
        assertEquals("key is null", e.getMessage());
    }

    /**
     * Tests That a mix of properties-based, xml-based and class-based resource bundles
     * can be used.
     */
    @Test
    void testMixedBundleTypes()
    {
        // Retrieve the properties-based bundle using the fully qualified class name
        LocalizableSub3 sub3 = new LocalizableSub3();
        NestedResourceBundle rb = assertDoesNotThrow(() -> (NestedResourceBundle) sub3.getResourceBundle());
        assertEquals("Value for key2 from LocalizableSub3Bundle.properties for root locale.", rb.getString("key2"));
        // Retrieve the xml-based bundle using the fully qualified class name.
        assertDoesNotThrow(() -> sub3.setBundleLocale(Locale.FRENCH));
        ResourceBundle rb2 = assertDoesNotThrow(() -> sub3.getResourceBundle());
        assertEquals("Value for key2 from LocalizableSub3Bundle_fr.xml.", rb2.getString("key2"));
        assertEquals("Value for key1 from LocalizableSuperBundle_fr locale.", rb2.getString("key1"));
    }

    /**
     * Tests that dump() produces the expected log output for a two-level
     * NestedResourceBundle hierarchy (LocalizableSub2 → LocalizableSuper).
     */
    @Test
    void testDump()
    {
        LocalizableSub2 sub2 = new LocalizableSub2();
        NestedResourceBundle rb = assertDoesNotThrow(() -> (NestedResourceBundle) sub2.getResourceBundle());

        // Temporarily enable DEBUG (FINE) level on the JUL logger backing I18N_LOGGER
        java.util.logging.Logger julLogger = java.util.logging.Logger.getLogger("dev.javai18n.core");
        java.util.logging.Level savedLevel = julLogger.getLevel();
        List<String> messages = new ArrayList<>();
        Handler handler = new Handler()
        {
            @Override
            public void publish(LogRecord record)
            {
                String msg = record.getMessage();
                ResourceBundle bundle = record.getResourceBundle();
                if (bundle != null && msg != null)
                {
                    try { msg = bundle.getString(msg); }
                    catch (java.util.MissingResourceException ex) { /* use msg as-is */ }
                }
                if (record.getParameters() != null && record.getParameters().length > 0)
                {
                    msg = MessageFormat.format(msg, record.getParameters());
                }
                messages.add(msg);
            }

            @Override public void flush() {}
            @Override public void close() {}
        };
        handler.setLevel(java.util.logging.Level.ALL);
        julLogger.setLevel(java.util.logging.Level.ALL);
        julLogger.addHandler(handler);
        try
        {
            rb.dump();
        }
        finally
        {
            julLogger.removeHandler(handler);
            julLogger.setLevel(savedLevel);
        }

        assertEquals(8, messages.size());
        assertEquals("\"NestedResourceBundle dump begin\"", messages.get(0));
        assertEquals("\"base bundle name: dev.javai18n.core.test.LocalizableSub2\"", messages.get(1));
        assertEquals("\"   delegate name: dev.javai18n.core.test.LocalizableSub2Bundle\"", messages.get(2));
        assertEquals("\"   delegate type: dev.javai18n.core.test.LocalizableSub2Bundle\"", messages.get(3));
        assertEquals("\"base bundle name: dev.javai18n.core.test.LocalizableSuper\"", messages.get(4));
        assertEquals("\"   delegate name: dev.javai18n.core.test.LocalizableSuperBundle\"", messages.get(5));
        assertEquals("\"   delegate type: dev.javai18n.core.test.LocalizableSuperBundle\"", messages.get(6));
        assertEquals("\"NestedResourceBundle dump end\"", messages.get(7));
    }
}
