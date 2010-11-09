/*
 * Copyright (c) 2009, 2010, B3log Team
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

package org.b3log.latke.util.jabsorb.serializer;

import org.b3log.latke.FwkStatusCodes;
import org.jabsorb.serializer.AbstractSerializer;
import org.jabsorb.serializer.MarshallException;
import org.jabsorb.serializer.ObjectMatch;
import org.jabsorb.serializer.SerializerState;
import org.jabsorb.serializer.UnmarshallException;

/**
 * Serializer for {@link FwkStatusCodes}.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.0, May 31, 2010
 */
public class FwkStatusCodesSerializer extends AbstractSerializer {

    /**
     * Default serial version uid.
     */
    private static final long serialVersionUID = 1L;
    /**
     * Classes that this can serialise.
     */
    private static Class<?>[] serializableClasses = new Class<?>[]{
        FwkStatusCodes.class};
    /**
     * Classes that this can serialise to.
     */
    private static Class<?>[] jsonClasses = new Class<?>[]{
        FwkStatusCodes.class};

    @Override
    public Class<?>[] getJSONClasses() {
        return jsonClasses;
    }

    @Override
    public Class<?>[] getSerializableClasses() {
        return serializableClasses;
    }

    @Override
    public Object marshall(final SerializerState state, final Object p,
                           final Object o)
            throws MarshallException {
        return o;
    }

    @Override
    public ObjectMatch tryUnmarshall(final SerializerState state,
                                     final Class<?> clazz, final Object jso)
            throws UnmarshallException {
        final ObjectMatch toReturn;
        if (jso instanceof FwkStatusCodes) {
            toReturn = ObjectMatch.OKAY;
        } else {
            toReturn = ObjectMatch.ROUGHLY_SIMILAR;
        }

        state.setSerialized(jso, toReturn);

        return toReturn;
    }

    @Override
    public Object unmarshall(final SerializerState state,
                             final Class<?> clazz, final Object jso)
            throws UnmarshallException {
        FwkStatusCodes returnValue = null;

        if (jso instanceof Boolean || clazz == FwkStatusCodes.class) {
            returnValue = (FwkStatusCodes) jso;
        }

        state.setSerialized(jso, returnValue);

        return returnValue;
    }
}
