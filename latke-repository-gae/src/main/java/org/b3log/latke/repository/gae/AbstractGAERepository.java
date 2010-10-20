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

import com.google.appengine.api.datastore.DataTypeUtils;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import static com.google.appengine.api.datastore.FetchOptions.Builder.*;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.QueryResultList;
import com.google.appengine.api.datastore.Text;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.b3log.latke.Keys;
import org.b3log.latke.model.Pagination;
import org.b3log.latke.repository.Repository;
import org.b3log.latke.repository.RepositoryException;
import org.b3log.latke.repository.SortDirection;
import org.b3log.latke.util.CollectionUtils;
import org.b3log.latke.util.Ids;
import org.json.JSONArray;
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
 * @version 1.0.1.1, Oct 20, 2010
 */
public abstract class AbstractGAERepository implements Repository {

    /**
     * Logger.
     */
    private static final Logger LOGGER =
            Logger.getLogger(AbstractGAERepository.class.getName());
    /**
     * GAE datastore service.
     */
    public static final DatastoreService DATASTORE_SERVICE =
            DatastoreServiceFactory.getDatastoreService();
    /**
     * GAE datastore supported types.
     */
    private static final Set<Class<?>> SUPPORTED_TYPES =
            DataTypeUtils.getSupportedTypes();
    /**
     * Eventual deadline time(seconds) used by read policy.
     */
    private static final double EVENTUAL_DEADLINE = 5.0;
    /**
     * Parent key.
     */
    private final Key parent = KeyFactory.createKey("parentKind",
                                                    "parentKeyName");

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
        try {
            if (!jsonObject.has(Keys.OBJECT_ID)) {
                ret = Ids.genTimeMillisId();
                jsonObject.put(Keys.OBJECT_ID, ret);
            } else {
                ret = jsonObject.getString(Keys.OBJECT_ID);
            }

            final String kind = getName();
            final Entity entity = new Entity(kind, ret, parent);
            setProperties(entity, jsonObject);

            DATASTORE_SERVICE.put(entity);
        } catch (final Exception e) {
            LOGGER.severe(e.getMessage());
            throw new RepositoryException(e);
        }

