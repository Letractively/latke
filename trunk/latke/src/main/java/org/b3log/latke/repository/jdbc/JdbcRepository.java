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

import java.io.Serializable;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.b3log.latke.Keys;
import org.b3log.latke.cache.Cache;
import org.b3log.latke.repository.Query;
import org.b3log.latke.repository.Repository;
import org.b3log.latke.repository.RepositoryException;
import org.b3log.latke.repository.Transaction;
import org.b3log.latke.repository.jdbc.util.Connections;
import org.b3log.latke.repository.jdbc.util.JdbcRepositories;
import org.b3log.latke.repository.jdbc.util.JdbcUtil;
import org.b3log.latke.util.Ids;
import org.b3log.latke.util.Strings;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * JdbcRepository.
 * 
 * @author <a href="mailto:wmainlove@gmail.com">Love Yao</a>
 * @version 1.0.0.0, Dec 20, 2011
 */
public class JdbcRepository implements Repository {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(JdbcRepository.class
            .getName());

    /**
     * Repository name.
     */
    private String name;

    /**
     * Is cache enabled?
     */
    private boolean cacheEnabled = true;

    /**
     * The current transaction.
     */
    public static final ThreadLocal<JdbcTransaction> TX = new InheritableThreadLocal<JdbcTransaction>();

    @Override
    public String add(final JSONObject jsonObject) throws RepositoryException {

        final JdbcTransaction currentTransaction = TX.get();
        if (null == currentTransaction) {
            throw new RepositoryException(
                    "Invoking add() outside a transaction");
        }

        final Connection connection = Connections.getConnection();
        final List<Object> paramList = new ArrayList<Object>();
        final StringBuffer sql = new StringBuffer();
        String id = null;

        try {
            id = buildAddSql(jsonObject, paramList, sql);
            JdbcUtil.executeSql(sql.toString(), paramList, connection);
        } catch (final Exception e) {
            LOGGER.log(Level.SEVERE, "add:" + e.getMessage(), e);
            throw new RepositoryException(e);
        }

        return id;
    }

    /**
     * buildAddSql.
     * @param jsonObject jsonObject
     * @param paramlist paramlist 
     * @param sql sql
     * @return id
     * @throws JSONException  JSONException
     */
    private String buildAddSql(final JSONObject jsonObject,
            final List<Object> paramlist, final StringBuffer sql)
            throws JSONException {

        String ret = null;

        if (!jsonObject.has(Keys.OBJECT_ID)) {
            ret = Ids.genTimeMillisId();
            jsonObject.put(Keys.OBJECT_ID, ret);
        } else {
            ret = jsonObject.getString(Keys.OBJECT_ID);
        }

        setProperties(jsonObject, paramlist, sql);

        return ret;

    }

    /**
     * setProperties.
     * 
     * @param jsonObject jsonObject
     * @param paramlist paramlist
     * @param sql sql
     * @throws JSONException JSONException 
     */
    private void setProperties(final JSONObject jsonObject,
            final List<Object> paramlist, final StringBuffer sql)
            throws JSONException {

        @SuppressWarnings("unchecked")
        final Iterator<String> keys = jsonObject.keys();

        final StringBuffer insertString = new StringBuffer();
        final StringBuffer wildcardString = new StringBuffer();

        boolean isFirst = true;
        String key = null;
        Object value = null;

        while (keys.hasNext()) {

            key = keys.next();

            if (isFirst) {
                insertString.append("(").append(key);
                wildcardString.append("(?");
                isFirst = false;
            } else {
                insertString.append(",").append(key);
                wildcardString.append(",?");
            }

            value = jsonObject.get(key);
            paramlist.add(value);

            if (keys.hasNext()) {
                insertString.append(")");
                wildcardString.append(")");
            }
        }

        /**
         * TODO table name Prefix.
         */
        sql.append("insert into ").append(getName()).append(insertString)
                .append(" value ").append(wildcardString);

    }

    @Override
    public void update(final String id, final JSONObject jsonObject)
            throws RepositoryException {

        if (Strings.isEmptyOrNull(id)) {
            return;
        }

        final JdbcTransaction currentTransaction = TX.get();

        if (null == currentTransaction) {
            throw new RepositoryException(
                    "Invoking update() outside a transaction");
        }

        final JSONObject oldJsonObject = get(id);

        final Connection connection = Connections.getConnection();
        final List<Object> paramList = new ArrayList<Object>();
        final StringBuffer sql = new StringBuffer();
        try {
            update(id, oldJsonObject, jsonObject, paramList, sql);
            JdbcUtil.executeSql(sql.toString(), paramList, connection);
        } catch (final Exception e) {
            LOGGER.log(Level.SEVERE, "update:" + e.getMessage(), e);
            throw new RepositoryException(e);
        }

    }

    /**
     * 
     * update.
     * 
     * @param id id
     * @param oldJsonObject oldJsonObject
     * @param jsonObject newJsonObject
     * @param paramList paramList
     * @param sql sql
     * @throws JSONException JSONException
     */
    private void update(final String id, final JSONObject oldJsonObject,
            final JSONObject jsonObject, final List<Object> paramList,
            final StringBuffer sql) throws JSONException {

        final JSONObject needUpdateJsonObject = getNeedUpdateJsonObject(
                oldJsonObject, jsonObject);

        if (needUpdateJsonObject.length() == 0) {
            LOGGER.log(Level.INFO,
                    "nothing to update [{0}]for  repository[{1}]",
                    new Object[] {id, getName() });
            return;
        }

        setUpdateProperties(id, needUpdateJsonObject, paramList, sql);

    }

