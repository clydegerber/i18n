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

import dev.javai18n.core.Localizable.LocaleEvent;
import dev.javai18n.core.Resource;
import dev.javai18n.core.ResourcefulDelegate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link ResourcefulDelegate}.
 */
public class TestResourcefulDelegate
{
    @Test
    void testGetResourceReturnsInitialResource()
    {
        LocalizableSuper source = new LocalizableSuper();
        Resource resource = new Resource(source, "testKey");

        ResourcefulDelegate delegate = new ResourcefulDelegate(resource, event -> {});

        assertSame(resource, delegate.getResource());
    }

    @Test
    void testRegisterListenerRegistersOnSource()
    {
        LocalizableSuper source = new LocalizableSuper();
        Resource resource = new Resource(source, "testKey");
        List<LocaleEvent> capturedEvents = new ArrayList<>();

        ResourcefulDelegate delegate = new ResourcefulDelegate(resource, capturedEvents::add);

        delegate.registerListener();
        source.setBundleLocale(Locale.KOREA);

        assertEquals(1, capturedEvents.size());
        assertSame(source, capturedEvents.get(0).getLocalizableSource());
    }

    @Test
    void testProcessLocaleEventForwardsToHandler()
    {
        LocalizableSuper source = new LocalizableSuper();
        Resource resource = new Resource(source, "testKey");
        List<LocaleEvent> capturedEvents = new ArrayList<>();

        ResourcefulDelegate delegate = new ResourcefulDelegate(resource, capturedEvents::add);

        LocaleEvent event = new LocaleEvent(source);
        delegate.processLocaleEvent(event);

        assertEquals(1, capturedEvents.size());
        assertSame(event, capturedEvents.get(0));
    }

    @Test
    void testSetResourceReturnsNewResource()
    {
        LocalizableSuper source1 = new LocalizableSuper();
        LocalizableSuper source2 = new LocalizableSuper();
        Resource resource1 = new Resource(source1, "key1");
        Resource resource2 = new Resource(source2, "key2");

        ResourcefulDelegate delegate = new ResourcefulDelegate(resource1, event -> {});

        delegate.registerListener();
        delegate.setResource(resource2);

        assertSame(resource2, delegate.getResource());
    }

    @Test
    void testSetResourceUnregistersFromOldSource()
    {
        LocalizableSuper source1 = new LocalizableSuper();
        LocalizableSuper source2 = new LocalizableSuper();
        Resource resource1 = new Resource(source1, "key1");
        Resource resource2 = new Resource(source2, "key2");
        int[] eventCount = {0};

        ResourcefulDelegate delegate = new ResourcefulDelegate(resource1,
                event -> eventCount[0]++);

        delegate.registerListener();
        delegate.setResource(resource2);
        eventCount[0] = 0;

        // Changing locale on old source should NOT trigger event
        source1.setBundleLocale(Locale.GERMANY);
        assertEquals(0, eventCount[0]);
    }

    @Test
    void testSetResourceRegistersOnNewSource()
    {
        LocalizableSuper source1 = new LocalizableSuper();
        LocalizableSuper source2 = new LocalizableSuper();
        Resource resource1 = new Resource(source1, "key1");
        Resource resource2 = new Resource(source2, "key2");
        List<LocaleEvent> capturedEvents = new ArrayList<>();

        ResourcefulDelegate delegate = new ResourcefulDelegate(resource1,
                capturedEvents::add);

        delegate.registerListener();
        delegate.setResource(resource2);
        capturedEvents.clear();

        // Changing locale on new source SHOULD trigger event
        source2.setBundleLocale(Locale.FRANCE);

        assertEquals(1, capturedEvents.size());
        assertSame(source2, capturedEvents.get(0).getLocalizableSource());
    }
}
