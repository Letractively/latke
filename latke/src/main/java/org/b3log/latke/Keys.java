/*
 * Copyright (C) 2009, 2010, B3log Team
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

package org.b3log.latke;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * This class defines framework(non-functional) keys.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.1.3, Aug 28, 2010
 */
public final class Keys {

    /**
     * Key of action status code.
     */
    public static final String STATUS_CODE = "sc";
    /**
     * Key of session id.
     */
    public static final String SESSION_ID = "sId";
    /**
     * Key of results.
     */
    public static final String RESULTS = "rslts";
    /**
     * Key of id of an entity json object.
     */
    public static final String OBJECT_ID = "oId";
    /**
     * Key of ids.
     */
    public static final String OBJECT_IDS = "oIds";
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
     * Simple date format. (yyyy-MM-dd HH:mm:ss)
     */
    public static final DateFormat SIMPLE_DATE_FORMAT1 =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * Private constructor.
     */
    private Keys() {
    }
}