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

module dev.javai18n.core.test
{
    // The core i18n module this module is testing
    requires dev.javai18n.core;
    // The junit module it requires
    requires org.junit.jupiter.api;
    requires java.base;
    requires java.logging;
    exports dev.javai18n.core.test;
    opens dev.javai18n.core.test;
    // The ResourceBundleProvider interfaces and implementations that this module uses and provides.
    // Note that a separate interface must be provided for each Localizable class. A common implementation for all
    // of the interfaces is provided.
    uses dev.javai18n.core.test.spi.LocalizableSuperProvider;
    provides dev.javai18n.core.test.spi.LocalizableSuperProvider with dev.javai18n.core.test.spi.ModuleProviderImpl;
    uses dev.javai18n.core.test.spi.LocalizableSub1Provider;
    provides dev.javai18n.core.test.spi.LocalizableSub1Provider with dev.javai18n.core.test.spi.ModuleProviderImpl;
    uses dev.javai18n.core.test.spi.LocalizableSub2Provider;
    provides dev.javai18n.core.test.spi.LocalizableSub2Provider with dev.javai18n.core.test.spi.ModuleProviderImpl;
    uses dev.javai18n.core.test.spi.LocalizableSub3Provider;
    provides dev.javai18n.core.test.spi.LocalizableSub3Provider with dev.javai18n.core.test.spi.ModuleProviderImpl;
    uses dev.javai18n.core.test.spi.TestInnerLocalizable$LocalizableSuperProvider;
    provides dev.javai18n.core.test.spi.TestInnerLocalizable$LocalizableSuperProvider with dev.javai18n.core.test.spi.ModuleProviderImpl;
    uses dev.javai18n.core.test.spi.TestInnerLocalizable$LocalizableSub1Provider;
    provides dev.javai18n.core.test.spi.TestInnerLocalizable$LocalizableSub1Provider with dev.javai18n.core.test.spi.ModuleProviderImpl;
    uses dev.javai18n.core.test.spi.TestInnerLocalizable$LocalizableSub2Provider;
    provides dev.javai18n.core.test.spi.TestInnerLocalizable$LocalizableSub2Provider with dev.javai18n.core.test.spi.ModuleProviderImpl;
    uses dev.javai18n.core.test.spi.TestInnerLocalizable$LocalizableSub3Provider;
    provides dev.javai18n.core.test.spi.TestInnerLocalizable$LocalizableSub3Provider with dev.javai18n.core.test.spi.ModuleProviderImpl;
    uses dev.javai18n.core.test.spi.EmptyJsonNonEmptyPropertiesProvider;
    provides dev.javai18n.core.test.spi.EmptyJsonNonEmptyPropertiesProvider with dev.javai18n.core.test.spi.ModuleProviderImpl;
    uses dev.javai18n.core.test.spi.JsonPropertiesProvider;
    provides dev.javai18n.core.test.spi.JsonPropertiesProvider with dev.javai18n.core.test.spi.ModuleProviderImpl;
    uses dev.javai18n.core.test.spi.EmptyXmlNonEmptyPropertiesProvider;
    provides dev.javai18n.core.test.spi.EmptyXmlNonEmptyPropertiesProvider with dev.javai18n.core.test.spi.ModuleProviderImpl;
    uses dev.javai18n.core.test.spi.XmlPropertiesProvider;
    provides dev.javai18n.core.test.spi.XmlPropertiesProvider with dev.javai18n.core.test.spi.ModuleProviderImpl;
}
