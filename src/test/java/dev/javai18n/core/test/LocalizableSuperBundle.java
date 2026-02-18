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

import java.util.ListResourceBundle;

/**
 * The resources for the LocalizableSuper class.
 */
public class LocalizableSuperBundle extends ListResourceBundle
{
    @Override
    public String getBaseBundleName()
    {
        return this.getClass().getName();
    }

    /**
     * Override the base class method to return the resources we want.
     * @return the contents of the ResourceBundle.
     */
    @Override
    protected Object[][] getContents()
    {
        return new Object[][]
        {
            {"key1", "Value for key1 from LocalizableSuperBundle for root locale."},
            {"key2", "Value for key2 from LocalizableSuperBundle for root locale."},
            {"key3", "Value for key3 from LocalizableSuperBundle for root locale."} // key3 has a value only in the root bundle
        };
    }
}
