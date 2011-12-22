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

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.b3log.latke.Latkes;
import org.b3log.latke.RuntimeDatabase;
import org.b3log.latke.repository.jdbc.util.FieldDefinition;

/**
 * 
 * JdbcFactory.
 * 
 * @author <a href="mailto:wmainlove@gmail.com">Love Yao</a>
 * @version 1.0.0.0, Dec 20, 2011
 */
public final class JdbcFactory implements JdbcDatabase {

    /**
     * the holder of the databaseSolution.
     */
    private AbstractJdbcDatabaseSolution databaseSolution;

    /**
     * the singleton  of jdbcfactory.
     */
    private static JdbcFactory jdbcFactory;

    /**
     * all JdbcDatabaseSolution in here.
     */
    private static Map<RuntimeDatabase, AbstractJdbcDatabaseSolution> jdbcDatabaseSolutionMap 
        = new HashMap<RuntimeDatabase, AbstractJdbcDatabaseSolution>();

    @Override
    public boolean createTable(final String tableName,
            final List<FieldDefinition> fieldDefinitions) throws SQLException {
        return databaseSolution.createTable(null, fieldDefinitions);
    }

    /**
     * singleton way to get jdbcFactory.
     * 
     * @return JdbcFactory jdbcFactory.
     */
    public static synchronized JdbcFactory createJdbcFactory() {

        if (jdbcFactory == null) {
            jdbcFactory = new JdbcFactory();
        }
        return jdbcFactory;
    }

    /**
     * Private constructor.
     */
    private JdbcFactory() {

        /**
         * Latkes.getRuntimeDatabase();
         */
        databaseSolution = jdbcDatabaseSolutionMap.get(Latkes
                .getRuntimeDatabase());
    }

}
