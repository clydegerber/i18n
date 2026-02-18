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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import dev.javai18n.core.Localizable.LocaleEvent;
import dev.javai18n.core.Localizable.LocaleEventListener;
import dev.javai18n.core.LocalizableLogger;
import java.util.Locale;
import java.util.ResourceBundle;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for LocalizableLogger.
 */
public class TestLocalizableLogger
{
    // --- Factory and identity ---

    @Test
    void testCreateLocalizableLogger()
    {
        LocalizableLogger logger = LocalizableLogger.createLocalizableLogger("test.logger");
        assertNotNull(logger);
        assertEquals("test.logger", logger.getName());
    }

    @Test
    void testI18nLoggerExists()
    {
        assertNotNull(LocalizableLogger.I18N_LOGGER);
        assertEquals("dev.javai18n.core", LocalizableLogger.I18N_LOGGER.getName());
    }

    @Test
    void testGetLogger()
    {
        LocalizableLogger logger = LocalizableLogger.createLocalizableLogger("test.getlogger");
        assertNotNull(logger.getLogger());
    }

    // --- Localizable interface ---

    @Test
    void testGetBundleLocale()
    {
        LocalizableLogger logger = LocalizableLogger.createLocalizableLogger("test.locale");
        assertEquals(Locale.getDefault(), logger.getBundleLocale());
    }

    @Test
    void testSetBundleLocale()
    {
        LocalizableLogger logger = LocalizableLogger.createLocalizableLogger("test.setlocale");
        Locale original = logger.getBundleLocale();
        assertDoesNotThrow(() -> logger.setBundleLocale(Locale.FRENCH));
        assertEquals(Locale.FRENCH, logger.getBundleLocale());
        assertDoesNotThrow(() -> logger.setBundleLocale(original));
    }

    @Test
    void testGetAvailableLocales()
    {
        LocalizableLogger logger = LocalizableLogger.createLocalizableLogger("test.availlocales");
        Locale[] expected =
        {
            Locale.ROOT,
            Locale.ENGLISH,
            Locale.CHINESE,
            Locale.FRENCH,
            Locale.GERMAN,
            Locale.ITALIAN,
            Locale.JAPANESE,
            Locale.KOREAN
        };
        assertArrayEquals(expected, logger.getAvailableLocales());
    }

    @Test
    void testGetResourceBundle()
    {
        LocalizableLogger logger = LocalizableLogger.createLocalizableLogger("test.bundle");
        assertNotNull(logger.getResourceBundle());
    }

    @Test
    void testSetBundleLocaleUpdatesResourceBundle()
    {
        LocalizableLogger logger = LocalizableLogger.createLocalizableLogger("test.bundleupdate");
        Locale original = logger.getBundleLocale();
        assertDoesNotThrow(() -> logger.setBundleLocale(Locale.FRENCH));
        ResourceBundle rb = logger.getResourceBundle();
        assertNotNull(rb);
        assertEquals(Locale.FRENCH, rb.getLocale());
        assertDoesNotThrow(() -> logger.setBundleLocale(original));
    }

    // --- Locale event listeners ---

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
    void testLocaleEventListeners()
    {
        LocalizableLogger logger = LocalizableLogger.createLocalizableLogger("test.listeners");
        TestLocaleEventListener listener1 = new TestLocaleEventListener();
        TestLocaleEventListener listener2 = new TestLocaleEventListener();

        // No listeners yet
        assertDoesNotThrow(() -> logger.setBundleLocale(Locale.getDefault()));
        assertEquals(0, listener1.getNumCallbacks());
        assertEquals(0, listener2.getNumCallbacks());

        // Add first listener
        logger.addLocaleEventListener(listener1);
        assertDoesNotThrow(() -> logger.setBundleLocale(Locale.getDefault()));
        assertEquals(1, listener1.getNumCallbacks());
        assertEquals(0, listener2.getNumCallbacks());

        // Add second listener
        logger.addLocaleEventListener(listener2);
        assertDoesNotThrow(() -> logger.setBundleLocale(Locale.getDefault()));
        assertEquals(2, listener1.getNumCallbacks());
        assertEquals(1, listener2.getNumCallbacks());

        // Remove first listener
        logger.removeLocaleEventListener(listener1);
        assertDoesNotThrow(() -> logger.setBundleLocale(Locale.getDefault()));
        assertEquals(2, listener1.getNumCallbacks());
        assertEquals(2, listener2.getNumCallbacks());

        // Remove second listener
        logger.removeLocaleEventListener(listener2);
        assertDoesNotThrow(() -> logger.setBundleLocale(Locale.getDefault()));
        assertEquals(2, listener1.getNumCallbacks());
        assertEquals(2, listener2.getNumCallbacks());
    }

    // --- System.Logger delegation ---

    @Test
    void testLogString()
    {
        LocalizableLogger logger = LocalizableLogger.createLocalizableLogger("test.log.string");
        assertDoesNotThrow(() -> logger.log(System.Logger.Level.INFO, "msg"));
    }

    @Test
    void testLogSupplier()
    {
        LocalizableLogger logger = LocalizableLogger.createLocalizableLogger("test.log.supplier");
        assertDoesNotThrow(() -> logger.log(System.Logger.Level.INFO, () -> "msg"));
    }

    @Test
    void testLogObject()
    {
        LocalizableLogger logger = LocalizableLogger.createLocalizableLogger("test.log.object");
        assertDoesNotThrow(() -> logger.log(System.Logger.Level.INFO, (Object) "msg"));
    }

    @Test
    void testLogStringThrowable()
    {
        LocalizableLogger logger = LocalizableLogger.createLocalizableLogger("test.log.stringthrow");
        assertDoesNotThrow(() -> logger.log(System.Logger.Level.INFO, "msg", new RuntimeException()));
    }

    @Test
    void testLogSupplierThrowable()
    {
        LocalizableLogger logger = LocalizableLogger.createLocalizableLogger("test.log.supplierthrow");
        assertDoesNotThrow(() -> logger.log(System.Logger.Level.INFO, () -> "msg", new RuntimeException()));
    }

    @Test
    void testLogFormatParams()
    {
        LocalizableLogger logger = LocalizableLogger.createLocalizableLogger("test.log.format");
        assertDoesNotThrow(() -> logger.log(System.Logger.Level.INFO, "format {0}", "param"));
    }

    @Test
    void testLogBundleStringThrowable()
    {
        LocalizableLogger logger = LocalizableLogger.createLocalizableLogger("test.log.bundlethrow");
        ResourceBundle bundle = logger.getResourceBundle();
        assertDoesNotThrow(() -> logger.log(System.Logger.Level.INFO, bundle, "key", new RuntimeException()));
    }

    @Test
    void testLogBundleFormatParams()
    {
        LocalizableLogger logger = LocalizableLogger.createLocalizableLogger("test.log.bundleformat");
        ResourceBundle bundle = logger.getResourceBundle();
        assertDoesNotThrow(() -> logger.log(System.Logger.Level.INFO, bundle, "format {0}", "param"));
    }

    @Test
    void testIsLoggable()
    {
        LocalizableLogger logger = LocalizableLogger.createLocalizableLogger("test.log.loggable");
        // isLoggable returns a boolean without throwing; actual value depends on JUL config
        assertDoesNotThrow(() -> logger.isLoggable(System.Logger.Level.ERROR));
    }
}
