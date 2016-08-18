package cleb.security.dao;

import static cleb.uploading.util.JDBCPoolUtil.closeConnection;
import static cleb.uploading.util.JDBCPoolUtil.getConnection;

import org.hibernate.Session;
import org.hibernate.SessionBuilder;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import java.sql.Connection;
import java.util.List;

import cleb.security.tables.UserRole;

/**
 * This utility class used to provide api for working with user roles in
 * database.
 */
public class UserRoleDAO {

    /**
     * This method returns List of all roles assigned to user with specified
     * email.
     *
     * @param email
     *        String representing user email
     * @return List with type UserRole representing given user roles. Caution
     *         should be taken as this list potentially can be null if no user
     *         with given email exists.
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
            e.printStackTrace();
        } finally {
            closeConnection(connection);
        }

        return roles;
    }

    /**
     * Adds role to database.
     *
     * @param role
     *        UserRole object with setted email and role.
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

            e.printStackTrace();
        } finally {
            closeConnection(connection);
        }
    }

}
