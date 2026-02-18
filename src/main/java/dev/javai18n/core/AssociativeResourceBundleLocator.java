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
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import javax.xml.stream.XMLStreamException;

/**
 * Locates a ResourceBundle for a given baseName and locale by appending a suffix to the baseName and searching
 * for ResourceBundles that match the composed name. Bundles are searched in the following order:
 *
 * 1. Java classes for the composed name that are assignable to ResourceBundle;
 * 2. JSON files matching the composed name.
 * 3. XML files matching the composed name. XML files must conform to the dtd contained in this distribution at
 *    dev/javai18n/core/properties.dtd, which is a superset of the DTD located at
 *    http://java.sun.com/dtd/properties.dtd (so XML files that conform to it may also be used).
 * 4. Properties files matching the composed name.
 */
public class AssociativeResourceBundleLocator
{
    /**
     * The String to append to the baseName when searching for ResourceBundles.
     */
    private String suffix;

    /**
     * The supported ResourceBundle formats.
     */
    private static final List<String> FORMATS = List.of("java.class", "json", "xml", "java.properties");

    /**
     * The supported ResourceBundle formats.
     */
    protected List<String> formats = FORMATS;

    /**
     * Required for its toBundleName() and toResourceName methods.
     */
    private static final class LocatorCtrl extends ResourceBundle.Control {};

    private static final LocatorCtrl ctrl = new LocatorCtrl();

    /**
     * Construct a new ResourceBundleLocator for the given suffix.
     *
     * @param suffix The string that will be appended to the baseName to locate ResourceBundles.
     * @throws NullPointerException if suffix is null
     * @throws IllegalArgumentException if suffix is empty
     */
    public AssociativeResourceBundleLocator(String suffix)
    {
        if (null == suffix) throw new NullPointerException("suffix is null");
        this.suffix = suffix;
    }

    /**
     * Returns a List of Strings containing formats available to locate ResourceBundles for this object's
     * baseName.
     *
     * @return A List of String objects
     */
    public List<String> getFormats()
    {
        return formats;
    }

    /**
     * Locate a class-based ResourceBundle for the given bundleName, locale,
     * format and loader.
     *
     * @param bundleName
     *        the name of the ResourceBundle
     * @param locale
     *        the locale for which the ResourceBundle should be instantiated
     * @param streamLoader
     *        the {@code ResourceStreamLoader} to use to load the bundle class.
     * @return A ResourceBundle instance, or null if none could be found.
     * @throws    NullPointerException
     *        if {@code bundleName}, {@code locale}, or
     *        {@code streamLoader} is {@code null}
     * @throws    ClassCastException
     *        if the loaded class cannot be cast to {@code ResourceBundle}
     * @throws IllegalAccessException
     *        if the class or its nullary constructor is not
     *        accessible.
     * @throws InstantiationException
     *        if the initialization provoked by this method fails.
     * @throws IOException
     *        if an error occurred when reading resources using
     *        any I/O operations
     */
    protected ResourceBundle getClassBundle(String bundleName,
                                    Locale locale,
                                    ResourceStreamLoader streamLoader)
                            throws IllegalAccessException,
                                   InstantiationException,
                                   IOException
    {
        if (null == bundleName) throw new NullPointerException("baseName is null");
        if (null == locale) throw new NullPointerException("locale is null");
        if (null == streamLoader) throw new NullPointerException("streamLoader is null");
        ClassLoader loader = streamLoader.getResourceClassLoader();
        try
        {
            Class<?> c = null;
            try
            {
                c = loader.loadClass(bundleName);
            }
            catch (ClassNotFoundException e)
            {
                // Expected if bundle is not implemented in a Java Class
                return null;
            }
            if (!ResourceBundle.class.isAssignableFrom(c))
            {
                throw new ClassCastException(c.getName() + " cannot be cast to ResourceBundle");
            }
            @SuppressWarnings("unchecked")
            Class<ResourceBundle> bundleClass = (Class<ResourceBundle>)c;

            Constructor<ResourceBundle> ctor = bundleClass.getDeclaredConstructor();
            if (!Modifier.isPublic(ctor.getModifiers()))
            {
                throw new IllegalAccessException("no-arg constructor in " +
                    bundleClass.getName() + " is not publicly accessible.");
            }
            return ctor.newInstance((Object[]) null);
        }
        catch (InvocationTargetException e)
        {
            InstantiationException ie = new InstantiationException(
                "InvocationTargetException raised invoking default constructor for " +
                bundleName + " message: " + e.getMessage());
            ie.initCause(e);
            throw ie;
        }
        catch (NoSuchMethodException e)
        {
            InstantiationException ie = new InstantiationException(
                "public no-arg constructor does not exist in " + bundleName);
            ie.initCause(e);
            throw ie;
        }
    }

