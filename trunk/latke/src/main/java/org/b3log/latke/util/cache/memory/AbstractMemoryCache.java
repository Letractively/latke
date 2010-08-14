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

import org.b3log.latke.util.cache.Cache;

/**
 * The abstract memory cache. 
 *
 * @param <K> the type of the key of objects
 * @param <V> the type of objects
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.2.0, Jul 8, 2010
 */
public abstract class AbstractMemoryCache<K, V> implements Cache<K, V> {

    /**
     * Maximum objects count of this cache.
     */
    private int maxCount;
    /**
     * Hit count of this cache.
     */
    private int hitCount;
    /**
     * Miss count of this cache.
     */
    private int missCount;
    /**
     * Put count of this cache.
     */
    private int putCount;
    /**
     * Cached object count of this cache.
     */
    private int cachedCount;

    /**
     * {@inheritDoc}
     */
    @Override
    public final int getHitCount() {
        return hitCount;
    }

    /**
     * Adds one to hit count itself.
     */
    protected final void hitCountInc() {
        hitCount++;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int getMissCount() {
        return missCount;
    }

    /**
     * Adds one to miss count itself.
     */
    protected final void missCountInc() {
        missCount++;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int getPutCount() {
        return putCount;
    }

    /**
     * Sets put count by the specified put count.
     *
     * @param putCount the specified put count
     */
    protected final void setPutCount(final int putCount) {
        this.putCount = putCount;
    }

    /**
     * Adds one to put count itself.
     */
    protected final void putCountInc() {
        putCount++;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int getCachedCount() {
        return cachedCount;
    }

    /**
     * Adds one to cached count itself.
     */
    protected final void cachedCountInc() {
        cachedCount++;
    }

    /**
     * Subtracts one to cached count itself.
     */
    protected final void cachedCountDec() {
        cachedCount--;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int getMaxCount() {
        return maxCount;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setMaxCount(final int maxCount) {
        this.maxCount = maxCount;
    }
}
