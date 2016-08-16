package cleb.security.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import cleb.security.tables.User;

// TODO add javadoc
public class UserDAO {

    @SuppressWarnings("deprecation")
    public static User getUserByEmail(String email) {
        User user = null;

        SessionFactory factory = new Configuration().configure()
            .buildSessionFactory();

        Transaction transaction = null;

        try (Session session = factory.openSession()) {
            transaction = session.beginTransaction();
            user = (User) session.createQuery("from User where email=?")
                .setParameter(0, email).uniqueResult();
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return user;
    }

}
