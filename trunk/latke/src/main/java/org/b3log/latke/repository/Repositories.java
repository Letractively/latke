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

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;
import org.b3log.latke.repository.impl.UserRepositoryImpl;
import org.b3log.latke.util.CollectionUtils;
import org.b3log.latke.util.Strings;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Repository utilities.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.1, Dec 15, 2011
 */
public final class Repositories {

    /**
     * Logger.
     */
    private static final Logger LOGGER =
            Logger.getLogger(Repositories.class.getName());
    /**
     * Repository holder.
     * 
     * <p>
     * &lt;repositoryName, {@link Repository repository}&gt;
     * <p>
     */
    private static final Map<String, Repository> REPOS_HOLDER =
            new ConcurrentHashMap<String, Repository>();
    /**
     * Repositories description (repository.json).
     */
    private static JSONObject repositoriesDescription;

    /**
     * Gets repositories description.
     * 
     * @return repositories description, returns {@code null} if not found or
     * parse the description failed
     */
    public static JSONObject getRepositoriesDescription() {
        return repositoriesDescription;
    }

    static {
        // Initializes the Latke built-in user repository.
        addRepository(UserRepositoryImpl.getInstance());

        loadRepositoryDescription();
    }

    /**
     * Determines whether the specified json object can not be persisted 
     * (add or update) into an repository which specified by the given 
     * repository name.
     * 
     * <p>
     * A valid json object to persist must match keys definitions 
     * (including type and length if had) in the repository 
     * description (repository.json) with the json object names itself.
     * </p>
     * 
     * <p>
     * The specified keys to ignore will be bypassed, regardless of matching
     * keys definitions.
     * </p>
     * 
     * @param repositoryName the given repository name
     * @param jsonObject the specified json object
     * @param ignoredKeys the specified keys to ignore
     * @return {@code true} if can not be persisted, returns {@code false} 
     * otherwise
     * @see Repository#add(org.json.JSONObject) 
     * @see Repository#update(java.lang.String, org.json.JSONObject) 
     */
    // TODO: 88250, type and length validation
    public static boolean invalid(final String repositoryName,
                                  final JSONObject jsonObject,
                                  final String... ignoredKeys) {
        if (null == jsonObject) {
            LOGGER.log(Level.WARNING, "Null to persist to repository[{0}]",
                       repositoryName);

            return true;
        }

        final boolean needIgnoreKeys = null != ignoredKeys
                                       && 0 < ignoredKeys.length;

        final JSONArray names = jsonObject.names();
        final Set<Object> nameSet = CollectionUtils.jsonArrayToSet(names);

        final JSONArray keysDescription =
                getRepositoryKeysDescription(repositoryName);

        final Set<String> keySet = new HashSet<String>();

        // Checks whether the specified json object has all keys defined
        for (int i = 0; i < keysDescription.length(); i++) {
            final JSONObject keyDescription = keysDescription.optJSONObject(i);

            final String key = keyDescription.optString("name");

            keySet.add(key);

            if (needIgnoreKeys && Strings.contains(key, ignoredKeys)) {
                continue;
            }

            if (!nameSet.contains(key)) {
                LOGGER.log(Level.WARNING,
                           "A json object to persist to repository[name="
                           + repositoryName + "] does not contain a key[{0}]",
                           key);

                return true;
            }
        }

        // Checks whether the specified json object has an redundant (undefined) key
        for (int i = 0; i < names.length(); i++) {
            final String name = names.optString(i);

            if (!keySet.contains(name)) {
                LOGGER.log(Level.WARNING,
                           "A json object to persist to repository[name="
                           + repositoryName + "] contains an redundant key[{0}]",
                           name);

                return true;
            }
        }

        return false;
    }

    /**
     * Gets the keys description of an repository specified by the given 
     * repository name.
     * 
     * @param repositoryName the given repository name
     * @return keys description, returns {@code null} if not found
     */
    public static JSONArray getRepositoryKeysDescription(
            final String repositoryName) {
        if (Strings.isEmptyOrNull(repositoryName)) {
            return null;
        }

        if (null == repositoriesDescription) {
            return null;
        }

        final JSONArray repositories =
                repositoriesDescription.optJSONArray("repositories");
        for (int i = 0; i < repositories.length(); i++) {
            final JSONObject repository = repositories.optJSONObject(i);
            if (repositoryName.equals(repository.optString("name"))) {
                return repository.optJSONArray("keys");
            }
        }

        throw new RuntimeException(
                "Not found the repository[name=" + repositoryName
                + "] description, please define it in repositories.json");
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
     * Loads repository description.
     */
    private static void loadRepositoryDescription() {
        LOGGER.log(Level.INFO, "Loading repository description....");

        final InputStream inputStream =
                AbstractRepository.class.getClassLoader().getResourceAsStream(
                "repository.json");
        if (null == inputStream) {
            LOGGER.log(Level.INFO,
                       "Not found repository description[repository.json] file under classpath");
            return;
        }

        LOGGER.log(Level.INFO, "Parsing repository description....");

        try {
            final String description = IOUtils.toString(inputStream);

            LOGGER.log(Level.CONFIG, "\n" + description);

            repositoriesDescription = new JSONObject(description);
        } catch (final Exception e) {
            LOGGER.log(Level.SEVERE, "Parses repository description failed",
                       e);
        } finally {
            try {
                inputStream.close();
            } catch (final IOException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Private constructor.
     */
    private Repositories() {
    }
}
