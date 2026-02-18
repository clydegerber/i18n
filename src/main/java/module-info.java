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

/**
 * Welcome to the dev/javai18n/core API documentation.
 *
 * For general information, installation instructions, and examples, please see the
 * <a href="{@docRoot}/README.html">Project README</a>.
 */
module dev.javai18n.core
{
    exports dev.javai18n.core;
    requires transitive java.xml;
    requires transitive com.fasterxml.jackson.databind;
    requires transitive com.fasterxml.jackson.dataformat.xml;
    uses dev.javai18n.core.spi.LocalizableLoggerProvider;
    provides dev.javai18n.core.spi.LocalizableLoggerProvider with dev.javai18n.core.spi.ModuleProviderImpl;
}
