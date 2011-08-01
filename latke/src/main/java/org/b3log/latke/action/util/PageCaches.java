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
package org.b3log.latke.action.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.b3log.latke.Latkes;
import org.b3log.latke.RuntimeEnv;
import org.b3log.latke.action.AbstractCacheablePageAction;
import org.b3log.latke.cache.Cache;
import org.b3log.latke.cache.CacheFactory;
import org.b3log.latke.util.Serializer;
import org.b3log.latke.util.Strings;
import org.b3log.latke.util.freemarker.Templates;
import org.json.JSONObject;

/**
 * Page cache.
 * 
 * <p>
 *   This cache contains some pages and their statistics as the following: 
 *   <pre>
 *     &lt;pageCacheKey1, JSONObject1{oId, title, title, type}&gt;
 *     &lt;pageCacheKey2, JSONObject2{oId, title, title, type}&gt;
 *     ....
 *     &lt;"keys", Set&lt;pageCacheKey&gt;&gt;
 *   </pre>
 * </p>
 * 
 * <p>
 * <i>Page Cache Key</i> generated by method 
 * {@linkplain #getPageCacheKey(java.lang.String, java.lang.String)}.
 * </p>
 *
 * <p>
 *   <b>Note</b>: The method <a href="http://code.google.com/appengine/docs/java/javadoc/
 *  com/google/appengine/api/memcache/MemcacheService.html#clearAll%28%29">
 *   clearAll</a> of <a href="http://code.google.com/appengine/docs/java/javadoc/
 *  com/google/appengine/api/memcache/MemcacheService.html">MemcacheService</a>
 *   does not respect namespaces - this flushes the cache for every namespace.
 * </p>
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.8, Aug 1, 2011
 */
public final class PageCaches {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(
            PageCaches.class.getName());
    /**
     * Page cache.
     * <p>
     * &lt;requestURI?queryString, page info&gt;
     * </p>
     */
    private static final Cache<String, Object> CACHE;
    /**
     * Maximum count of cacheable pages.
     */
    private static final int MAX_CACHEABLE_PAGE_CNT = 1024;
    /**
     * Key of page cache name.
     */
    public static final String PAGE_CACHE_NAME = "page";
    /**
     * Key of cached page keys.
     */
    public static final String KEYS = "keys";
    /**
     * Key of cached time.
     */
    public static final String CACHED_TIME = "cachedTime";
    /**
     * Key of cached bytes length.
     */
    public static final String CACHED_BYTES_LENGTH = "cachedBtypesLength";
    /**
     * key of cached hit count.
     */
    public static final String CACHED_HIT_COUNT = "cachedHitCount";

    /**
     * Initializes the cache.
     */
    static {
        CACHE = CacheFactory.getCache(PAGE_CACHE_NAME);
        final RuntimeEnv runtimeEnv = Latkes.getRuntimeEnv();
        if (runtimeEnv.equals(RuntimeEnv.LOCAL)) {
            CACHE.setMaxCount(MAX_CACHEABLE_PAGE_CNT);
            LOGGER.log(Level.INFO, "Initialized page cache[maxCount={0}]",
                       MAX_CACHEABLE_PAGE_CNT);
        }

        @SuppressWarnings("unchecked")
        Set<String> keys = (Set<String>) CACHE.get(KEYS);
        if (null == keys) {
            keys = new HashSet<String>();
            CACHE.put(KEYS, keys);
        }
    }

    /**
     * Gets a page cache key by the specified URI and query string.
     *
     * @param uri the specified URI
     * @param queryString the specified query string
     * @return cache key
     */
    public static String getPageCacheKey(final String uri,
                                         final String queryString) {
        String ret = uri;

        try {
            if (!Strings.isEmptyOrNull(queryString)) {
                ret += "?" + queryString;
            }

            return ret;
        } catch (final Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }

        return ret;
    }

    /**
     * Gets all cached page keys.
     * 
     * @return cached page keys, returns an empty set if not found
     */
    @SuppressWarnings("unchecked")
    public static Set<String> getKeys() {
        final Set<String> keys = (Set<String>) CACHE.get(KEYS);

        if (null == keys) { // Occurs sometime on GAE
            removeAll();
        }

        // TODO: sort

        return Collections.unmodifiableSet(keys);
    }

    /**
     * Gets cache.
     *
     * @return cache
     */
    public static Cache<String, Object> getCache() {
        return CACHE;
    }

