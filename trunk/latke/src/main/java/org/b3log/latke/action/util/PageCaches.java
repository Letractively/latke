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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.b3log.latke.Latkes;
import org.b3log.latke.RunsOnEnv;
import org.b3log.latke.action.AbstractCacheablePageAction;
import org.b3log.latke.cache.Cache;
import org.b3log.latke.cache.CacheFactory;
import org.b3log.latke.util.freemarker.Templates;
import org.json.JSONObject;

/**
 * Page cache.
 * 
 * <p>
 *   This cache contains some pages and their statistics as the following: 
 *   <pre>
 *     &lt;"URL a", JSONObject{oId, title, title, type}&gt;
 *     &lt;"URL b", JSONObject{oId, title, title, type}&gt;
 *     ....
 *     &lt;{@value #PAGES}, Map&lt;"URL", JSONObject{TODO: statistic info}&gt;&gt;
 *   </pre>
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
 * @version 1.0.0.5, Jun 18, 2011
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
    public static final String PAGES = "pages";

    /**
     * Initializes the cache.
     */
    static {
        CACHE = CacheFactory.getCache(PAGE_CACHE_NAME);
        final RunsOnEnv runsOnEnv = Latkes.getRunsOnEnv();
        if (runsOnEnv.equals(RunsOnEnv.LOCAL)) {
            CACHE.setMaxCount(MAX_CACHEABLE_PAGE_CNT);
            LOGGER.log(Level.INFO, "Initialized page cache[maxCount={0}]",
                       MAX_CACHEABLE_PAGE_CNT);
        }

        @SuppressWarnings("unchecked")
        Map<String, ?> pages = (Map<String, ?>) CACHE.get(PAGES);
        if (null == pages) {
            pages = new HashMap<String, JSONObject>();
            CACHE.put(PAGES, pages);
        }
    }

    /**
     * Gets all cached page keys.
     * 
     * @return cached page keys, returns an empty set if not found
     */
    @SuppressWarnings("unchecked")
    public static Set<String> getKeys() {
        final Map<String, ?> keys = (Map<String, ?>) CACHE.get(PAGES);

        // TODO: sort

        return Collections.unmodifiableSet(keys.keySet());
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
     * Gets a cached page with the specified page key. 
     * <pre>
     * </pre>
     * "URL a", JSONObject{oId, title, title, type}&gt;
     *
     * @param pageKey the specified page key
     * @return for example,
     * <pre>
     * {
     *     {@value AbstractCacheablePageAction#CACHED_CONTENT}": "",
     *     {@value AbstractCacheablePageAction#CACHED_OID}: "",
     *     {@value AbstractCacheablePageAction#CACHED_TITLE}: "",
     *     {@value AbstractCacheablePageAction#CACHED_TYPE}: ""
     * }
     * </pre>
     */
    public static JSONObject get(final String pageKey) {
        return (JSONObject) CACHE.get(pageKey);
    }

    /**
     * Puts a page into cache.
     *
     * @param pageKey key of the page to put
     * @param cachedValue value to put, for example, 
     * <pre>
     * {
     *     {@value AbstractCacheablePageAction#CACHED_CONTENT}": "",
     *     {@value AbstractCacheablePageAction#CACHED_OID}: "",
     *     {@value AbstractCacheablePageAction#CACHED_TITLE}: "",
     *     {@value AbstractCacheablePageAction#CACHED_TYPE}: ""
     * }
     * </pre>
     */
    public static void put(final String pageKey, final JSONObject cachedValue) {
        check(cachedValue);

        CACHE.put(pageKey, cachedValue);

        @SuppressWarnings("unchecked")
        final Map<String, JSONObject> pages =
                (Map<String, JSONObject>) CACHE.get(PAGES);
        JSONObject stat = pages.get(pageKey);
        if (null == stat) {
            stat = new JSONObject();
        }

        // TODO: page stat. info (put count, etc)
        pages.put(pageKey, stat);
        CACHE.put(PAGES, pages);

        LOGGER.log(Level.FINEST, "Put a page[key={0}, value={1} into page cache,"
                                 + " cached pages[{2}]",
                   new Object[]{pageKey, cachedValue, pages});
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

        @SuppressWarnings("unchecked")
        final Map<String, JSONObject> keys =
                (Map<String, JSONObject>) CACHE.get(PAGES);
        keys.remove(pageKey);
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

        CACHE.put(PAGES, new HashMap<String, JSONObject>());
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
