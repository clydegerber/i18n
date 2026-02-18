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

/**
 * An interface that calls back to ResourceBundle.getBundle(). In java modular applications, the resources exposed
 * via the ResourceBundle.getBundle() methods must reside within the module of the caller. In order to expose
 * resources of classes to an extending class which may be in a different module, this abstraction is introduced
 * to support routing the calls to ResourceBundle.getBundle() via the appropriate module.
 */
public interface GetResourceBundleCallback
{
    /**
     * Invokes ResourceBundle.getBundle() for the given baseName and locale.
     * @param baseName The baseName to pass ResourceBundle.getBundle().
     * @param locale   The locale to pass ResourceBundle.getBundle().
     * @return A ResourceBundle for the baseName and locale.
     * @throws NullPointerException If baseName or locale is null.
     * @throws MissingResourceException If the ResourceBundle cannot be located.
     */
    public ResourceBundle getResourceBundle(String baseName, Locale locale)
            throws NullPointerException, MissingResourceException;
}
