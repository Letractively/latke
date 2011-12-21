package org.b3log.latke.repository.jdbc;

import java.util.List;

import org.b3log.latke.repository.jdbc.util.FieldDefinition;

/**
 * 
 * JdbcDatabaseSolution.
 * 
 * @author <a href="mailto:wmainlove@gmail.com">Love Yao</a>
 * @version 1.0.0.0, Dec 20, 2011
 */
public abstract class AbstractJdbcDatabaseSolution implements JdbcDatabase {

    @Override
    public boolean createTable(final String tableName,
            final List<FieldDefinition> fieldDefinitions) {

        return false;
    }

}
