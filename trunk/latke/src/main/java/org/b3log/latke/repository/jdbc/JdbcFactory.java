package org.b3log.latke.repository.jdbc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.b3log.latke.repository.jdbc.util.FieldDefinition;

/**
 * 
 * JdbcFactory.
 * 
 * @author <a href="mailto:wmainlove@gmail.com">Love Yao</a>
 * @version 1.0.0.0, Dec 20, 2011
 */
public final class JdbcFactory implements JdbcDatabase {

    /**
     * the holder of the databaseSolution.
     */
    private AbstractJdbcDatabaseSolution databaseSolution;

    /**
     * the singleton  of jdbcfactory.
     */
    private static JdbcFactory jdbcFactory;

    /**
     * all JdbcDatabaseSolution in here.
     */
    private static Map<RuntimeDatabase, AbstractJdbcDatabaseSolution> jdbcDatabaseSolutionMap = new HashMap<RuntimeDatabase, AbstractJdbcDatabaseSolution>();

    @Override
    public boolean createTable(final String tableName,
            final List<FieldDefinition> fieldDefinitions) {
        return databaseSolution.createTable(null, fieldDefinitions);
    }

    /**
     * singleton way to get jdbcFactory.
     * 
     * @return JdbcFactory jdbcFactory.
     */
    public static synchronized JdbcFactory createJdbcFactory() {

        if (jdbcFactory == null) {
            jdbcFactory = new JdbcFactory();
        }
        return jdbcFactory;
    }

    /**
     * Private constructor.
     */
    private JdbcFactory() {

        /**
         * Latkes.getRuntimeDatabase();
         */
        databaseSolution = jdbcDatabaseSolutionMap.get("todo");
    }

}
