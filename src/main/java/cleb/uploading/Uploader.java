package cleb.uploading;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.temporal.ChronoField;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cleb.book.BookType;

/**
 * This servlet handles uploading user books onto server.
 */
public class Uploader extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = LogManager
        .getLogger(Uploader.class.getName());

    private int maxFileSize;

    private String tempFolderPath;

    private String fileName;
    private String fileType;

    private String errorDesc;

    @Override
    public void init() {
        // Load properties
        Properties properties = new Properties();

        try (InputStream propIn = getServletContext()
            .getResourceAsStream("/WEB-INF/classes/props.properties")) {
            properties.load(propIn);
        } catch (IOException e) {
            logger.error("Can not load properties", e);
        }

        // Maximum file size that can be uploaded
        maxFileSize = Integer.valueOf(properties.getProperty("maxfilesize"));

        // Directory for temporary storing uploaded books - till it's checked by
        // DuplicateChecker servlet
        tempFolderPath = properties.getProperty("file-temp-upload");

        // Error description when book can not be uploaded
        errorDesc = properties.getProperty("uploader-error");

        logger.info("Uploader initialized");
    }

    @Override
    public void destroy() {
        // Cleans temporary directory in case there are left some uploaded but
        // unprocessed books.
        try {
            FileUtils.cleanDirectory(new File(tempFolderPath));
        } catch (IOException e) {
            logger.error("Can not clean temporary directory", e);
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

        // Check that we have a file upload request
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);

        if (!isMultipart || !getFile(request)) {
            // Inform user about error
            request.setAttribute("errordesc", errorDesc);
            request.setAttribute("previouspage", "/upload");

            RequestDispatcher dispatcher = getServletContext()
                .getRequestDispatcher("/error");

            dispatcher.forward(request, response);
        } else {
            // Forward request to next servlet - DuplicateChecker, including
            // reference for uploaded book
            request.setAttribute("file", fileName);
            request.setAttribute("type", fileType);

            RequestDispatcher dispatcher = getServletContext()
                .getRequestDispatcher("/DuplicateChecker");

            dispatcher.forward(request, response);
        }
    }

    /**
     * Gets the file, checks if its type is supported by the library and writes
     * supported file into temporary directory.
     *
     * @param request {@code HttpServletRequest} passed down from {@code doPost}
     *        method to extract the file.
     *
     * @return {@code true}, if file type is supported and the file was
     *         successfully written, otherwise - {@code false}.
     *
     * @see cleb.book.BookType
     */
    private boolean getFile(HttpServletRequest request) {

        File file = null;

        // Create a new file upload handler
        DiskFileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);

        // Maximum file size to be uploaded.
        upload.setSizeMax(maxFileSize);

        try {
            // Parse the request to get file items.
            List<FileItem> fileItems = upload.parseRequest(request);

            // Process the uploaded file items
            Iterator<FileItem> iterator = fileItems.iterator();

            while (iterator.hasNext()) {
                FileItem fi = iterator.next();
                if (!fi.isFormField()) {
                    // Get the uploaded file name and extension
                    fileName = fi.getName();
                    fileType = FilenameUtils.getExtension(fileName);

                    // Check if the file type supported by library
                    // Unsupported file types or files without extension will
                    // throw exception
                    try {
                        BookType.valueOf(fileType.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        logger.error("File type \"{}\" is not supported",
                            fileType, e);

                        return false;
                    } catch (NullPointerException e) {
                        logger.error("No file type provided", e);

                        return false;
                    }

                    // File type is supported, OK to write the file
                    // Add random prefix to file name based on current time
                    String prefix = String.valueOf(
                        Instant.now().get(ChronoField.MILLI_OF_SECOND)) + "-";

                    file = new File(tempFolderPath + prefix + fileName);

                    fi.write(file);

                    // Update fileName with added prefix
                    fileName = prefix + fileName;
                }
            }
        } catch (FileUploadException e) {
            logger.error("Can not upload file", e);

            return false;
        } catch (Exception e) {
            logger.error("Can not write file \"{}\"", file, e);

            return false;
        }

        logger.info("File \"{}\" uploaded", file);

        return true;
    }

}