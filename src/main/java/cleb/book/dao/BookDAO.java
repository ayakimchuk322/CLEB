package cleb.book.dao;

import static cleb.uploading.util.JDBCPoolUtil.closeConnection;
import static cleb.uploading.util.JDBCPoolUtil.getConnection;

import org.hibernate.Session;
import org.hibernate.SessionBuilder;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import java.sql.Connection;
import java.util.List;

import cleb.book.Book;

/**
 * This utility class provides methods for working with books in database.
 */
// TODO move saving book from ISaver here
// TODO add javadoc
public class BookDAO {

    @SuppressWarnings({ "deprecation", "rawtypes", "unchecked" })
    public static List<Book> getAllBooks() {
        List books = null;

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
            books = session.createQuery("from Book").list();
            transaction.commit();
        } catch (Exception e) {
            // TODO replace by logger
            e.printStackTrace();
        } finally {
            closeConnection(connection);
        }

        return books;
    }
}
