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
package org.b3log.latke.repository.jdbc.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;

import org.b3log.latke.Keys;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * the jdbc connection pool utils.
 * 
 * @author <a href="mailto:wmainlove@gmail.com">Love Yao</a>
 * @version 1.0.0.0, Dec 20, 2011
 */
public final class Connections {

    /**
     * getConnetcion from pool --TODO pool.
     * 
     * @return {@link Connection}
     */
    public static Connection getConnection() {

        Connection con = null;
        try {
            Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
            con = DriverManager.getConnection("jdbc:odbc:wombat", "login",
                    "password");
            return con;
        } catch (final Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * executeSql.
     * 
     * @param sql sql
     * @param connection connection
     * @return ifsuccess
     * @throws SQLException SQLException
     */
    public static boolean executeSql(final String sql,
            final Connection connection) throws SQLException {

        return connection.createStatement().execute(sql);

    }

    /**
     * querySql.
     * 
     * @param sql sql
     * @param paramList paramList
     * @param connection connection
     * @return JSONObject
     * @throws SQLException SQLException
     * @throws JSONException JSONException 
     */
    public static JSONObject querySql(final String sql,
            final List<Object> paramList, final Connection connection)
            throws SQLException, JSONException {

        final PreparedStatement preparedStatement = connection
                .prepareStatement(sql);

        for (int i = 1; i <= paramList.size(); i++) {

            preparedStatement.setObject(i, paramList.get(i));
        }

        final ResultSet resultSet = preparedStatement.executeQuery();

        final JSONObject jsonObject = resultSetToJsonObject(resultSet);
        preparedStatement.close();
        return jsonObject;

    }

    /**
     * jdbc resultSetToJsonObject to JSONObject.
     * 
     * @param resultSet resultSet
     * @return JSONObject
     * @throws SQLException SQLException 
     * @throws JSONException JSONException
     */
    private static JSONObject resultSetToJsonObject(final ResultSet resultSet)
            throws SQLException, JSONException {

        final ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        final int numColumns = resultSetMetaData.getColumnCount();

        final JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject = null;
        String columnName = null;
        while (resultSet.next()) {
            jsonObject = new JSONObject();

            for (int i = 1; i < numColumns + 1; i++) {
                columnName = resultSetMetaData.getColumnName(i);
                jsonObject.put(columnName, resultSet.getObject(columnName));

            }

            jsonArray.put(jsonObject);
        }

        jsonObject = new JSONObject();
        jsonObject.put(Keys.RESULTS, jsonArray);

        return jsonObject;

    }

    /**
     * Private constructor.
     */
    private Connections() {

    }
}
