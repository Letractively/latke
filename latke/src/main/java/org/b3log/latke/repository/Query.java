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
package org.b3log.latke.repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Query.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.2, Jan 20, 2011
 */
public class Query {

    /**
     * Default value of current page number.
     */
    public static final int DEFAULT_CUR_PAGE_NUM = -1;
    /**
     * Default value of page size.
     */
    public static final int DEFAULT_PAGE_SIZE = DEFAULT_CUR_PAGE_NUM;
    /**
     * Current page number.
     */
    private int currentPageNum = DEFAULT_CUR_PAGE_NUM;
    /**
     * Page size.
     */
    private int pageSize = DEFAULT_PAGE_SIZE;
    /**
     * Sorts.
     */
    private Map<String, SortDirection> sorts =
            new HashMap<String, SortDirection>();
    /**
     * Filters.
     */
    private Collection<Filter> filters = new ArrayList<Filter>();
    /**
     * Initialization value for hashing.
     */
    private static final int INIT_HASH = 5;
    /**
     * Base for hashing.
     */
    private static final int BASE = 83;

    /**
     * Adds sort for the specified property with the specified direction.
     *
     * @param propertyName the specified property name to sort
     * @param sortDirection the specified sort
     * @return the current query object
     */
    public Query addSort(final String propertyName,
                         final SortDirection sortDirection) {
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
    public Query addFilter(final String propertyName,
                           final FilterOperator filterOperator,
                           final Object value) {
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
    public Collection<Filter> getFilters() {
        return Collections.unmodifiableCollection(filters);
    }

    /**
     * Sets the filters with the specified filters.
     *
     * @param filters the specified filters
     * @return the current query object
     */
    public Query setFilters(final Collection<Filter> filters) {
        this.filters = filters;

        return this;
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
     * Sets the sorts with the specified sorts.
     *
     * @param sorts the specified sorts
     * @return the current query object
     */
    public Query setSorts(final Map<String, SortDirection> sorts) {
        this.sorts = sorts;

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
        if (this.sorts != other.sorts && (this.sorts == null || !this.sorts.
                                          equals(other.sorts))) {
            return false;
        }
        if (this.filters != other.filters && (this.filters == null || !this.filters.
                                              equals(other.filters))) {
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
        hash = BASE * hash
               + (this.filters != null ? this.filters.hashCode() : 0);

        return hash;
    }
}
