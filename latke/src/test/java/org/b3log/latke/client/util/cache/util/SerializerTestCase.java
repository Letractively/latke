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

package org.b3log.latke.client.util.cache.util;

import org.b3log.latke.util.cache.util.Serializer;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

/**
 * {@link Serializer} unit test case.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.0, May 12, 2009
 */
public class SerializerTestCase {

    /**
     * Serializer.
     */
    private Serializer serializer = Serializer.getInstance();
    /**
     * Length of an integer object in bytes.
     */
    private static final int INTEGER_LENGTH = 81;

    /**
     * Tests {@linkplain Serializer#serialize(java.io.Serializable)} method.
     * @throws Exception exception
     */
    @Test
    public void serialize() throws Exception {
        final byte[] bytes = serializer.serialize(new Integer(0));
        assertEquals(bytes.length, INTEGER_LENGTH);
    }
}
