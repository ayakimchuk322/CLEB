package cleb.uploading.saving;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

/**
 * This interface contains methods to get information about uploaded book and
 * save it's cover. Any concrete saver class should implement this interface and
 * provide specific implementations for {@code getBasicInfo} and
 * {@code saveCover} methods based on book type.
 *
 * This interface also contains default method for saving book in storing
 * directory.
 */
public interface ISaver {

    /**
     * Implementation of this method should retrieve all information about
     * uploaded book according to its type. book parameter should be cast to its
     * actual type depending on book type and exctraction logic.
     *
     * @param request {@code HttpServletRequest} from {@code doPost} method to
     *        retrieve information about file.
     * @param book {@code Object} representing actual book.
     *
     * @return boolean value returned by storeInDB method.
     */
    public boolean getBasicInfo(HttpServletRequest request, Object book);

    /**
     * Implementation of this method should get annotation from uploaded book
     * and save it in special directory for annotations. Annotation should be
     * saved under specific name - it constists of the book file name and 'txt'
     * extension.
     *
     * @param annotationHolder {@code Object} that can be parsed for annotation.
     * @param fileName {@code String} uploaded book file name.
     */
    public void saveAnnotation(Object annotationHolder, String fileName);

    /**
     * Implementation of this method should get cover from uploaded book and
     * save it in special directory for covers. Cover should be saved under
     * specific name - it constists of the book file name and image extension
     * (jpeg/png). Implementation should return {@code String} with cover file
     * name or empty {@code String} if there is no cover for this book.
     *
     * @param book {@code Object} representing actual book.
     * @param fileName {@code String} book file name.
     *
     * @return {@code String} cover file name.
     */
    public String saveCover(Object book, String fileName);

    /**
     * Moves book from temporary to storing directory.
     *
     * @param srcFile {@code String} path to file in temporary directory.
     * @param destFile {@code String} path to file in storing directory.
     */
    public default void storeInDir(String srcFile, String destFile) {
        try {
            FileUtils.moveFile(new File(srcFile), new File(destFile));
        } catch (IOException e) {
        }
    }

}
