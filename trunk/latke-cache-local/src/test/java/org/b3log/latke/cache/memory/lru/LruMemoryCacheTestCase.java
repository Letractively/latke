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

package org.b3log.latke.cache.memory.lru;

import org.b3log.latke.cache.Cache;
import org.b3log.latke.cache.local.memory.LruMemoryCache;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

/**
 * {@link LruMemoryCache} test case.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.2.0, Jul 8, 2010
 */
public class LruMemoryCacheTestCase {

    /**
     * Test cacheable object sum.
     */
    public static final int CACHEABLE_OBJECT_SUM = 1024;

    /**
     * Constructs a {@code LruMemoryCacheTestCase} instance.
     */
    public LruMemoryCacheTestCase() {
        final long freeMemoryMb = Runtime.getRuntime().freeMemory();
        System.out.println("Current free memory: " + freeMemoryMb + "b");
    }

    /**
     * Tests LRU memory cache.
     */
    @Test
    public void lruMemoryCache() {
        final Cache<String, ObjectToCache> cache =
                new LruMemoryCache<String, ObjectToCache>();
        cache.setMaxCount(CACHEABLE_OBJECT_SUM);

        printStat(cache);

        for (int i = 0; i < CACHEABLE_OBJECT_SUM; i++) {
            final ObjectToCache page = new ObjectToCache(String.valueOf(i));
            cache.put(page.getId(), page);
        }

        final ObjectToCache p = cache.get("0");

        assertNotNull(p);
        assertEquals(p.getId(), "0");
        printStat(cache);

        assertEquals(CACHEABLE_OBJECT_SUM, cache.getCachedCount());
        cache.collect();
        assertEquals(CACHEABLE_OBJECT_SUM - 1, cache.getCachedCount());

        printStat(cache);
    }

    /**
     * Prints states for the specified cache.
     *
     * @param cache the specified cache
     */
    private void printStat(final Cache<?, ?> cache) {
        System.out.println("----");
        System.out.println("Cached object: " + cache.getCachedCount());
        System.out.println("Put count: " + cache.getPutCount());
        System.out.println("Hit count: " + cache.getHitCount());
        System.out.println("Miss count: " + cache.getMissCount());
    }
}

/**
 * An dummy object to test cache.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.2.0, Jul 8, 2010
 */
final class ObjectToCache {

    /**
     * Generated serial version uid.
     */
    private static final long serialVersionUID = -5075609672986347710L;
    /**
     * Id.
     */
    private String id;
    /**
     * Dummy bytes.
     */
    private byte[] dummy;
    /**
     * Length of dummy bytes.
     */
    private static final int DUMMY_LENGTH = 100;

    /**
     * Constructs a object of {@code ObjectToCache} with the specified id.
     *
     * @param id the specified id
     */
    public ObjectToCache(final String id) {
        this.id = id;
        dummy = new byte[DUMMY_LENGTH];
    }

    /**
     * Gets id.
     *
     * @return id
     */
    public String getId() {
        return id;
    }
}
