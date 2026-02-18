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

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * A base implementation of the Localizable interface that uses a LocalizationDelegate for Localizable services.
 *
 * Extending classes will need to ensure that a callback is registered for the implementing module. The suggested
 * implementation is a static block in each direct subclass:
 *
 * static
 * {
 *     GetResourceBundleRegistrar.registerGetResourceBundleCallback(callback);
 * }
 *
 * The callback argument must specify an implementation of the GetResourceBundleCallback interface that is a
 * singleton instance in the module.
 *
 * Extending classes of the subclass that reside in different modules than the original extender will likewise have
 * to ensure a callback is registered for their module.
 */
public class LocalizableImpl implements Localizable
{
    /**
     * A LocalizationDelegate to which the Localizable functions will be delegated.
     */
    protected final LocalizationDelegate delegate;

    /**
     * Get the current Locale for ResourceBundles provided by the object.
     *
     * @return The Locale for the object.
     */
    @Override
    public Locale getBundleLocale() {return delegate.getBundleLocale();}

    /**
     * Set the current Locale for ResourceBundles provided by the object.
     *
     * @param locale The intended Locale for ResourceBundles provided by the object.
     */
    @Override
    public void setBundleLocale(Locale locale) throws NoCallbackRegisteredForModuleException
    {
        delegate.setBundleLocale(locale);
    }

    /**
     * Returns the Locales supported by the object.
     *
     * @return The Locales supported by the object.
     */
    @Override
    public Locale[] getAvailableLocales()
    {
        return delegate.getAvailableLocales();
    }

    /**
     * Returns the ResourceBundle for the object according to its locale.
     *
     * @return The ResourceBundle for the object according to its locale.
     */
    @Override
    public ResourceBundle getResourceBundle() throws NoCallbackRegisteredForModuleException
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
     * Construct a LocalizableImpl.
     */
    public LocalizableImpl()
    {
        this.delegate = new LocalizationDelegate(this);
    }
}
