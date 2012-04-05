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
package org.b3log.latke.repository.jdbc.util;

import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.b3log.latke.Latkes;
import org.b3log.latke.util.Strings;

/**
 * JDBC connection utilities.
 * 
 * <p>
 * Uses <a href="http://jolbox.com/">BoneCP</a> as the underlying connection pool.
 * </p>
 * 
 * @author <a href="mailto:wmainlove@gmail.com">Love Yao</a>
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.3, Apr 4, 2012
 */
public final class Connections {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(Connections.class.getName());
    /**
     * Connection pool.
     */
    private static final BoneCP CONN_POOL;

    static {
        try {
            Class.forName(Latkes.getLocalProperty("jdbc.driver"));

            final BoneCPConfig config = new BoneCPConfig();
            config.setDefaultAutoCommit(false);
            config.setDefaultTransactionIsolation(Latkes.getLocalProperty("jdbc.transactionIsolation"));
            config.setJdbcUrl(Latkes.getLocalProperty("jdbc.URL"));
            config.setUsername(Latkes.getLocalProperty("jdbc.username"));
            config.setPassword(Latkes.getLocalProperty("jdbc.password"));
            config.setMinConnectionsPerPartition(Integer.valueOf(Latkes.getLocalProperty("jdbc.minConnCnt")));
            config.setMaxConnectionsPerPartition(Integer.valueOf(Latkes.getLocalProperty("jdbc.maxConnCnt")));
            config.setPartitionCount(1);

            CONN_POOL = new BoneCP(config);

            LOGGER.info("Initialized connection pool");
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets a connection.
     * 
     * @return a connection
     * @throws SQLException SQL exception 
     */
    public static Connection getConnection() throws SQLException {
        if (LOGGER.isLoggable(Level.FINEST)) {
            // TODO: D, Call stack utilities
            final Throwable throwable = new Throwable();
            final StackTraceElement[] stackElements = throwable.getStackTrace();

            if (null != stackElements) {
                final StringBuilder stackBuilder = new StringBuilder("CallStack (org.b3lg.*)[").append(Strings.LINE_SEPARATOR);
                
                for (int i = 0; i < stackElements.length; i++) {
                    if (!stackElements[i].getClassName().startsWith("org.b3log")) {
                        continue;
                    }

                    stackBuilder.append("    [className=").append(stackElements[i].getClassName()).append(", fileName=").
                            append(stackElements[i].getFileName()).append(", lineNumber=").append(stackElements[i].getLineNumber()).
                            append(", methodName=").append(stackElements[i].getMethodName()).append(']').append(Strings.LINE_SEPARATOR);
                }
                stackBuilder.append("], fullDepth=[").append(stackElements.length).append("]");
                
                LOGGER.log(Level.FINEST, stackBuilder.toString());
            }
        }

        LOGGER.log(Level.FINEST, "Connection pool[createdConns={0}, freeConns={1}, leasedConns={2}]",
                   new Object[]{CONN_POOL.getTotalCreatedConnections(), CONN_POOL.getTotalFree(), CONN_POOL.getTotalLeased()});
        return CONN_POOL.getConnection();
    }

    /**
     * Shutdowns the connection pool.
     */
    public static void shutdownConnectionPool() {
        CONN_POOL.shutdown();
        LOGGER.info("Shutdowns connection pool sucessfully");
    }

    /**
     * Private constructor.
     */
    private Connections() {
    }
}
