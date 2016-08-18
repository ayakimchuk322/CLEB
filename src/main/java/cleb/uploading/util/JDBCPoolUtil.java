package cleb.uploading.util;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;

import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.http.HttpServlet;

// TODO remove e printstacktrace
/**
 * This utility class instantiates connection pool and provides methods for
 * working with connections.
 */
public class JDBCPoolUtil extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private String dbURL;
    private String dbuser;
    private String dbpass;

    private static DataSource dataSource;

    @Override
    public void init() {
        // Initialize database URL for driver
        dbURL = getServletContext().getInitParameter("database");

        // Initialize database user
        dbuser = getServletContext().getInitParameter("dbuser");

        // Initialize database user password
        dbpass = getServletContext().getInitParameter("dbpass");

        PoolProperties poolProps = new PoolProperties();
        poolProps.setUrl(dbURL);
        poolProps.setDriverClassName("org.postgresql.Driver");
        poolProps.setUsername(dbuser);
        poolProps.setPassword(dbpass);
        poolProps.setJmxEnabled(true);
        poolProps.setTestWhileIdle(false);
        poolProps.setTestOnBorrow(true);
        poolProps.setValidationQuery("SELECT 1");
        poolProps.setTestOnReturn(false);
        poolProps.setValidationInterval(30000);
        poolProps.setTimeBetweenEvictionRunsMillis(30000);
        poolProps.setMaxActive(20);
        poolProps.setInitialSize(10);
        poolProps.setMaxWait(10000);
        poolProps.setRemoveAbandonedTimeout(60);
        poolProps.setMinEvictableIdleTimeMillis(30000);
        poolProps.setMinIdle(10);
        poolProps.setLogAbandoned(true);
        poolProps.setRemoveAbandoned(true);
        poolProps.setJdbcInterceptors(
            "org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;"
                + "org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer;"
                + "org.apache.tomcat.jdbc.pool.interceptor.ResetAbandonedTimer");

        dataSource = new DataSource(poolProps);
    }

    /**
     * Attempts to return idle connection from connection pool.
     *
     * @return Free connection from pool
     */
    public static Connection getConnection() {
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return connection;
    }

    /**
     * Attempts to close connection. Classes that use connections from this
     * connection pool should call getConnection method inside
     * try-with-resources for efficient closing instead of manually calling this
     * method.
     *
     * @param connection
     *        Connection object to close
     */
    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

}
