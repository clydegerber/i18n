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

package dev.javai18n.core;

import static java.lang.System.LoggerFinder.getLoggerFinder;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.function.Supplier;

/**
 * A System.Logger that is Localizable.
 */
public class LocalizableLogger implements Localizable, System.Logger
{
    /**
     * Register the callback.
     */
    static
    {
        GetResourceBundleRegistrar.registerGetResourceBundleCallback(ModuleResourceBundleCallback.GET_BUNDLE_CALLBACK);
    }

    /**
     * The LocalizationDelegate to which Localizable methods are delegated.
     */
    protected final LocalizationDelegate delegate;

    /**
     * The System.Logger that does the logging.
     */
    protected volatile System.Logger logger;

    /**
     * A lock to synchronize locale changes with logger replacement.
     */
    protected final Object logLock = new Object();

    /**
     * The name of this logger.
     */
    protected String name;

    /**
     * The locales supported by this logger.
     */
    protected Locale[] availableLocales =
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

    /**
     * The logger used internally by the i18n framework.
     */
    public static final LocalizableLogger I18N_LOGGER = createLocalizableLogger("dev.javai18n.core");

    /**
     * A factory method that returns a LocalizableLogger for the specified name in the default Locale.
     * @param name the name of the logger.
     * @return A LocalizableLogger for the specified name in the default Locale.
     */
    public static LocalizableLogger createLocalizableLogger(String name)
    {
        LocalizableLogger logger = new LocalizableLogger(name);
        logger.logger = getLoggerFinder().getLocalizedLogger(
                name, logger.getResourceBundle(), logger.getClass().getModule());
        return logger;
    }

    /**
     * Constructs a new LocalizableLogger for the specified name with the default Locale.
     * @param name the name of the logger.
     */
    private LocalizableLogger(String name)
    {
        this.name = name;
        delegate = new LocalizationDelegate(this);
    }

    /**
     * Returns the System.Logger that is actually doing the logging.
     * @return The System.Logger that is actually doing the logging.
     */
    public System.Logger getLogger()
    {
        return logger;
    }

    /**
     * Get the current Locale for ResourceBundles provided by the object.
     *
     * @return The Locale for ResourceBundles provided by the object.
     */
    @Override
    public Locale getBundleLocale()
    {
        return delegate.getBundleLocale();
    }

    /**
     * Set the current Locale for ResourceBundles provided by the object.
     *
     * @param locale The intended Locale for ResourceBundles provided by the object.
     * @throws dev.javai18n.core.NoCallbackRegisteredForModuleException if no callback has been registered for the module.
     */
    @Override
    public void setBundleLocale(Locale locale)
    {
        synchronized (logLock)
        {
            delegate.setBundleLocale(locale);
            logger = getLoggerFinder().getLocalizedLogger(name, getResourceBundle(), this.getClass().getModule());
        }
    }

    /**
     * Returns the Locales supported by the object.
     *
     * @return The Locales supported by the object.
     */
    @Override
    public Locale[] getAvailableLocales()
    {
        return availableLocales.clone();
    }

    /**
     * Returns the ResourceBundle for the object according to its locale.
     *
     * @return The ResourceBundle for the object according to its locale.
     * @throws dev.javai18n.core.NoCallbackRegisteredForModuleException if no callback has been registered for the module.
     */
    @Override
    public ResourceBundle getResourceBundle()
    {
        return delegate.getResourceBundle();
    }

    /**
     * Register LocaleEventListeners.
     *
     * @param listener A LocaleEventListener that is to be notified when
     *        the Localizable object updates its Locale.
     */

    @Override
    public void addLocaleEventListener(LocaleEventListener listener)
    {
        delegate.addLocaleEventListener(listener);
    }

    /**
     * Unregister LocaleEventListeners.
     *
     * @param listener A LocaleEventListener that is no longer to be notified
     *                 when the Localizable object updates its Locale.
     */
    @Override
    public void removeLocaleEventListener(LocaleEventListener listener)
    {
        delegate.removeLocaleEventListener(listener);
    }

