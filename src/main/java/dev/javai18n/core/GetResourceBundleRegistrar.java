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

import java.util.concurrent.ConcurrentHashMap;

/**
 * This class allows registration of GetResourceBundleCallback instances so that they may be retrieved and used to
 * place the call to ResourceBundle.getBundle() from the appropriate module.
 */
public class GetResourceBundleRegistrar
{
    /**
     * The singleton GetResourceBundleRegistrar.
     */
    protected final static GetResourceBundleRegistrar registrar = new GetResourceBundleRegistrar();

    /**
     * Are we running under JPMS?
     */
    protected final static boolean moduleMode = GetResourceBundleRegistrar.class.getModule().isNamed();

    /**
     * A map to associate a GetResourceBundleCallback to a Module.
     */
    protected final ConcurrentHashMap<Module, GetResourceBundleCallback> map;

    /**
     * Construct the GetResourceBundleRegistrar.
     */
    protected GetResourceBundleRegistrar()
    {
        map = new ConcurrentHashMap<>();
    }

    /**
     * Register a GetResourceBundleCallback.
     *
     * @param callback The object that calls ResourceBundle.getBundle().
     * @throws IllegalStateException if a different callback is already registered for the
     *         callback's module.
     */
    protected void registerCallbackForModule(GetResourceBundleCallback callback)
    {
        Module module = callback.getClass().getModule();
        synchronized(map)
        {
            if (!moduleMode && !map.isEmpty())
            {
                //First registration wins when running on the classpath.
                return;
            }
            GetResourceBundleCallback existing = map.get(module);
            if (null != existing && existing != callback)
            {
                throw new IllegalStateException(
                    "A different GetResourceBundleCallback is already registered for module " +
                    module.getName());
            }
            map.put(module, callback);
        }
    }

    /**
     * Returns the GetResourceBundleCallback associated with the specified Module.
     *
     * @param module The Module containing the call to ResourceBundle.getBundle()
     *
     * @return The GetResourceBundleCallback for the specified Module.
     */
    protected GetResourceBundleCallback getCallbackForModule(Module module)
    {
        return map.get(module);
    }

    /**
     * A static method to register the GetResourceBundleCallback associated with the specified Module.
     *
     * @param caller The object within module that calls ResourceBundle.getBundle()
     * @throws NullPointerException if caller is null.
     * @throws IllegalStateException if a different callback is already registered for the
     *         caller's module.
     */
    public static void registerGetResourceBundleCallback(GetResourceBundleCallback caller)
    {
        if (null == caller) throw new NullPointerException("caller is null");
        registrar.registerCallbackForModule(caller);
    }

    /**
     * A static method to retrieve the GetResourceBundleCallback associated with the specified Module.
     *
     * @param module The Module for which the GetResourceBundleCallback is desired.
     * @return The GetResourceBundleCallback for the Module.
     */
    public static GetResourceBundleCallback getGetResourceBundleCallback(Module module)
    {
        return registrar.getCallbackForModule(module);
    }
}
