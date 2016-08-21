package cleb.security.dao;

import static cleb.uploading.util.JDBCPoolUtil.getConnection;
import static cleb.uploading.util.JDBCPoolUtil.closeConnection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
 * This utility class provides methods for working with users in database.
 */
public class UserDAO {

    // Logger for this class
    private static final Logger logger = LogManager
        .getLogger(UserDAO.class.getName());

    /**
     * This method returns user with specified email.
     *
     * @param email String representing user email
     *
     * @return User object. Caution should be taken as object potentially can be
     *         null if no user with given email exists.
     *
     * @see cleb.security.tables.User
     * @see cleb.uploading.util.JDBCPoolUtil#closeConnection(Connection)
     * @see cleb.uploading.util.JDBCPoolUtil#getConnection()
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
            logger.error("Can not get user from database", e);
        } finally {
            closeConnection(connection);
        }

        return user;
    }

    /**
     * Registers new user in database.
     *
     * @param name String with user name
     * @param email String with user email
     * @param plainTextPassword String with user password, not encrypted
     *
     * @return True if user was successfully added to database and false
     *         otherwise.
     *
     * @see #generatePassword(User, String)
     * @see cleb.security.dao.UserDAO#getUserByEmail(String)
     * @see cleb.security.tables.User
     * @see cleb.uploading.util.JDBCPoolUtil#closeConnection(Connection)
     * @see cleb.uploading.util.JDBCPoolUtil#getConnection()
     */
    @SuppressWarnings("rawtypes")
    public static boolean register(String name, String email,
        String plainTextPassword) {
        boolean registered = false;

        // Check if no user with same email already exists in db
        User checked = getUserByEmail(email);
        if (checked != null) {
            logger.warn("User with email \"{}\" already registered", email);

            return registered;
        }

        // OK, create new user
        User user = new User();
        user.setUserName(name);
        user.setEmail(email);

        // Encrypt user password
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

            registered = false;

            logger.error(
                "Can not register new user with name \"{}\", email \"{}\"",
                name, email, e);
        } finally {
            closeConnection(connection);
        }

        logger.info("Registered new user with name \"{}\", email \"{}\"", name,
            email);

        return registered;
    }

    /**
     * Hashes user password and adds salt for randomizing it.
     *
     * @param user User object which password to hash
     * @param plainTextPassword String with not encrypted user password
     *
     * @see cleb.security.tables.User
     * @see cleb.security.tables.User#setPassword(String)
     * @see cleb.security.tables.User#setSalt(String)
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
