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

import java.util.ResourceBundle;
import java.util.spi.ResourceBundleControlProvider;

/**
 * A ResourceBundleControlProvider that uses an AssociativeResourceBundleControl to locate ResourceBundles.
 */
public class AssociativeResourceBundleControlProvider implements ResourceBundleControlProvider
{
    /**
     * Constructs an AssociativeResourceBundleControlProvider.
     */
    public AssociativeResourceBundleControlProvider() {}

    /**
     * A static AssociativeResourceBundleControl exposed to the package.
     */
    protected static final AssociativeResourceBundleControl CONTROL = new AssociativeResourceBundleControl();

    /**
     * Return a ResourceBundle found by adding the literal "Bundle" to the baseName and searching for
     * matching java classes, JSON files, XML files and properties files.
     *
     * @param baseName
     *        The baseName of the desired resource bundle to which the suffix "Bundle" is appended.
     * @return A ResourceBundle or null.
     * @throws NullPointerException
     *         If baseName is null.
     */
    @Override
    public ResourceBundle.Control getControl(String baseName)
    {
        return CONTROL;
    }
}
