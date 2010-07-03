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

package org.b3log.latke.util.cache.memory;

import org.b3log.latke.util.cache.util.DoubleLinkedMap;
import org.b3log.latke.util.cache.util.Serializer;
import java.io.IOException;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is a Least Recently Used (LRU) pure memory cache. This cache use a 
 * thread-safe <code>DoubleLinkedList</code> to hold the cacheable objects, and 
 * the least recently used objects will be moved to the end of the list and to
 * remove by invoking {@linkplain #collect()} method. 
 *
 * @param <K> the type of the key of the cacheable object
 * @param <V> the type of the cacheable objects
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.1.3, Jun 12, 2009
 */
public final class LruMemoryCache<K, V extends Serializable>
        extends AbstractMemoryCache<K, V> {

    /**
     * a thread-safe double linked list is used to hold all cacheable objects.
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
    public void put(final K key, final V cacheableObject) {
        putCountInc();
        synchronized (this) {
            int newSize = 0;
            try {
                newSize =
                        Serializer.getInstance().serialize(cacheableObject).length;
            } catch (final IOException e) {
                Logger.getLogger(LruMemoryCache.class.getName()).log(
                        Level.SEVERE, null, e);
                return;
            }

            if ((getCurrentSize() + newSize) > getMaxSize()
                || getCachedCount() > getMaxCount()) {
                collect();
            }

            map.addFirst(key, cacheableObject);

            cachedCountInc();
            setCurrentSize(newSize + getCurrentSize());
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
        final V v = map.removeLast();
        cachedCountDec();
        int sizeOfOld = 0;
        
        try {
            sizeOfOld = Serializer.getInstance().serialize(v).length;
            setCurrentSize(getCurrentSize() - sizeOfOld);
        } catch (final IOException e) {
            Logger.getLogger(LruMemoryCache.class.getName()).log(Level.SEVERE,
                                                                 null, e);
        }
    }
}
