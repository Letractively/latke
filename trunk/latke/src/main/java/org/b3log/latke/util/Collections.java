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

package org.b3log.latke.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;

/**
 * Collection utilities.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.3, Apr 30, 2010
 */
public final class Collections {

    /**
     * Private default constructor.
     */
    private Collections() {
    }

    /**
     * Converts the specified array to a set.
     *
     * @param <T> the type of elements maintained by the specified array
     * @param array the specified array
     * @return a hash set
     */
    public static <T> Set<T> arrayToSet(final T[] array) {
        final Set<T> ret = new HashSet<T>();
        for (int i = 0; i < array.length; i++) {
            final T object = array[i];
            ret.add(object);
        }

        return ret;
    }

    /**
     * Converts the specified {@link List list} to a
     * {@link JSONArray JSON array}.
     *
     * @param <T> the type of elements maintained by the specified list
     * @param list the specified list
     * @return a {@link JSONArray JSON array}
     * @throws JSONException json exception
     */
    public static <T> JSONArray listToJSONArray(final List<T> list)
            throws JSONException {
        final JSONArray ret = new JSONArray();
        for (final T object : list) {
            ret.put(object);
        }

        return ret;
    }

    /**
     * Converts the specified {@link JSONArray JSON array} to a
     * {@link List list}.
     *
     * @param <T> the type of elements maintained by the specified json array
     * @param jsonArray the specified json array
     * @return an {@link ArrayList array list}
     * @throws JSONException json exception
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> jsonArrayToList(final JSONArray jsonArray)
            throws JSONException {
        final int newLength = jsonArray.length();
        final List<T> ret = new ArrayList<T>();

        for (int i = 0; i < newLength; i++) {
            ret.add((T) jsonArray.get(i));
        }

        return ret;
    }

    /**
     * Converts the specified {@link JSONArray JSON array} to an array.
     *
     * @param <T> the type of elements maintained by the specified json array
     * @param jsonArray the specified json array
     * @param newType the class of the copy to be returned
     * @return an array
     * @throws JSONException json exception
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] jsonArrayToArray(final JSONArray jsonArray,
                                           final Class<? extends T[]> newType)
            throws JSONException {
        final int newLength = jsonArray.length();
        final Object[] original = new Object[newLength];
        for (int i = 0; i < newLength; i++) {
            original[i] = jsonArray.get(i);
        }

        return Arrays.copyOf(original, newLength, newType);
    }
}
