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

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import dev.javai18n.core.GetResourceBundleCallback;

/**
 * A class that is called back from the i18n module so that it can issue the ResourceBundle.getBundle() call
 * from this module's context.
 */
public class ModuleResourceBundleCallback implements GetResourceBundleCallback
{
    /**
     * A singleton that will be registered with the GetResourceBundleRegistrar.
     */
    public static final ModuleResourceBundleCallback GET_BUNDLE_CALLBACK = new ModuleResourceBundleCallback();

    @Override
    public ResourceBundle getResourceBundle(String baseName, Locale locale)
            throws NullPointerException, MissingResourceException
    {
        return ResourceBundle.getBundle(baseName, locale);
    }
}
