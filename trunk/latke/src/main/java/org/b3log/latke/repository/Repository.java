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

import java.util.List;
import org.json.JSONObject;

/**
 * Repository.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.1.6, Jan 20, 2011
 */
public interface Repository {

    /**
     * Adds the specified json object.
     *
     * @param jsonObject the specified json object
     * @return the generated object id
     * @throws RepositoryException repository exception
     */
    String add(final JSONObject jsonObject) throws RepositoryException;

    /**
     * Updates a certain json object by the specified id and the specified new
     * json object.
     *
     * @param id the specified id
     * @param jsonObject the specified new json object
     * @throws RepositoryException repository exception
     */
    void update(final String id, final JSONObject jsonObject)
            throws RepositoryException;

    /**
     * Removes a json object by the specified id.
     *
     * @param id the specified id
     * @throws RepositoryException repository exception
     */
    void remove(final String id) throws RepositoryException;

    /**
     * Gets a json object by the specified id.
     *
     * @param id the specified id
     * @return a json object, {@code null} if not found
     * @throws RepositoryException repository exception
     */
    JSONObject get(final String id) throws RepositoryException;

    /**
     * Determines a json object specified by the given id exists in this 
     * repository.
     * 
     * @param id the given id
     * @return {@code true} if it exists, otherwise {@code false}
     * @throws RepositoryException repository exception
     */
    boolean has(final String id) throws RepositoryException;

    /**
     * Gets json objects by the specified query.
     *
     * <p>
     *   <b>Note</b>:the order of elements of the returned result list is
     *   decided by datastore implementation.
     * </p>
     *
     * @param query the specified query
     * @return for example,
     * <pre>
     * {
     *     "pagination": {
     *       "paginationPageCount": 88250
     *     },
     *     "rslts": [{
     *         "oId": "...."
     *     }, ....]
     * }
     * </pre>
     * @throws RepositoryException repository exception
     */
    JSONObject get(final Query query) throws RepositoryException;

    /**
     * Gets json objects by the specified current page number and page size.
     *
     * <p>
     *   <b>Note</b>:the order of elements of the returned result list is
     *   decided by datastore implementation.
     * </p>
     *
     * @param currentPageNum the specified current page number, MUST greater
     * then {@code 0}
     * @param pageSize the specified page size(count of a page contains objects),
     * MUST greater then {@code 0}
     * @return for example,
     * <pre>
     * {
     *     "pagination": {
     *       "paginationPageCount": 88250
     *     },
     *     "rslts": [{
     *         "oId": "...."
     *     }, ....]
     * }
     * </pre>
     * @throws RepositoryException repository exception
     */
//    JSONObject get(final int currentPageNum,
//                   final int pageSize) throws RepositoryException;

    /**
     * Gets json objects by the specified sorts, current page number and page
     * size.
     *
     * @param currentPageNum the specified current page number, MUST greater
     * then {@code 0}
     * @param pageSize the specified page size(count of a page contains objects),
     * MUST greater then {@code 0}
     * @param sorts the specified sort parameters
     * @return for example,
     * <pre>
     * {
     *     "pagination": {
     *       "paginationPageCount": 88250
     *     },
     *     "rslts": [{
     *         "oId": "...."
     *     }, ....]
     * }
     * </pre>
     * @throws RepositoryException repository exception
     */
//    JSONObject get(final int currentPageNum,
//                   final int pageSize,
//                   final Map<String, SortDirection> sorts)
//            throws RepositoryException;

    /**
     * Gets json objects by the specified sorts, filters, current page number
     * and page size.
     *
     * @param currentPageNum the specified current page number, MUST greater
     * then {@code 0}
     * @param pageSize the specified page size(count of a page contains objects),
     * MUST greater then {@code 0}
     * @param sorts the specified sort parameters
     * @param filters the specified filters
     * @return for example,
     * <pre>
     * {
     *     "pagination": {
     *       "paginationPageCount": 88250
     *     },
     *     "rslts": [{
     *         "oId": "...."
     *     }, ....]
     * }
     * </pre>
     * @throws RepositoryException repository exception
     */
//    JSONObject get(final int currentPageNum,
//                   final int pageSize,
//                   final Map<String, SortDirection> sorts,
//                   final Collection<Filter> filters)
//            throws RepositoryException;

    /**
     * Gets a list of json objects randomly with the specified fetch size.
     *
     * @param fetchSize the specified fetch size
     * @return a list of json objects, its size less or equal to the specified
     * fetch size, returns an empty list if not found
     * @throws RepositoryException repository exception
     */
    List<JSONObject> getRandomly(final int fetchSize)
            throws RepositoryException;

    /**
     * Gets the count of all json objects.
     *
     * @return count, returns {@code -1} if not available
     * @throws RepositoryException repository exception
     */
    long count() throws RepositoryException;

    /**
     * Gets the name of this repository.
     *
     * @return the name of this repository
     */
    String getName();

    /**
     * Begins a transaction against the repository.
     *
     * Callers are responsible for explicitly calling {@linkplain Transaction#commit()}
     * or {@linkplain Transaction#rollback()} when they no longer need the
     * {@code Transaction}. The {@code Transaction} returned by this call will
     * be considered <i>the current transaction</i> until one of the
     * following happens:
     * <ol>
     *   <li>{@linkplain #beginTransaction()} is invoked from the same thread</li>
     *   <li>{@linkplain Transaction#commit()} is invoked on the
     *        {@code Transaction} returned by this method</li>
     *   Whether or not the commit returns successfully, the {@code Transaction}
     *   will no longer be <i>the current transaction</i>.
     *   <li>{@linkplain Transaction#rollback()} is invoked on the
     *   {@code Transaction} returned by this method</li>
     *    Whether or not the rollback returns successfully, the {@code Transaction}
     *    will no longer be <i>the current transaction</i>.
     * </ol>
     * @return the transaction that was started.
     */
    Transaction beginTransaction();
}
