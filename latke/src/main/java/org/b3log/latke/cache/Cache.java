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
package org.b3log.latke.cache;

import java.util.Collection;

/**
 * This is the top interface of cache like structures.
 *
 * @param <K> the key of an object
 * @param <V> the type of objects
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.2.3, Oct 27, 2010
 */
public interface Cache<K, V> {

    /**
     * Puts the specified object into this cache.
     *
     * <p>
     * Throws {@link IllegalArgumentException} if the specified key is null
     * </p>
     *
     * @param key the key of the specified object
     * @param value the specified object
     */
    void put(final K key, final V value);

    /**
     * Gets a object by the specified key.
     *
     * @param key the specified key
     * @return if found, returns the object, otherwise returns
     * <code>null</code>
     */
    V get(final K key);

    /**
     * Removes a object by the specified key.
     *
     * @param key the specified key
     */
    void remove(final K key);
    /**
     * Removes objects by the specified keys.
     *
     * @param keys the specified keys
     */
    void remove(final Collection<K> keys);

    /**
     * Removes all cached objects.
     */
    void removeAll();

    /**
     * Sets the maximum objects count of this cache.
     *
     * @param maxCount the maximum count of this cache
     */
    void setMaxCount(final long maxCount);

    /**
     * Gets the maximum objects count of this cache.
     *
     * @return the maximum objects count of this cache 
     */
    long getMaxCount();

    /**
     * Gets the hit count of this cache.
     *
     * @return hit count of this cache
     */
    long getHitCount();

    /**
     * Gets the miss count of this cache.
     *
     * @return miss count of this cache
     */
    long getMissCount();

    /**
     * Gets the put count of this cache.
     *
     * @return put count of this cache
     */
    long getPutCount();

    /**
     * Gets current cached object count of this cache.
     *
     * @return current cached object count of this cache
     */
    long getCachedCount();

    /**
     * Collects all useless cached objects. 
     */
    void collect();
}
