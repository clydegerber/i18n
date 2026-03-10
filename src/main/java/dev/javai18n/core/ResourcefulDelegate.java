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

import dev.javai18n.core.Localizable.LocaleEvent;
import dev.javai18n.core.Localizable.LocaleEventListener;

/**
 * A delegate that handles {@link Resource} ownership and {@link LocaleEventListener}
 * registration on behalf of {@link Resourceful} objects that cannot share a common base class.
 *
 * <p>The delegate owns the {@code Resource}, registers itself as a {@code LocaleEventListener}
 * on the resource's {@link Localizable} source, and forwards locale events to the specified
 * event handler. This keeps {@code setResource()} clean: the delegate unregisters itself from
 * the old source and registers on the new one.</p>
 *
 * <p>The delegate is toolkit-agnostic. Toolkit-specific behavior such as updating component
 * properties or dispatching to an event thread is the responsibility of the event handler.
 * For example, Swing consumers can wrap this delegate with a handler that uses
 * {@code SwingUtilities.invokeLater}.</p>
 *
 * <p>This is the {@code Resourceful} counterpart of {@link LocalizationDelegate},
 * which serves the same role for the {@link Localizable} interface.</p>
 */
public class ResourcefulDelegate implements LocaleEventListener
{
    /**
     * The event handler to which locale events are forwarded.
     */
    private final LocaleEventListener eventHandler;

    /**
     * The Resource containing localized values for the owning component.
     */
    private Resource resource;

    /**
     * Construct a ResourcefulDelegate.
     *
     * @param resource     The initial Resource for the owning component.
     * @param eventHandler A LocaleEventListener to which locale events are forwarded.
     * @throws NullPointerException if resource or eventHandler is null.
     */
    public ResourcefulDelegate(Resource resource, LocaleEventListener eventHandler)
    {
        if (null == resource) throw new NullPointerException("resource is null");
        if (null == eventHandler) throw new NullPointerException("eventHandler is null");
        this.resource = resource;
        this.eventHandler = eventHandler;
    }

    /**
     * Register this delegate as a {@link LocaleEventListener} on the resource's source.
     */
    public void registerListener()
    {
        resource.getSource().addLocaleEventListener(this);
    }

    /**
     * Forward a LocaleEvent to the event handler.
     *
     * @param event The LocaleEvent that has been raised.
     */
    @Override
    public void processLocaleEvent(LocaleEvent event)
    {
        eventHandler.processLocaleEvent(event);
    }

    /**
     * Get the Resource holding locale-specific values for the owning component.
     *
     * @return The Resource holding locale-specific values for the owning component.
     */
    public Resource getResource()
    {
        return resource;
    }

    /**
     * Set the Resource holding locale-specific values for the owning component.
     * Unregisters this delegate from the old source, sets the new resource,
     * and registers this delegate on the new source.
     *
     * @param resource The new Resource holding locale-specific values for the owning component.
     */
    public void setResource(Resource resource)
    {
        this.resource.getSource().removeLocaleEventListener(this);
        this.resource = resource;
        this.resource.getSource().addLocaleEventListener(this);
    }
}
