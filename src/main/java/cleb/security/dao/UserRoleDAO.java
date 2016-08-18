package cleb.security.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import java.util.List;

import cleb.security.tables.UserRole;

/**
 * This utility class used to provide api for working with user roles in db.
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
    @SuppressWarnings({ "deprecation", "unchecked" })
    public static List<UserRole> getUserRolesByEmail(String email) {
        List<UserRole> roles = null;

        SessionFactory factory = new Configuration().configure()
            .buildSessionFactory();

        Transaction transaction = null;

        try (Session session = factory.openSession()) {
            transaction = session.beginTransaction();
            roles = session.createQuery("from Userrole where email=?")
                .setParameter(0, email).list();
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return roles;
    }

    /**
     * Adds role to database.
     *
     * @param role
     *        UserRole object with setted email and role.
     */
    public static void addRole(UserRole role) {
        SessionFactory factory = new Configuration().configure()
            .buildSessionFactory();

        Transaction transaction = null;

        try (Session session = factory.openSession()) {
            transaction = session.beginTransaction();
            session.save(role);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }

            e.printStackTrace();
        }
    }

}
