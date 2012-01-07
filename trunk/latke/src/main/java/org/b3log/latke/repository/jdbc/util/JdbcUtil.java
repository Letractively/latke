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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.logging.Logger;

import org.b3log.latke.Keys;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 
 * jdbcUtil.
 * 
 * @author <a href="mailto:wmainlove@gmail.com">Love Yao</a>
 * @version 1.0.0.0, Dec 20, 2011
 */
public final class JdbcUtil {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(JdbcUtil.class
            .getName());

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

        final Statement statement = connection.createStatement();
        final boolean isSuccess = statement.execute(sql);
        statement.close();

        return isSuccess;
    }

    /**
     * 
     * executeSql.
     * 
     * @param sql sql
     * @param paramList paramList
     * @param connection connection
     * @return issuccess
     * @throws SQLException SQLException
     */
    public static boolean executeSql(final String sql,
            final List<Object> paramList, final Connection connection)
            throws SQLException {

        LOGGER.info("executeSql:" + sql);

        final PreparedStatement preparedStatement = connection
                .prepareStatement(sql);

        for (int i = 1; i <= paramList.size(); i++) {

            preparedStatement.setObject(i, paramList.get(i - 1));
        }
        final boolean isSuccess = preparedStatement.execute();
        preparedStatement.close();

        return isSuccess;
    }

    /**
     * queryJsonObject.
     * 
     * 
     * @param sql sql
     * @param paramList paramList
     * @param connection connection
     * @return JSONObject only one record.
     * @throws SQLException SQLException
     * @throws JSONException JSONException
     */
    public static JSONObject queryJsonObject(final String sql,
            final List<Object> paramList, final Connection connection)
            throws SQLException, JSONException {

        return queryJson(sql, paramList, connection, true);

    }

    /**
     * 
     * queryJsonArray.
     * 
     * @param sql sql
     * @param paramList paramList
     * @param connection connection
     * @return JSONArray
     * @throws SQLException SQLException
     * @throws JSONException JSONException
     */
    public static JSONArray queryJsonArray(final String sql,
            final List<Object> paramList, final Connection connection)
            throws SQLException, JSONException {

        final JSONObject jsonObject = queryJson(sql, paramList, connection,
                false);
        return jsonObject.getJSONArray(Keys.RESULTS);

    }

    /**
     * 
     * @param sql sql
     * @param paramList paramList
     * @param connection connection
     * @param ifOnlyOne ifOnlyOne to determine return object or array.
     * @return JSONObject
     * @throws SQLException SQLException
     * @throws JSONException JSONException
     */
    private static JSONObject queryJson(final String sql,
            final List<Object> paramList, final Connection connection,
            final boolean ifOnlyOne) throws SQLException, JSONException {

        LOGGER.info("querySql:" + sql);

        final PreparedStatement preparedStatement = connection
                .prepareStatement(sql);

        for (int i = 1; i <= paramList.size(); i++) {

            preparedStatement.setObject(i, paramList.get(i));
        }

        final ResultSet resultSet = preparedStatement.executeQuery();

        final JSONObject jsonObject = resultSetToJsonObject(resultSet,
                ifOnlyOne);
        preparedStatement.close();
        return jsonObject;

    }

    /**
     * resultSetToJsonObject.
     * 
     * @param resultSet resultSet
     * @param ifOnlyOne ifOnlyOne
     * @return JSONObject
     * @throws SQLException SQLException
     * @throws JSONException JSONException
     */
    private static JSONObject resultSetToJsonObject(final ResultSet resultSet,
            final boolean ifOnlyOne) throws SQLException, JSONException {

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

        if (ifOnlyOne) {

            jsonObject = jsonArray.getJSONObject(0);
            return jsonObject;

        }

        jsonObject = new JSONObject();
        jsonObject.put(Keys.RESULTS, jsonArray);

        return jsonObject;

    }

    /**
     * Private constructor.
     */
    private JdbcUtil() {

    }

}
