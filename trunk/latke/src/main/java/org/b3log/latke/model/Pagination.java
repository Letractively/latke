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
package org.b3log.latke.model;

/**
 * This class defines all pagination model relevant keys.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.1, Sep 2, 2010
 */
public final class Pagination {

    /**
     * Pagination.
     */
    public static final String PAGINATION = "pagination";
    /**
     * Pagination page count.
     */
    public static final String PAGINATION_PAGE_COUNT = "paginationPageCount";
    /**
     * Pagination current page number.
     */
    public static final String PAGINATION_CURRENT_PAGE_NUM =
            "paginationCurrentPageNum";
    /**
     * Pagination page size.
     */
    public static final String PAGINATION_PAGE_SIZE = "paginationPageSize";
    /**
     * Pagination window size.
     */
    public static final String PAGINATION_WINDOW_SIZE = "paginationWindowSize";
    /**
     * Pagination page numbers.
     */
    public static final String PAGINATION_PAGE_NUMS = "paginationPageNums";
    /**
     * Pagination first page number.
     */
    public static final String PAGINATION_FIRST_PAGE_NUM =
            "paginationFirstPageNum";
    /**
     * Pagination last page number.
     */
    public static final String PAGINATION_LAST_PAGE_NUM =
            "paginationLastPageNum";

    /**
     * Private constructor.
     */
    private Pagination() {
    }
}