    /**
     * Locate a JSON-based ResourceBundle for the given bundleName, locale,
     * format and loader.
     *
     * @param bundleName
     *        the name of the ResourceBundle
     * @param locale
     *        the locale for which the ResourceBundle should be instantiated
     * @param streamLoader
     *        the {@code ResourceStreamLoader} to use to read the bundle's JSON file.
     * @return A ResourceBundle instance, or null if none could be found.
     * @throws IllegalAccessException
     *        if the class or its nullary constructor is not
     *        accessible.
     * @throws InstantiationException
     *        if the initialization provoked by this method fails.
     * @throws IOException
     *        if an error occurred when reading resources using
     *        any I/O operations
     */
    protected ResourceBundle getJsonBundle(String bundleName,
                                    Locale locale,
                                    ResourceStreamLoader streamLoader)
                            throws IllegalAccessException,
                                   InstantiationException,
                                   IOException
    {
        if (null == bundleName) throw new NullPointerException("baseName is null");
        if (null == locale) throw new NullPointerException("locale is null");
        if (null == streamLoader) throw new NullPointerException("loader is null");
        String jsonResourceName = ctrl.toResourceName(bundleName, "json");
        try (InputStream stream = streamLoader.getResourceAsStream(jsonResourceName))
        {
            if (stream == null) return null;
            try (BufferedInputStream bis = new BufferedInputStream(stream))
            {
                return new JsonResourceBundle(bis);
            }
        }
    }

    /**
     * Locate an xml-based ResourceBundle for the given bundleName, locale,
     * format and loader.
     *
     * @param bundleName
     *        the name of the ResourceBundle
     * @param locale
     *        the locale for which the ResourceBundle should be instantiated
     * @param streamLoader
     *        the {@code ResourceStreamLoader} to use to read the bundle's xml file.
     * @return A ResourceBundle instance, or null if none could be found.
     * @throws IllegalAccessException
     *        if the class or its nullary constructor is not
     *        accessible.
     * @throws InstantiationException
     *        if the initialization provoked by this method fails.
     * @throws IOException
     *        if an error occurred when reading resources using
     *        any I/O operations
     * @throws javax.xml.stream.XMLStreamException
     *        if the XML input is not well formed
     */
    protected ResourceBundle getXmlBundle(String bundleName,
                                    Locale locale,
                                    ResourceStreamLoader streamLoader)
                            throws IllegalAccessException,
                                   InstantiationException,
                                   IOException,
                                   XMLStreamException
    {
        if (null == bundleName) throw new NullPointerException("baseName is null");
        if (null == locale) throw new NullPointerException("locale is null");
        if (null == streamLoader) throw new NullPointerException("loader is null");
        String xmlResourceName = ctrl.toResourceName(bundleName, "xml");
        try (InputStream stream = streamLoader.getResourceAsStream(xmlResourceName))
        {
            if (stream == null) return null;
            try (BufferedInputStream bis = new BufferedInputStream(stream))
            {
                return new XMLResourceBundle(bis);
            }
        }
    }

    /**
     * Locate a properties-based ResourceBundle for the given bundleName, locale,
     * format and loader.
     *
     * @param bundleName
     *        the name of the ResourceBundle
     * @param locale
     *        the locale for which the ResourceBundle should be instantiated
     * @param streamLoader
     *        the {@code ResourceStreamLoader} to use to read the bundle's properties file.
     * @return A ResourceBundle instance, or null if none could be found.
     * @throws IllegalAccessException
     *        if the class or its nullary constructor is not
     *        accessible.
     * @throws InstantiationException
     *        if the initialization provoked by this method fails.
     * @throws IOException
     *        if an error occurred when reading resources using
     *        any I/O operations
     */
    protected ResourceBundle getPropertiesBundle(String bundleName,
                                    Locale locale,
                                    ResourceStreamLoader streamLoader)
                            throws IllegalAccessException,
                                   InstantiationException,
                                   IOException
    {
        if (null == bundleName) throw new NullPointerException("baseName is null");
        if (null == locale) throw new NullPointerException("locale is null");
        if (null == streamLoader) throw new NullPointerException("streamLoader is null");
        String propertiesResourceName = ctrl.toResourceName(bundleName, "properties");
        try (InputStream stream = streamLoader.getResourceAsStream(propertiesResourceName))
        {
            if (stream == null) return null;
            try (BufferedInputStream bis = new BufferedInputStream(stream))
            {
                return new PropertyResourceBundle(bis);
            }
        }
    }

