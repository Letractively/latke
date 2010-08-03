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
package org.b3log.latke.repository.sleepycat;

import com.sleepycat.je.Cursor;
import com.sleepycat.je.CursorConfig;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.log4j.Logger;
import org.b3log.latke.Keys;
import org.b3log.latke.model.Pagination;
import org.b3log.latke.repository.Repository;
import org.b3log.latke.repository.RepositoryException;
import org.b3log.latke.service.ServiceException;
import org.json.JSONObject;

/**
 * Abstract repository.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.1, Jul 28, 2010
 */
public abstract class AbstractRepository implements Repository {

    /**
     * Logger.
     */
    private static final Logger LOGGER =
            Logger.getLogger(AbstractRepository.class);
    /**
     * Lock for unique key generation.
     */
    private static Lock keyGenLock = new ReentrantLock();
    /**
     * Sleep millisecond. 
     */
    private static final long KEY_GEN_SLEEP_MILLIS = 5;

    /**
     * Gets database configuration of this repository.
     *
     * @return database configuration
     */
    public abstract DatabaseConfig getDatabaseConfig();

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
        keyGenLock.lock();
        try {
            ret = String.valueOf(System.currentTimeMillis());

            try {
                Thread.sleep(KEY_GEN_SLEEP_MILLIS);
            } catch (final InterruptedException e) {
                LOGGER.warn(e.getMessage(), e);
            }
        } finally {
            keyGenLock.unlock();
        }

        if (null == ret) {
            throw new RuntimeException("Time millis key generation fail!");
        }

        final Database database = Sleepycat.get(getName(), getDatabaseConfig());

        try {
            final DatabaseEntry entryKey = new DatabaseEntry(
                    ret.getBytes("UTF-8"));

            if (!jsonObject.has(Keys.OBJECT_ID)) {
                jsonObject.put(Keys.OBJECT_ID, ret);
            }

            final DatabaseEntry data = new DatabaseEntry(
                    jsonObject.toString().getBytes("UTF-8"));

            final OperationStatus operationStatus =
                    database.putNoOverwrite(null, entryKey, data);

            switch (operationStatus) {
                case KEYEXIST:
                    LOGGER.warn("Found duplicated object[id=" + ret
                                + "] in repository[name=" + getName()
                                + "] , ignores add object operation");
                    break;
                case SUCCESS:
                    LOGGER.debug("Added object[id=" + ret + "]");
                    break;
                default:
                    throw new ServiceException("Add object[id="
                                               + ret + "] fail");
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new RepositoryException(e);
        } finally {
            database.sync();
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
     *     <li>Invokes {@linkplain #add(org.json.JSONObject)} with the
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
        final Database database = Sleepycat.get(getName(),
                                                getDatabaseConfig());
        final Cursor cursor = database.openCursor(null, CursorConfig.DEFAULT);

        final DatabaseEntry foundKey = new DatabaseEntry();
        final DatabaseEntry foundData = new DatabaseEntry();

        try {
            while (cursor.getNext(foundKey, foundData, LockMode.DEFAULT)
                   == OperationStatus.SUCCESS) {
                final JSONObject jsonObject =
                        new JSONObject(new String(foundData.getData(), "UTF-8"));
                if (jsonObject.getString(Keys.OBJECT_ID).equals(id)) {
                    if (cursor.delete().equals(OperationStatus.SUCCESS)) {
                        LOGGER.debug("Removed object[id=" + id + "] from "
                                     + "repository[name=" + getName() + "]");
                    }

                    return;
                }
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new RepositoryException(e);
        } finally {
            cursor.close();
            database.sync();
        }

        LOGGER.warn("Not found object[id="
                    + id + "] in repository[name=" + getName()
                    + "], ignores remove object operation");
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
                openCursor(null, CursorConfig.DEFAULT);

        final DatabaseEntry foundKey = new DatabaseEntry();
        final DatabaseEntry foundData = new DatabaseEntry();

        try {
            while (cursor.getNext(foundKey, foundData, LockMode.DEFAULT)
                   == OperationStatus.SUCCESS) {
                final JSONObject ret =
                        new JSONObject(new String(foundData.getData(), "UTF-8"));
                if (ret.getString(Keys.OBJECT_ID).equals(id)) {
                    LOGGER.debug("Got an object[id=" + id + "] from "
                                 + "repository[name=" + getName() + "]");

                    return ret;
                }
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new RepositoryException(e);
        } finally {
            cursor.close();
        }

        LOGGER.warn("Not found an object[id=" + id + "] in repository[name="
                    + getName() + "]");

        return null;
    }

    @Override
    public List<JSONObject> get(final int currentPageNum,
                                final int pageSize)
            throws RepositoryException {
        final Database database = Sleepycat.get(getName(),
                                                getDatabaseConfig());
        final Cursor cursor = database.openCursor(null, CursorConfig.DEFAULT);

        final List<JSONObject> ret = new ArrayList<JSONObject>();
        final JSONObject pagination = new JSONObject();
        ret.add(pagination);

        final long count = database.count();
        final int pageCount =
                (int) Math.ceil((double) count / (double) pageSize);

        final DatabaseEntry foundKey = new DatabaseEntry();
        final DatabaseEntry foundData = new DatabaseEntry();
        try {
            pagination.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
            final int passCount = pageSize * (currentPageNum - 1);
            int cnt = 0;
            while (cnt < passCount) {
                cursor.getNext(foundKey, foundData, LockMode.RMW);

                cnt++;
            }

            cnt = 0;
            while (cnt < pageSize
                   && cursor.getNext(foundKey, foundData, LockMode.DEFAULT)
                      == OperationStatus.SUCCESS) {
                final JSONObject jsonObject =
                        new JSONObject(new String(foundData.getData(), "UTF-8"));
                ret.add(jsonObject);

                cnt++;
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new RepositoryException(e);
        } finally {
            cursor.close();
        }

        LOGGER.debug("Found objects[size=" + (ret.size() - 1) + "] at page"
                     + "[currentPageNum=" + currentPageNum + ", pageSize="
                     + pageSize + "] in repository[" + getName() + "]");

        return ret;
    }
}
