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

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import static com.google.appengine.api.datastore.FetchOptions.Builder.*;
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
            jsonObject.put(Keys.OBJECT_ID, ret);

            final String kind = getName();
            final Entity entity = new Entity(kind, ret);
            entity.setProperty(Keys.DATA, jsonObject);

            DATASTORE_SERVICE.put(entity);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new RepositoryException(e);
        }

        return ret;
    }

    @Override
    public void update(final String id, final JSONObject jsonObject)
            throws RepositoryException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void remove(final String id) throws RepositoryException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public JSONObject get(final String id) throws RepositoryException {
        throw new UnsupportedOperationException("Not supported yet.");
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
        } catch (final JSONException e) {
            LOGGER.error(e.getMessage(), e);
            throw new RepositoryException(e);
        }

        int offset = pageSize * (currentPageNum - 1);
        final QueryResultList<Entity> queryResultList =
                preparedQuery.asQueryResultList(
                withOffset(offset).limit(pageSize));

        for (final Entity entity : queryResultList) {
            final JSONObject jsonObject =
                    (JSONObject) entity.getProperty(Keys.DATA);

            ret.add(jsonObject);
        }

        LOGGER.debug("Found objects[size=" + (ret.size() - 1) + "] at page"
                + "[currentPageNum=" + currentPageNum + ", pageSize="
                + pageSize + "] in repository[" + getName() + "]");

        return ret;
    }
}
