/*
 * Copyright 2009, 2010, B3log Team
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

/**
 * This is the top interface of cache like structures.
 *
 * @param <K> the key of a cacheable object
 * @param <V> the type of cacheable objects
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.2.0, Jul 8, 2010
 */
public interface Cache<K, V> {

    /**
     * Puts the specified cacheable object into this cache.
     *
     * @param key the key of the specified cacheable object
     * @param cacheableObject the specified cacheable object
     */
    void put(final K key, final V cacheableObject);

    /**
     * Gets a cacheable object by the specified key.
     *
     * @param key the specified key
     * @return if found, returns the cacheable object, otherwise returns
     * <code>null</code>
     */
    V get(final K key);

    /**
     * Removes a cacheable object by the specified key.
     *
     * @param key the specified key
     */
    void remove(final K key);

    /**
     * Sets the maximum cacheable objects count of this cache.
     *
     * @param maxCount the maximum count of this cache
     */
    void setMaxCount(final int maxCount);

    /**
     * Gets the maximum cacheable objects count of this cache.
     *
     * @return the maximum cacheable objects count of this cache 
     */
    int getMaxCount();

    /**
     * Gets the hit count of this cache.
     *
     * @return hit count of this cache
     */
    int getHitCount();

    /**
     * Gets the miss count of this cache.
     *
     * @return miss count of this cache
     */
    int getMissCount();

    /**
     * Gets the put count of this cache.
     *
     * @return put count of this cache
     */
    int getPutCount();

    /**
     * Gets current cached object count of this cache.
     *
     * @return current cached object count of this cache
     */
    int getCachedCount();

    /**
     * Collects all useless cached objects. 
     */
    void collect();
}
