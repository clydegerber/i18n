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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import dev.javai18n.core.Localizable.LocaleEvent;
import dev.javai18n.core.Localizable.LocaleEventListener;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the core functionality.
 */
public class TestBaseLocalizable
{
    @Test
    void testMissingBundleThrowsException()
    {
        BaseLocalizable b = new BaseLocalizable();
        MissingResourceException ex = assertThrows(MissingResourceException.class, ()->b.getResourceBundle());
        assertEquals("Unable to locate ResourceBundle for dev.javai18n.core.test.BaseLocalizable", ex.getMessage());
    }

    @Test
    void getBundle()
    {
        LocalizableSuper l = new LocalizableSuper();
        ResourceBundle rb = assertDoesNotThrow(() -> l.getResourceBundle());
        assertNotNull(rb);
    }

    @Test
    void getBundleLocale()
    {
        BaseLocalizable l = new BaseLocalizable();
        Locale locale = l.getBundleLocale();
        assertEquals(locale, Locale.getDefault());
    }

    @Test
    void getAvailableLocales()
    {
        BaseLocalizable l = new BaseLocalizable();
        Locale[] myLocales = l.getAvailableLocales();
        Locale[] vmLocales = Locale.getAvailableLocales();
        assertEquals(myLocales.length, vmLocales.length);
        for(int i = 0; i < myLocales.length; ++i)
        {
            assertEquals(myLocales[i], vmLocales[i]);
        }
    }

    public class TestLocaleEventListener implements LocaleEventListener
    {
        private int numCallbacks = 0;

        public int getNumCallbacks()
        {
            return numCallbacks;
        }

        public void processLocaleEvent(LocaleEvent e)
        {
            ++numCallbacks;
        }
    }

    @Test
    void testSetLocale()
    {
        LocalizableSuper l = new LocalizableSuper();
        Locale newLocale = Locale.KOREA;
        assertDoesNotThrow(() -> l.setBundleLocale(newLocale));
        assertEquals(newLocale, l.getBundleLocale());
    }

    @Test
    void testLocaleListeners()
    {
        TestLocaleEventListener listener1 = new TestLocaleEventListener();
        TestLocaleEventListener listener2 = new TestLocaleEventListener();
        LocalizableSuper l = new LocalizableSuper();
        assertDoesNotThrow(() -> l.setBundleLocale(Locale.getDefault()));
        assertEquals(0, listener1.getNumCallbacks());
        assertEquals(0, listener2.getNumCallbacks());
        l.addLocaleEventListener(listener1);
        assertDoesNotThrow(() -> l.setBundleLocale(Locale.getDefault()));
        assertEquals(1, listener1.getNumCallbacks());
        assertEquals(0, listener2.getNumCallbacks());
        l.addLocaleEventListener(listener2);
        assertDoesNotThrow(() -> l.setBundleLocale(Locale.getDefault()));
        assertEquals(2, listener1.getNumCallbacks());
        assertEquals(1, listener2.getNumCallbacks());
        l.removeLocaleEventListener(listener1);
        assertDoesNotThrow(() -> l.setBundleLocale(Locale.getDefault()));
        assertEquals(2, listener1.getNumCallbacks());
        assertEquals(2, listener2.getNumCallbacks());
        l.removeLocaleEventListener(listener2);
        assertDoesNotThrow(() -> l.setBundleLocale(Locale.getDefault()));
        assertEquals(2, listener1.getNumCallbacks());
        assertEquals(2, listener2.getNumCallbacks());
    }

    @Test
    public void testRunningInModuleMode()
    {
        Module module = this.getClass().getModule();

        if (module.isNamed()) {
            System.out.println(" Running in MODULE mode");
            System.out.println(" Module name: " + module.getName());
        } else {
            System.out.println(" Running in CLASSPATH mode");
            System.out.println(" Module: unnamed");
        }
    }

    @Test
    public void debugModulePathSetup() {
        System.out.println("=== MODULE DEBUG INFO ===");
        Module module = this.getClass().getModule();
        System.out.println("Module isNamed: " + module.isNamed());
        System.out.println("Module name: " + module.getName());
        System.out.println("Module descriptor: " + module.getDescriptor());
        System.out.println("=========================");
        System.out.println("jdk.module.path: " + System.getProperty("jdk.module.path"));
        System.out.println("java.class.path: " + System.getProperty("java.class.path"));
        System.out.println("=========================");
    }
}