    /**
     * setUpdateProperties.
     * 
     * @param id id
     * @param needUpdateJsonObject needUpdateJsonObject
     * @param paramList paramList
     * @param sql sql
     * @throws JSONException JSONException
     */
    private void setUpdateProperties(final String id,
            final JSONObject needUpdateJsonObject,
            final List<Object> paramList, final StringBuffer sql)
            throws JSONException {

        final Iterator<String> keys = needUpdateJsonObject.keys();
        String key;

        boolean isFirst = true;
        final StringBuffer wildcardString = new StringBuffer();

        while (keys.hasNext()) {
            key = keys.next();

            if (isFirst) {
                wildcardString.append("set ").append(key).append("=?");
                isFirst = false;
            } else {
                wildcardString.append(",").append(key).append("=?");
            }

            paramList.add(needUpdateJsonObject.get(key));
        }

        sql.append("update ").append(getName()).append(wildcardString)
                .append("where ").append(JdbcRepositories.OID).append("=")
                .append(id);

    }

    /**
     * 
     * getNeedUpdateJsonObject.
     * 
     * @param oldJsonObject oldJsonObject
     * @param jsonObject newJsonObject
     * @return JSONObject
     * @throws JSONException jsonObject
     */
    private JSONObject getNeedUpdateJsonObject(final JSONObject oldJsonObject,
            final JSONObject jsonObject) throws JSONException {

        final JSONObject needUpdateJsonObject = new JSONObject();

        @SuppressWarnings("unchecked")
        final Iterator<String> keys = jsonObject.keys();

        String key = null;
        while (keys.hasNext()) {

            key = keys.next();

            if (jsonObject.get(key) == null && oldJsonObject.get(key) == null) {
                //???????????????????????????
                needUpdateJsonObject.put(key, jsonObject.get(key));

            } else if (!jsonObject.getString(key).equals(
                    oldJsonObject.getString(key))) {
                needUpdateJsonObject.put(key, jsonObject.get(key));
            }

        }

        return jsonObject;
    }

    @Override
    public void remove(final String id) throws RepositoryException {

        if (Strings.isEmptyOrNull(id)) {
            return;
        }

        final JdbcTransaction currentTransaction = TX.get();

        if (null == currentTransaction) {
            throw new RepositoryException(
                    "Invoking remove() outside a transaction");
        }

        final StringBuffer sql = new StringBuffer();
        final Connection connection = Connections.getConnection();

        try {
            remove(id, sql);
            JdbcUtil.executeSql(sql.toString(), connection);
        } catch (final Exception e) {
            LOGGER.log(Level.SEVERE, "remove:" + e.getMessage(), e);
            throw new RepositoryException(e);
        }

    }

    /**
     * remove record.
     * 
     * @param id id
     * @param sql sql
     */
    private void remove(final String id, final StringBuffer sql) {
        sql.append("delete from ").append(getName()).append(" where ")
                .append(JdbcRepositories.OID).append("=").append(id);
    }

    @Override
    public JSONObject get(final String id) throws RepositoryException {

        final StringBuffer sql = new StringBuffer();
        final Connection connection = Connections.getConnection();
        JSONObject jsonObject = null;

        try {
            get(id, sql);
            jsonObject = JdbcUtil.querySql(sql.toString(),
                    new ArrayList<Object>(), connection);
        } catch (final Exception e) {
            LOGGER.log(Level.SEVERE, "get:" + e.getMessage(), e);
            throw new RepositoryException(e);
        }

        return jsonObject;

    }

    /**
     * get.
     * 
     * @param id id
     * @param sql sql
     */
    private void get(final String id, final StringBuffer sql) {

        sql.append("select * from ").append(getName()).append(" where ")
                .append(JdbcRepositories.OID).append("=").append(id);

    }

    @Override
    public Map<String, JSONObject> get(final Iterable<String> ids)
            throws RepositoryException {

        return null;
    }

    @Override
    public boolean has(final String id) throws RepositoryException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public JSONObject get(final Query query) throws RepositoryException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<JSONObject> getRandomly(final int fetchSize)
            throws RepositoryException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long count() throws RepositoryException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Transaction beginTransaction() {

        final JdbcTransaction ret = TX.get();
        if (null != ret) {
            LOGGER.log(Level.FINER,
                    "There is a transaction[isActive={0}] in current thread",
                    ret.isActive());
            if (ret.isActive()) {
                return TX.get(); // Using 'the current transaction'
            }
        }

        final JdbcTransaction jdbcTransaction = new JdbcTransaction();
        TX.set(jdbcTransaction);

        return ret;

    }

    @Override
    public boolean isCacheEnabled() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setCacheEnabled(final boolean isCacheEnabled) {
        // TODO Auto-generated method stub

    }

    @Override
    public Cache<String, Serializable> getCache() {
        // TODO Auto-generated method stub
        return null;
    }

}
