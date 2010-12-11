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
 * @version 1.0.0.1, Dec 11, 2010
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
    private Map<String, SortDirection> sorts
            = new HashMap<String, SortDirection>();
    /**
     * Filters.
     */
    private Collection<Filter> filters = new ArrayList<Filter>();

    /**
     * Adds sort for the specified property with the specified direction.
     *
     * @param propertyName the specified property name to sort
     * @param sortDirection the specified sort
     */
    public void addSort(final String propertyName,
                        final SortDirection sortDirection) {
        sorts.put(propertyName, sortDirection);
    }

    /**
     * Adds a filter for the specified property with the specified operator and
     * property value.
     *
     * @param propertyName the specified property name to sort
     * @param filterOperator th specified operator
     * @param value the specified property value
     */
    public void addFilter(final String propertyName,
                          final FilterOperator filterOperator,
                          final Object value) {
        filters.add(new Filter(propertyName, filterOperator, value));
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
     */
    public void setCurrentPageNum(final int currentPageNum) {
        this.currentPageNum = currentPageNum;
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
     */
    public void setPageSize(final int pageSize) {
        this.pageSize = pageSize;
    }

    /**
     * Gets the sorts.
     *
     * @return sorts
     */
    public Map<String, SortDirection> getSorts() {
        return Collections.unmodifiableMap(sorts);
    }
}
