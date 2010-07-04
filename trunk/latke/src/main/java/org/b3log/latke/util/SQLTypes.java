/*
 * Copyright 2009, 2010, B3log Team
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

package org.b3log.latke.util;

/**
 * SQL type utilities.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.4, May 4, 2010
 */
public final class SQLTypes {

    /**
     * Returns the specified type.
     *
     * @param type the specified type
     * @return the specified type
     * @see #toSQLExtra(java.lang.String, java.lang.String, java.lang.String[])
     */
    public static String toSQLExtra(final String type) {
        return type;
    }

    /**
     * Combines the specified type, legnth and extra.
     * <P>
     * For example, the specified type is {@code VARCHAR}, the specified
     * length is {@code 255} and the specified extra is {@code NOT NULL},
     * returns {@code VARCHAR(255) NOT NULL}.
     * </p>
     *
     * @param type the specified type
     * @param length the specified length
     * @param extras the specified extras
     * @return a combination of the these specified arguments
     */
    public static String toSQLExtra(final String type,
                                    final String length,
                                    final String... extras) {
        final StringBuilder sb = new StringBuilder(type);
        sb.append("(");
        sb.append(length);
        sb.append(")");

        for (int i = 0; i < extras.length; i++) {
            final String extra = extras[i];
            sb.append(" ");
            sb.append(extra.trim());
        }

        return sb.toString();
    }

    /**
     * Private default constructor.
     */
    private SQLTypes() {
    }
}
