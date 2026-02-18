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
import java.io.InputStream;

/**
 * A helper class to facilitate use of either Modules or ClassLoaders to read resource data.
 */
public class ResourceStreamLoader
{
    private ClassLoader loader;

    private Module module;

    /**
     * Construct a ResourceStreamLoader with the specified ClassLoader.
     * @param loader A ClassLoader which will be used to return resources as an InputStream.
     */
    public ResourceStreamLoader(ClassLoader loader)
    {
        this.loader = loader;
    }

    /**
     * Construct a ResourceStreamLoader with the specified Module.
     * @param module A Module which will be used to return resources as an InputStream.
     */
    public ResourceStreamLoader(Module module)
    {
        this.module = module;
    }

    /**
     * Get an InputStream for the specified name, using the Module if one was provided or the ClassLoader if not.
     * @param name The name for the resource
     * @return An InputStream for the resource.
     * @throws IOException If the resource cannot be located.
     */
    public InputStream getResourceAsStream(String name) throws IOException
    {
        if (null != module)
        {
            return module.getResourceAsStream(name);
        }
        return loader.getResourceAsStream(name);
    }

    /**
     * If this object was constructed with a ClassLoader, it is returned otherwise the ClassLoader for the Module is
     * returned.
     * @return A ClassLoader.
     */
    public ClassLoader getResourceClassLoader()
    {
        if (null != loader) return loader;
        if (null != module) return module.getClassLoader();
        return null;
    }
}
