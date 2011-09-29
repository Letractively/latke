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
package org.b3log.latke.repository.sleepycat;

import com.sleepycat.je.Cursor;
import com.sleepycat.je.CursorConfig;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.TransactionConfig;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.b3log.latke.Keys;
import org.b3log.latke.cache.Cache;
import org.b3log.latke.cache.CacheFactory;
import org.b3log.latke.model.Pagination;
import org.b3log.latke.repository.Filter;
import org.b3log.latke.repository.FilterOperator;
import org.b3log.latke.repository.Query;
import org.b3log.latke.repository.Repository;
import org.b3log.latke.repository.RepositoryException;
import org.b3log.latke.repository.SortDirection;
import org.b3log.latke.util.CollectionUtils;
import org.b3log.latke.util.Ids;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Sleepycat repository.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.9, Sep 28, 2011
 */
public final class SleepycatRepository implements Repository {

    /**
     * Logger.
     */
    private static final Logger LOGGER =
            Logger.getLogger(SleepycatRepository.class.getName());
    /**
     * Repository name.
     */
    private String name;
    /**
     * Is cache enabled?
     */
    private boolean cacheEnabled = true;
    /**
     * Cache key prefix.
     */
    public static final String CACHE_KEY_PREFIX = "repository";
    /**
     * Repository cache name.
     */
    public static final String REPOSITORY_CACHE_NAME = "repositoryCache";
    /**
     * Repository cache.
     * <p>
     * &lt;oId, JSONObject&gt;
     * </p>
     */
    public static final Cache<String, Object> CACHE;
    /**
     * The current transaction.
     */
    public static final ThreadLocal<SleepycatTransaction> TX =
            new InheritableThreadLocal<SleepycatTransaction>();

    static {
        CACHE = CacheFactory.getCache(REPOSITORY_CACHE_NAME);
    }

    /**
     * Constructs a Sleepycat repository with the specified name.
     * 
     * @param name the specified name
     */
    public SleepycatRepository(final String name) {
        this.name = name;
    }

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
        final SleepycatTransaction currentTransaction = TX.get();

        if (null == currentTransaction) {
            throw new RepositoryException("Invoking add() outside a transaction");
        }

        final String ret = Ids.genTimeMillisId();

        final Database database = Sleepycat.get(getName(),
                                                Sleepycat.DEFAULT_DB_CONFIG);

        try {
            final DatabaseEntry entryKey = new DatabaseEntry(
                    ret.getBytes("UTF-8"));

            if (!jsonObject.has(Keys.OBJECT_ID)) {
                jsonObject.put(Keys.OBJECT_ID, ret);
            }

            final DatabaseEntry data = new DatabaseEntry(
                    jsonObject.toString().getBytes("UTF-8"));

            final OperationStatus operationStatus =
                    database.putNoOverwrite(
                    currentTransaction.getSleepycatTransaction(), entryKey, data);

            switch (operationStatus) {
                case KEYEXIST:
                    LOGGER.log(Level.SEVERE,
                               "Found a duplicated object[oId={0}] in repository[name={1}], ignores this add object operation",
                               new Object[]{ret, getName()});
                    throw new RepositoryException(
                            "Add an object into repository[name=" + getName()
                            + "] failed, caused by duplicated id[" + ret + "]");
                case SUCCESS:
                    LOGGER.log(Level.FINER,
                               "Added an object[oId={0}] in repository[name={1}]",
                               new Object[]{ret, getName()});
                    break;
                default:
                    throw new RepositoryException("Add an object[oId="
                                                  + ret + "] fail");
            }
        } catch (final Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new RepositoryException(e);
        }

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
        final SleepycatTransaction currentTransaction = TX.get();

