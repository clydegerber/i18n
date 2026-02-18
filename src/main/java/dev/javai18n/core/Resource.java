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
 * A Resource object encapsulates a resource obtainable from a Localizable object with a specified key.
 */
public class Resource
{
   /**
     * A Localizable object that is the source for a locale-sensitive
     * resource using the key for this object.
     */
    protected Localizable source;

    /**
     * The key used to look up the locale-sensitive resource.
     */
    protected String key;

    /**
     * Constructs a Resource from a Localizable source and a String key.
     *
     * @param source A Localizable object.
     *
     * @param key The key used to lookup the resource from the
     *            ResourceBundle associated with source.
     */
    public Resource(Localizable source, String key)
    {
        this.source = source;
        this.key = key;
    }

    /**
     * Get the localized Object for the Resource.
     *
     * @return The localized Object associated with the Resource's key from
     *         the ResourceBundle provided by source.
     * @throws dev.javai18n.core.NoCallbackRegisteredForModuleException if no callback has been registered for the module.
     */
    public final Object getObject() throws NoCallbackRegisteredForModuleException
    {
        return source.getResourceBundle().getObject(key);
    }

    /**
     * Get the localized String for the Resource.
     *
     * @return The localized String associated with the Resource's key from
     *         the ResourceBundle provided by source.
     * @throws dev.javai18n.core.NoCallbackRegisteredForModuleException if no callback has been registered for the module.
     */
    public final String getString() throws NoCallbackRegisteredForModuleException
    {
        return source.getResourceBundle().getString(key);
    }

    /**
     * Get the localized String array for the Resource.
     *
     * @return The localized String array associated with the Resource's key
     *         from the ResourceBundle provided by source.
     * @throws dev.javai18n.core.NoCallbackRegisteredForModuleException if no callback has been registered for the module.
     */
    public final String[] getStringArray() throws NoCallbackRegisteredForModuleException
    {
        return source.getResourceBundle().getStringArray(key);
    }

    /**
     * Get the source Localizable object for this Resource.
     * @return The source Localizable object for this Resource.
     */
    public final Localizable getSource()
    {
        return source;
    }
}