    /**
     * Gets a cached page with the specified page cache key and update stat. 
     * flag. 
     * 
     * <p>Invoking this method will change statistic of a cached page if the 
     * specified update stat. flag is {@code true}, such as to update the 
     * cache hit count.</p>
     *
     * @param pageCacheKey the specified page cache key
     * @param needUpdateStat the specified update stat. flat, {@code true} 
     * indicates that need to update the statistic, {@code false} otherwise
     * @return for example,
     * <pre>
     * {
     *     "cachedContent: "",
     *     "cachedOid": "",
     *     "cachedTitle": "",
     *     "cachedType": ""
     * }
     * </pre>
     */
    public static JSONObject get(final String pageCacheKey, 
                                 final boolean needUpdateStat) {
        final JSONObject ret = (JSONObject) CACHE.get(pageCacheKey);
        
        if (needUpdateStat && ret != null) {
            try {
                final long hitCount = ret.optLong(CACHED_HIT_COUNT);
                ret.put(CACHED_HIT_COUNT, hitCount + 1);

                CACHE.put(pageCacheKey, ret);
            } catch (final Exception e) {
                LOGGER.log(Level.WARNING, "Set stat. of cached page[pageCacheKey="
                                          + pageCacheKey + "] failed", e);
            }
        }

        return ret;
    }

    /**
     * Puts a page into cache.
     *
     * @param pageKey key of the page to put
     * @param cachedValue value to put, for example, 
     * <pre>
     * {
     *     "cachedContent: "",
     *     "cachedOid": "",
     *     "cachedTitle": "",
     *     "cachedType": ""
     * }
     * </pre>
     */
    public static void put(final String pageKey, final JSONObject cachedValue) {
        check(cachedValue);

        @SuppressWarnings("unchecked")
        Set<String> keys = (Set<String>) CACHE.get(KEYS);
        if (null == keys) {
            keys = new HashSet<String>();
            CACHE.put(KEYS, keys);
        }

        try {
            final String content = cachedValue.getString(
                    AbstractCacheablePageAction.CACHED_CONTENT);
            final byte[] bytes = Serializer.serialize(content);

            cachedValue.put(CACHED_BYTES_LENGTH, bytes.length);
            cachedValue.put(CACHED_TIME, System.currentTimeMillis());
        } catch (final Exception e) {
            LOGGER.log(Level.WARNING, "Cache stat. failed[pageKey=" + pageKey
                                      + "]", e);
        }

        keys.add(pageKey);

        CACHE.put(KEYS, keys);
        CACHE.put(pageKey, cachedValue);

        LOGGER.log(Level.FINEST, "Put a page[key={0}, value={1} into page cache,"
                                 + " cached keys[{2}]",
                   new Object[]{pageKey, cachedValue, keys});
    }

    /**
     * Synchronizes the {@linkplain #PAGES keys} collection and cached page
     * objects.
     */
    public static void syncKeys() {
        @SuppressWarnings("unchecked")
        final Set<String> keys = (Set<String>) CACHE.get(KEYS);
        final Iterator<String> iterator = keys.iterator();
        while (iterator.hasNext()) {
            final String key = iterator.next();

            if (!CACHE.contains(key)) {
                iterator.remove();
                keys.remove(key);
            }
        }

        CACHE.put(KEYS, keys);
    }

    /**
     * Removes a cached pages specified by the given page key.
     * 
     * <p>
     *   <b>Note</b>: In addition to remove cached page content, invoking this 
     *   method will remove template of the cached page corresponds to.
     * </p>
     *
     * @param pageKey the given page key
     */
    public static void remove(final String pageKey) {
        CACHE.remove(pageKey);
        Templates.CACHE.clear();

        syncKeys();
    }

    /**
     * Removes all cached pages.
     *
     * <p>
     *   <b>Note</b>: This method will flush the cache for every namespace.
     * </p>
     */
    public static void removeAll() {
        CACHE.removeAll();
        Templates.CACHE.clear();

        CACHE.put(KEYS, new HashSet<String>());
        LOGGER.info("Removed all cache....");
    }

    /**
     * Checks if all keys of the specified cached page are ready.
     * 
     * @param cachedPage the specified cached page
     */
    private static void check(final JSONObject cachedPage) {
        final int numOfKeys = 4;
        if (numOfKeys != cachedPage.length()) {
            throw new IllegalArgumentException("Illegal arguments for caching page, "
                                               + "resolve this bug first!");
        }

        if (!cachedPage.has(AbstractCacheablePageAction.CACHED_CONTENT)
            || !cachedPage.has(AbstractCacheablePageAction.CACHED_OID)
            || !cachedPage.has(AbstractCacheablePageAction.CACHED_TITLE)
            || !cachedPage.has(AbstractCacheablePageAction.CACHED_TYPE)) {
            throw new IllegalArgumentException("Illegal arguments for caching page, "
                                               + "resolve this bug first!");
        }
    }

    /**
     * Private default constructor.
     */
    private PageCaches() {
    }
}