        if (null == currentTransaction) {
            throw new RepositoryException(
                    "Invoking update() outside a transaction");
        }

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
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new RepositoryException(e);
        }
    }

    /**
     * Removes a json object by the specified id.
     *
     * <p>
     *   <b>Note</b>: the specified id is NOT the key of a database record, but
     *   the value of "oId" stored in database value entry of a record.
     * </p>
     *
     * @param id the specified id
     * @throws RepositoryException repository exception
     * @see Keys#OBJECT_ID
     */
    @Override
    public void remove(final String id) throws RepositoryException {
        final SleepycatTransaction currentTransaction = TX.get();

        if (null == currentTransaction) {
            throw new RepositoryException(
                    "Invoking remove() outside a transaction");
        }

        final Database database = Sleepycat.get(getName(),
                                                Sleepycat.DEFAULT_DB_CONFIG);
        final Cursor cursor = database.openCursor(currentTransaction.
                getSleepycatTransaction(), CursorConfig.READ_COMMITTED);

        final DatabaseEntry foundKey = new DatabaseEntry();
        final DatabaseEntry foundData = new DatabaseEntry();

        // XXX: optimize performance by using searchKey of cursor
        try {
            while (cursor.getNext(foundKey, foundData, LockMode.DEFAULT)
                   == OperationStatus.SUCCESS) {
                final JSONObject jsonObject =
                        new JSONObject(new String(foundData.getData(), "UTF-8"));
                if (jsonObject.getString(Keys.OBJECT_ID).equals(id)) {
                    if (cursor.delete().equals(OperationStatus.SUCCESS)) {
                        LOGGER.log(Level.FINER,
                                   "Removed an object[oId={0}] from repository[name={1}]",
                                   new Object[]{id, getName()});
                    }

                    return;
                }
            }
        } catch (final Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new RepositoryException(e);
        } finally {
            cursor.close();
        }

        LOGGER.log(Level.WARNING,
                   "Not found object[oId={0}] in repository[name={1}], ignores remove object operation",
                   new Object[]{id, getName()});
    }

    /**
     * Gets a json object by the specified id.
     *
     * <p>
     *   <b>Note</b>: the specified id is NOT the key of a database record, but
     *   the value of "oId" stored in database value entry of a record.
     * </p>
     *
     * @param id the specified id
     * @return a json object, {@code null} if not found
     * @throws RepositoryException repository exception
     * @see Keys#OBJECT_ID 
     */
    @Override
    public JSONObject get(final String id) throws RepositoryException {
        final Cursor cursor = Sleepycat.get(getName(),
                                            Sleepycat.DEFAULT_DB_CONFIG).
                openCursor(null, CursorConfig.READ_UNCOMMITTED);

        final DatabaseEntry foundKey = new DatabaseEntry();
        final DatabaseEntry foundData = new DatabaseEntry();

        try {
            // XXX: optimize performance by using searchKey of cursor
            while (cursor.getNext(foundKey, foundData, LockMode.DEFAULT)
                   == OperationStatus.SUCCESS) {
                final JSONObject ret =
                        new JSONObject(new String(foundData.getData(), "UTF-8"));
                if (ret.getString(Keys.OBJECT_ID).equals(id)) {
                    LOGGER.log(Level.FINER,
                               "Got an object[oId={0}] from repository[name={1}]",
                               new Object[]{id, getName()});

                    return ret;
                }
            }
        } catch (final Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new RepositoryException(e);
        } finally {
            cursor.close();
        }

        LOGGER.log(Level.WARNING,
                   "Not found an object[oId={0}] in repository[name={1}]",
                   new Object[]{id, getName()});

        return null;
    }

    @Override
    public List<JSONObject> getRandomly(final int fetchSize)
            throws RepositoryException {
        throw new UnsupportedOperationException("Not supported yet!");
    }

    @Override
    public long count() throws RepositoryException {
        final Database database = Sleepycat.get(getName(),
                                                Sleepycat.DEFAULT_DB_CONFIG);

        return database.count();
    }

    @Override
    public SleepycatTransaction beginTransaction() {
        SleepycatTransaction ret = TX.get();
        if (null != ret) {
            LOGGER.log(Level.FINER,
                       "There is a transaction[isActive={0}] in current thread",
                       ret.isActive());
            if (ret.isActive()) {
                return TX.get(); // Using 'the current transaction'
            }
        }

        final com.sleepycat.je.Transaction sleepycatTx =
                Sleepycat.ENV.beginTransaction(null, TransactionConfig.DEFAULT);

        ret = new SleepycatTransaction(sleepycatTx);
        TX.set(ret);

        return ret;
    }

    @Override
    public boolean has(final String id) throws RepositoryException {
        final Cursor cursor = Sleepycat.get(getName(),
                                            Sleepycat.DEFAULT_DB_CONFIG).
                openCursor(null, CursorConfig.READ_COMMITTED);

        final DatabaseEntry foundKey = new DatabaseEntry();
        final DatabaseEntry foundData = new DatabaseEntry();

        try {
            while (cursor.getNext(foundKey, foundData, LockMode.DEFAULT)
                   == OperationStatus.SUCCESS) {
                final JSONObject ret =
                        new JSONObject(new String(foundData.getData(), "UTF-8"));
                if (ret.getString(Keys.OBJECT_ID).equals(id)) {
                    return true;
                }
            }
        } catch (final Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new RepositoryException(e);
        } finally {
            cursor.close();
        }

        return false;
    }

    @Override
    public JSONObject get(final Query query) throws RepositoryException {
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
    // XXX: performance issue
    private JSONObject get(final int currentPageNum,
                           final int pageSize,
                           final Map<String, SortDirection> sorts,
                           final List<Filter> filters)
            throws RepositoryException {
        final Database database = Sleepycat.get(getName(),
                                                Sleepycat.DEFAULT_DB_CONFIG);
        final Cursor cursor =
                database.openCursor(null, CursorConfig.READ_COMMITTED);

        final JSONObject ret = new JSONObject();
        try {

            final JSONObject pagination = new JSONObject();
            ret.put(Pagination.PAGINATION, pagination);

            final DatabaseEntry foundKey = new DatabaseEntry();
            final DatabaseEntry foundData = new DatabaseEntry();

            // Step 1: Retrives by filters
            final List<JSONObject> foundList = new ArrayList<JSONObject>();

            while (cursor.getNext(foundKey, foundData, LockMode.DEFAULT)
                   == OperationStatus.SUCCESS) {
                final JSONObject jsonObject =
                        new JSONObject(new String(foundData.getData(), "UTF-8"));

                for (final Filter filter : filters) {
                    final String key = filter.getKey();
                    final Object value = filter.getValue();
                    final FilterOperator operator = filter.getOperator();

                    final Object property = jsonObject.opt(key);

                    if (value.getClass() != property.getClass()) {
                        throw new RepositoryException(
                                "The specified filter[key=" + key
                                + ", valueClass=" + value.getClass()
                                + "] can not compare to property[class="
                                + property.getClass() + "]");
                    }

                    switch (operator) {
                        case EQUAL:
                            if (value.equals(property)) {
                                foundList.add(jsonObject);
                            }

                            break;
                        case NOT_EQUAL:
                            if (!value.equals(property)) {
                                foundList.add(jsonObject);
                            }

                            break;
                        case GREATER_THAN:
                            if (greater(value, property)) {
                                foundList.add(jsonObject);
                            }

                            break;
                        case GREATER_THAN_OR_EQUAL:
                            if (greaterOrEqual(value, property)) {
                                foundList.add(jsonObject);
                            }

                            break;

                        case LESS_THAN:
                            if (less(value, property)) {
                                foundList.add(jsonObject);
                            }

                            break;
                        case LESS_THAN_OR_EQUAL:
                            if (lessOrEqual(value, property)) {
                                foundList.add(jsonObject);
                            }

                            break;
                        default:
                            throw new RepositoryException("Unsupported filter operator["
                                                          + operator + "]");
                    }
                }
            }

            final int pageCount = (int) Math.ceil((double) foundList.size()
                                                  / (double) pageSize);
            pagination.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);

            // Step 2: Sorts
            for (final Map.Entry<String, SortDirection> sort : sorts.entrySet()) {
                sort(foundList, sort);
            }

            // Step 3: Paginates
            final int passCount = pageSize * (currentPageNum - 1);
            if (0 == pageCount) {
                // Not found
                ret.put(Keys.RESULTS, new JSONArray());

                return ret;
            }

            // Step 4: Retrives
            int fromIndex = passCount - 1;
            if (fromIndex < 0) {
                fromIndex = 0;
            }
            int toIndex = passCount - 1 + pageSize;
            if (toIndex > foundList.size()) {
                toIndex = foundList.size();
            }

            final List<JSONObject> resultList =
                    foundList.subList(fromIndex, toIndex);

            final JSONArray resultArray =
                    CollectionUtils.listToJSONArray(resultList);
            ret.put(Keys.RESULTS, resultArray);

            LOGGER.log(Level.FINER,
                       "Found objects[size={0}] at page[currentPageNum={1}, pageSize={2}] in repository[{3}]",
                       new Object[]{resultArray.length(),
                                    currentPageNum,
                                    pageSize,
                                    getName()});
        } catch (final Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new RepositoryException(e);
        } finally {
            cursor.close();
        }

        return ret;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isCacheEnabled() {
        return cacheEnabled;
    }

    @Override
    public void setCacheEnabled(final boolean isCacheEnabled) {
        this.cacheEnabled = isCacheEnabled;
    }

    /**
     * Returns {@code true} if the specified object1 greater than the specified 
     * object2.
     * 
     * @param object1 the specified object1
     * @param object2 the specified object2
     * @return {@code true} if the specified object1 is greater than the 
     * specified object2, returns {@code false} otherwise
     * @throws RepositoryException if type of the specified object1 is illegal 
     */
    private boolean greater(final Object object1, final Object object2)
            throws RepositoryException {
        if (object1 instanceof String) {
            final String object1String = (String) object1;
            final String object2String = (String) object2;

            return object1String.compareTo(object2String) > 0;
        }

        if (object1 instanceof Boolean) {
            // true > false will returns true

            final Boolean object1Boolean = (Boolean) object1;
            final Boolean object2Boolean = (Boolean) object2;

            return object1Boolean && !object2Boolean;
        }

        if (object1 instanceof Double) {
            final Double object1Double = (Double) object1;
            final Double object2Double = (Double) object2;

            return object1Double > object2Double;
        }

        if (object1 instanceof Integer) {
            final Integer object1Integer = (Integer) object1;
            final Integer object2Integer = (Integer) object2;

            return object1Integer > object2Integer;
        }

        if (object1 instanceof Long) {
            final Long object1Long = (Long) object1;
            final Long object2Long = (Long) object2;

            return object1Long > object2Long;
        }

        if (object1 instanceof Date) {
            final Date object1Date = (Date) object1;
            final Date object2Date = (Date) object2;

            return object1Date.compareTo(object2Date) > 0;
        }

        throw new RepositoryException("Unsupported type[class=" + object1.
                getClass() + "] for comparison");
    }

    /**
     * Returns {@code true} if the specified object1 greater than or equal 
     * the specified object2.
     * 
     * @param object1 the specified object1
     * @param object2 the specified object2
     * @return {@code true} if the specified object1 is greater than the 
     * specified object2, returns {@code false} otherwise
     * @throws RepositoryException if type of the specified object1 is illegal 
     */
    private boolean greaterOrEqual(final Object object1,
                                   final Object object2)
            throws RepositoryException {
        if (object1.equals(object2)) {
            return true;
        }

        return greater(object1, object2);
    }

    /**
     * Returns {@code true} if the specified object1 less than the specified 
     * object2.
     * 
     * @param object1 the specified object1
     * @param object2 the specified object2
     * @return {@code true} if the specified object1 is greater than the 
     * specified object2, returns {@code false} otherwise
     * @throws RepositoryException if type of the specified object1 is illegal 
     */
    private boolean less(final Object object1, final Object object2)
            throws RepositoryException {
        if (object1.equals(object2)) {
            return false;
        }

        return !greater(object1, object2);
    }

    /**
     * Returns {@code true} if the specified object1 less than or equal the 
     * specified object2.
     * 
     * @param object1 the specified object1
     * @param object2 the specified object2
     * @return {@code true} if the specified object1 is greater than the 
     * specified object2, returns {@code false} otherwise
     * @throws RepositoryException if type of the specified object1 is illegal 
     */
    private boolean lessOrEqual(final Object object1, final Object object2)
            throws RepositoryException {
        if (object1.equals(object2)) {
            return true;
        }

        return less(object1, object2);
    }

    /**
     * Sorts the specified list with the specified sort rule.
     * 
     * @param list the specified list
     * @param sort the specified sort rule
     */
    private void sort(final List<JSONObject> list,
                      final Entry<String, SortDirection> sort) {
        Collections.sort(list, new Comparator<JSONObject>() {

            @Override
            public int compare(final JSONObject o1, final JSONObject o2) {
                if (SortDirection.DESCENDING == sort.getValue()) {
                    return o1.optString(sort.getKey()).compareTo(
                            o2.optString(sort.getKey()));
                }

                return o2.optString(sort.getKey()).compareTo(
                        o1.optString(sort.getKey()));
            }
        });
    }
}
