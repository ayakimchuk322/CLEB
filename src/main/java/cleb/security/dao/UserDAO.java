package cleb.security.dao;

import static cleb.uploading.util.JDBCPoolUtil.getConnection;
import static cleb.uploading.util.JDBCPoolUtil.closeConnection;

import org.apache.shiro.crypto.RandomNumberGenerator;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.hibernate.Session;
import org.hibernate.SessionBuilder;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import java.sql.Connection;

import cleb.security.tables.User;
import cleb.security.tables.UserRole;

/**
 * This utility class used to provide api for working with users in database.
 */
public class UserDAO {

    /**
     * This method returns user with specified email.
     *
     * @param email
     *        String representing user email
     * @return User object. Caution should be taken as object potentially can be
     *         null if no user with given email exists.
     */
    @SuppressWarnings({ "deprecation", "rawtypes" })
    public static User getUserByEmail(String email) {
        User user = null;

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
            user = (User) session.createQuery("from User where email=?")
                .setParameter(0, email).uniqueResult();
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeConnection(connection);
        }

        return user;
    }

    /**
     * Registers new user in database.
     *
     * @param name
     *        String with user name
     * @param email
     *        String with user email
     * @param plainTextPassword
     *        String with user password, not encrypted
     * @return True if user was successfully added to database and false
     *         otherwise.
     */
    @SuppressWarnings("rawtypes")
    public static boolean registrate(String name, String email,
        String plainTextPassword) {
        boolean registered = false;

        // Check if no user with same email already exists in db
        User checked = getUserByEmail(email);
        if (checked != null) {
            // TODO add some meaningful message
            System.err.println("NOPE");

            return registered;
        }

        // OK, create new user
        User user = new User();
        user.setUserName(name);
        user.setEmail(email);

        generatePassword(user, plainTextPassword);

        // Create 'reader' role for new user
        UserRole role = new UserRole();
        role.setEmail(email);
        role.setRoleName("reader");

        // Proceed with db
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
            // Save user in db
            session.save(user);

            // Save role in db
            session.save(role);

            transaction.commit();

            registered = true;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }

            e.printStackTrace();

            registered = false;
        } finally {
            closeConnection(connection);
        }

        return registered;
    }

    /**
     * Hashes user password and adds salt for randomizing it.
     *
     * @param user
     *        User object which password to hash
     * @param plainTextPassword
     *        String with not encrypted user password
     */
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
