package cleb.security.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import java.util.List;

import cleb.security.tables.UserRole;

// TODO add javadoc
public class UserRoleDAO {

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
