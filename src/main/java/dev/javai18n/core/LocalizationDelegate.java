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
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import dev.javai18n.core.Localizable.LocaleEvent;
import dev.javai18n.core.Localizable.LocaleEventListener;
import static dev.javai18n.core.LocalizableLogger.I18N_LOGGER;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

/**
 * A Class that objects that implement the Localizable interface can embed and to which they can delegate
 * the required methods of that interface. Consumers provide the Localizable object that is embedding this
 * delegate in the constructor. This object returns a NestedResourceBundle in the getResourceBundle() method
 * that uses the class hierarchy of the consumer to provide polymorphic resource lookup.
 *
 * Classes that embed this object will need to ensure that a callback is registered for the implementing module.
 * The suggested implementation is a static block in each class that embeds this object:
 *
 * static
 * {
 *     GetResourceBundleRegistrar.registerGetResourceBundleCallback(callback);
 * }
 *
 * The callback argument must specify an implementation of the GetResourceBundleCallback interface that is a
 * singleton instance in the module.
 *
 * Extending classes of the class that embeds this object that reside in different modules will likewise have
 * to ensure a callback is registered for their module.
 */
public class LocalizationDelegate
{
    /**
     * The class for which this delegate handles Localizable functionality. ResourceBundles will be searched using
     * the name of the class plus the suffix "Bundle" and a NestedResourceBundle will be used to support polymorphic
     * localization.
     */
    protected Localizable localizedObject;

    /**
     * The locale for the Localizable object.
     */
    protected Locale locale = Locale.getDefault();

    /**
     * The ResourceBundle for this object's current locale.
     */
    protected ResourceBundle rb;

    /**
     * Get the current Locale for ResourceBundles provided by the object.
     *
     * @return The Locale for ResourceBundles provided by the object.
     */
    public Locale getBundleLocale()
    {
        synchronized (lock) { return locale; }
    }

    /**
     * Set the current Locale for ResourceBundles provided by the object.
     *
     * @param locale The intended Locale for ResourceBundles provided by the object.
     * @throws dev.javai18n.core.NoCallbackRegisteredForModuleException if a callback has not been registered for the
     *         module.
     */
    public void setBundleLocale(Locale locale)
    {
        LocaleEventListener[] snapshot;
        synchronized (lock)
        {
            this.locale = locale;
            rb = getNestedResourceBundle();
            snapshot = listeners.toArray(LocaleEventListener[]::new);
        }
        LocaleEvent event = new LocaleEvent(localizedObject);
        for (LocaleEventListener listener : snapshot)
        {
            listener.processLocaleEvent(event);
        }
    }

    /**
     * Returns the Locales supported by the object.
     *
     * @return The Locales supported by the object.
     */
    public Locale[] getAvailableLocales()
    {
        return Locale.getAvailableLocales();
    }

    /**
     * Returns the ResourceBundle for the object according to its locale.
     *
     * @return The ResourceBundle for the object according to its locale.
     * @throws dev.javai18n.core.NoCallbackRegisteredForModuleException if a callback has not been registered for the
     *         module.
     */
    public ResourceBundle getResourceBundle()
    {
        synchronized (lock)
        {
            if (null == rb) rb = getNestedResourceBundle();
            return rb;
        }
    }

