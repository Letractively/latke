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
import com.google.appengine.api.utils.SystemProperty;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.b3log.latke.Keys;
import org.b3log.latke.Latkes;
import org.b3log.latke.RunsOnEnv;
import org.b3log.latke.cache.Cache;
import org.b3log.latke.cache.CacheFactory;
import org.b3log.latke.model.Pagination;
import org.b3log.latke.repository.Filter;
import org.b3log.latke.repository.Repository;
import org.b3log.latke.repository.Transaction;
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
 * @version 1.0.2.5, Jan 20, 2011
 */
// XXX: (1) ID generation in cluster issue
//      (2) All entities store in the same entity group? 
// TODO: pagination query cache
public abstract class AbstractGAERepository implements Repository {

    /**
     * Logger.
     */
    private static final Logger LOGGER =
            Logger.getLogger(AbstractGAERepository.class.getName());
    /**
     * GAE datastore service.
     */
    private final DatastoreService datastoreService =
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
     * Repository cache.
     * <p>
     * &lt;oId, JSONObject&gt;
     * </p>
     */
    private static final Cache<String, Object> CACHE;
    /**
     * Repository cache name.
     */
    public static final String REPOSITORY_CACHE_NAME = "repositoryCache";
    /**
     * Repository cache count.
     */
    public static final String REPOSITORY_CACHE_COUNT = "#count";
    /**
     * Is cache enabled?
     */
    private boolean isCacheEnabled = true;
    /**
     * Instance replica id.
     */
    private static final String INSTANCE_ID = SystemProperty.instanceReplicaId.
            get() + "_";

    /**
     * Initializes cache.
     */
    static {
        final RunsOnEnv runsOnEnv = Latkes.getRunsOnEnv();
        if (!runsOnEnv.equals(RunsOnEnv.GAE)) {
            throw new RuntimeException(
                    "GAE repository can only runs on Google App Engine, please "
                    + "check your configuration and make sure "
                    + "Latkes.setRunsOnEnv(RunsOnEnv.GAE) was invoked before "
                    + "using GAE repository.");
        }

        CACHE = CacheFactory.getCache(REPOSITORY_CACHE_NAME);
    }

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

