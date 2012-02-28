/*
 * Copyright (c) 2009, 2010, 2011, 2012, B3log Team
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

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.b3log.latke.Keys;
import org.b3log.latke.Latkes;
import org.b3log.latke.RuntimeDatabase;
import org.b3log.latke.RuntimeEnv;
import org.b3log.latke.cache.Cache;
import org.b3log.latke.repository.jdbc.JDBCRepositoryException;
import org.json.JSONObject;

/**
 * Abstract repository.
 * 
 * <p>
 * This is a base adapter for wrapped {@link #repository repository}, the 
 * underlying repository will be instantiated in the 
 * {@link #AbstractRepository(java.lang.String) constructor} with 
 * {@link Latkes#getRuntimeEnv() the current runtime environment}.
 * </p>
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.7, Feb 28, 2012
 */
public abstract class AbstractRepository implements Repository {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(AbstractRepository.class.getName());
    /**
     * Repository.
     */
    private Repository repository;

    /**
     * Constructs a repository with the specified name.
     * 
     * @param name the specified name
     */
    @SuppressWarnings("unchecked")
    public AbstractRepository(final String name) {
        final RuntimeEnv runtimeEnv = Latkes.getRuntimeEnv();

        try {
            Class<Repository> repositoryClass = null;

            switch (runtimeEnv) {
                //local:sleepcat,mysql.
                case LOCAL:
                    final RuntimeDatabase runtimeDatabase = Latkes.getRuntimeDatabase();
                    switch (runtimeDatabase) {
                        case SLEEPYCAT:
                            repositoryClass = (Class<Repository>) Class.forName("org.b3log.latke.repository.sleepycat.SleepycatRepository");
                            break;
                        case MYSQL:
                            repositoryClass = (Class<Repository>) Class.forName("org.b3log.latke.repository.jdbc.JdbcRepository");
                            break;
                        default:
                            throw new RuntimeException("the runtimeDatabase[" + runtimeDatabase + "] is not support NOW!  ");
                    }
                    break;
                case GAE:
                    repositoryClass = (Class<Repository>) Class.forName("org.b3log.latke.repository.gae.GAERepository");
                    break;
                default:
                    throw new RuntimeException("Latke runs in the hell.... Please set the enviornment correctly");
            }

            final Constructor<Repository> constructor = repositoryClass.getConstructor(String.class);

            repository = constructor.newInstance(name);
        } catch (final Exception e) {
            throw new RuntimeException("Can not initialize repository!", e);
        }

        Repositories.addRepository(repository);
    }

    @Override
    public String add(final JSONObject jsonObject) throws RepositoryException {
        Repositories.check(getName(), jsonObject, Keys.OBJECT_ID);

        return repository.add(jsonObject);
    }

    @Override
    public void update(final String id, final JSONObject jsonObject)
            throws RepositoryException {
        Repositories.check(getName(), jsonObject, Keys.OBJECT_ID);

        repository.update(id, jsonObject);
    }

    @Override
    public void remove(final String id) throws RepositoryException {
        repository.remove(id);
    }

    @Override
    public JSONObject get(final String id) throws RepositoryException {
        try {
            return repository.get(id);
        } catch (final JDBCRepositoryException e) {
            LOGGER.log(Level.WARNING, "SQL exception[msg={0}]", e.getMessage());
            return null;
        }
    }

    @Override
    public Map<String, JSONObject> get(final Iterable<String> ids)
            throws RepositoryException {
        return repository.get(ids);
    }

    @Override
    public boolean has(final String id) throws RepositoryException {
        return repository.has(id);
    }

    @Override
    public JSONObject get(final Query query) throws RepositoryException {
        return repository.get(query);
    }

    @Override
    public List<JSONObject> getRandomly(final int fetchSize)
            throws RepositoryException {
        return repository.getRandomly(fetchSize);
    }

    @Override
    public long count() throws RepositoryException {
        return repository.count();
    }

    @Override
    public Transaction beginTransaction() {
        return repository.beginTransaction();
    }

    @Override
    public final boolean isCacheEnabled() {
        return repository.isCacheEnabled();
    }

    @Override
    public final void setCacheEnabled(final boolean isCacheEnabled) {
        repository.setCacheEnabled(isCacheEnabled);
    }

    @Override
    public String getName() {
        return repository.getName();
    }

    @Override
    public Cache<String, Serializable> getCache() {
        return repository.getCache();
    }

    /**
     * Gets the underlying repository.
     * 
     * @return underlying repository
     */
    protected Repository getUnderlyingRepository() {
        return repository;
    }
}
