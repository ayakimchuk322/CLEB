package cleb.security.dao;

import static cleb.uploading.util.JDBCPoolUtil.closeConnection;
import static cleb.uploading.util.JDBCPoolUtil.getConnection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionBuilder;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import java.sql.Connection;
import java.util.List;

import cleb.security.tables.UserRole;

/**
 * This utility class provides methods for working with user roles in database.
 */
public class UserRoleDAO {

    // Logger for this class
    private static final Logger logger = LogManager
        .getLogger(UserRoleDAO.class.getName());

    /**
     * Returns {@link java.util.List List} of all roles assigned to user with
     * specified email.
     *
     * @param email {@code String} representing user email.
     *
     * @return {@code List} with type {@code UserRole} representing given user
     *         roles. Caution should be taken as this {@code List} potentially
     *         can be {@code null} if no user with given email exists.
     *
     * @see cleb.security.tables.UserRole
     * @see cleb.uploading.util.JDBCPoolUtil#closeConnection(Connection)
     * @see cleb.uploading.util.JDBCPoolUtil#getConnection()
     */
    @SuppressWarnings({ "deprecation", "unchecked", "rawtypes" })
    public static List<UserRole> getUserRolesByEmail(String email) {
        List<UserRole> roles = null;

        // Create session and transaction
        SessionFactory factory = new Configuration().configure()
            .buildSessionFactory();

        SessionBuilder builder = factory.withOptions();
        // Supply connection from connection pool
        Connection connection = getConnection();
        builder.connection(connection);

        Transaction transaction = null;

        try (Session session = builder.openSession()) {
            transaction = session.beginTransaction();
            roles = session.createQuery("from Userrole where email=?")
                .setParameter(0, email).list();
            transaction.commit();
        } catch (Exception e) {
            logger.error("Can not get user roles from database", e);
        } finally {
            closeConnection(connection);
        }

        return roles;
    }

    /**
     * Adds role to database.
     *
     * @param role {@code UserRole} object with setted email and role.
     *
     * @see cleb.security.tables.UserRole
     * @see cleb.uploading.util.JDBCPoolUtil#closeConnection(Connection)
     * @see cleb.uploading.util.JDBCPoolUtil#getConnection()
     */
    @SuppressWarnings("rawtypes")
    public static void addRole(UserRole role) {
        // Create session and transaction
        SessionFactory factory = new Configuration().configure()
            .buildSessionFactory();

        SessionBuilder builder = factory.withOptions();
        // Supply connection from connection pool
        Connection connection = getConnection();
        builder.connection(connection);

        Transaction transaction = null;

        try (Session session = builder.openSession()) {
            transaction = session.beginTransaction();
            session.save(role);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }

            logger.error("Can not add new role to database", e);
        } finally {
            closeConnection(connection);
        }
    }

}
