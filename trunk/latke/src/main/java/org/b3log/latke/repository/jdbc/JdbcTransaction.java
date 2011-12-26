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

import java.sql.Connection;
import java.sql.SQLException;

import org.b3log.latke.repository.Transaction;
import org.b3log.latke.repository.jdbc.util.Connections;

/**
 *
 * JdbcTransaction.
 * 
 * @author <a href="mailto:wmainlove@gmail.com">Love Yao</a>
 * @version 1.0.0.0, Dec 20, 2011
 */
public  class JdbcTransaction implements Transaction {

    /**
     * Public constructor.
     * @throws SQLException SQLException 
     */
    public JdbcTransaction() throws SQLException {
        connection = Connections.getConnection();
        connection.setAutoCommit(false);

    }

    /**
     * connecton.
     */
    private Connection connection;

    /**
     * isActive.
     */
    private boolean isActive;

    @Override
    public String getId() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void commit() {
        // TODO Auto-generated method stub

    }

    @Override
    public void rollback() {
        // TODO Auto-generated method stub

    }

    /**
     * setActive.
     * @param isActive isActive
     */
    public void setActive(final boolean isActive) {
        this.isActive = isActive;
    }

    @Override
    public boolean isActive() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void clearQueryCache(final boolean flag) {
        // TODO Auto-generated method stub

    }

    /**
     * 
     * getConnection.
     * @return {@link Connection}
     */
    public Connection getConnection() {
        return connection;
    }

}
