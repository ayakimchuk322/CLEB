package cleb.saving;

import org.apache.commons.io.FileUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import java.io.File;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import cleb.book.Book;

/**
 * This interface contains necessary method to get information about uploaded
 * book. Any concrete saver class should implement this interface and override
 * its getBasicInfo method based on book type.
 *
 * This interface also contains default methods for storing book in database and
 * in storing directory.
 */
// TODO remove e.printstacktraces
public interface ISaver {

    /**
     * Implementation of this method should retrieve all information about
     * uploaded book according to its type. book parameter should be cast to its
     * actual type depending on book type and exctraction logic.
     *
     * @param request
     *        HttpServletRequest from doPost method to retrieve information
     *        about file
     * @param book
     *        Object representing actual book
     *
     * @return value returned by storeInDB method
     */
    public boolean getBasicInfo(HttpServletRequest request, Object book);

    /**
     * Stores information about uploaded book in a database.
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
     */
    public default boolean storeInDB(String fileName, String md5, Long fileSize,
            String fileType, String genre, String authorFirstName,
            String authorLastName, String title, String seqName,
            String seqNumber, String published, String uploadedBy) {

        Book book = new Book(fileName, md5, fileSize, fileType, genre,
                authorFirstName, authorLastName, title, seqName, seqNumber,
                published, uploadedBy);

        SessionFactory factory = new Configuration().configure()
                .buildSessionFactory();

        Transaction transaction = null;

        try (Session session = factory.openSession()) {
            transaction = session.beginTransaction();
            session.save(book);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }

            e.printStackTrace();

            return false;
        }

        return true;
    }

    /**
     * Moves book from temporary to storing directory.
     *
     * @param srcFile
     *        Path to file in temporary directory
     * @param destFile
     *        Path to file in storing directory
     */
    public default void storeInDir(String srcFile, String destFile) {
        try {
            FileUtils.moveFile(new File(srcFile), new File(destFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
