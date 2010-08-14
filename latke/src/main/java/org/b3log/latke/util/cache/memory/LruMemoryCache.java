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

package org.b3log.latke.util.cache.memory;

import org.b3log.latke.util.cache.util.DoubleLinkedMap;

/**
 * This is a Least Recently Used (LRU) pure memory cache. This cache use a 
 * thread-safe <code>DoubleLinkedList</code> to hold the objects, and 
 * the least recently used objects will be moved to the end of the list and to
 * remove by invoking {@linkplain #collect()} method. 
 *
 * @param <K> the type of the key of the object
 * @param <V> the type of the objects
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.2.1, Aug 14, 2010
 */
public final class LruMemoryCache<K, V> extends AbstractMemoryCache<K, V> {

    /**
     * a thread-safe double linked list is used to hold all objects.
     */
    private DoubleLinkedMap<K, V> map;

    /**
     * Constructs a {@code LruMemoryCache} object.
     */
    public LruMemoryCache() {
        map = new DoubleLinkedMap<K, V>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void put(final K key, final V value) {
        putCountInc();

        synchronized (this) {
            if (getCachedCount() > getMaxCount()) {
                collect();
            }

            map.addFirst(key, value);

            cachedCountInc();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized V get(final K key) {
        final V v = map.get(key);

        if (v != null) {
            hitCountInc();
            map.makeFirst(key);
        } else {
            missCountInc();
        }

        return v;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void remove(final K key) {
        map.remove(key);
    }

    /**
     * {@inheritDoc} Removes these useless objects directly. 
     */
    @Override
    public synchronized void collect() {
        map.removeLast();
        cachedCountDec();
    }
}
