package cleb.book.dao;

import static cleb.book.util.GenresLoader.getGenreNormal;
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

import cleb.book.Book;

/**
 * This utility class provides methods for working with books in database.
 */
public class BookDAO {

    // Logger for this class
    private static final Logger logger = LogManager
        .getLogger(BookDAO.class.getName());

    /**
     * Saves information about uploaded book in a database.
     *
     * @param fileName
     * @param md5
     * @param fileSize
     * @param fileType
     * @param genre
     * @param authorFirstName
     * @param authorLastName
     * @param title
     * @param seqName
     * @param seqNumber
     * @param published
     * @param uploadedBy
     *
     * @return true if transaction was successful and false - otherwise.
     *
     * @see cleb.book.util.GenresLoader#getGenreNormal(String)
     * @see cleb.uploading.util.JDBCPoolUtil#closeConnection(Connection)
     * @see cleb.uploading.util.JDBCPoolUtil#getConnection()
     */
    @SuppressWarnings("rawtypes")
    public static boolean storeInDB(String fileName, String md5, Long fileSize,
        String fileType, String genre, String authorFirstName,
        String authorLastName, String title, String seqName, String seqNumber,
        String published, String uploadedBy) {

        boolean stored = false;

        // Get normal genre name
        String genreNormal = getGenreNormal(genre);

        // Create new Book object
        // Name of the cover will be added later, if this book has one
        Book book = new Book(fileName, md5, fileSize, fileType, genreNormal,
            authorFirstName, authorLastName, title, seqName, seqNumber,
            published, uploadedBy, "");

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
            session.save(book);
            transaction.commit();

            stored = true;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }

            stored = false;

            logger.error(
                "Can not store information about book \"{}\" in database",
                fileName, e);
        } finally {
            closeConnection(connection);
        }

        return stored;
    }

    /**
     * Updates record in database for {@code fileName} book with
     * {@code coverName} file name of it's cover. Callers of this method should
     * call it only when the cover is present - default value for cover name in
     * database is empty {@code String}, so no point to set it again.
     *
     * @param fileName {@code String} file name of the book.
     * @param coverName {@code String} file name of the cover.
     */
    @SuppressWarnings({ "deprecation", "rawtypes" })
    public static void addCoverName(String fileName, String coverName) {

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
            Book book = (Book) session.createQuery("FROM Book WHERE fileName=?")
                .setParameter(0, fileName).uniqueResult();
            book.setCover(coverName);
            session.update(book);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }

            logger.error("Can not update book \"{}\" with cover in database",
                fileName, e);
        } finally {
            closeConnection(connection);
        }
    }

    /**
     * Returns {@code List} with all {@code Book} objects stored in database.
     *
     * @return {@code List} with books.
     */
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
            books = session.createQuery("FROM Book").list();
            transaction.commit();
        } catch (Exception e) {
            logger.error(
                "Can not retrieve information about all books from database",
                e);
        } finally {
            closeConnection(connection);
        }

        return books;
    }

    @SuppressWarnings({ "deprecation", "rawtypes", "unchecked" })
    public static Book[] getLatestBooks() {
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
            books = session.createQuery("FROM Book ORDER BY id DESC")
                .setMaxResults(3).list();
            transaction.commit();
        } catch (Exception e) {
            logger.error(
                "Can not retrieve information about latest books from database",
                e);
        } finally {
            closeConnection(connection);
        }

        Book[] booksArray = null;

        // If less than 3 books were uploaded, return null
        if (books.size() == 3) {
            booksArray = new Book[3];
            books.toArray(booksArray);
        }

        return booksArray;
    }

}
