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

package org.b3log.latke.repository;

import java.util.List;
import org.json.JSONObject;

/**
 * Repository.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.0, Jul 23, 2010
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
     * Gets json objects by the specified current page number and page size.
     *
     * @param currentPageNum the specified current page number, MUST grater
     * then 0
     * @param pageSize the specified page size(count of a page contains objects),
     * MUST grater then 0
     * @return the first element depicts the 
     * {@link org.b3log.latke.model.Pagination pagination info}, the rest of
     * elements are objects, for example:
     * <pre>
     * [
     *   {
     *     "pagination": {
     *       "paginationPageCount": 88250}
     *   },
     *   {
     *     "oId": "...."
     *   }, ....
     * ], if not found any objects by the specified current page number and
     * page size, returns pagination info as the only element of the returned
     * list
     * </pre>
     * @throws RepositoryException repository exception
     * @see org.b3log.latke.model.Pagination
     */
    List<JSONObject> get(final int currentPageNum,
                         final int pageSize) throws RepositoryException;

    /**
     * Gets the name of this repository.
     *
     * @return the name of this repository
     */
    String getName();
}
