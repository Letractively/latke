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
package org.b3log.latke.repository.gae;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import static com.google.appengine.api.datastore.FetchOptions.Builder.*;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.QueryResultList;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.log4j.Logger;
import org.b3log.latke.Keys;
import org.b3log.latke.model.Pagination;
import org.b3log.latke.repository.Repository;
import org.b3log.latke.repository.RepositoryException;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Google App Engine datastore.
 * <p>
 *   See <a href="http://code.google.com/appengine/docs/java/javadoc/com/google/appengine/api/datastore/package-summary.html">
 *   The Datastore Java API(Low-level API)</a> for more details.
 * </p>
 * 
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.0, Aug 4, 2010
 */
public abstract class AbstractGAERepository implements Repository {

    /**
     * Logger.
     */
    private static final Logger LOGGER =
            Logger.getLogger(AbstractGAERepository.class);
    /**
     * Lock for unique key generation.
     */
    private static final Lock KEY_GEN_LOCK = new ReentrantLock();
    /**
     * Sleep millisecond.
     */
    private static final long KEY_GEN_SLEEP_MILLIS = 5;
    /**
     * GAE datastore service.
     */
    private static final DatastoreService DATASTORE_SERVICE =
            DatastoreServiceFactory.getDatastoreService();

    /**
     * Adds the specified json object.
     *
     * <p>
     *   The stored record looks like:
     *   <pre>
     *   key = {
     *     "oId": key
     *   }
     *   </pre>
     *   The key is generated by current time mills, and it will be used for
     *   database key entry sorting. If user need to update an certain json
     *   object, just {@linkplain #remove(java.lang.String) removes} the old
     *   json object by key, and invoke this method to add the new json object
     *   which the value of "oId" as the same as the old one, the "oId" will
     *   NOT be generated because it exists.
     * </p>
     * @param jsonObject the specified json object
     * @return the generated object id
     * @throws RepositoryException repository exception
     * @see #update(java.lang.String, org.json.JSONObject)
     * @see Keys#OBJECT_ID
     */
    @Override
    public String add(final JSONObject jsonObject) throws RepositoryException {
        String ret = null;
        KEY_GEN_LOCK.lock();
        try {
            ret = String.valueOf(System.currentTimeMillis());

            try {
                Thread.sleep(KEY_GEN_SLEEP_MILLIS);
            } catch (final InterruptedException e) {
                LOGGER.warn(e.getMessage(), e);
            }
        } finally {
            KEY_GEN_LOCK.unlock();
        }

        if (null == ret) {
            throw new RuntimeException("Time millis key generation fail!");
        }

        try {
            if (!jsonObject.has(Keys.OBJECT_ID)) {
                jsonObject.put(Keys.OBJECT_ID, ret);
            }

            final String kind = getName();
            final Entity entity = new Entity(kind, ret);
            entity.setProperty(Keys.DATA, jsonObject.toString());

            DATASTORE_SERVICE.put(entity);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new RepositoryException(e);
        }

        LOGGER.debug("Added object[id=" + ret + "] in repository["
                + getName() + "]");

        return ret;
    }

    /**
     * Updates a certain json object by the specified id and the specified new
     * json object.
     *
     * <p>
     *   Update algorithm steps:
     *   <ol>
     *     <li>Finds the old record by the id stored in database value entry</li>
     *     O(n)
     *     <li>Removes the found old record if exists</li>
     *     <li>Sets id of the old one into the specified new json object</li>
     *     <li>Invokes {@linkplain #add(org.json.JSONObject) add} with the
     *         new json object as argument
     *     </li>
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
     * @throws RepositoryException repository exception
     * @see Keys#OBJECT_ID
     */
    @Override
    public void update(final String id, final JSONObject jsonObject)
            throws RepositoryException {
        try {
            LOGGER.debug("Updating object[id=" + id + "] in repository[name="
                    + getName() + "]");
            // step 1, 2:
            remove(id);
            // step 3:
            jsonObject.put(Keys.OBJECT_ID, id);
            // step 4:
            add(jsonObject);
            LOGGER.debug("Updated object[id=" + id + "] in repository[name="
                    + getName() + "]");
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new RepositoryException(e);
        }
    }

    @Override
    public void remove(final String id) throws RepositoryException {
        final Key key = KeyFactory.createKey(getName(), id);
        DATASTORE_SERVICE.delete(key);
        LOGGER.debug("Removed object[id=" + id + "] from "
                + "repository[name=" + getName() + "]");
    }

    @Override
    public JSONObject get(final String id) throws RepositoryException {
        JSONObject ret = null;

        final Key key = KeyFactory.createKey(getName(), id);
        try {
            final Entity entity = DATASTORE_SERVICE.get(key);
            final String jsonObjectString =
                    (String) entity.getProperty(Keys.DATA);
            ret = new JSONObject(jsonObjectString);

            LOGGER.debug("Got an object[id=" + id + "] from "
                    + "repository[name=" + getName() + "]");
        } catch (final EntityNotFoundException e) {
            LOGGER.warn("Not found an object[id=" + id + "] in repository[name="
                    + getName() + "]");
        } catch (final JSONException e) {
            LOGGER.error(e.getMessage(), e);
            throw new RepositoryException(e);
        }

        return ret;
    }

    @Override
    public List<JSONObject> get(final int currentPageNum, final int pageSize)
            throws RepositoryException {
        final List<JSONObject> ret = new ArrayList<JSONObject>();
        final JSONObject pagination = new JSONObject();
        ret.add(pagination);

        final Query query = new Query(getName());
        final PreparedQuery preparedQuery = DATASTORE_SERVICE.prepare(query);

        final int count = preparedQuery.countEntities();
        final int pageCount =
                (int) Math.ceil((double) count / (double) pageSize);
        try {
            pagination.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);


            final int offset = pageSize * (currentPageNum - 1);
            final QueryResultList<Entity> queryResultList =
                    preparedQuery.asQueryResultList(
                    withOffset(offset).limit(pageSize));

            for (final Entity entity : queryResultList) {
                final String jsonObjectString =
                        (String) entity.getProperty(Keys.DATA);
                final JSONObject jsonObject = new JSONObject(jsonObjectString);

                ret.add(jsonObject);
            }
        } catch (final JSONException e) {
            LOGGER.error(e.getMessage(), e);
            throw new RepositoryException(e);
        }

        LOGGER.debug("Found objects[size=" + (ret.size() - 1) + "] at page"
                + "[currentPageNum=" + currentPageNum + ", pageSize="
                + pageSize + "] in repository[" + getName() + "]");

        return ret;
    }
}
