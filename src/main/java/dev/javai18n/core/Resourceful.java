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
 * An interface to identify Resourceful objects - objects that support setting and getting a Resource.
 */
public interface Resourceful
{
    /**
     * Get the current Resource for the object.
     *
     * @return The Resource for the object.
     */
    public Resource getResource();

    /**
     * Set the current Resource for the object.
     *
     * @param resource The Resource for the object.
     */
    public void setResource(Resource resource);
}