    /**
     * Get a NestedResourceBundle for the localizedObject and its current locale. Subclasses in different modules
     * must ensure that a GetResourceBundleCallback from their module is registered with the GetResourceBundleRegistrar.
     * @return A NestedResourceBundle associated with this object's class and locale.
     * @throws dev.javai18n.core.NoCallbackRegisteredForModuleException if no callback has been registered for the module.
     */
    protected NestedResourceBundle getNestedResourceBundle()
    {
        NestedResourceBundle bundle = null;
        Class<?> clazz = null;
        for (Class<?> c : classHierarchy)
        {
            ResourceBundle delegate = null;
            clazz = c;
            Module module = clazz.getModule();
            GetResourceBundleCallback caller = GetResourceBundleRegistrar.getGetResourceBundleCallback(module);
            if (null == caller)
            {
                NoCallbackRegisteredForModuleException ex = new NoCallbackRegisteredForModuleException(module.getName());
                if (null != I18N_LOGGER) // Defer logging until initialization is complete
                {
                    I18N_LOGGER.log(System.Logger.Level.ERROR, "no.callback.for.module", module.getName(), ex);
                }
                throw ex;
            }
            try
            {
                // One might expect the following to work and eliminate the need for the callback:
                // delegate = ResourceBundle.getBundle(clazz.getName(), getLocale(), module);
                // That was not my experience, despite trying various combinations of "uses" and "opens" statements
                // in module-info.java files and --add-opens and --add-reads options to the command line.
                delegate = caller.getResourceBundle(clazz.getName(), getBundleLocale());
            }
            catch (MissingResourceException e)
            {
                if (null != I18N_LOGGER) // Defer logging until initialization is complete
                {
                    I18N_LOGGER.log(System.Logger.Level.DEBUG, "missing.resource.loading.nested.bundle", clazz.getName(),
                        getBundleLocale().getDisplayName(), e);
                }
            }
            if (null != delegate)
            {
                bundle = new NestedResourceBundle(delegate, bundle, clazz.getName());
            }
        }
        if (null == bundle) throw new MissingResourceException("Unable to locate ResourceBundle for " + clazz.getName(),
                clazz.getName(), null);
        return bundle;
    }

    /**
     * Updates the ResourceBundle for the object based on its current locale.
     * @throws dev.javai18n.core.NoCallbackRegisteredForModuleException if a callback has not been registered for the
     *         module.
     */
    public void updateResourceBundle()
    {
        synchronized (lock)
        {
            rb = getNestedResourceBundle();
        }
    }

    /**
     * Lock guarding {@code locale}, {@code rb}, and {@code listeners}.
     */
    private final Object lock = new Object();

    /**
     * The set of LocaleEventListeners.
     */
    protected final Set<LocaleEventListener> listeners = Collections.newSetFromMap(new IdentityHashMap<>());

    /**
     * Register LocaleEventListeners.
     *
     * @param listener A LocaleEventListener that is to be notified when
     *        the Localizable object updates its Locale.
     */
    public void addLocaleEventListener(LocaleEventListener listener)
    {
        synchronized (lock)
        {
            listeners.add(listener);
        }
    }

    /**
     * Unregister LocaleEventListeners.
     *
     * @param listener A LocaleEventListener that is no longer to be notified
     *                 when the Localizable object updates its Locale.
     */
    public void removeLocaleEventListener(LocaleEventListener listener)
    {
        synchronized (lock)
        {
            listeners.remove(listener);
        }
    }

    /**
     * The class hierarchy from the concrete class up to (but not including) the i18n module classes.
     */
    private final List<Class<?>> classHierarchy;

    /**
     * Construct a LocalizationDelegate.
     * @param localizedObject A Localizable for which this delegate handles Localizable functionality.
     */
    public LocalizationDelegate(Localizable localizedObject)
    {
        this.localizedObject = localizedObject;
        this.classHierarchy = computeClassHierarchy();
    }

    private List<Class<?>> computeClassHierarchy()
    {
        ArrayList<Class<?>> hierarchy = new ArrayList<>();
        Class<?> clazz = localizedObject.getClass();
        Module i18nModule = Localizable.class.getModule();
        while (Localizable.class.isAssignableFrom(clazz))
        {
            hierarchy.add(clazz);
            clazz = clazz.getSuperclass();
            Module clazzModule = clazz.getModule();
            if (clazzModule.isNamed() && clazzModule.equals(i18nModule)) break;
        }
        // Reverse so base class is processed first
        Collections.reverse(hierarchy);
        return Collections.unmodifiableList(hierarchy);
    }
}
