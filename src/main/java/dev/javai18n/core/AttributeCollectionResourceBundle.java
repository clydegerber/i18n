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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import static dev.javai18n.core.LocalizableLogger.I18N_LOGGER;

/**
 * The base class for JsonResourceBundle and XMLResourceBundle, it maintains properties in a
 * {@code ConcurrentHashMap<String, Object>} and provides methods for constructing an AttributeCollection object
 * from a Class name and converting an {@code ArrayList<Object>} to an Object array, returning String arrays when
 * all elements of the {@code ArrayList} are String objects.
 */
public abstract class AttributeCollectionResourceBundle extends ResourceBundle
{
    /**
     * The set of package names whose AttributeCollection implementations are permitted to be
     * instantiated from resource file type fields. Packages must be registered via
     * {@link #registerAttributeCollectionPackage} before resource files referencing types
     * in that package can be parsed.
     */
    private static final Set<String> allowedPackages = ConcurrentHashMap.newKeySet();

    /**
     * Register a package whose AttributeCollection implementations are permitted for
     * instantiation from resource files. Must be called before parsing any resource files
     * that reference types in the package.
     *
     * @param packageName The fully qualified package name to allow.
     * @throws NullPointerException if packageName is null.
     * @throws IllegalArgumentException if packageName is empty.
     */
    public static void registerAttributeCollectionPackage(String packageName)
    {
        if (null == packageName) throw new NullPointerException("packageName is null");
        if (packageName.isEmpty()) throw new IllegalArgumentException("packageName is empty");
        allowedPackages.add(packageName);
    }

    /**
     * The HashMap that contains the resource keys and values.
     */
    protected ConcurrentHashMap<String, Object> props;

    /**
     * Default constructor for subclasses.
     */
    protected AttributeCollectionResourceBundle() {}

    /**
     * Construct an AttributeCollection object for the specified Class name.
     * @param className A String specifying the Class name of the object to be created.
     * @return An AttributeCollection object.
     * @throws IOException If the Class cannot be found, is not assignable to the AttributeCollection interface,
     *                     if a no-argument constructor does not exist for the Class, or if the constructor is not
     *                     public.
     */
    protected AttributeCollection constructAttributeCollectionObject(String className) throws IOException
    {
        int lastDot = className.lastIndexOf('.');
        if (lastDot < 0 || !allowedPackages.contains(className.substring(0, lastDot)))
        {
            IOException ex = new IOException("Class " + className + " is not in a registered AttributeCollection package");
            I18N_LOGGER.log(System.Logger.Level.ERROR, "class.not.in.registered.package", className, ex);
            throw ex;
        }
        AttributeCollection coll;
        ClassLoader loader = this.getClass().getClassLoader();
        Class<?> c;
        try
        {
            c = loader.loadClass(className);
        } catch (ClassNotFoundException ex)
        {
            I18N_LOGGER.log(System.Logger.Level.DEBUG, "class.not.found", className, ex);
            throw new IOException("Failed to construct AttributeCollection object", ex);
        }
        if (!AttributeCollection.class.isAssignableFrom(c))
        {
            I18N_LOGGER.log(System.Logger.Level.DEBUG, "type.not.attribute.collection", className);
            throw new IOException("Failed to construct AttributeCollection object");
        }
        Constructor<?> ctor;
        try
        {
            ctor = c.getDeclaredConstructor();
        } catch (NoSuchMethodException ex)
        {
            I18N_LOGGER.log(System.Logger.Level.DEBUG, "no-arg.constructor.not.found", className, ex);
            throw new IOException("Failed to construct AttributeCollection object", ex);
        }
        if (!Modifier.isPublic(ctor.getModifiers()))
        {
            I18N_LOGGER.log(System.Logger.Level.DEBUG, "constructor.not.public", className);
            throw new IOException("Failed to construct AttributeCollection object");
        }
        try
        {
            coll = (AttributeCollection) ctor.newInstance((Object[]) null);
        } catch (IllegalAccessException | IllegalArgumentException | InstantiationException | InvocationTargetException ex)
        {
            I18N_LOGGER.log(System.Logger.Level.DEBUG, "failed.to.instantiate", className, ex.getClass().getName(), ex);
            throw new IOException("Failed to construct AttributeCollection object", ex);
        }
        return coll;
    }

    /**
     * Convert the specified {@code ArrayList<Object>} to an Object array. If the {@code ArrayList<Object>} contains only Strings,
     * it will be converted to a String array (to ensure that the getStringArray() function works as expected).
     * @param list The {@code ArrayList<Object>} to convert.
     * @return An Object array containing all of the elements of list.
     */
    protected Object[] convertToArray(ArrayList<Object> list)
    {
        // If the array contains only Strings, return a String array to ensure that getStringArray works
        boolean allStrings = true;
        for (Object o : list)
        {
            if (!(o instanceof String))
            {
                allStrings = false;
                break;
            }
        }
        return allStrings ? list.toArray(new String[0]) : list.toArray();
    }

    /**
     * Gets an object for the given key from this resource bundle. Returns null if this
     * resource bundle does not contain an object for the given key.
     *
     * @param key the name for the desired object
     *
     * @return the object for the given name, or null
     *
     * @throws NullPointerException - if key is null
     */
    @Override
    protected Object handleGetObject(String key)
    {
        return props.get(key);
    }

    /**
     * Returns an enumeration of the keys.
     *
     * @return an Enumeration of the keys contained in this ResourceBundle and its parent bundles.
     */
    @Override
    public Enumeration<String> getKeys()
    {
        HashSet<String> keys = new HashSet<>();
        if (null != parent)
        {
            Enumeration<String> e = parent.getKeys();
            while (e.hasMoreElements())
            {
                keys.add(e.nextElement());
            }
        }
        Enumeration<String> myKeys = props.keys();
        while (myKeys.hasMoreElements())
        {
            keys.add(myKeys.nextElement());
        }
        return Collections.enumeration(keys);
    }

}
