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

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.b3log.latke.Keys;
import org.b3log.latke.cache.Cache;
import org.b3log.latke.model.Pagination;
import org.b3log.latke.repository.Filter;
import org.b3log.latke.repository.FilterOperator;
import org.b3log.latke.repository.Query;
import org.b3log.latke.repository.Repository;
import org.b3log.latke.repository.RepositoryException;
import org.b3log.latke.repository.SortDirection;
import org.b3log.latke.repository.Transaction;
import org.b3log.latke.repository.jdbc.util.Connections;
import org.b3log.latke.repository.jdbc.util.JdbcRepositories;
import org.b3log.latke.repository.jdbc.util.JdbcUtil;
import org.b3log.latke.util.Ids;
import org.b3log.latke.util.Strings;
import org.json.JSONArray;
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
    public static final ThreadLocal<JdbcTransaction> TX =
            new InheritableThreadLocal<JdbcTransaction>();

    @Override
    public String add(final JSONObject jsonObject) throws RepositoryException {

        final JdbcTransaction currentTransaction = TX.get();
        if (null == currentTransaction) {
            throw new RepositoryException(
                    "Invoking add() outside a transaction");
        }

        final Connection connection = getConnection();
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

            if (!keys.hasNext()) {
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

        final Connection connection = getConnection();
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

        final JSONObject needUpdateJsonObject =
                getNeedUpdateJsonObject(oldJsonObject, jsonObject);

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

        @SuppressWarnings("unchecked")
        final Iterator<String> keys = needUpdateJsonObject.keys();
        String key;

        boolean isFirst = true;
        final StringBuffer wildcardString = new StringBuffer();

        while (keys.hasNext()) {
            key = keys.next();

            if (isFirst) {
                wildcardString.append(" set ").append(key).append("=?");
                isFirst = false;
            } else {
                wildcardString.append(",").append(key).append("=?");
            }

            paramList.add(needUpdateJsonObject.get(key));
        }

        sql.append("update ").append(getName()).append(wildcardString)
                .append(" where ").append(JdbcRepositories.OID).append("=")
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

        return needUpdateJsonObject;
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
        final Connection connection = getConnection();

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
        final Connection connection = getConnection();
        JSONObject jsonObject = null;

        try {
            get(id, sql);
            jsonObject =
                    JdbcUtil.queryJsonObject(sql.toString(),
                            new ArrayList<Object>(), connection);
        } catch (final Exception e) {
            LOGGER.log(Level.SEVERE, "get:" + e.getMessage(), e);
            throw new RepositoryException(e);
        }

        closeQueryConnection(connection);

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

        final Map<String, JSONObject> map = new HashMap<String, JSONObject>();

        JSONObject jsonObject = null;

        try {
            for (String id : ids) {
                jsonObject = get(id);
                map.put(jsonObject.getString(JdbcRepositories.OID), jsonObject);
            }
        } catch (final Exception e) {
            LOGGER.log(Level.SEVERE, "get ids :" + e.getMessage(), e);
            throw new RepositoryException(e);
        }

        return map;
    }

    @Override
    public boolean has(final String id) throws RepositoryException {

        final StringBuffer sql =
                new StringBuffer("select count(" + JdbcRepositories.OID
                        + ") from ").append(getName()).append(" where ")
                        .append(JdbcRepositories.OID).append("=").append(id);

        if (count(sql, new ArrayList<Object>()) > 0) {

            return true;
        }

        return false;
    }

    @Override
    public JSONObject get(final Query query) throws RepositoryException {

        final int currentPageNum = query.getCurrentPageNum();
        final List<Filter> filters = query.getFilters();
        final int pageSize = query.getPageSize();
        final Map<String, SortDirection> sorts = query.getSorts();
        final int pageCount = query.getPageCount();

        final StringBuffer sql = new StringBuffer();
        final Connection connection = getConnection();
        final List<Object> paramList = new ArrayList<Object>();
        final JSONObject jsonObject = new JSONObject();

        try {

            final int pageCnt =
                    get(currentPageNum, pageSize, pageCount, sorts, filters,
                            sql, paramList);

            if (pageCnt == 0) {
                jsonObject.put(Keys.RESULTS, new JSONArray());
                return jsonObject;
            }

            //result
            final JSONArray jsonResults =
                    JdbcUtil.queryJsonArray(sql.toString(), paramList,
                            connection);
            jsonObject.put(Keys.RESULTS, jsonResults);

            //page
            final JSONObject pagination = new JSONObject();
            pagination.put(Pagination.PAGINATION_PAGE_COUNT, pageCnt);
            jsonObject.put(Pagination.PAGINATION, pagination);

        } catch (final Exception e) {
            LOGGER.log(Level.SEVERE, "query :" + e.getMessage(), e);
            throw new RepositoryException(e);
        }

        closeQueryConnection(connection);

        return jsonObject;

    }

    /**
     * 
     * getQuery sql.
     * 
     * @param currentPageNum currentPageNum
     * @param pageSize pageSize
     * @param pageCount pageCount
     * @param sorts sorts
     * @param filters filters
     * @param sql sql
     * @param paramList paramList
     * @return pageCnt
     * @throws RepositoryException  RepositoryException
     */
    private int get(final int currentPageNum, final int pageSize,
            final int pageCount, final Map<String, SortDirection> sorts,
            final List<Filter> filters, final StringBuffer sql,
            final List<Object> paramList) throws RepositoryException {

        int pageCnt = pageCount;

        final StringBuffer filterSql = new StringBuffer();
        final StringBuffer orderBySql = new StringBuffer();
        getFilterSql(filterSql, paramList, filters);
        getOrderBySql(orderBySql, sorts);

        if (-1 == pageCount) {
            final StringBuffer countSql =
                    new StringBuffer("select count(" + JdbcRepositories.OID
                            + ") from ").append(getName());

            countSql.append(" where ").append(filterSql);
            final long count = count(countSql, paramList);
            pageCnt = (int) Math.ceil((double) count / (double) pageSize);
        }

        if (pageCnt == 0) {
            return 0;
        }

        if (currentPageNum >  pageCnt) {
            LOGGER.severe("currentPageNum > pageCount ");
            throw new RepositoryException("currentPageNum > pageCount");
        }

        getQuerySql(currentPageNum, pageSize, filterSql, orderBySql, sql);
        return pageCnt;
    }

    /**
     * 
     * getQuerySql.
     * 
     * @param currentPageNum currentPageNum
     * @param pageSize  pageSize
     * @param filterSql filterSql
     * @param orderBySql orderBySql
     * @param sql sql
     */
    private void getQuerySql(final int currentPageNum, final int pageSize,
            final StringBuffer filterSql, final StringBuffer orderBySql,
            final StringBuffer sql) {

        final int start = (currentPageNum - 1) * pageSize;
        final int end = start + pageSize;

        sql.append(JdbcFactory.createJdbcFactory().queryPage(start, end,
                filterSql.toString(), orderBySql.toString(), getName()));

    }

    /**
     * 
     * get filterSql and paramList.
     * 
     * @param filterSql filterSql
     * @param paramList paramList
     * @param filters filters
     * @throws RepositoryException RepositoryException
     */
    private void getFilterSql(final StringBuffer filterSql,
            final List<Object> paramList, final List<Filter> filters)
            throws RepositoryException {

        boolean isFirst = true;
        String filterOperator = null;

        for (Filter filter : filters) {

            switch (filter.getOperator()) {
            case EQUAL:
                filterOperator = "=";
                break;
            case GREATER_THAN:
                filterOperator = ">";
                break;
            case GREATER_THAN_OR_EQUAL:
                filterOperator = ">=";
                break;
            case LESS_THAN:
                filterOperator = "<";
                break;
            case LESS_THAN_OR_EQUAL:
                filterOperator = "<=";
                break;
            case NOT_EQUAL:
                filterOperator = "!=";
                break;
            case IN:
                filterOperator = "in";
                break;
            default:
                throw new RepositoryException("Unsupported filter operator["
                        + filter.getOperator() + "]");
            }

            if (isFirst) {
                isFirst = false;
            } else {
                filterSql.append(" and ");
            }

            if (FilterOperator.IN != filter.getOperator()) {
                filterSql.append(filter.getKey()).append(filterOperator)
                        .append("?");
                paramList.add(filter.getValue());
            } else {

                @SuppressWarnings("unchecked")
                final Collection<Object> objects =
                        (Collection<Object>) filter.getValue();

                boolean isSubFist = true;
                if (objects != null && objects.size() > 0) {
                    filterSql.append(filter.getKey()).append(" in ");

                    final Iterator<Object> obs = objects.iterator();
                    while (obs.hasNext()) {
                        if (isSubFist) {
                            filterSql.append("(");
                            isSubFist = false;
                        } else {
                            filterSql.append(",");
                        }
                        filterSql.append("?");
                        paramList.add(obs.next());

                        if (!obs.hasNext()) {
                            filterSql.append(") ");
                        }

                    }

                }
            }

        }

    }

    /**
     * 
     * getOrderBySql.
     * 
     * @param orderBySql orderBySql
     * @param sorts sorts
     */
    private void getOrderBySql(final StringBuffer orderBySql,
            final Map<String, SortDirection> sorts) {

        boolean isFirst = true;
        String querySortDirection = null;
        for (final Map.Entry<String, SortDirection> sort : sorts.entrySet()) {
            if (isFirst) {
                orderBySql.append(" order by ");
                isFirst = false;
            } else {
                orderBySql.append(",");
            }

            if (sort.getValue().equals(SortDirection.ASCENDING)) {
                querySortDirection = "asc";
            } else {
                querySortDirection = "desc";
            }

            orderBySql.append(sort.getKey()).append(" ")
                    .append(querySortDirection);

        }

    }

    @Override
    public List<JSONObject> getRandomly(final int fetchSize)
            throws RepositoryException {

        final List<JSONObject> jsonObjects = new ArrayList<JSONObject>();

        final StringBuffer sql = new StringBuffer();
        JSONArray jsonArray = null;

        final Connection connection = getConnection();
        getRandomly(fetchSize, sql);
        try {
            jsonArray =
                    JdbcUtil.queryJsonArray(sql.toString(),
                            new ArrayList<Object>(), connection);

            for (int i = 0; i < jsonArray.length(); i++) {
                jsonObjects.add(jsonArray.getJSONObject(i));
            }
        } catch (final Exception e) {
            LOGGER.log(Level.SEVERE, "getRandomly :" + e.getMessage(), e);
            throw new RepositoryException(e);
        }

        closeQueryConnection(connection);
        return jsonObjects;
    }

    /**
     * getRandomly.
     * 
     * @param fetchSize fetchSize
     * @param sql sql
     */
    private void getRandomly(final int fetchSize, final StringBuffer sql) {

        sql.append(JdbcFactory.createJdbcFactory().getRandomlySql(getName(),
                fetchSize));
    }

    @Override
    public long count() throws RepositoryException {

        final StringBuffer sql =
                new StringBuffer("select count(" + JdbcRepositories.OID
                        + ") from ").append(getName());
        return count(sql, new ArrayList<Object>());
    }

    /**
     * count.
     * 
     * @param sql sql
     * @param paramList paramList
     * @return count
     * @throws RepositoryException RepositoryException
     */
    private long count(final StringBuffer sql, final List<Object> paramList)
            throws RepositoryException {

        final Connection connection = getConnection();

        JSONObject jsonObject;
        long count;
        try {
            jsonObject =
                    JdbcUtil.queryJsonObject(sql.toString(), paramList,
                            connection);

            count = jsonObject.getLong(jsonObject.keys().next().toString());
        } catch (final Exception e) {
            LOGGER.log(Level.SEVERE, "count :" + e.getMessage(), e);
            throw new RepositoryException(e);
        }

        closeQueryConnection(connection);

        return count;
    }

    @Override
    public String getName() {
        return name;
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

        JdbcTransaction jdbcTransaction = null;
        try {
            jdbcTransaction = new JdbcTransaction();
        } catch (final SQLException e) {
            LOGGER.severe("init jdbcTransaction wrong");
        }
        TX.set(jdbcTransaction);

        return jdbcTransaction;

    }

    @Override
    public boolean isCacheEnabled() {
        return cacheEnabled;
    }

    @Override
    public void setCacheEnabled(final boolean isCacheEnabled) {
        cacheEnabled = isCacheEnabled;
    }

    @Override
    public Cache<String, Serializable> getCache() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Constructs a JDBC repository with the specified name.
     * 
     * @param name the specified name
     */
    public JdbcRepository(final String name) {
        this.name = name;
    }

    /**
     * dispose the resource when requestDestroyed .
     */
    public static void dispose() {

        final JdbcTransaction jdbcTransaction = TX.get();
        if (jdbcTransaction == null) {
            return;
        }

        if (jdbcTransaction.getConnection() != null) {
            jdbcTransaction.dispose();
        }

    }

    /**
     * 
     * getConnection.
     * default using current JdbcTransaction's connection,if null get a new one.
     * 
     * @return {@link Connection}
     */
    private Connection getConnection() {

        final JdbcTransaction jdbcTransaction = TX.get();
        if (jdbcTransaction == null || !jdbcTransaction.isActive()) {
            return Connections.getConnection();
        }

        return jdbcTransaction.getConnection();
    }

    /**
     * closeQueryConnection,this connection not in JdbcTransaction,
     * should be closed in code.
     * 
     * @param connection {@link Connection}
     * @throws RepositoryException  RepositoryException
    
     */
    private void closeQueryConnection(final Connection connection)
            throws RepositoryException {

        final JdbcTransaction jdbcTransaction = TX.get();
        if (jdbcTransaction == null || !jdbcTransaction.isActive()) {
            try {
                connection.close();
            } catch (final SQLException e) {
                LOGGER.log(Level.SEVERE,
                        "closeQueryConnection :" + e.getMessage(), e);
                throw new RepositoryException(e);
            }
        }

    }

}
