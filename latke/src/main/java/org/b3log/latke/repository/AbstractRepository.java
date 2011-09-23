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
package org.b3log.latke.repository;

import java.lang.reflect.Constructor;
import java.util.List;
import org.b3log.latke.Latkes;
import org.b3log.latke.RuntimeEnv;
import org.json.JSONObject;

/**
 * Abstract repository.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.0, Sep 23, 2011
 */
public abstract class AbstractRepository implements Repository {

    /**
     * Repository.
     */
    private Repository repository;
    /**
     * Is cache enabled?
     */
    private boolean cacheEnabled = true;

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
                case LOCAL:
                    repositoryClass =
                            (Class<Repository>) Class.forName(
                            "org.b3log.latke.repository.sleepycat.SleepycatRepository");
                    break;
                case GAE:
                    repositoryClass =
                            (Class<Repository>) Class.forName(
                            "org.b3log.latke.repository.gae.GAERepository");
                    break;
                default:
                    throw new RuntimeException(
                            "Latke runs in the hell.... "
                            + "Please set the enviornment correctly");
            }

            final Constructor<Repository> constructor =
                    repositoryClass.getConstructor(String.class);

            repository = constructor.newInstance(name);
        } catch (final Exception e) {
            throw new RuntimeException("Can not initialize Mail Service!", e);
        }
    }

    @Override
    public String add(final JSONObject jsonObject) throws RepositoryException {
        return repository.add(jsonObject);
    }

    @Override
    public void update(final String id, final JSONObject jsonObject)
            throws RepositoryException {
        repository.update(id, jsonObject);
    }

    @Override
    public void remove(final String id) throws RepositoryException {
        repository.remove(id);
    }

    @Override
    public JSONObject get(final String id) throws RepositoryException {
        return repository.get(id);
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
        return cacheEnabled;
    }

    @Override
    public final void setCacheEnabled(final boolean isCacheEnabled) {
        this.cacheEnabled = isCacheEnabled;
    }

    @Override
    public String getName() {
        return repository.getName();
    }
}
