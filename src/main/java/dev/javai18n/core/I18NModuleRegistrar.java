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

/**
 * Consolidates module-level registrations for the i18n module.
 * Ensures that the {@link dev.javai18n.core.GetResourceBundleCallback} and
 * {@link dev.javai18n.core.AttributeCollection} package are registered
 * exactly once, regardless of which class triggers the initialization first.
 */
public final class I18NModuleRegistrar
{
    private static volatile boolean registered;

    private I18NModuleRegistrar() {}

    /**
     * Ensure that all module-level registrations for the i18n module
     * have been performed. This method is idempotent and safe to call from
     * multiple threads.
     */
    public static void ensureRegistered()
    {
        if (!registered)
        {
            GetResourceBundleRegistrar.registerGetResourceBundleCallback(
                ModuleResourceBundleCallback.GET_BUNDLE_CALLBACK);
            AttributeCollectionResourceBundle.registerAttributeCollectionPackage(
                I18NModuleRegistrar.class.getPackageName());
            registered = true;
        }
    }
}
