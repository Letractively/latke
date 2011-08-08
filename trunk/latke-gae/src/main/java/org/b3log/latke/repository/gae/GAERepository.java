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

package org.b3log.latke.repository.gae;

import org.b3log.latke.repository.Repository;
import org.b3log.latke.repository.RepositoryException;
import org.json.JSONObject;

/**
 * Google App Engine datastore.
 *
 * <p>
 * See <a href="http://code.google.com/appengine/docs/java/javadoc/com/google/appengine/api/datastore/package-summary.html">
 * The Datastore Java API(Low-level API)</a> for more details.
 * </p>
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.1, Jan 30, 2011
 */
public interface GAERepository extends Repository {

    /**
     * Adds the specified json object with the {@linkplain #defaultParentKey
     * default parent key}.
     *
     * @param jsonObject the specified json object
     * @param parentKeyKind the specified kind of the parent key of the
     * specified json object
     * @param parentKeyName the specified name of the parent key of the
     * specified json object
     * @return the generated object id
     * @throws RepositoryException repository exception
     */
    String add(final JSONObject jsonObject,
               final String parentKeyKind, final String parentKeyName)
            throws RepositoryException;

    /**
     * The asynchronous version of interface {@linkplain #add(org.json.JSONObject)}.
     *
     * @param jsonObject the specified json object
     * @return the generated object id
     * @throws RepositoryException repository exception
     */
    String addAsync(final JSONObject jsonObject)
            throws RepositoryException;

    /**
     * The asynchronous version of interface
     * {@linkplain #add(org.json.JSONObject, java.lang.String, java.lang.String)}.
     *
     * @param jsonObject the specified json object
     * @param parentKeyKind the specified kind of the parent key of the
     * specified json object
     * @param parentKeyName the specified name of the parent key of the
     * specified json object
     * @return the generated object id
     * @throws RepositoryException repository exception
     */
    String addAsync(final JSONObject jsonObject,
                    final String parentKeyKind, final String parentKeyName)
            throws RepositoryException;

    /**
     * Updates a certain json object by the specified id and the specified new
     * json object.
     *
     * <p>
     * The parent key of the entity to update is the {@linkplain #defaultParentKey
     * default parent key}.
     * </p>
     *
     * <p>
     *   Invokes this method for an non-existent entity will create a new entity
     *   in database, as the same effect of method {@linkplain #add(org.json.JSONObject)}.
     * </p>
     *
     * <p>
     *   Update algorithm steps:
     *   <ol>
     *     <li>Sets the specified id into the specified new json object</li>
     *     <li>Creates a new entity with the specified id</li>
     *     <li>Puts the entity into database</li>
     *   </ol>
     * </p>
     *
     * <p>
     *   <b>Note</b>: the specified id is NOT the key of a database record, but
     *   the value of "oId" stored in database value entry of a record.
     * </p>
     *
     * @param id the specified id
     * @param jsonObject the specified new json object
     * @param parentKeyKind the specified kind of the parent key of the
     * specified json object
     * @param parentKeyName the specified name of the parent key of the
     * specified json object
     * @throws RepositoryException repository exception
     */
    void update(final String id, final JSONObject jsonObject,
                final String parentKeyKind, final String parentKeyName)
            throws RepositoryException;

    /**
     * The asynchronous version of interface 
     * {@linkplain #update(java.lang.String, org.json.JSONObject)}. 
     *
     * @param id the specified id
     * @param jsonObject the specified new json object
     * @throws RepositoryException repository exception
     */
    void updateAsync(final String id, final JSONObject jsonObject)
            throws RepositoryException;

    /**
     * The asynchronous version of interface
     * {@linkplain #update(java.lang.String, org.json.JSONObject, java.lang.String, java.lang.String)}.
     *
     * @param id the specified id
     * @param jsonObject the specified new json object
     * @param parentKeyKind the specified kind of the parent key of the
     * specified json object
     * @param parentKeyName the specified name of the parent key of the
     * specified json object
     * @throws RepositoryException repository exception
     */
    void updateAsync(final String id, final JSONObject jsonObject,
                     final String parentKeyKind, final String parentKeyName)
            throws RepositoryException;

    /**
     * Removes a json object by the specified id with the {@linkplain #defaultParentKey
     * default parent key}.
     *
     * @param id the specified id
     * @param parentKeyKind the specified kind of the parent key of the
     * specified json object
     * @param parentKeyName the specified name of the parent key of the
     * specified json object
     * @throws RepositoryException repository exception
     */
    void remove(final String id,
                final String parentKeyKind, final String parentKeyName)
            throws RepositoryException;

    /**
     * Gets a json object by the specified id with the {@linkplain #defaultParentKey
     * default parent key}.
     *
     * @param id the specified id
     * @param parentKeyKind the specified kind of the parent key of the
     * specified json object
     * @param parentKeyName the specified name of the parent key of the
     * specified json object
     * @return a json object, {@code null} if not found
     * @throws RepositoryException repository exception
     */
    JSONObject get(final String id,
                   final String parentKeyKind, final String parentKeyName)
            throws RepositoryException;
}
