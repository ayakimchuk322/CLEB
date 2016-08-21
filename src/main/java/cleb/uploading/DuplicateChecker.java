package cleb.uploading;

import static cleb.uploading.util.JDBCPoolUtil.getConnection;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cleb.uploading.validating.factory.ValidatorFactory;

/**
 * This servlet checks if newly uploading book is not already in library.
 */
public class DuplicateChecker extends HttpServlet {

    private static final long serialVersionUID = 1L;

    // Logger for this class
    private static final Logger logger = LogManager
        .getLogger(DuplicateChecker.class.getName());

    private String tempFolderPath;

    private String fileName;

    // Prepared query (statement)
    //@formatter:off
    private final String query = "SELECT COUNT(*) "
	    + "FROM books "
	    + "WHERE md5sum = ? "
	    + "AND file_size = ?";
    //@formatter:on

    /**
     * Initializes temporary directory.
     */
    @Override
    public void init() {
        // Directory for temporary storing uploaded books - till it's checked by
        // this servlet
        tempFolderPath = getServletContext()
            .getInitParameter("file-temp-upload");

        logger.info("DuplicateChecker initialized");
    }

    @Override
    protected void doPost(HttpServletRequest request,
        HttpServletResponse response) throws ServletException, IOException {

        fileName = (String) request.getAttribute("file");

        String tempBookPath = tempFolderPath
            + (String) request.getAttribute("file");
        File tempBookFile = new File(tempBookPath);

        String md5sum = getMd5sum(tempBookFile);

        if (md5sum != null) {
            long fileSize = tempBookFile.length();

            if (checkBookPresence(md5sum, fileSize)) {
                // There is already this book in library, delete this book in
                // temporary directory and inform user
                // TODO add forwarding to page and inform user
                FileUtils.deleteQuietly(tempBookFile);
            } else {
                // This book is new, proceed with validation
                // Necessary attributes for further processing
                request.setAttribute("md5", md5sum);
                request.setAttribute("size", fileSize);

                // Get the string reference for concrete validator
                String type = (String) request.getAttribute("type");
                ValidatorFactory factory = new ValidatorFactory(type);
                String validator = factory.getValidator();

                RequestDispatcher dispatcher = request
                    .getRequestDispatcher(validator);
                dispatcher.forward(request, response);
            }
        } else {
            logger.error("md5 sum for file \"{}\" is null", fileName);
        }
    }

    /**
     * This method calculates temporarily uploaded book MD5 sum.
     *
     * @param file
     *        Previously uploaded book in temp folder
     * @return String representing this book MD5 sum value
     */
    private String getMd5sum(File file) {
        String md5sum = null;

        try (FileInputStream fileIn = new FileInputStream(file);
             BufferedInputStream bufferIn = new BufferedInputStream(fileIn);) {

            md5sum = DigestUtils.md5Hex(IOUtils.toByteArray(bufferIn));
        } catch (IOException e) {
            logger.error("Can not calculate md5 sum for file \"{}\"", fileName,
                e);
        }

        return md5sum;
    }

    /**
     * This method connects to database and checks if there is already a book
     * with given md5 sum and file size.
     *
     * @param md5sum
     *        String representing new book md5 sum to check among already
     *        uploaded books
     * @param fileSize
     *        long representing new book file size to check among already
     *        uploaded books
     * @return true - if database already contains book with given md5 sum and
     *         file size, otherwise - false
     */
    private boolean checkBookPresence(String md5sum, long fileSize) {
        boolean present = false;

        try (Connection connection = getConnection();
             PreparedStatement pstatement = connection
                 .prepareStatement(query);) {

            pstatement.setString(1, md5sum);
            pstatement.setLong(2, fileSize);

            ResultSet results = pstatement.executeQuery();

            while (results.next()) {
                if (results.getInt(1) > 0) {
                    present = true;

                    logger.info("Book \"{}\" is already in library", fileName);
                } else {
                    present = false;

                    logger.info("Book \"{}\" is not a duplicate", fileName);
                }
            }
        } catch (SQLException e) {
            logger.error("Can not check if book \"{}\" is presented in library",
                fileName, e);
        }

        return present;
    }

}
