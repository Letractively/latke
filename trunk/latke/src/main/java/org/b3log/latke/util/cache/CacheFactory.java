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
package org.b3log.latke.util.cache;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.b3log.latke.util.cache.memory.LruMemoryCache;

/**
 * Cache factory.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.0, Aug 26, 2010
 */
public final class CacheFactory {

    /**
     * LRU memory cache.
     */
    public static final String CACHE_LRU_MEMORY_CACHE =
            "cacheLruMemoryCache";
    /**
     * Caches.
     */
    private static final Map<String, Cache<String, Object>> CACHES =
            Collections.synchronizedMap(
            new HashMap<String, Cache<String, Object>>());

    /**
     * Gets a cache specified by the given cache name.
     *
     * @param cacheName the given cache name
     * @return a cache specified by the given cache name
     */
    public static Cache<String, Object> getCache(final String cacheName) {
        Cache<String, Object> ret = CACHES.get(cacheName);
        if (null == ret) {
            ret = new LruMemoryCache<String, Object>();
            CACHES.put(cacheName, ret);
        }


        return ret;
    }

    /**
     * Private default constructor.
     */
    private CacheFactory() {
    }
}
