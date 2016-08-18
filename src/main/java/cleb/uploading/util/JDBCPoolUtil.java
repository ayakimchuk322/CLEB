package cleb.uploading.util;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;

import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.http.HttpServlet;

// TODO remove e printstacktrace
// TODO add javadoc
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
                + "org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer");
        dataSource = new DataSource(poolProps);
        // datasource.setPoolProperties(p);
    }

    public static Connection getConnection() {
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return connection;
    }

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
