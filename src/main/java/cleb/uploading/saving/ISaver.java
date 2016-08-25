package cleb.uploading.saving;

import static cleb.uploading.util.JDBCPoolUtil.closeConnection;
import static cleb.uploading.util.JDBCPoolUtil.getConnection;

import org.apache.commons.io.FileUtils;
import org.hibernate.Session;
import org.hibernate.SessionBuilder;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;

import javax.servlet.http.HttpServletRequest;

import cleb.book.Book;

/**
 * This interface contains methods to get information about uploaded book and
 * save it's cover. Any concrete saver class should implement this interface and
 * provide specific implementations for {@code getBasicInfo} and
 * {@code saveCover} methods based on book type.
 *
 * This interface also contains default methods for saving book in database and
 * in storing directory.
 */
public interface ISaver {

    /**
     * Implementation of this method should retrieve all information about
     * uploaded book according to its type. book parameter should be cast to its
     * actual type depending on book type and exctraction logic.
     *
     * @param request HttpServletRequest from doPost method to retrieve
     *        information about file.
     * @param book Object representing actual book.
     *
     * @return boolean value returned by storeInDB method.
     */
    public boolean getBasicInfo(HttpServletRequest request, Object book);

    /**
     * Implementation of this method should get cover from uploaded book and
     * save it in special directory for covers. Cover should be saved under
     * specific name - it constists of the book file name and image extension
     * (jpeg/png).
     *
     * @param book Object representing actual book.
     * @param name Book file name.
     *
     * @return {@code String} path to cover.
     */
    public String saveCover(Object book, String name);

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
     * @see cleb.uploading.util.JDBCPoolUtil#closeConnection(Connection)
     * @see cleb.uploading.util.JDBCPoolUtil#getConnection()
     */
    @SuppressWarnings("rawtypes")
    public default boolean storeInDB(String fileName, String md5, Long fileSize,
        String fileType, String genre, String authorFirstName,
        String authorLastName, String title, String seqName, String seqNumber,
        String published, String uploadedBy) {

        boolean stored = false;

        // Create new Book object
        // Pathes for book and cover will be added later
        Book book = new Book(fileName, md5, fileSize, fileType, genre,
            authorFirstName, authorLastName, title, seqName, seqNumber,
            published, uploadedBy, "", "");

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
        } finally {
            closeConnection(connection);
        }

        return stored;
    }

    /**
     * Moves book from temporary to storing directory.
     *
     * @param srcFile Path to file in temporary directory.
     * @param destFile Path to file in storing directory.
     */
    public default void storeInDir(String srcFile, String destFile) {
        try {
            FileUtils.moveFile(new File(srcFile), new File(destFile));
        } catch (IOException e) {
        }
    }

}
