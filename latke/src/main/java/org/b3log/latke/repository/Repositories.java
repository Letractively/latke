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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.b3log.latke.repository.impl.UserRepositoryImpl;

/**
 * Repository utilities.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.0, Oct 9, 2011
 */
public final class Repositories {

    /**
     * Repository holder.
     * 
     * <p>
     * &lt;repositoryName, {@link Repository repository}&gt;
     * <p>
     */
    private static final Map<String, Repository> REPOS_HOLDER =
            new ConcurrentHashMap<String, Repository>();

    static {
        // Initializes the Latke built-in user repository.
        addRepository(UserRepositoryImpl.getInstance());
    }

    /**
     * Gets a repository with the specified repository name.
     * 
     * @param repositoryName the specified repository name
     * @return repository, returns {@code null} if not found
     */
    public static Repository getRepository(final String repositoryName) {
        return REPOS_HOLDER.get(repositoryName);
    }

    /**
     * Adds the specified repository.
     * 
     * @param repository the specified repository
     */
    public static void addRepository(final Repository repository) {
        REPOS_HOLDER.put(repository.getName(), repository);
    }

    /**
     * Private constructor.
     */
    private Repositories() {
    }
}
