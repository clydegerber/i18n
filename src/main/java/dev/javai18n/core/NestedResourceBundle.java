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

import static dev.javai18n.core.LocalizableLogger.I18N_LOGGER;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * A ResourceBundle that allows for nesting other ResourceBundles in a hierarchy and searching for resources
 * in the next higher level of the hierarchy when the resource is not found at a lower level. This allows, for example,
 * a resource hierarchy to be established parallel to a class hierarchy.
 */
public class NestedResourceBundle extends ResourceBundle
{
    /**
     * Return the parent bundle of this bundle.
     *
     * @return The parent bundle of this bundle.
     */
    public NestedResourceBundle getParent() {return (NestedResourceBundle) parent;};

    /**
     * The ResourceBundle that actually contains the resources.
     */
    protected ResourceBundle delegate;

    /**
     * Returns the delegate ResourceBundle.
     *
     * @return The ResourceBundle that this NestedResourceBundle delegates to.
     */
    public ResourceBundle getDelegate()
    {
        return delegate;
    }

    /**
     * The NestedResourceBundle for the next higher level in the nesting hierarchy.
     */
    protected NestedResourceBundle superBundle;

    /**
     * Returns the superBundle
     *
     * @return The bundle for the next higher level in the nesting hierarchy.
     */
    public NestedResourceBundle getSuperBundle()
    {
        return superBundle;
    }

    /**
     * The base name of the bundle
     */
    protected String baseBundleName;

    /**
     * Construct a NestedResourceBundle from the specified delegate bundle, superBundle and baseBundleName.
     * @param delegate        The standard delegate to which handleGetObject() calls will be initially directed.
     * @param superBundle     The bundle for the next level up in the hierarchy which will be searched if a resource
     *                        is not located in the delegate bundle.
     * @param baseBundleName  The baseName assigned to the bundle.
     */
    public NestedResourceBundle(ResourceBundle delegate, NestedResourceBundle superBundle, String baseBundleName)
    {
        this.delegate = delegate;
        this.superBundle = superBundle;
        this.baseBundleName = baseBundleName;
    }

    /**
     * Overrides the getBaseBundleName of ResourceBundle to provide the name supplied to the constructor.
     * @return The baseBundleName for the NestedResourceBundle.
     */
    @Override
    public String getBaseBundleName()
    {
        return baseBundleName;
    }

    /**
     * Gets an object for the given key from this ResourceBundle.
     * Returns null if this ResourceBundle  does not contain an
     * object for the given key. Calls the getObject() method in
     * the delegate resource bundle and if not found, moves up the
     * parent chain, calling the getObject() method of the delegate
     * at every level. If not found in parent chain, calls the
     * getObject() method of the resource bundle for the next higher
     * level in the nesting hierarchy.
     *
     * @param key The key for the desired object
     *
     * @return The object for the given key, or null
     *
     * @throws NullPointerException if key is null
     */
    @Override
    protected Object handleGetObject(String key)
    {
        if (null == key)
        {
            throw new NullPointerException("key is null");
        }
        if (null != delegate && delegate.containsKey(key))
        {
            return delegate.getObject(key);
        }
        NestedResourceBundle searchBundle = getParent();
        while (null != searchBundle)
        {
            ResourceBundle parentDelegate = searchBundle.getDelegate();
            if (null != parentDelegate && parentDelegate.containsKey(key))
            {
                return parentDelegate.getObject(key);
            }
            searchBundle = searchBundle.getParent();
        }
        if (null != superBundle)
        {
            return superBundle.getObject(key);
        }
        throw new MissingResourceException("Can't find resource for bundle "
                + this.getClass().getName() + ", key " + key,
                this.getClass().getName(), key);
    }

    /**
     * Returns an enumeration of the keys.
     *
     * @return an Enumeration of the keys contained in this NestedResourceBundle and its parent
     *         as well as the higher levels in the nesting hierarchy.
     */
    @Override
    public Enumeration<String> getKeys()
    {
        Set<String> keys = handleKeySet();
        if (null != parent)
        {
            Enumeration<String> e = parent.getKeys();
            while (e.hasMoreElements())
            {
                keys.add(e.nextElement());
            }
        }
        if (null != superBundle)
        {
            Enumeration<String> e = superBundle.getKeys();
            while (e.hasMoreElements())
            {
                keys.add(e.nextElement());
            }
        }
        return Collections.enumeration(keys);
    }

    /**
     * Returns a Set of all keys contained in this NestedResourceBundle and its parent bundles.
     *
     * @return a Set of all keys contained in this NestedResourceBundle and its parent bundles.
     */
    @Override
    protected Set<String> handleKeySet()
    {
        Set<String> keys = new HashSet<>();
        if (null != delegate)
        {
            Enumeration<String> e = delegate.getKeys();
            while (e.hasMoreElements())
            {
                keys.add(e.nextElement());
            }
        }
        return keys;
    }

    /**
     * The locale of the delegate.
     * @return The locale of the delegate ResourceBundle.
     */
    @Override
    public Locale getLocale()
    {
        if (null != delegate)
        {
            return delegate.getLocale();
        }
        else if (null != superBundle)
        {
            return superBundle.getLocale();
        }
        return Locale.ROOT;
    }

    /**
     * Dump the contents of the NestedResourceBundle to the system log.
     */
    public void dump()
    {
        NestedResourceBundle rb = this;
        I18N_LOGGER.log(System.Logger.Level.DEBUG, "nested.bundle.dump.start");
        while (null != rb)
        {
            I18N_LOGGER.log(System.Logger.Level.DEBUG, "base.bundle.name.log.line", rb.getBaseBundleName());
            ResourceBundle delegate = rb.getDelegate();
            if (null == delegate)
            {
                I18N_LOGGER.log(System.Logger.Level.DEBUG, "delegate.bundle.name.log.line", "null");
                I18N_LOGGER.log(System.Logger.Level.DEBUG, "delegate.type.name.log.line", "null");
            }
            else
            {
                I18N_LOGGER.log(System.Logger.Level.DEBUG, "delegate.bundle.name.log.line", delegate.getBaseBundleName());
                I18N_LOGGER.log(System.Logger.Level.DEBUG, "delegate.type.name.log.line", delegate.getClass().getName());
            }
            rb = rb.getSuperBundle();
        }
        I18N_LOGGER.log(System.Logger.Level.DEBUG, "nested.bundle.dump.end");
    }
}
