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
package org.b3log.latke.repository.jdbc;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.b3log.latke.repository.jdbc.util.Connections;
import org.b3log.latke.repository.jdbc.util.FieldDefinition;
import org.b3log.latke.repository.jdbc.util.JdbcUtil;

/**
 * 
 * JdbcDatabaseSolution.
 * 
 * @author <a href="mailto:wmainlove@gmail.com">Love Yao</a>
 * @version 1.0.0.0, Dec 20, 2011
 */
public abstract class AbstractJdbcDatabaseSolution implements JdbcDatabase {

    /**
     * the map Mapping type to real database type. 
     */
    private Map<String, String> jdbcTypeMap = new HashMap<String, String>();

    /**
     * 
     * registerType.
     * 
     * @param type type from json
     * @param databaseType real databaseType
     */
    protected void registerType(final String type, final String databaseType) {
        jdbcTypeMap.put(type, databaseType);
    }

    @Override
    public boolean createTable(final String tableName,
            final List<FieldDefinition> fieldDefinitions) throws SQLException {

        final StringBuffer createTableSql = new StringBuffer();

        createTableHead(createTableSql, tableName);
        createTableBody(createTableSql, fieldDefinitions);
        createTableEnd(createTableSql);

        return JdbcUtil.executeSql(createTableSql.toString(),
                Connections.getConnection());

    }

    /**
     * 
     * abstract createTableHead for each DB to impl.
     * 
     * @param createTableSql createSql
     * @param tableName tableName
     */
    protected abstract void createTableHead(StringBuffer createTableSql,
            String tableName);

    /**
     * abstract createTableBody for each DB to impl.
     * 
     * @param createTableSql createSql
     * @param fieldDefinitions {@link FieldDefinition}
     */
    protected abstract void createTableBody(StringBuffer createTableSql,
            List<FieldDefinition> fieldDefinitions);

    /**
     * abstract createTableEnd for each DB to impl.
     * @param createTableSql createSql 
     */
    protected abstract void createTableEnd(StringBuffer createTableSql);

}
