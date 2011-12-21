package org.b3log.latke.repository.jdbc;

import java.util.List;

import org.b3log.latke.repository.jdbc.util.FieldDefinition;

/**
 * interface JdbcDatabase.
 * 
 * @author <a href="mailto:wmainlove@gmail.com">Love Yao</a>
 * @version 1.0.0.0, Dec 20, 2011
 */
public interface JdbcDatabase {

    /**
     * createTable.
     * @param tableName tableName
     * @param fieldDefinitions fieldDefinitions
     * 
     * @return ifseccuss
     */
    boolean createTable(String tableName, List<FieldDefinition> fieldDefinitions);

}
