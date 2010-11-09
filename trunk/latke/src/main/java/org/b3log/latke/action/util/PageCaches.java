/*
 * Copyright (c) 2009, 2010, B3log Team
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

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.b3log.latke.Latkes;
import org.b3log.latke.RunsOnEnv;
import org.b3log.latke.cache.Cache;
import org.b3log.latke.cache.CacheFactory;

/**
 * Page cache.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.0, Oct 27, 2010
 */
// XXX:  Why GAE memcache can not clear all by namespace?
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
     * Page.
     */
    public static final String PAGE = "page";
    /**
     * Key of page keys in cache.
     */
    public static final String PAGE_KEYS = "pageKeys";

    /**
     * Initializes cache.
     */
    static {
        CACHE = CacheFactory.getCache(PAGE);
        final RunsOnEnv runsOnEnv = Latkes.getRunsOnEnv();
        if (runsOnEnv.equals(RunsOnEnv.LOCALE)) {
            CACHE.setMaxCount(MAX_CACHEABLE_PAGE_CNT);
            LOGGER.log(Level.INFO, "Initialized page cache[maxCount={0}]",
                       MAX_CACHEABLE_PAGE_CNT);
        }

        CACHE.put(PAGE_KEYS, new HashSet<String>());
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
        if (PAGE_KEYS.equals(pageKey)) {
            throw new IllegalArgumentException(
                    "key of a page put into this cache MUST differ with \"pageKeys\"");
        }

        return (String) CACHE.get(pageKey);
    }

    /**
     * Puts a page into cache.
     *
     * @param pageKey key of the page to put
     * @param pageContent content of the page to put
     */
    public static void put(final String pageKey, final String pageContent) {
        if (PAGE_KEYS.equals(pageKey)) {
            throw new IllegalArgumentException(
                    "key of a page put into this cache MUST differ with \"pageKeys\"");
        }

        @SuppressWarnings("unchecked")
        final Set<String> pageKeys = (Set<String>) CACHE.get(PAGE_KEYS);
        pageKeys.add(pageKey);
        CACHE.put(PAGE_KEYS, pageKeys);

        CACHE.put(pageKey, pageContent);
    }

    /**
     * Removes a cached pages specified by the given page key.
     *
     * @param pageKey the given page key
     */
    public static void remove(final String pageKey) {
        if (PAGE_KEYS.equals(pageKey)) {
            throw new IllegalArgumentException(
                    "key of a page put into this cache MUST differ with \"pageKeys\"");
        }

        @SuppressWarnings("unchecked")
        final Set<String> pageKeys = (Set<String>) CACHE.get(PAGE_KEYS);
        pageKeys.remove(pageKey);
        CACHE.put(PAGE_KEYS, pageKeys);

        CACHE.remove(pageKey);
    }

    /**
     * Removes all cached pages.
     */
    public static void removeAll() {
        @SuppressWarnings("unchecked")
        final Set<String> pageKeys = (Set<String>) CACHE.get(PAGE_KEYS);

        CACHE.remove(pageKeys);
        CACHE.put(PAGE_KEYS, new HashSet<String>());
    }

    /**
     * Private default constructor.
     */
    private PageCaches() {
    }
}
