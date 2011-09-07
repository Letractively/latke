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

import com.google.appengine.api.datastore.AsyncDatastoreService;
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
import com.google.appengine.api.utils.SystemProperty.Environment.Value;
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
import org.b3log.latke.Latkes;
import org.b3log.latke.RuntimeEnv;
import org.b3log.latke.RuntimeMode;
import org.b3log.latke.cache.Cache;
import org.b3log.latke.cache.CacheFactory;
import org.b3log.latke.model.Pagination;
import org.b3log.latke.repository.Blob;
import org.b3log.latke.repository.Filter;
import org.b3log.latke.repository.RepositoryException;
import org.b3log.latke.repository.SortDirection;
import org.b3log.latke.util.CollectionUtils;
import org.b3log.latke.util.Ids;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Abstract Google App Engine repository implementation, wraps
 * <a href="http://code.google.com/appengine/docs/java/javadoc/com/google/appengine/api/datastore/package-summary.html">
 * The Datastore Java API(Low-level API)</a> of GAE.
 * 
 * <p>
 * The invocation of {@link #add(org.json.JSONObject) add}, 
 * {@link #update(java.lang.String, org.json.JSONObject) update} and
 * {@link #remove(java.lang.String) remove} MUST in a transaction. 
 * Invocation of method {@link #get(java.lang.String) get} (by id) in a 
 * transaction will try to get object from cache of the transaction, if not hit,
 * retrieve object from transaction snapshot; if the invocation made is not in
 * a transaction, retrieve object from datastore directly. See 
 * <a href="http://88250.b3log.org/transaction_isolation.html">GAE 事务隔离</a>
 * for more details.
 * </p>
 * 
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.3.4, Sep 7, 2011
 * @see GAETransaction
 */
public abstract class AbstractGAERepository implements GAERepository {

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
     * GAE asynchronous datastore service.
     */
    private final AsyncDatastoreService asyncDatastoreService =
            DatastoreServiceFactory.getAsyncDatastoreService();
    /**
     * GAE datastore supported types.
     */
    private static final Set<Class<?>> GAE_SUPPORTED_TYPES =
            DataTypeUtils.getSupportedTypes();
    /**
     * Default parent key. Kind is {@code "parentKind"}, name is
     * {@code "parentKeyName"}.
     */
    private final Key defaultParentKey = KeyFactory.createKey("parentKind",
                                                              "parentKeyName");
    /**
     * Repository cache.
     * <p>
     * &lt;oId, JSONObject&gt;
     * </p>
     */
    public static final Cache<String, Object> CACHE;
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
    private boolean cacheEnabled = true;
    /**
     * Cache key prefix.
     */
    public static final String CACHE_KEY_PREFIX = "repository";
    /**
     * The current transaction.
     */
    public static final ThreadLocal<GAETransaction> TX =
            new InheritableThreadLocal<GAETransaction>();

    /**
     * Initializes cache.
     */
    static {
        final RuntimeEnv runtime = Latkes.getRuntimeEnv();
        if (!runtime.equals(RuntimeEnv.GAE)) {
            throw new RuntimeException(
                    "GAE repository can only runs on Google App Engine, please "
                    + "check your configuration and make sure "
                    + "Latkes.setRuntimeEnv(RuntimeEnv.GAE) was invoked before "
                    + "using GAE repository.");
        }

        CACHE = CacheFactory.getCache(REPOSITORY_CACHE_NAME);

        // TODO: Intializes the runtime mode at application startup
        LOGGER.info("Initializing runtime mode....");
        final Value gaeEnvValue = SystemProperty.environment.value();
        if (SystemProperty.Environment.Value.Production == gaeEnvValue) {
            LOGGER.info("B3log Solo runs in [production] mode");
            Latkes.setRuntimeMode(RuntimeMode.PRODUCTION);
        } else {
            LOGGER.info("B3log Solo runs in [development] mode");
            Latkes.setRuntimeMode(RuntimeMode.DEVELOPMENT);
        }
    }

    /**
     * Adds the specified json object with the {@linkplain #defaultParentKey
     * default parent key}.
     *
     * @param jsonObject the specified json object
     * @return the generated object id
     * @throws RepositoryException repository exception
     */
    @Override
    public String add(final JSONObject jsonObject) throws RepositoryException {
        final GAETransaction currentTransaction = TX.get();

        if (null == currentTransaction) {
            throw new RepositoryException("Invoking add() outside a transaction");
        }

        final String ret = add(jsonObject,
                               defaultParentKey.getKind(), defaultParentKey.
                getName());

        currentTransaction.putUncommitted(ret, jsonObject);

        return ret;
    }

    /**
     * Adds.
     * 
     * @param jsonObject the specified json object
     * @param parentKeyKind the specified parent key kind
     * @param parentKeyName the specified parent key name
     * @return id
     * @throws RepositoryException repository exception
     */
    private String add(final JSONObject jsonObject,
                       final String parentKeyKind, final String parentKeyName)
            throws RepositoryException {
        String ret = null;
        try {
            if (!jsonObject.has(Keys.OBJECT_ID)) {
                ret = genTimeMillisId();

                jsonObject.put(Keys.OBJECT_ID, ret);
            } else {
                ret = jsonObject.getString(Keys.OBJECT_ID);
            }

            final Key parentKey = KeyFactory.createKey(parentKeyKind,
                                                       parentKeyName);
            final Entity entity = new Entity(getName(), ret, parentKey);
            setProperties(entity, jsonObject);

            datastoreService.put(entity);
        } catch (final Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new RepositoryException(e);
        }

        LOGGER.log(Level.FINER, "Added an object[oId={0}] in repository[{1}]",
                   new Object[]{ret, getName()});

        if (cacheEnabled) {
            final String key = CACHE_KEY_PREFIX + ret;
            CACHE.put(key, jsonObject);
            LOGGER.log(Level.FINER,
                       "Added an object[cacheKey={0}] in repository cache[{1}]",
                       new Object[]{key, getName()});
        }

        return ret;
    }

    @Override
    public String addAsync(final JSONObject jsonObject)
            throws RepositoryException {
        return addAsync(jsonObject,
                        defaultParentKey.getKind(), defaultParentKey.getName());
    }

    /**
     * Adds async.
     * 
     * @param jsonObject the specified json object
     * @param parentKeyKind the specified parent key kind
     * @param parentKeyName the specified parent key name
     * @return id
     * @throws RepositoryException repository exception
     */
    private String addAsync(final JSONObject jsonObject,
                            final String parentKeyKind,
                            final String parentKeyName)
            throws RepositoryException {
        String ret = null;
        try {
            if (!jsonObject.has(Keys.OBJECT_ID)) {
                ret = genTimeMillisId();
                jsonObject.put(Keys.OBJECT_ID, ret);
            } else {
                ret = jsonObject.getString(Keys.OBJECT_ID);
            }

            final Key parentKey = KeyFactory.createKey(parentKeyKind,
                                                       parentKeyName);
            final Entity entity = new Entity(getName(), ret, parentKey);
            setProperties(entity, jsonObject);

            asyncDatastoreService.put(entity);
        } catch (final Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new RepositoryException(e);
        }

        LOGGER.log(Level.FINER, "Added an object[oId={0}] in repository[{1}]",
                   new Object[]{ret, getName()});

        if (cacheEnabled) {
            final String key = CACHE_KEY_PREFIX + ret;
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
     * @throws RepositoryException repository exception
     * @see Keys#OBJECT_ID
     */
    @Override
    public void update(final String id, final JSONObject jsonObject)
            throws RepositoryException {
        final GAETransaction currentTransaction = TX.get();

        if (null == currentTransaction) {
            throw new RepositoryException(
                    "Invoking update() outside a transaction");
        }

        update(id, jsonObject,
               defaultParentKey.getKind(), defaultParentKey.getName());

        currentTransaction.putUncommitted(id, jsonObject);
    }

    /**
     * Updates.
     * 
     * @param id the specified id
     * @param jsonObject the specified json object
     * @param parentKeyKind the specified parent key kind
     * @param parentKeyName the specified parent key name
     * @throws RepositoryException repository exception
     */
    private void update(final String id, final JSONObject jsonObject,
                        final String parentKeyKind, final String parentKeyName)
            throws RepositoryException {
        try {
            jsonObject.put(Keys.OBJECT_ID, id);

            final Key parentKey = KeyFactory.createKey(parentKeyKind,
                                                       parentKeyName);
            final Entity entity = new Entity(getName(), id, parentKey);
            setProperties(entity, jsonObject);

            datastoreService.put(entity);
        } catch (final Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new RepositoryException(e);
        }

        LOGGER.log(Level.FINER,
                   "Updated an object[oId={0}] in repository[name={1}]",
                   new Object[]{id, getName()});

        if (cacheEnabled) {
            final String key = CACHE_KEY_PREFIX + id;
            CACHE.put(key, jsonObject);
            LOGGER.log(Level.FINER,
                       "Updated an object[cacheKey={0}] in repository cache[{1}]",
                       new Object[]{key, getName()});
        }
    }

    @Override
    public void updateAsync(final String id, final JSONObject jsonObject)
            throws RepositoryException {
        updateAsync(id, jsonObject,
                    defaultParentKey.getKind(),
                    defaultParentKey.getName());
    }

    /**
     * Updates async.
     * 
     * @param id the specified id
     * @param jsonObject the specified json object
     * @param parentKeyKind the specified parent key kind
     * @param parentKeyName the specified parent key name
     * @throws RepositoryException repository exception
     */
    private void updateAsync(final String id, final JSONObject jsonObject,
                             final String parentKeyKind,
                             final String parentKeyName)
            throws RepositoryException {
        try {
            jsonObject.put(Keys.OBJECT_ID, id);

            final Key parentKey = KeyFactory.createKey(parentKeyKind,
                                                       parentKeyName);
            final Entity entity = new Entity(getName(), id, parentKey);
            setProperties(entity, jsonObject);

            asyncDatastoreService.put(entity);
        } catch (final Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new RepositoryException(e);
        }

        LOGGER.log(Level.FINER,
                   "Updated an object[oId={0}] in repository[name={1}]",
                   new Object[]{id, getName()});

        if (cacheEnabled) {
            final String key = CACHE_KEY_PREFIX + id;
            CACHE.put(key, jsonObject);
            LOGGER.log(Level.FINER,
                       "Updated an object[cacheKey={0}] in repository cache[{1}]",
                       new Object[]{key, getName()});
        }
    }

    /**
     * Removes a json object by the specified id with the {@linkplain #defaultParentKey
     * default parent key}.
     *
     * @param id the specified id
     * @throws RepositoryException repository exception
     */
    @Override
    public void remove(final String id) throws RepositoryException {
        final GAETransaction currentTransaction = TX.get();

        if (null == currentTransaction) {
            throw new RepositoryException(
                    "Invoking remove() outside a transaction");
        }

        remove(id, defaultParentKey.getKind(), defaultParentKey.getName());

        currentTransaction.putUncommitted(id, null);
    }

    /**
     * Remmoves.
     * 
     * @param id the specified id
     * @param parentKeyKind the specified parent key kind
     * @param parentKeyName the specified parent key name
     * @throws RepositoryException repository exception
     */
    private void remove(final String id,
                        final String parentKeyKind, final String parentKeyName)
            throws RepositoryException {

        final Key parentKey = KeyFactory.createKey(parentKeyKind, parentKeyName);
        final Key key = KeyFactory.createKey(parentKey, getName(), id);
        datastoreService.delete(key);
        LOGGER.log(Level.FINER,
                   "Removed an object[oId={0}] from repository[name={1}]",
                   new Object[]{id, getName()});

        if (cacheEnabled) {
            LOGGER.log(Level.FINER,
                       "Clear all objects in repository cache[{1}]",
                       getName());
        }
    }

    /**
     * Gets a json object by the specified id with the {@linkplain #defaultParentKey
     * default parent key}.
     *
     * @param id the specified id
     * @return a json object, returns {@code null} if not found
     * @throws RepositoryException repository exception
     */
    @Override
    public JSONObject get(final String id) throws RepositoryException {
        final GAETransaction currentTransaction = TX.get();
        if (null == currentTransaction) {
            // Gets outside a transaction
            return get(id, defaultParentKey.getKind(),
                       defaultParentKey.getName());
        }

        // Works in a transaction....

        if (!currentTransaction.hasUncommitted(id)) {
            // Has not mainipulat the object in the current transaction
            // Gets from transaction snapshot view
            return get(id, defaultParentKey.getKind(),
                       defaultParentKey.getName());
        }

        // The returned value may be null if it has been set to null in the 
        // current transaction
        return currentTransaction.getUncommitted(id);
    }

    /**
     * Gets.
     * 
     * @param id the specified id
     * @param parentKeyKind the specified parent key kind
     * @param parentKeyName the specified parent key name
     * @return a json object, returns {@code null} if not found
     * @throws RepositoryException repository exception
     */
    private JSONObject get(final String id,
                           final String parentKeyKind,
                           final String parentKeyName)
            throws RepositoryException {
        JSONObject ret = null;

        if (cacheEnabled) {
            final String cacheKey = CACHE_KEY_PREFIX + id;
            ret = (JSONObject) CACHE.get(cacheKey);
            if (null != ret) {
                LOGGER.log(Level.FINER,
                           "Got an object[cacheKey={0}] from repository cache[name={1}]",
                           new Object[]{cacheKey, getName()});
                return ret;
            }
        }

        final Key parentKey = KeyFactory.createKey(parentKeyKind, parentKeyName);
        final Key key = KeyFactory.createKey(parentKey, getName(), id);
        try {
            final Entity entity = datastoreService.get(key);
            ret = entity2JSONObject(entity);

            LOGGER.log(Level.FINER,
                       "Got an object[oId={0}] from repository[name={1}]",
                       new Object[]{id, getName()});

            if (cacheEnabled) {
                final String cacheKey = CACHE_KEY_PREFIX + id;
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
        if (cacheEnabled) {
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

        if (cacheEnabled) {
            final String cacheKey = CACHE_KEY_PREFIX + query.hashCode() + "_"
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
        final List<Filter> filters = query.getFilters();
        final int pageSize = query.getPageSize();
        final Map<String, SortDirection> sorts = query.getSorts();

        if (org.b3log.latke.repository.Query.DEFAULT_CUR_PAGE_NUM
            != currentPageNum
            && org.b3log.latke.repository.Query.DEFAULT_PAGE_SIZE != pageSize) {
            ret = get(currentPageNum, pageSize, sorts, filters);
        } else {
            ret = get(1, Integer.MAX_VALUE, sorts, filters);
        }

        if (cacheEnabled) {
            final String cacheKey = CACHE_KEY_PREFIX + query.hashCode() + "_"
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
                           final List<Filter> filters)
            throws RepositoryException {
        final Query query = new Query(getName());
        for (final Filter filter : filters) {
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

        for (final Map.Entry<String, SortDirection> sort : sorts.entrySet()) {
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
        final String cacheKey = CACHE_KEY_PREFIX + getName()
                                + REPOSITORY_CACHE_COUNT;
        if (cacheEnabled) {
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

        if (cacheEnabled) {
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
                jsonMap.put(k, valueText.getValue());
            } else if (v instanceof com.google.appengine.api.datastore.Blob) {
                final com.google.appengine.api.datastore.Blob blob =
                        (com.google.appengine.api.datastore.Blob) v;
                jsonMap.put(k, new Blob(blob.getBytes()));
            } else {
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

            if (!GAE_SUPPORTED_TYPES.contains(value.getClass())
                && !(value instanceof Blob)) {
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
                       || GAE_SUPPORTED_TYPES.contains(value.getClass())) {
                entity.setProperty(key, value);
            } else if (value instanceof Blob) {
                final Blob blob = (Blob) value;
                entity.setProperty(key,
                                   new com.google.appengine.api.datastore.Blob(
                        blob.getBytes()));
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

    /**
     * Gets current date time string.
     *
     * @return a time millis string
     */
    public static String genTimeMillisId() {
        final String timeMillisId = Ids.genTimeMillisId();
        final long inc = CACHE.inc("id-step-generator", 1);

        LOGGER.log(Level.FINEST, "[timeMillisId={0}, inc={1}]",
                   new Object[]{timeMillisId, inc});

        return String.valueOf(Long.parseLong(timeMillisId) + inc);
    }

    @Override
    public GAETransaction beginTransaction() {
        if (null != TX.get()) {
            return TX.get(); // Using 'the current transaction'
        }

        final com.google.appengine.api.datastore.Transaction gaeTx =
                datastoreService.beginTransaction();

        final GAETransaction ret = new GAETransaction(gaeTx);
        TX.set(ret);

        return ret;
    }

    /**
     * Is the cache enabled?
     *
     * @return {@code true} for enabled, {@code false} otherwise
     */
    public final boolean isCacheEnabled() {
        return cacheEnabled;
    }

    /**
     * Sets the cache enabled with the specified switch.
     *
     * @param isCacheEnabled the specified switch, {@code true} for enable
     * cache, {@code false} otherwise
     */
    public final void setCacheEnabled(final boolean isCacheEnabled) {
        this.cacheEnabled = isCacheEnabled;
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
