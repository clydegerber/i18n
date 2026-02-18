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

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.ResourceBundle.Control;
import javax.xml.stream.XMLStreamException;

/**
 * A ResourceBundle.Control that uses an AssociativeResourceBundleLocator to load ResourceBundles
 * associated with a baseName. The association is done by adding a suffix ("Bundle" is the default) to the
 * baseName and searching for a java class, JSON file, XML file or properties file that matches the
 * constructed name. If no ResourceBundle is found for the constructed name, it attempts to find a
 * ResourceBundle using the baseName only.
 */
public class AssociativeResourceBundleControl extends Control
{
    AssociativeResourceBundleLocator locator;

    AssociativeResourceBundleLocator noSuffixLocator;

    /**
     * Construct an AssociativeResourceBundleControl with an AssociativeResourceBundleLocator that
     * uses the suffix "Bundle" to locate ResourceBundles.
     */
    public AssociativeResourceBundleControl ()
    {
        locator = new AssociativeResourceBundleLocator("Bundle");
        noSuffixLocator = new AssociativeResourceBundleLocator("");
    }

    /**
     * Returns the list of formats from the locator.
     * @param baseName Ignored
     * @return The list of formats from the locator.
     */
    @Override
    public List<String> getFormats(String baseName)
    {
        return locator.getFormats();
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
     * @throws IOException
     *        if an error occurred when reading resources using
     *        any I/O operations
     */
    @Override
    public ResourceBundle newBundle(String baseName,
                                    Locale locale,
                                    String format,
                                    ClassLoader loader,
                                    boolean reload)
                            throws IllegalAccessException,
                                   InstantiationException,
                                   IOException
    {
        ResourceStreamLoader streamLoader = new ResourceStreamLoader(loader);
        ResourceBundle rb;
        try
        {
            rb = locator.newBundle(baseName, locale, format, streamLoader, reload);
            if (null != rb)
            {
                return rb;
            }
            else
            {
                return noSuffixLocator.newBundle(baseName, locale, format, streamLoader, reload);
            }
        }
        catch (XMLStreamException ex)
        {
            throw new IOException(ex.getMessage(), ex);
        }
    }
}