    /**
     * Returns the name of this logger.
     * @return the name of this logger.
     */
    @Override
    public String getName()
    {
        return name;
    }

    /**
     * Checks if a message of the given level would be logged by this logger.
     * @param level the log message level.
     * @return true if the given log message level is currently being logged.
     * @throws NullPointerException - if level is null.
     */
    @Override
    public boolean isLoggable(Level level)
    {
        return logger.isLoggable(level);
    }

    /**
     * Logs a message.
     * @param level the log message level.
     * @param msg The key in the message catalog.
     * @throws NullPointerException - if level is null.
     */
    @Override
    public void log(Level level, String msg)
    {
        logger.log(level, msg);
    }

    /**
     * Logs a lazily supplied message. If the logger is currently
     * enabled for the given log message level then a message
     * is logged that is the result produced by the given supplier
     * function. Otherwise, the supplier is not operated on.
     * @param level the log message level.
     * @param msgSupplier a supplier function that produces a message.
     * @throws NullPointerException if level is null, or msgSupplier is null.
     */
    @Override
    public void log(Level level, Supplier<String> msgSupplier)
    {
        logger.log(level, msgSupplier);
    }

    /**
     * Logs a message produced from the given object. If the logger
     * is currently enabled for the given log message level then a
     * message is logged that, by default, is the result produced
     * from calling toString on the given object. Otherwise, the object is not operated on.
     * @param level the log message level.
     * @param obj the object to log.
     * @throws NullPointerException - if level is null, or obj is null.
     */
    @Override
    public void log(Level level, Object obj)
    {
        logger.log(level, obj);
    }

    /**
     * Logs a message associated with a given throwable.
     * @param level the log message level.
     * @param msg the key in the message catalog.
     * @param thrown a Throwable associated with the log message; can be null.
     * @throws NullPointerException - if level is null.
     */
    @Override
    public void log(Level level, String msg, Throwable thrown)
    {
        logger.log(level, msg, thrown);
    }

    /**
     * Logs a lazily supplied message associated with a given throwable. If the
     * logger is currently enabled for the given log message level then a message
     * is logged that is the result produced by the given supplier function.
     * Otherwise, the supplier is not operated on.
     * @param level one of the log message level identifiers.
     * @param msgSupplier a supplier function that produces a message.
     * @param thrown a Throwable associated with log message; can be null.
     * @throws NullPointerException - if level is null.
     */
    @Override
    public void log(Level level, Supplier<String> msgSupplier, Throwable thrown)
    {
        logger.log(level, msgSupplier, thrown);
    }

    /**
     * Logs a message with an optional list of parameters.
     * @param level one of the log message level identifiers.
     * @param format the key in the message catalog.
     * @param params an optional list of parameters to the message (may be none).
     * @throws NullPointerException - if level is null.
     */
    @Override
    public void log(Level level, String format, Object... params)
    {
        logger.log(level, format, params);
    }

    /**
     * Logs a localized message associated with a given throwable. If the given
     * resource bundle is non-null, the msg string is localized using the given
     * resource bundle. Otherwise the msg string is not localized.
     * @param level the log message level.
     * @param bundle a resource bundle to localize msg; can be null.
     * @param msg the string message (or a key in the message catalog, if bundle is not null); can be null.
     * @param thrown a Throwable associated with the log message; can be null.
     * @throws NullPointerException - if level is null.
     */
    @Override
    public void log(Level level, ResourceBundle bundle, String msg, Throwable thrown)
    {
        logger.log(level, bundle, msg, thrown);
    }

    /**
     * Logs a message with resource bundle and an optional list of parameters.
     * If the given resource bundle is non-null, the format string is localized
     * using the given resource bundle. Otherwise the format string is not localized.
     * @param level the log message level.
     * @param bundle a resource bundle to localize format; can be null.
     * @param format the string message format in MessageFormat format, (or a key in the message catalog if bundle is not null); can be null.
     * @param params an optional list of parameters to the message (may be none).
     * @throws NullPointerException - if level is null.
     */
    @Override
    public void log(Level level, ResourceBundle bundle, String format, Object... params)
    {
        logger.log(level, bundle, format, params);
    }
}
