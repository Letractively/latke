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

import java.util.ArrayList;
import java.util.List;

import org.b3log.latke.repository.jdbc.mapping.BooleanMapping;
import org.b3log.latke.repository.jdbc.mapping.IntMapping;
import org.b3log.latke.repository.jdbc.mapping.Mapping;
import org.b3log.latke.repository.jdbc.mapping.NumberMapping;
import org.b3log.latke.repository.jdbc.mapping.StringMapping;
import org.b3log.latke.repository.jdbc.util.FieldDefinition;

/**
 * DefaultJdbcDatabaseSolution,for extend .
 * 
 * @author <a href="mailto:wmainlove@gmail.com">Love Yao</a>
 * @version 1.0.0.0, Jan 12, 2012
 */
public class DefaultJdbcDatabaseSolution extends AbstractJdbcDatabaseSolution {

    /**
     * public constructor.
     */
    public DefaultJdbcDatabaseSolution() {
        registerType("int", new IntMapping());
        registerType("boolean", new BooleanMapping());
        registerType("long", new IntMapping());
        registerType("double", new NumberMapping());
        registerType("String", new StringMapping());

    }

    @Override
    public String queryPage(final int start, final int end,
            final String filterSql, final String orderBySql,
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
    protected void createDropTableSql(final StringBuffer dropTableSql,
            final String tableName) {
        dropTableSql.append("DROP TABLE  IF EXISTS ").append(tableName)
                .append(";");

    }

    @Override
    protected void createTableHead(final StringBuffer createTableSql,
            final String tableName) {
        //        createTableSql.append("DROP TABLE  IF EXISTS ").append(tableName)
        //                .append(";");
        createTableSql.append("CREATE TABLE ").append(tableName).append("(");

    }

    @Override
    protected void createTableBody(final StringBuffer createTableSql,
            final List<FieldDefinition> fieldDefinitions) {

        final List<FieldDefinition> keyDefinitionList =
                new ArrayList<FieldDefinition>();
        for (FieldDefinition fieldDefinition : fieldDefinitions) {

            final String type = fieldDefinition.getType();
            if (type == null) {
                throw new RuntimeException(
                        "the type of fieldDefinitions should not be null");
            }
            final Mapping mapping = getJdbcTypeMapping().get(type);
            if (mapping != null) {

                createTableSql.append(mapping.toDataBaseSting(fieldDefinition))
                        .append(",   ");

                if (fieldDefinition.getIsKey()) {
                    keyDefinitionList.add(fieldDefinition);
                }
            } else {

                throw new RuntimeException("the type["
                        + fieldDefinition.getType()
                        + "] is not register for mapping ");
            }

        }

        if (keyDefinitionList.size() < 0) {
            throw new RuntimeException("no key talbe is not allow");

        } else {
            createTableSql.append(createKeyDefinition(keyDefinitionList));
        }

    }

    /**
     * 
     * the keyDefinitionList tableSql.
     * 
     * 
     * @param keyDefinitionList keyDefinitionList
     * @return createKeyDefinitionsql
     */
    private String createKeyDefinition(
            final List<FieldDefinition> keyDefinitionList) {

        final StringBuffer sql = new StringBuffer();
        sql.append(" PRIMARY KEY");
        boolean isFirst = true;
        for (FieldDefinition fieldDefinition : keyDefinitionList) {
            if (isFirst) {
                sql.append("(");
                isFirst = false;
            } else {
                sql.append(",");
            }
            sql.append(fieldDefinition.getName());
        }

        sql.append(")");
        return sql.toString();
    }

    @Override
    protected void createTableEnd(final StringBuffer createTableSql) {
        createTableSql.append(") ENGINE= InnoDB DEFAULT CHARSET= utf8;");

    }


}
