package cleb.uploading.validating;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

/**
 * This servlet validates new epub books.
 */
// TODO remove e.printstacktraces
public class EPUBValidator extends HttpServlet implements IValidator {

    private static final long serialVersionUID = 1L;

    // Logger for this class
    private static final Logger logger = LogManager
        .getLogger(EPUBValidator.class.getName());

    private String tempFolderPath;

    private ZipFile book;

    /**
     * Initializes temporary directory.
     */
    @Override
    public void init() throws ServletException {
        // Directory for temporary storing uploaded books - till it's checked by
        // this servlet
        tempFolderPath = getServletContext()
            .getInitParameter("file-temp-upload");

        logger.info("EPUBValidator initialized");
    }

    @Override
    protected void doPost(HttpServletRequest request,
        HttpServletResponse response) throws ServletException, IOException {
        String tempBookPath = tempFolderPath
            + (String) request.getAttribute("file");
        File tempBookFile = new File(tempBookPath);

        if (validateBook(tempBookFile)) {
            request.setAttribute("book", book);
            RequestDispatcher dispatcher = request
                .getRequestDispatcher("/EPUBSaver");
            dispatcher.forward(request, response);
        } else {
            // TODO inform user about malformed book
        }
    }

    /**
     * This method validates given epub book.
     *
     * @param file
     *        epub book to validate
     * @return true, if given file is valid epub book and false - otherwise
     */
    // Valid epub book should be valid zip archive with certain directory
    // structure inside
    @Override
    public boolean validateBook(File file) {
        boolean validated = false;

        try {
            book = new ZipFile(file);

            if (book.isValidZipFile()) {
                try {
                    // Any valid epub book should contain these two files
                    book.getFileHeader("META-INF/container.xml").getFileName();
                    book.getFileHeader("mimetype").getFileName();

                    logger.info("File \"{}\" successfully validated",
                        file.getName());

                    validated = true;
                } catch (NullPointerException e) {
                    logger.warn("File \"{}\" is not a valid epub book",
                        file.getName());

                    validated = false;
                }
            } else {
                logger.warn("File \"{}\" is not a valid epub book",
                    file.getName());

                validated = false;
            }
        } catch (ZipException e) {
            logger.error("Can not validate book \"{}\"", file.getName(), e);
        }

        return validated;
    }

}
