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

import static org.testng.Assert.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import java.sql.Connection;

import org.b3log.latke.repository.Transaction;
import org.b3log.latke.repository.jdbc.util.Connections;
import org.b3log.latke.repository.jdbc.util.JdbcRepositories;
import org.b3log.latke.repository.jdbc.util.JdbcUtil;
import org.json.JSONObject;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * JdbcRepositoryTestCase,now using mysql5.5.9 for test.

 * 
 * @author <a href="mailto:wmainlove@gmail.com">Love Yao</a>
 * @version 1.0.0.0, Jan 7, 2012
 */
public class JdbcRepositoryTestCase {

    /**
     * jdbcRepository.
     */
    private JdbcRepository jdbcRepository = new JdbcRepository("basetable");

    /**
     * if the datebase environment  is wrong,do not run all the other test.
     */
    private boolean ifRun = true;

    /**
     * test JsonData.
     * 
     * @return Object[][] {{JsonObject},{jsonObject}}.
     */
    @DataProvider(name = "jsonData")
    public static Object[][] createJsonData() {

        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("col1", new Integer("100"));
        jsonObject.put("col2", "======aaaaaaaaaaaaaaaaaaaaaaaaaaaaa=======");
        jsonObject.put("col3", "1.4");
        jsonObject.put("col4", false);

        final Object[][] ret = new Object[][] {{jsonObject } };
        return ret;
    }

    /**
     * createTestTable.
     */
    @BeforeGroups(groups = {"jdbc" })
    public void createTestTable() {

        final StringBuffer createTableSql = new StringBuffer();

        createTableSql.append("   CREATE TABLE IF NOT EXISTS basetable");
        createTableSql.append("   ( ");
        createTableSql.append("  oId VARCHAR(200) NOT NULL, ");
        createTableSql.append("  col1 INT, ");
        createTableSql.append("  col2 VARCHAR(200), ");
        createTableSql.append("  col3 DECIMAL(10,2), ");
        createTableSql.append("  col4 CHAR(1), ");
        createTableSql.append("  PRIMARY KEY (oId) ");
        createTableSql.append(" ) ");
        createTableSql.append(" ENGINE=InnoDB DEFAULT CHARSET=utf8; ");

        try {
            final Connection connection = Connections.getConnection();
            JdbcUtil.executeSql(createTableSql.toString(), connection);
            connection.close();
        } catch (final Exception e) {
            //e.printStackTrace();
            ifRun = false;
            System.out.println("skip JdbcRepositoryTestCase test");
        }

    }

    /**
     * add test.
     * 
     * @param jsonObject jsonObject
     * @throws Exception Exception  
     */
    @Test(groups = {"jdbc" }, dataProvider = "createJsonData")
    public void add(final JSONObject jsonObject) throws Exception {

        if (!ifRun) {
            return;
        }

        final Transaction transaction = jdbcRepository.beginTransaction();
        jdbcRepository.add(jsonObject);
        transaction.commit();

        final JSONObject jsonObjectDb = jdbcRepository.get(jsonObject
                .getString(JdbcRepositories.OID));
        assertNotNull(jsonObjectDb);

    }

    /**
     * update test.
     * 
     * @param jsonObject jsonObject
     * @throws Exception Exception
     */
    @Test(groups = {"jdbc" }, dataProvider = "createJsonData")
    public void update(final JSONObject jsonObject) throws Exception {

        if (!ifRun) {
            return;
        }

        final Transaction transaction = jdbcRepository.beginTransaction();
        jdbcRepository.add(jsonObject);

        jsonObject.put("col2", "=================bbbb========================");
        jsonObject.put("col4", true);

        jdbcRepository.update(jsonObject.getString(JdbcRepositories.OID),
                jsonObject);
        transaction.commit();

    }

    /**
     * remove test.
     * 
     * @param jsonObject jsonObject
     * @throws Exception Exception
     */
    @Test(groups = {"jdbc" }, dataProvider = "createJsonData")
    public void remove(final JSONObject jsonObject) throws Exception {

        if (!ifRun) {
            return;
        }

        final Transaction transaction = jdbcRepository.beginTransaction();
        jdbcRepository.add(jsonObject);
        jdbcRepository.remove(jsonObject.getString(JdbcRepositories.OID));
        transaction.commit();

        final JSONObject jsonObjectDB = jdbcRepository.get(jsonObject
                .getString(JdbcRepositories.OID));

        assertNull(jsonObjectDB);

    }

    /**
     * hasAndCount test.
     * 
     * @param jsonObject jsonObject
     * @throws Exception Exception
     */
    @Test(groups = {"jdbc" }, dataProvider = "createJsonData")
    public void hasAndCount(final JSONObject jsonObject) throws Exception {

        if (!ifRun) {
            return;
        }

        final long oCount = jdbcRepository.count();

        final Transaction transaction = jdbcRepository.beginTransaction();
        jdbcRepository.add(jsonObject);
        transaction.commit();

        assertTrue(jdbcRepository.has(jsonObject
                .getString(JdbcRepositories.OID)));

        final long nCount = jdbcRepository.count();
        assertTrue(nCount > oCount);

    }
}
