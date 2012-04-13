/*
 * Copyright (c) 2009, 2010, 2011, 2012, B3log Team
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
package org.b3log.latke.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.b3log.latke.util.Strings;

/**
 * Query.
 * 
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.8, Apr 13, 2012
 */
public final class Query {

    /**
     * Current page number.
     */
    private int currentPageNum = 1;
    /**
     * Page count.
     */
    private Integer pageCount;
    /**
     * Page size.
     */
    private int pageSize = Integer.MAX_VALUE;
    /**
     * Cache key.
     * 
     * <p>
     * If the repository executes this query {@link Repository#isCacheEnabled() enabled}
     * query results caching, this field will be used as the key of the cached 
     * results.
     * </p>
     */
    private String cacheKey;
    /**
     * Sorts.
     */
    private Map<String, SortDirection> sorts = new LinkedHashMap<String, SortDirection>();
    /**
     * Filters.
     */
    private List<Filter> filters = new ArrayList<Filter>();
    /**
     * Indices.
     */
    private Set<String[]> indexes = new HashSet<String[]>();
    /**
     * Initialization value for hashing.
     */
    private static final int INIT_HASH = 5;
    /**
     * Base for hashing.
     */
    private static final int BASE = 83;

    /**
     * Indexes the specified properties for future queries.
     * 
     * @param properties the specified properties
     * @return the current query object
     */
    public Query index(final String... properties) {
        if (null == properties || 0 == properties.length) {
            return this;
        }

        indexes.add(properties);

        return this;
    }

    /**
     * Gets the indices.
     * 
     * @return indices
     */
    public Set<String[]> getIndexes() {
        return Collections.unmodifiableSet(indexes);
    }

    /**
     * Adds sort for the specified property with the specified direction.
     *
     * @param propertyName the specified property name to sort
     * @param sortDirection the specified sort
     * @return the current query object
     */
    public Query addSort(final String propertyName, final SortDirection sortDirection) {
        sorts.put(propertyName, sortDirection);

        return this;
    }

    /**
     * Adds a filter for the specified property with the specified operator and
     * property value.
     *
     * @param propertyName the specified property name to sort
     * @param filterOperator th specified operator
     * @param value the specified property value
     * @return the current query object
     */
    public Query addFilter(final String propertyName, final FilterOperator filterOperator, final Object value) {
        filters.add(new Filter(propertyName, filterOperator, value));

        return this;
    }

    /**
     * Gets the current page number.
     *
     * <p>
     *   <b>Note</b>: The default value of the current page number is
     *   {@code -1}.
     * </p>
     *
     * @return current page number
     */
    public int getCurrentPageNum() {
        return currentPageNum;
    }

    /**
     * Sets the current page number with the specified current page number.
     *
     * @param currentPageNum the specified current page number
     * @return the current query object
     */
    public Query setCurrentPageNum(final int currentPageNum) {
        this.currentPageNum = currentPageNum;

        return this;
    }

    /**
     * Gets the filters.
     *
     * @return filters
     */
    public List<Filter> getFilters() {
        return Collections.unmodifiableList(filters);
    }

    /**
     * Sets the page size.
     *
     * <p>
     *   <b>Note</b>: The default value of the page size {@code -1}.
     * </p>
     *
     * @return page size
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     * Sets the page size with the specified page size.
     *
     * @param pageSize the specified page size
     * @return the current query object
     */
    public Query setPageSize(final int pageSize) {
        this.pageSize = pageSize;

        return this;
    }

    /**
     * Gets the sorts.
     *
     * @return sorts
     */
    public Map<String, SortDirection> getSorts() {
        return Collections.unmodifiableMap(sorts);
    }

    /**
     * Sets the cache key with the specified cache key.
     * 
     * @param cacheKey the specified cache key
     * @return the current query object 
     */
    public Query setCacheKey(final String cacheKey) {
        this.cacheKey = cacheKey;

        return this;
    }

    /**
     * Gets the cache key.
     * 
     * <p>
     * If no application specified cache key, uses {@link #hashCode() the hash 
     * code} of this query.
     * </p>
     * 
     * @return cache key
     */
    public String getCacheKey() {
        if (Strings.isEmptyOrNull(cacheKey)) {
            setCacheKey(String.valueOf(hashCode()));
        }

        return cacheKey;
    }

    /**
     * Gets the page count.
     * 
     * @return page count
     */
    public Integer getPageCount() {
        return pageCount;
    }

    /**
     * Sets the page count with the specified page count.
     * 
     * @param pageCount the specified page count
     * @return the current query object 
     */
    public Query setPageCount(final int pageCount) {
        this.pageCount = pageCount;

        return this;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        final Query other = (Query) obj;
        if (this.currentPageNum != other.currentPageNum) {
            return false;
        }

        if (this.pageSize != other.pageSize) {
            return false;
        }

        if (this.sorts != other.sorts
            && (this.sorts == null || !this.sorts.equals(other.sorts))) {
            return false;
        }

        if (this.filters != other.filters
            && (this.filters == null || !this.filters.equals(other.filters))) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int hash = INIT_HASH;
        hash = BASE * hash + this.currentPageNum;
        hash = BASE * hash + this.pageSize;
        hash = BASE * hash + (this.sorts != null ? this.sorts.hashCode() : 0);
        hash = BASE * hash + (this.filters != null ? this.filters.hashCode() : 0);

        return hash;
    }

    @Override
    public String toString() {
        final StringBuilder stringBuilder = new StringBuilder("currentPageNum=").append(currentPageNum).
                append(", pageSize=").append(pageSize).append(", pageCount=").append(pageCount).append(", sorts=[");

        final Set<Entry<String, SortDirection>> entrySet = sorts.entrySet();
        final Iterator<Entry<String, SortDirection>> iterator = entrySet.iterator();
        while (iterator.hasNext()) {
            final Entry<String, SortDirection> sort = iterator.next();
            stringBuilder.append("[key=").append(sort.getKey()).append(", direction=").append(sort.getValue().name()).append("]");

            if (iterator.hasNext()) {
                stringBuilder.append(", ");
            }
        }

        stringBuilder.append("], filters=[");
        for (final Filter filter : filters) {
            stringBuilder.append(filter.toString());
        }
        stringBuilder.append("]");

        return stringBuilder.toString();
    }
}
