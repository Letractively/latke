/*
 * Copyright 2009, 2010, B3log
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.b3log.latke;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * This class defines framework(non-functional) keys.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.9, May 15, 2010
 */
public final class Keys {

    /**
     * Default locale. Initializes this by
     * {@link #setDefaultLocale(java.util.Locale)}.
     */
    private static Locale defaultLocale;
    /**
     * Key of action status code.
     */
    public static final String STATUS_CODE = "sc";
    /**
     * Key of session id.
     */
    public static final String SESSION_ID = "sid";
    /**
     * Key of data model(domain entity model).
     */
    public static final String DATA = "dt";
    /**
     * Key of locale.
     */
    public static final String LOCALE = "locale";
    /**
     * Key of language.
     */
    public static final String LANGUAGE = "lang";
    /**
     * Key of messages.
     */
    public static final String MESSAGES = "msgs";
    /**
     * Simple date format.
     */
    public static final DateFormat SIMPLE_DATE_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * Sets the default locale.
     *
     * @param locale a new default locale
     */
    public static void setDefaultLocale(final Locale locale) {
        defaultLocale = locale;
    }

    /**
     * Gets the default locale. If the {@link #defaultLocale} has not been
     * initialized, invoking this method will throw {@link RuntimeException}.
     *
     * @return the default locale
     */
    public static Locale getDefaultLocale() {
        if (null == defaultLocale) {
            throw new RuntimeException(
                    "Default locale has not been initialized!");
        }
        
        return defaultLocale;
    }

    /**
     * Private constructor.
     */
    private Keys() {
    }
}