    /**
     * Locate a ResourceBundle for the given baseName plus this AssociativeResourceBundleLocator's suffix for the given
     * locale, format and loader.
     *
     * @param baseName
     *        the base bundle name of the resource bundle, to which the suffix will be appended to locate the
     *        ResourceBundle
     * @param locale
     *        the locale for which the resource bundle should be instantiated
     * @param format
     *        the resource bundle format to be loaded (must be one of the strings in the formats object)
     * @param loader
     *        the {@code ClassLoader} to use to load the bundle and delegate bundles.
     * @param reload
     *        the flag to indicate bundle and delegate bundle reloading; {@code true}
     *        if reloading an expired resource bundle,
     *        {@code false} otherwise
     * @return The ResourceBundle instance, or null if none could be found.
     * @throws IllegalArgumentException
     *        if {@code format} is unknown, or if the resource
     *        found for the given parameters contains malformed data.
     * @throws IllegalAccessException
     *        if the class or its nullary constructor is not
     *        accessible.
     * @throws InstantiationException
     *        if the initialization provoked by this method fails.
     * @throws javax.xml.stream.XMLStreamException
     *        if an XML document that is not well formed is found for the baseName and locale
     * @throws IOException
     *        if an error occurred when reading resources using
     *        any I/O operations
     */
    public ResourceBundle newBundle(String baseName,
                                    Locale locale,
                                    String format,
                                    ResourceStreamLoader loader,
                                    boolean reload)
                            throws IllegalAccessException,
                                   InstantiationException,
                                   IOException,
                                   XMLStreamException
    {
        if (null == baseName) throw new NullPointerException("baseName is null");
        if (null == locale) throw new NullPointerException("locale is null");
        if (null == format) throw new NullPointerException("format is null");
        if (null == loader) throw new NullPointerException("loader is null");
        if (null == loader.getResourceClassLoader()) throw new NullPointerException("loader.getResourceClassLoader returns null");
        String bundleBaseName = baseName + suffix;
        String bundleName = ctrl.toBundleName(bundleBaseName, locale);
        if ("java.class".equals(format)) return getClassBundle(bundleName, locale, loader);
        if ("json".equals(format)) return getJsonBundle(bundleName,locale, loader);
        if ("xml".equals(format))return getXmlBundle(bundleName, locale, loader);
        if ("java.properties".equals(format))return getPropertiesBundle(bundleName, locale, loader);
        throw new IllegalArgumentException("unknown format: " + format);
    }

    /**
     * Locate a ResourceBundle for the given baseName plus this AssociativeResourceBundleLocator's suffix for the given
     * locale and loader.
     *
     * @param baseName The baseName to which the suffix will be appended prior to searching for the bundle.
     * @param locale   The desired locale for the ResourceBundle.
     * @param loader   The ResourceStreamLoader to be used to load ResourceBundles.
     * @return A ResourceBundle for the given baseName + suffix and locale or null.
     */
    public ResourceBundle getBundle(String baseName, Locale locale, ResourceStreamLoader loader)
    {
        for (String format : formats)
        {
            try
            {
                ResourceBundle rb = newBundle(baseName, locale, format, loader, false);
                if (null != rb) return rb;
            }
            catch (IllegalAccessException | InstantiationException | IOException | XMLStreamException e)
            {
                I18N_LOGGER.log(
                        System.Logger.Level.WARNING, "resource.bundle.load.error",
                        e.getClass().getName(), baseName, (null == locale) ? "ROOT" : locale.getDisplayName(),
                        e);
            }
        }
        return null;
    }
}
