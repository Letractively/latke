/*
 * Copyright (c) 2009, 2010, B3log Team
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

import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.log4j.Logger;
import org.b3log.latke.Latkes;

/**
 * Sleepycat.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.2, Jul 23, 2010
 */
public final class Sleepycat {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(Sleepycat.class);
    /**
     * Default environment configurations. Set the following options explicitly: 
     * <ul>
     *   <li>allowCreate=true</li>
     * </ul>
     */
    public static final EnvironmentConfig DEFAULT_ENV_CONFIG =
            new EnvironmentConfig();
    /**
     * Default environment.
     */
    public static final Environment DEFAULT_ENV;
    /**
     * Database cache.
     */
    private static final Map<String, Set<SleepycatDatabase>> DATABASES =
            new HashMap<String, Set<SleepycatDatabase>>();
    /**
     * Default database configurations. Set the following options explicitly:
     * <ul>
     *   <li>allowCreate=true</li>
     *   <li>deferredWrite=true</li>
     * </ul>
     */
    public static final DatabaseConfig DEFAULT_DB_CONFIG = new DatabaseConfig();
    /**
     * Environment path.
     */
    private static final String ENV_PATH;

    static {
        try {
            ENV_PATH = Latkes.getRepositoryPath();
            DEFAULT_ENV_CONFIG.setAllowCreate(true);

            DEFAULT_ENV = new Environment(new File(ENV_PATH),
                                          DEFAULT_ENV_CONFIG);

            DEFAULT_DB_CONFIG.setAllowCreate(true).setDeferredWrite(true);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets a database with the specified repository name and database
     * configuration.
     *
     * @param repositoryName the specified repository name
     * @param databaseConfig the specified database configuration
     * @return the database if it exists {@linkplain #DATABASES cache}, creates
     * and returns a new database with the specified repository name and
     * database configuration
     */
    public static synchronized Database get(final String repositoryName,
                                            final DatabaseConfig databaseConfig) {
        if (DATABASES.containsKey(repositoryName)) {
            final Set<SleepycatDatabase> sleepycatDatabases = DATABASES.get(
                    repositoryName);
            for (SleepycatDatabase sleepycatDatabase : sleepycatDatabases) {
                if (sleepycatDatabase.getDatabaseConfig().equals(databaseConfig)) {
                    return sleepycatDatabase.getDatabase();
                }
            }
        }

        final Database ret = DEFAULT_ENV.openDatabase(null,
                                                      repositoryName,
                                                      databaseConfig);
        LOGGER.info("Created database[repositoryName=" + repositoryName + "]");

        final Set<SleepycatDatabase> sleepycatDatabases =
                new HashSet<SleepycatDatabase>();
        sleepycatDatabases.add(new SleepycatDatabase(ret, databaseConfig));
        DATABASES.put(repositoryName, sleepycatDatabases);

        return ret;
    }

    /**
     * Shutdowns databases and default environment.
     */
    public static synchronized void shutdown() {
        for (Entry<String, Set<SleepycatDatabase>> entry : DATABASES.entrySet()) {
            final Set<SleepycatDatabase> sleepycatDatabases = entry.getValue();
            for (final SleepycatDatabase sleepycatDatabase : sleepycatDatabases) {
                final Database database = sleepycatDatabase.getDatabase();
                database.close();
                LOGGER.info("Closed database[name=" + entry.getKey() + "]");
            }
        }

        DEFAULT_ENV.close();
        LOGGER.info("Closed data store envionment");
        LOGGER.info("SleepCat has been shutdown");
    }

    /**
     * Private constructor.
     */
    private Sleepycat() {
    }
}

/**
 * Sleepycat database.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.0, Jul 21, 2010
 * @see Database
 * @see DatabaseConfig
 */
final class SleepycatDatabase {

    /**
     * Database.
     */
    private Database database;
    /**
     * Database configuration.
     */
    private DatabaseConfig databaseConfig;

    /**
     * Package protected constructor.
     *
     * @param database database
     * @param databaseConfig database configuration
     */
    SleepycatDatabase(final Database database,
                      final DatabaseConfig databaseConfig) {
        this.database = database;
        this.databaseConfig = databaseConfig;
    }

    /**
     * Gets the database.
     *
     * @return database
     */
    public Database getDatabase() {
        return database;
    }

    /**
     * Gets the database configuration.
     *
     * @return database configuration
     */
    public DatabaseConfig getDatabaseConfig() {
        return databaseConfig;
    }
}
