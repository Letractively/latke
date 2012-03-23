/*
 * Copyright (c) 2009, 2010, 2011, 2012, B3log Team
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

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.b3log.latke.Latkes;

/**
 * Cache factory.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.6, Mar 22, 2012
 */
public final class CacheFactory {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(CacheFactory.class.getName());
    /**
     * Caches.
     */
    private static final Map<String, Cache<String, ?>> CACHES = Collections.synchronizedMap(new HashMap<String, Cache<String, ?>>());

    /**
     * Removes all caches.
     */
    public static synchronized void removeAll() {
        switch (Latkes.getRuntimeEnv()) {
            case GAE:
                // Clears one will clears all on GAE
                final Iterator<Cache<String, ?>> iterator = CACHES.values().iterator();
                if (iterator.hasNext()) {
                    iterator.next().removeAll();
                }
                break;
            case LOCAL:
                // Clears cache one by one
                for (final Cache<String, ?> cache : CACHES.values()) {
                    cache.removeAll();
                }

                break;
            default:
                throw new RuntimeException("Latke runs in the hell.... Please set the enviornment correctly");
        }

        CACHES.clear();
    }

    /**
     * Gets a cache specified by the given cache name.
     *
     * @param cacheName the given cache name
     * @return a cache specified by the given cache name
     */
    @SuppressWarnings("unchecked")
    public static synchronized Cache<String, ? extends Serializable> getCache(
            final String cacheName) {
        LOGGER.log(Level.INFO, "Constructing Cache[name={0}]....", cacheName);

        Cache<String, ?> ret = CACHES.get(cacheName);

        try {
            if (null == ret) {
                switch (Latkes.getRuntime("cache")) {
                    case LOCAL:
                        final Class<Cache<String, ?>> localLruCache =
                                (Class<Cache<String, ?>>) Class.forName("org.b3log.latke.cache.local.memory.LruMemoryCache");
                        ret = localLruCache.newInstance();
                        break;
                    case GAE:
                        final Class<Cache<String, ?>> gaeMemcache =
                                (Class<Cache<String, ?>>) Class.forName("org.b3log.latke.cache.gae.Memcache");
                        final Constructor<Cache<String, ?>> constructor =
                                gaeMemcache.getConstructor(String.class);
                        ret = constructor.newInstance(cacheName);
                        break;
                    default:
                        throw new RuntimeException("Latke runs in the hell.... Please set the enviornment correctly");
                }

                CACHES.put(cacheName, ret);
            }
        } catch (final Exception e) {
            throw new RuntimeException("Can not get cache: " + e.getMessage(), e);
        }

        LOGGER.log(Level.INFO, "Constructed Cache[name={0}]", cacheName);

        return (Cache<String, Serializable>) ret;
    }

    /**
     * Private default constructor.
     */
    private CacheFactory() {
    }
}
