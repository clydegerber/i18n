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
import java.util.Enumeration;
import java.util.HashSet;
import java.util.ListResourceBundle;
import java.util.Locale;
import java.util.ResourceBundle;
import dev.javai18n.core.AssociativeResourceBundleControl;
import dev.javai18n.core.NestedResourceBundle;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for inner classes that implement the Localizable interface.
 */
public class TestInnerLocalizable
{
    public static class LocalizableSuper extends BaseLocalizable
    {
    }

    public static class LocalizableSub1 extends LocalizableSuper
    {
    }

    public static class LocalizableSub2 extends LocalizableSub1
    {
    }

    public static class LocalizableSub3 extends LocalizableSuper
    {
    }

    public static class LocalizableSuperBundle extends ListResourceBundle
    {

        /**
         * Override the base class method to return the resources we want.
         * @return the contents of the ResourceBundle.
         */
        @Override
        protected Object[][] getContents()
        {
            return new Object[][]
            {
                {"key1", "Value for key1 from LocalizableSuperBundle for root locale."},
                {"key2", "Value for key2 from LocalizableSuperBundle for root locale."},
                {"key3", "Value for key3 from LocalizableSuperBundle for root locale."} // key3 has a value only in the root bundle
            };
        }
    }

    public static class LocalizableSuperBundle_fr extends ListResourceBundle
    {

        /**
         * Override the base class method to return the resources we want.
         * @return the contents of the ResourceBundle.
         */
        @Override
        protected Object[][] getContents()
        {
            return new Object[][]
            {
                {"key1", "Value for key1 from LocalizableSuperBundle_fr locale."}
            };
        }
    }

    public static class LocalizableSub2Bundle extends ListResourceBundle
    {
        /**
         * Override the base class method to return the resources we want.
         * @return the contents of the ResourceBundle.
         */
        @Override
        protected Object[][] getContents()
        {
            return new Object[][]
            {
                {"key1", "Value for key1 from LocalizableSub2Bundle for root locale."}
            };
        }
    }

    public static class LocalizableSub2Bundle_fr extends ListResourceBundle
    {
        /**
         * Override the base class method to return the resources we want.
         * @return the contents of the ResourceBundle.
         */
        @Override
        protected Object[][] getContents()
        {
            return new Object[][]
            {
                {"key1", "Value for key1 from LocalizableSub2Bundle_fr locale."}
            };
        }
    }

    public static class LocalizableSuper2 extends LocalizableSuper
    {
    }

    /**
     * An example of a badly named class - trying to get a newBundle for the
     * name "LocalizableSuper2" will raise a class cast exception since it
     * can't be assigned to ResourceBundle.
     */
    public static class LocalizableSuper2Bundle extends LocalizableSuper2
    {
    }

    @Test
    void getFrenchResource()
    {
        LocalizableSuper s = new LocalizableSuper();
        ResourceBundle rootRB = assertDoesNotThrow(() -> s.getResourceBundle());
        assertNotNull(rootRB);
        String root1 = rootRB.getString("key1");
        assertEquals("Value for key1 from LocalizableSuperBundle for root locale.", root1);
        String root2 = rootRB.getString("key2");
        assertEquals("Value for key2 from LocalizableSuperBundle for root locale.", root2);
        assertDoesNotThrow(() -> s.setBundleLocale(Locale.FRENCH));
        ResourceBundle frRB = assertDoesNotThrow(() -> s.getResourceBundle());
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
        ResourceBundle rootRB = assertDoesNotThrow(() -> sub1.getResourceBundle());
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
        ResourceBundle rootRB = assertDoesNotThrow(() -> sub2.getResourceBundle());
        assertDoesNotThrow(() -> sub2.setBundleLocale(Locale.FRENCH));
        ResourceBundle frRB = assertDoesNotThrow(() -> sub2.getResourceBundle());
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
        String baseBundleName = frRB.getBaseBundleName();
        assertEquals("dev.javai18n.core.test.TestInnerLocalizable$LocalizableSub2", baseBundleName);
    }

    /**
     * Test the locale found when no delegate exists that matches the locale.
     */
    @Test
    void getBundleLocale()
    {
        LocalizableSub2  sub2 = new LocalizableSub2();
        Locale locale = Locale.of("fr", "FR");
        assertDoesNotThrow(() -> sub2.setBundleLocale(locale));
        ResourceBundle frRB = assertDoesNotThrow(() -> sub2.getResourceBundle());
        assertEquals(Locale.FRENCH, frRB.getLocale());
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
                "dev.javai18n.core.test.TestInnerLocalizable$LocalizableSuper2", Locale.ROOT, "java.class",
                getClass().getClassLoader(), false); },
                "Exception not thrown");
        assertEquals("dev.javai18n.core.test.TestInnerLocalizable$LocalizableSuper2Bundle cannot be cast to ResourceBundle", e.getMessage());
    }

    /**
     * Tests That a mix of properties-based, xml-based and class-based resource bundles
     * can be used.
     */
    @Test
    void testMixedBundleTypes()
    {
        LocalizableSub3 sub3 = new LocalizableSub3();
        ResourceBundle rb = assertDoesNotThrow(() -> sub3.getResourceBundle());
        assertTrue(NestedResourceBundle.class.isAssignableFrom(rb.getClass()));
        assertEquals("Value for key2 from LocalizableSub3Bundle.properties for root locale.", rb.getString("key2"));
        // Retrieve the xml-based bundle using the fully qualified class name.
        assertDoesNotThrow(() -> sub3.setBundleLocale(Locale.FRENCH));
        ResourceBundle rb2 = assertDoesNotThrow(() -> sub3.getResourceBundle());
        assertTrue(NestedResourceBundle.class.isAssignableFrom(rb2.getClass()));
        assertEquals("Value for key2 from LocalizableSub3Bundle_fr.xml.", rb2.getString("key2"));
        assertEquals("Value for key1 from LocalizableSuperBundle_fr locale.", rb2.getString("key1"));
    }
}
