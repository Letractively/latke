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

import java.util.List;

import org.b3log.latke.repository.jdbc.util.FieldDefinition;

/**
 * DefaultJdbcDatabaseSolution,for extend .
 * 
 * @author <a href="mailto:wmainlove@gmail.com">Love Yao</a>
 * @version 1.0.0.0, Jan 12, 2012
 */
public class DefaultJdbcDatabaseSolution extends AbstractJdbcDatabaseSolution {

    @Override
    public String queryPage(final int start, final int end, final String filterSql, final String orderBySql,
            final String tableName) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getRandomlySql(final String tableName, final int fetchSize) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected void createTableHead(final StringBuffer createTableSql, final String tableName) {
        createTableSql.append("DROP TABLE  IF EXISTS ").append(tableName).append(";");
        createTableSql.append("CREATE TABLE ").append(tableName).append("(");

    }

    @Override
    protected void createTableBody(final StringBuffer createTableSql, final List<FieldDefinition> fieldDefinitions) {

        
    }

    @Override
    protected void createTableEnd(final StringBuffer createTableSql) {
        createTableSql.append(") ENGINE= InnoDB DEFAULT CHARSET= utf8;");

    }

}
