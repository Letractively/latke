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
package org.b3log.latke.repository.jdbc;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.b3log.latke.cache.Cache;
import org.b3log.latke.repository.Query;
import org.b3log.latke.repository.Repository;
import org.b3log.latke.repository.RepositoryException;
import org.b3log.latke.repository.Transaction;
import org.json.JSONObject;

/**
 * JdbcRepository.
 * 
 * @author <a href="mailto:wmainlove@gmail.com">Love Yao</a>
 * @version 1.0.0.0, Dec 20, 2011
 */
public class JdbcRepository implements Repository {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(JdbcRepository.class.getName());

    /**
     * Repository name.
     */
    private String name;

    /**
     * Is cache enabled?
     */
    private boolean cacheEnabled = true;

    @Override
    public String add(final JSONObject jsonObject) throws RepositoryException {

        return null;

    }

    @Override
    public void update(final String id, final JSONObject jsonObject) throws RepositoryException {
        // TODO Auto-generated method stub

    }

    @Override
    public void remove(final String id) throws RepositoryException {
        // TODO Auto-generated method stub

    }

    @Override
    public JSONObject get(final String id) throws RepositoryException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String, JSONObject> get(final Iterable<String> ids) throws RepositoryException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean has(final String id) throws RepositoryException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public JSONObject get(final Query query) throws RepositoryException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<JSONObject> getRandomly(final int fetchSize) throws RepositoryException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long count() throws RepositoryException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Transaction beginTransaction() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isCacheEnabled() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setCacheEnabled(final boolean isCacheEnabled) {
        // TODO Auto-generated method stub

    }

    @Override
    public Cache<String, Serializable> getCache() {
        // TODO Auto-generated method stub
        return null;
    }

}