        LOGGER.log(Level.FINER, "Added an object[oId={0}] in repository[{1}]",
                   new Object[]{ret, getName()});

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
            LOGGER.log(Level.FINER,
                       "Updating an object[oId={0}] in repository[name={1}]",
                       new Object[]{id, getName()});
            // Step 1, 2:
            remove(id);
            // Step 3:
            jsonObject.put(Keys.OBJECT_ID, id);
            // Step 4:
            add(jsonObject);
            LOGGER.log(Level.FINER,
                       "Updated an object[oId={0}] in repository[name={1}]",
                       new Object[]{id, getName()});
        } catch (final Exception e) {
            LOGGER.severe(e.getMessage());
            throw new RepositoryException(e);
        }
    }

    @Override
    public void remove(final String id) throws RepositoryException {
        final Key key = KeyFactory.createKey(parent, getName(), id);
        DATASTORE_SERVICE.delete(key);
        LOGGER.log(Level.FINER,
                   "Removed an object[oId={0}] from repository[name={1}]",
                   new Object[]{id, getName()});
    }

    @Override
    public JSONObject get(final String id) throws RepositoryException {
        JSONObject ret = null;

        final Key key = KeyFactory.createKey(parent, getName(), id);

        try {
            final Entity entity = DATASTORE_SERVICE.get(key);
            ret = entity2JSONObject(entity);

            LOGGER.log(Level.FINER,
                       "Got an object[oId={0}] from repository[name={1}]",
                       new Object[]{id,
                                    getName()});
        } catch (final EntityNotFoundException e) {
            LOGGER.log(Level.WARNING,
                       "Not found an object[oId={0}] in repository[name={1}]",
                       new Object[]{id, getName()});
        }

        return ret;
    }

    @Override
    public boolean has(final String id) throws RepositoryException {
        final Query query = new Query(getName());
        query.addFilter(Keys.OBJECT_ID, Query.FilterOperator.EQUAL, id);
        final PreparedQuery preparedQuery = DATASTORE_SERVICE.prepare(query);

        return 0 == preparedQuery.countEntities(
                FetchOptions.Builder.withDefaults()) ? false : true;
    }

    @Override
    public JSONObject get(final int currentPageNum, final int pageSize)
            throws RepositoryException {
        final Query query = new Query(getName());
        final PreparedQuery preparedQuery = DATASTORE_SERVICE.prepare(query);

        final int count = preparedQuery.countEntities(
                FetchOptions.Builder.withDefaults());
        final int pageCount =
                (int) Math.ceil((double) count / (double) pageSize);

        final JSONObject ret = new JSONObject();
        try {
            final JSONObject pagination = new JSONObject();
            ret.put(Pagination.PAGINATION, pagination);
            pagination.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);

            final int offset = pageSize * (currentPageNum - 1);
            final QueryResultList<Entity> queryResultList =
                    preparedQuery.asQueryResultList(
                    withOffset(offset).limit(pageSize));

            final JSONArray results = new JSONArray();
            for (final Entity entity : queryResultList) {
                final JSONObject jsonObject = entity2JSONObject(entity);

                results.put(jsonObject);
            }

            ret.put(Keys.RESULTS, results);

            LOGGER.log(Level.FINER,
                       "Found objects[size={0}] at page[currentPageNum={1}, pageSize={2}] in repository[{3}]",
                       new Object[]{results.length(),
                                    currentPageNum,
                                    pageSize,
                                    getName()});
        } catch (final JSONException e) {
            LOGGER.severe(e.getMessage());
            throw new RepositoryException(e);
        }

        return ret;
    }

    @Override
    public JSONObject get(final int currentPageNum,
                          final int pageSize,
                          final String sortPropertyName,
                          final SortDirection sortDirection,
                          final String... exceptedIds)
            throws RepositoryException {
        final Query query = new Query(getName());
        for (final String id : exceptedIds) {
            query.addSort(Keys.OBJECT_ID, Query.SortDirection.DESCENDING);
            query.addFilter(Keys.OBJECT_ID, Query.FilterOperator.NOT_EQUAL, id);
        }

        Query.SortDirection querySortDirection = null;
        if (sortDirection.equals(SortDirection.ASCENDING)) {
            querySortDirection = Query.SortDirection.ASCENDING;
        } else {
            querySortDirection = Query.SortDirection.DESCENDING;
        }
        query.addSort(sortPropertyName, querySortDirection);

        final PreparedQuery preparedQuery = DATASTORE_SERVICE.prepare(query);
        final int count = preparedQuery.countEntities(
                FetchOptions.Builder.withDefaults());
        final int pageCount =
                (int) Math.ceil((double) count / (double) pageSize);

        final JSONObject ret = new JSONObject();
        try {
            final JSONObject pagination = new JSONObject();
            ret.put(Pagination.PAGINATION, pagination);
            pagination.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);

            final int offset = pageSize * (currentPageNum - 1);
            final QueryResultList<Entity> queryResultList =
                    preparedQuery.asQueryResultList(
                    withOffset(offset).limit(pageSize));

            final JSONArray results = new JSONArray();
            for (final Entity entity : queryResultList) {
                final JSONObject jsonObject = entity2JSONObject(entity);

                results.put(jsonObject);
            }

            ret.put(Keys.RESULTS, results);

            LOGGER.log(Level.FINER,
                       "Found objects[size={0}] at page[currentPageNum={1}, pageSize={2}] in repository[{3}]",
                       new Object[]{results.length(),
                                    currentPageNum,
                                    pageSize,
                                    getName()});
        } catch (final JSONException e) {
            LOGGER.severe(e.getMessage());
            throw new RepositoryException(e);
        }

        return ret;
    }

    @Override
    public List<JSONObject> getRandomly(final int fetchSize)
            throws RepositoryException {
        final List<JSONObject> ret = new ArrayList<JSONObject>();
        final Query query = new Query(getName());
        final PreparedQuery preparedQuery = DATASTORE_SERVICE.prepare(query);
        final int count = preparedQuery.countEntities(
                FetchOptions.Builder.withDefaults());

        if (0 == count) {
            return ret;
        }

        final Iterable<Entity> entities = preparedQuery.asIterable();

        if (fetchSize >= count) {
            for (final Entity entity : entities) {
                final JSONObject jsonObject = entity2JSONObject(entity);
                ret.add(jsonObject);
            }

            return ret;
        }

        final List<Integer> fetchIndexes =
                CollectionUtils.getRandomIntegers(0, count - 1, fetchSize);

        int index = 0;
        for (final Entity entity : entities) { // XXX: performance issue
            index++;

            if (fetchIndexes.contains(index)) {
                final JSONObject jsonObject = entity2JSONObject(entity);
                ret.add(jsonObject);
            }
        }

        return ret;
    }

    @Override
    public long count() {
        final Query query = new Query(getName());
        final PreparedQuery preparedQuery = DATASTORE_SERVICE.prepare(query);
        return preparedQuery.countEntities(FetchOptions.Builder.withDefaults());
    }

    /**
     * Converts the specified {@link Entity entity} to a {@link JSONObject
     * json object}.
     *
     * @param entity the specified entity
     * @return converted json object
     */
    public JSONObject entity2JSONObject(final Entity entity) {
        final Map<String, Object> properties = entity.getProperties();
        final Map<String, Object> jsonMap = new HashMap<String, Object>();

        for (Map.Entry<String, Object> property : properties.entrySet()) {
            final String k = property.getKey();
            final Object v = property.getValue();
            if (v instanceof Text) {
                final Text valueText = (Text) v;
                LOGGER.log(Level.FINEST, "Put[key={0}, value={1}]",
                           new Object[]{k, valueText});
                jsonMap.put(k, valueText.getValue());
            } else {
                LOGGER.log(Level.FINEST, "Put[key={0}, value={1}]",
                           new Object[]{k, v});
                jsonMap.put(k, v);
            }
        }

        return new JSONObject(jsonMap);
    }

    /**
     * Sets the properties of the specified entity by the specified json object.
     *
     * @param entity the specified entity
     * @param jsonObject the specified json object
     * @throws JSONException json exception
     */
    public final void setProperties(final Entity entity,
                                    final JSONObject jsonObject)
            throws JSONException {
        @SuppressWarnings("unchecked")
        final Iterator<String> keys = jsonObject.keys();
        while (keys.hasNext()) {
            final String key = keys.next();
            final Object value = jsonObject.get(key);
            if (!SUPPORTED_TYPES.contains(value.getClass())) {
                throw new RuntimeException("Unsupported type[class=" + value.
                        getClass() + "] in Latke GAE repository");
            }

            if (value instanceof String) {
                final String valueString = (String) value;
                if (valueString.length()
                    > DataTypeUtils.MAX_STRING_PROPERTY_LENGTH) {
                    final Text text = new Text(valueString);

                    entity.setProperty(key, text);
                } else {
                    entity.setProperty(key, value);
                }
            } else if (value instanceof Number || value instanceof Date
                           || value instanceof Boolean) {
                entity.setProperty(key, value);
            } else {
                throw new RuntimeException("Need to add known data type[" + value.
                        getClass() + "]");
            }
        }
    }
}
