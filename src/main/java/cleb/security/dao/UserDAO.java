package cleb.security.dao;

import org.apache.shiro.crypto.RandomNumberGenerator;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import cleb.security.tables.User;
import cleb.security.tables.UserRole;

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

    public static void registrate(String name, String email,
        String plainTextPassword) {
        // Create new user
        User user = new User();
        user.setUserName(name);
        user.setEmail(email);

        generatePassword(user, plainTextPassword);

        // Create 'reader' role for new user
        UserRole role = new UserRole();
        role.setEmail(email);
        role.setRoleName("reader");

        // Proceed with db
        SessionFactory factory = new Configuration().configure()
            .buildSessionFactory();

        Transaction transaction = null;

        try (Session session = factory.openSession()) {
            transaction = session.beginTransaction();
            // Save user in db
            session.save(user);

            // Save role in db
            session.save(role);

            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }

            e.printStackTrace();
        }
    }

    private static void generatePassword(User user, String plainTextPassword) {
        RandomNumberGenerator rng = new SecureRandomNumberGenerator();
        Object salt = rng.nextBytes();

        // Hash the plain-text password with the random salt and multiple
        // iterations and then Base64-encode the value
        String hashedPasswordBase64 = new Sha256Hash(plainTextPassword, salt,
            1024).toBase64();

        // Update user object password and salt
        user.setPassword(hashedPasswordBase64);
        user.setSalt(salt.toString());
    }

}
