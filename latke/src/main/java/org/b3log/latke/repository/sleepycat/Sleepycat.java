/*
 * Copyright 2009, 2010, B3log Team
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
import java.util.Map;
import java.util.Map.Entry;
import org.apache.log4j.Logger;
import org.b3log.latke.Latkes;

/**
 * Sleepycat.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.0, Jul 21, 2010
 */
public final class Sleepycat {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(Sleepycat.class);
    /**
     * Default sleepycat environment.
     */
    public static final Environment DEFAULT_ENV;
    /**
     * Database cache.
     */
    private static final Map<String, SleepycatDatabase> DATABASES =
            new HashMap<String, SleepycatDatabase>();
    /**
     * Default database configurations. Set the following options explicitly:
     * <ul>
     *   <li>allowCreate=true</li>
     *   <li>deferredWrite=true</li>
     * </ul>
     */
    public static final DatabaseConfig DEFAULT_DB_CONFIG = new DatabaseConfig();
    /**
     *
     */
    private static final String ENV_PATH;

    static {
        try {
            ENV_PATH = Latkes.getRepositoryPath();
            final EnvironmentConfig envConfig = new EnvironmentConfig();
            envConfig.setAllowCreate(true);

            DEFAULT_ENV = new Environment(new File(ENV_PATH), envConfig);

            DEFAULT_DB_CONFIG.setAllowCreate(true);
            DEFAULT_DB_CONFIG.setDeferredWrite(true);
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
            final SleepycatDatabase sleepycatDatabase = DATABASES.get(repositoryName);

            if (sleepycatDatabase.getDatabaseConfig().equals(databaseConfig)) {
                return sleepycatDatabase.getDatabase();
            }
        }

        final Database ret = DEFAULT_ENV.openDatabase(null,
                                                      repositoryName, databaseConfig);
        LOGGER.info("Created database[repositoryName=" + repositoryName + "]");

        DATABASES.put(repositoryName, new SleepycatDatabase(ret, databaseConfig));

        return ret;
    }

    /**
     * Shutdowns sleepycat databases and default environment.
     */
    public static synchronized void shutdown() {
        for (Entry<String, SleepycatDatabase> entry : DATABASES.entrySet()) {
            entry.getValue().getDatabase().close();
        }

        DEFAULT_ENV.close();

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
