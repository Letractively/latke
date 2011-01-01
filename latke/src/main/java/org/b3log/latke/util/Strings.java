/*
 * Copyright (c) 2009, 2010, 2011, B3log Team
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * String utilities.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.7, Jul 23, 2010
 */
public final class Strings {

    /**
     * Private default constructor.
     */
    private Strings() {
    }

    /**
     * Converts the specified string into a string list line by line.
     *
     * @param string the specified string
     * @return a list of string lines
     * @throws IOException io exception
     */
    public static List<String> toLines(final String string)
            throws IOException {
        final BufferedReader bufferedReader =
                new BufferedReader(new StringReader(string));
        final List<String> ret = new ArrayList<String>();

        try {
            String line = bufferedReader.readLine();
            while (null != line) {
                ret.add(line);

                line = bufferedReader.readLine();
            }
        } finally {
            bufferedReader.close();
        }

        return ret;
    }

    /**
     * Determines whether the specified string is {@code ""} or {@code null}.
     *
     * @param string the specified string
     * @return {@code true} if the specified string is {@code ""} or
     * {@code null}, {@code false} otherwise
     */
    public static boolean isEmptyOrNull(final String string) {
        return string == null || string.trim().length() == 0;
    }

    /**
     * Trims every string in the specified strings array.
     *
     * @param strings the specified strings array
     * @return a trimmed strings array
     */
    public static String[] trimAll(final String[] strings) {
        final String[] ret = new String[strings.length];

        for (int i = 0; i < strings.length; i++) {
            ret[i] = strings[i].trim();
        }

        return ret;
    }
}