            datastoreService.put(entity);
        } catch (final Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new RepositoryException(e);
        }

        LOGGER.log(Level.FINER, "Added an object[oId={0}] in repository[{1}]",
                   new Object[]{ret, getName()});

        if (isCacheEnabled) {
            final String key = INSTANCE_ID + ret;
            CACHE.put(key, jsonObject);
            LOGGER.log(Level.FINER,
                       "Added an object[cacheKey={0}] in repository cache[{1}]",
                       new Object[]{key, getName()});
        }

        return ret;
    }

    /**
     * Updates a certain json object by the specified id and the specified new
     * json object.
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
     * @throws RepositoryException repository exception
     * @see Keys#OBJECT_ID
     */
    @Override
    public void update(final String id, final JSONObject jsonObject)
            throws RepositoryException {
        try {
            jsonObject.put(Keys.OBJECT_ID, id);

            final String kind = getName();
            final Entity entity = new Entity(kind, id, parent);
            setProperties(entity, jsonObject);

            datastoreService.put(entity);
        } catch (final Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new RepositoryException(e);
        }

        LOGGER.log(Level.FINER,
                   "Updated an object[oId={0}] in repository[name={1}]",
                   new Object[]{id, getName()});

        if (isCacheEnabled) {
            final String key = INSTANCE_ID + id;
            CACHE.put(key, jsonObject);
            LOGGER.log(Level.FINER,
                       "Updated an object[cacheKey={0}] in repository cache[{1}]",
                       new Object[]{key, getName()});
        }
    }

    @Override
    public void remove(final String id) throws RepositoryException {
        final Key key = KeyFactory.createKey(parent, getName(), id);
        datastoreService.delete(key);
        LOGGER.log(Level.FINER,
                   "Removed an object[oId={0}] from repository[name={1}]",
                   new Object[]{id, getName()});

        if (isCacheEnabled) {
            final String cacheKey = INSTANCE_ID + id;
            CACHE.remove(cacheKey);
            LOGGER.log(Level.FINER,
                       "Removed an object[cacheKey={0}] in repository cache[{1}]",
                       new Object[]{cacheKey, getName()});
        }
    }

    @Override
    public JSONObject get(final String id) throws RepositoryException {
        JSONObject ret = null;

        if (isCacheEnabled) {
            final String cacheKey = INSTANCE_ID + id;
            ret = (JSONObject) CACHE.get(cacheKey);
            if (null != ret) {
                LOGGER.log(Level.FINER,
                           "Got an object[cacheKey={0}] from repository cache[name={1}]",
                           new Object[]{cacheKey, getName()});
                return ret;
            }
        }

        final Key key = KeyFactory.createKey(parent, getName(), id);
        try {
            final Entity entity = datastoreService.get(key);
            ret = entity2JSONObject(entity);

            LOGGER.log(Level.FINER,
                       "Got an object[oId={0}] from repository[name={1}]",
                       new Object[]{id, getName()});

            if (isCacheEnabled) {
                final String cacheKey = INSTANCE_ID + id;
                CACHE.put(cacheKey, ret);
                LOGGER.log(Level.FINER,
                           "Added an object[cacheKey={0}] in repository cache[{1}]",
                           new Object[]{cacheKey, getName()});
            }
        } catch (final EntityNotFoundException e) {
            LOGGER.log(Level.WARNING,
                       "Not found an object[oId={0}] in repository[name={1}]",
                       new Object[]{id, getName()});
            return null;
        }

        return ret;
    }

    @Override
    public boolean has(final String id) throws RepositoryException {
        if (isCacheEnabled) {
            if (null != CACHE.get(id)) {
                return true;
            }
        }

        final Query query = new Query(getName());
        query.addFilter(Keys.OBJECT_ID, Query.FilterOperator.EQUAL, id);
        final PreparedQuery preparedQuery = datastoreService.prepare(query);

        return 0 == preparedQuery.countEntities(
                FetchOptions.Builder.withDefaults()) ? false : true;
    }

    @Override
    public JSONObject get(final org.b3log.latke.repository.Query query)
            throws RepositoryException {
        JSONObject ret = null;

        if (isCacheEnabled) {
            final String cacheKey = INSTANCE_ID + query.hashCode() + "_"
                                    + getName();
            ret = (JSONObject) CACHE.get(cacheKey);
            if (null != ret) {
                LOGGER.log(Level.FINER,
                           "Got query result[cacheKey={0}] from repository cache[name={1}]",
                           new Object[]{cacheKey, getName()});
                return ret;
            }
        }

        final int currentPageNum = query.getCurrentPageNum();
        final Collection<Filter> filters = query.getFilters();
        final int pageSize = query.getPageSize();
        final Map<String, SortDirection> sorts = query.getSorts();

        if (org.b3log.latke.repository.Query.DEFAULT_CUR_PAGE_NUM
            != currentPageNum
            && org.b3log.latke.repository.Query.DEFAULT_PAGE_SIZE != pageSize) {
            ret = get(currentPageNum, pageSize, sorts, filters);
        } else {
            ret = get(1, Integer.MAX_VALUE, sorts, filters);
        }

        if (isCacheEnabled) {
            final String cacheKey = INSTANCE_ID + query.hashCode() + "_"
                                    + getName();
            CACHE.put(cacheKey, ret);
            LOGGER.log(Level.FINER,
                       "Added query result[cacheKey={0}] in repository cache[{1}]",
                       new Object[]{cacheKey, getName()});
        }

        return ret;
    }

    /**
     * Gets the result object by the specified current page number, page size,
     * sorts and filters.
     *
     * @param currentPageNum the specified current page number
     * @param pageSize the specified page size
     * @param sorts the specified sorts
     * @param filters the specified filters
     * @return the result object, see return of
     * {@linkplain #get(org.b3log.latke.repository.Query)} for details
     * @throws RepositoryException repository exception
     */
    private JSONObject get(final int currentPageNum,
                           final int pageSize,
                           final Map<String, SortDirection> sorts,
                           final Collection<Filter> filters)
            throws RepositoryException {
        final Query query = new Query(getName());
        for (final Filter filter : filters) {
            query.addSort(filter.getKey(), Query.SortDirection.DESCENDING);

            Query.FilterOperator filterOperator = null;
            switch (filter.getOperator()) {
                case EQUAL:
                    filterOperator = Query.FilterOperator.EQUAL;
                    break;
                case GREATER_THAN:
                    filterOperator = Query.FilterOperator.GREATER_THAN;
                    break;
                case GREATER_THAN_OR_EQUAL:
                    filterOperator = Query.FilterOperator.GREATER_THAN_OR_EQUAL;
                    break;
                case LESS_THAN:
                    filterOperator = Query.FilterOperator.LESS_THAN;
                    break;
                case LESS_THAN_OR_EQUAL:
                    filterOperator = Query.FilterOperator.LESS_THAN_OR_EQUAL;
                    break;
                case NOT_EQUAL:
                    filterOperator = Query.FilterOperator.NOT_EQUAL;
                    break;
                default:
                    throw new RepositoryException("Unsupported filter operator["
                                                  + filter.getOperator() + "]");
            }

            query.addFilter(filter.getKey(), filterOperator, filter.getValue());
        }

        for (Map.Entry<String, SortDirection> sort : sorts.entrySet()) {
            Query.SortDirection querySortDirection = null;
            if (sort.getValue().equals(SortDirection.ASCENDING)) {
                querySortDirection = Query.SortDirection.ASCENDING;
            } else {
                querySortDirection = Query.SortDirection.DESCENDING;
            }

            query.addSort(sort.getKey(), querySortDirection);
        }

        return get(query, currentPageNum, pageSize);
    }

    @Override // XXX: performance issue
    public List<JSONObject> getRandomly(final int fetchSize)
            throws RepositoryException {
        final List<JSONObject> ret = new ArrayList<JSONObject>();
        final Query query = new Query(getName());
        final PreparedQuery preparedQuery = datastoreService.prepare(query);
        final int count = (int) count();

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
        for (final Entity entity : entities) {
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
        final String cacheKey = INSTANCE_ID + REPOSITORY_CACHE_COUNT;
        if (isCacheEnabled) {
            final Object o = CACHE.get(cacheKey);
            if (null != o) {
                LOGGER.log(Level.FINER,
                           "Got an object[cacheKey={0}] from repository cache[name={1}]",
                           new Object[]{cacheKey, getName()});
                try {
                    return (Long) o;
                } catch (final Exception e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);

                    return -1;
                }
            }
        }

        final Query query = new Query(getName());
        final PreparedQuery preparedQuery = datastoreService.prepare(query);

        final long ret =
                preparedQuery.countEntities(FetchOptions.Builder.withDefaults());

        if (isCacheEnabled) {
            CACHE.put(cacheKey, ret);
            LOGGER.log(Level.FINER,
                       "Added an object[cacheKey={0}] in repository cache[{1}]",
                       new Object[]{cacheKey, getName()});
        }

        return ret;
    }

    /**
     * Converts the specified {@link Entity entity} to a {@link JSONObject
     * json object}.
     *
     * @param entity the specified entity
     * @return converted json object
     */
    public static JSONObject entity2JSONObject(final Entity entity) {
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
    public static void setProperties(final Entity entity,
                                     final JSONObject jsonObject)
            throws JSONException {
        @SuppressWarnings("unchecked")
        final Iterator<String> keys = jsonObject.keys();
        while (keys.hasNext()) {
            final String key = keys.next();
            final Object value = jsonObject.get(key);
            if (!SUPPORTED_TYPES.contains(value.getClass())) {
                throw new RuntimeException("Unsupported type[class=" + value.
                        getClass().getName() + "] in Latke GAE repository");
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
            } else if (value instanceof Number
                           || value instanceof Date
                           || value instanceof Boolean
                           || SUPPORTED_TYPES.contains(value.getClass())) {
                entity.setProperty(key, value);
            } else {
                throw new RuntimeException("Need to add known data type[class=" + value.
                        getClass().getName() + "]");
            }
        }
    }

    /**
     * Gets result json object by the specified query, current page number and
     * page size.
     *
     * @param query the specified query
     * @param currentPageNum the specified current page number
     * @param pageSize the specified page size
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
    private JSONObject get(final Query query,
                           final int currentPageNum,
                           final int pageSize)
            throws RepositoryException {
        final PreparedQuery preparedQuery = datastoreService.prepare(query);
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
            ret.put(Keys.RESULTS, results);
            for (final Entity entity : queryResultList) {
                final JSONObject jsonObject = entity2JSONObject(entity);

                results.put(jsonObject);
            }

            LOGGER.log(Level.FINER,
                       "Found objects[size={0}] at page[currentPageNum={1}, pageSize={2}] in repository[{3}]",
                       new Object[]{results.length(),
                                    currentPageNum,
                                    pageSize,
                                    getName()});
        } catch (final JSONException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new RepositoryException(e);
        }

        return ret;
    }

    @Override
    public Transaction beginTransaction() {
        final com.google.appengine.api.datastore.Transaction tx =
                datastoreService.beginTransaction();

        return new GAETransaction(tx);
    }

    /**
     * Gets the underlying Google App Engine datastore service.
     *
     * @return datastore service
     */
    protected final DatastoreService getDatastoreService() {
        return datastoreService;
    }
}
