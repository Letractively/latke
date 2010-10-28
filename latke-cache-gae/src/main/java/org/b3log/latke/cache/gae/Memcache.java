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

package org.b3log.latke.cache.gae;

import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import org.b3log.latke.cache.Cache;

/**
 * Google app engine memcache service.
 *
 * @param <K> the key of an object
 * @param <V> the type of objects
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.0, Oct 27, 2010
 */
public final class Memcache<K, V> implements Cache<K, V> {

    /**
     * Memcache service.
     */
    private static final MemcacheService MEMCACHE_SERVICE =
            MemcacheServiceFactory.getMemcacheService();

    @Override
    public void put(final K key, final V value) {
        MEMCACHE_SERVICE.put(key, value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public V get(final K key) {
        return (V) MEMCACHE_SERVICE.get(key);
    }

    @Override
    public void remove(final K key) {
        MEMCACHE_SERVICE.delete(key);
    }

    @Override
    public void removeAll() {
        MEMCACHE_SERVICE.clearAll();
    }

    @Override
    public void setMaxCount(final long maxCount) {
    }

    @Override
    public long getMaxCount() {
        return Long.MAX_VALUE;
    }

    @Override
    public long getHitCount() {
        return MEMCACHE_SERVICE.getStatistics().getHitCount();
    }

    @Override
    public long getMissCount() {
        return MEMCACHE_SERVICE.getStatistics().getMissCount();
    }

    @Override
    public long getPutCount() {
        return getCachedCount();
    }

    @Override
    public long getCachedCount() {
        return MEMCACHE_SERVICE.getStatistics().getItemCount();
    }

    @Override
    public void collect() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
