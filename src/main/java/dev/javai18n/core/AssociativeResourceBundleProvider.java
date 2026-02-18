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
import java.util.spi.AbstractResourceBundleProvider;
import java.lang.StackWalker.Option;
import java.util.Set;

/**
 * A ResourceBundleProvider that uses an AssociativeResourceBundleLocator to load ResourceBundles
 * associated with a baseName. The association is done by adding a suffix ("Bundle" is the default) to the
 * baseName and searching for a java class, JSON file, XML file or properties file that matches the
 * constructed name.
 */
public class AssociativeResourceBundleProvider extends AbstractResourceBundleProvider
{
    private static final StackWalker STACK_WALKER =
            StackWalker.getInstance(Set.of(Option.DROP_METHOD_INFO, Option.RETAIN_CLASS_REFERENCE));

    AssociativeResourceBundleLocator locator;

    /**
     * Return a ResourceBundle found by appending "Bundle" to the baseName, or null.
     *
     * @param baseName
     *        The baseName of the desired resource bundle to which the literal "Bundle" will be appended prior to search.
     * @param locale
     *         The desired locale for the ResourceBundle
     * @return A ResourceBundle or null.
     * @throws NullPointerException
     *         If baseName or locale is null.
     */
    @Override
    public ResourceBundle getBundle(String baseName, Locale locale)
    {
        // Get the module of the caller class
        Module callerModule = STACK_WALKER.walk(s ->
            s.map(StackWalker.StackFrame::getDeclaringClass)
             .filter(c ->
             {
                 String moduleName = c.getModule().getName();
                 if (null == moduleName) return true;
                 return !moduleName.startsWith("java.") &&
                        !moduleName.startsWith("jdk.") &&
                        !(moduleName.equals("dev.javai18n.core") &&
                          !c.equals(ModuleResourceBundleCallback.class));
             })
             .findFirst()
             .orElseThrow(() -> new IllegalStateException("Unable to identify caller module"))
             .getModule());
        return locator.getBundle(baseName, locale, new ResourceStreamLoader(callerModule));
    }

    /**
     * Constructs an AssociativeResourceBundleProvider that uses an AssociativeResourceBundleLocator with
     * the suffix "Bundle" to locate ResourceBundles.
     */
    public AssociativeResourceBundleProvider()
    {
        locator = new AssociativeResourceBundleLocator("Bundle");
    }
}
