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

package org.b3log.latke.cache;

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.b3log.latke.Latkes;
import org.b3log.latke.RunsOnEnv;

/**
 * Cache factory.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.2, Jun 11, 2011
 */
public final class CacheFactory {

    /**
     * Caches.
     */
    private static final Map<String, Cache<String, Object>> CACHES =
            Collections.synchronizedMap(
            new HashMap<String, Cache<String, Object>>());

    /**
     * Gets a cache specified by the given cache name.
     *
     * @param <T> the type of cached objects
     * @param cacheName the given cache name
     * @return a cache specified by the given cache name
     */
    public static synchronized <T> Cache<String, T> getCache(
            final String cacheName) {
        Cache<String, Object> ret = CACHES.get(cacheName);

        try {
            if (null == ret) {
                final RunsOnEnv runsOnEnv = Latkes.getRunsOnEnv();

                switch (runsOnEnv) {
                    case LOCAL:
                        @SuppressWarnings("unchecked")
                        final Class<Cache<String, Object>> localLruCache =
                                (Class<Cache<String, Object>>) Class.forName(
                                "org.b3log.latke.cache.local.memory.LruMemoryCache");
                        ret = localLruCache.newInstance();
                        break;
                    case GAE:
                        @SuppressWarnings("unchecked")
                        final Class<Cache<String, Object>> gaeMemcache =
                                (Class<Cache<String, Object>>) Class.forName(
                                "org.b3log.latke.cache.gae.Memcache");
                        final Constructor<Cache<String, Object>> constructor =
                                gaeMemcache.getConstructor(String.class);
                        ret = constructor.newInstance(cacheName);
                        break;
                    default:
                        throw new RuntimeException(
                                "Latke runs in the hell.... "
                                + "Please set the enviornment correctly");
                }

                CACHES.put(cacheName, ret);
            }
        } catch (final Exception e) {
            throw new RuntimeException("Can not get cache: " + e.getMessage(), e);
        }

        return (Cache<String, T>) ret;
    }

    /**
     * Private default constructor.
     */
    private CacheFactory() {
    }
}
