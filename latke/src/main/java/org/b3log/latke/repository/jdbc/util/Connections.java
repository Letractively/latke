package org.b3log.latke.repository.jdbc.util;

import java.sql.Connection;
import java.sql.DriverManager;

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
     * Private constructor.
     */
    private Connections() {

    }
}
