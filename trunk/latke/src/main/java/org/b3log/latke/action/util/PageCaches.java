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

import java.util.logging.Level;
import java.util.logging.Logger;
import org.b3log.latke.Latkes;
import org.b3log.latke.RunsOnEnv;
import org.b3log.latke.cache.Cache;
import org.b3log.latke.cache.CacheFactory;
import org.b3log.latke.util.freemarker.Templates;
import org.json.JSONObject;

/**
 * Page cache.
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
 * @version 1.0.0.4, Jan 9, 2011
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
     * &lt;requestURI?queryString, page HTML content&gt;
     * </p>
     */
    private static final Cache<String, Object> CACHE;
    /**
     * Maximum count of cacheable pages.
     */
    private static final int MAX_CACHEABLE_PAGE_CNT = 1024;
    /**
     * Page cache name.
     */
    public static final String PAGE_CACHE_NAME = "page";

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
     * Gets page content with the specified page key.
     *
     * @param pageKey the specified page key
     * @return page content
     */
    public static String get(final String pageKey) {
        return (String) CACHE.get(pageKey);
    }

    /**
     * Puts a page into cache.
     *
     * @param pageKey key of the page to put
     * @param cachedValue value to put
     */
    public static void put(final String pageKey, final JSONObject cachedValue) {
        CACHE.put(pageKey, cachedValue);
    }

    /**
     * Removes a cached pages specified by the given page key.
     *
     * @param pageKey the given page key
     */
    public static void remove(final String pageKey) {
        CACHE.remove(pageKey);
        Templates.CACHE.clear();
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
    }

    /**
     * Private default constructor.
     */
    private PageCaches() {
    }
}
