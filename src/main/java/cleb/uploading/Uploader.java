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
import java.time.Instant;
import java.time.temporal.ChronoField;
import java.util.Iterator;
import java.util.List;

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

    // Max book size to be uploaded (10MB)
    private static final int MAX_FILE_SIZE = 10 * 1024 * 1024;

    private String tempFolderPath;

    private String fileName;
    private String fileType;

    @Override
    public void init() {
        // Directory for temporary storing uploaded books - till it's checked by
        // DuplicateChecker servlet
        tempFolderPath = getServletContext()
            .getInitParameter("file-temp-upload");

        logger.info("Uploader initialized");
    }

    /**
     * Cleans temporary directory in case there are left some uploaded but
     * unprocessed books.
     */
    @Override
    public void destroy() {
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
        if (!isMultipart) {
            return;
            // TODO show error page to user
        }

        if (!getFile(request)) {
            return;
            // TODO show error page to user
        }

        // Forward request to next servlet - DuplicateChecker, including
        // reference for uploaded book
        request.setAttribute("file", fileName);
        request.setAttribute("type", fileType);

        RequestDispatcher dispatcher = getServletContext()
            .getRequestDispatcher("/DuplicateChecker");

        dispatcher.forward(request, response);
    }

    /**
     * This method gets the file, checks if its type is supported by the library
     * and writes supported file into temporary directory.
     *
     * @param request
     *        HttpServletRequest passed down from doPost method to extract the
     *        file
     * @return true, if file type is supported and the file was successfully
     *         written and false - otherwise.
     */
    private boolean getFile(HttpServletRequest request) {
        boolean uploaded = false;

        File file = null;

        // Create a new file upload handler
        DiskFileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);

        // Maximum file size to be uploaded.
        upload.setSizeMax(MAX_FILE_SIZE);

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
                        uploaded = false;

                        logger.error("File type \"{}\" is not supported",
                            fileType, e);

                        return uploaded;
                    } catch (NullPointerException e) {
                        uploaded = false;

                        logger.error("No file type provided", e);

                        return uploaded;
                    }

                    // File type is supported, OK to write the file
                    // Add random prefix to file name based on current time
                    String prefix = String.valueOf(
                        Instant.now().get(ChronoField.MILLI_OF_SECOND)) + "-";

                    file = new File(tempFolderPath + prefix + fileName);

                    fi.write(file);

                    // Update fileName with added prefix
                    fileName = prefix + fileName;

                    uploaded = true;
                }
            }
        } catch (FileUploadException e) {
            uploaded = false;

            logger.error("Can not upload file", e);
        } catch (Exception e) {
            uploaded = false;

            logger.error("Can not write file \"{}\"", file, e);
        }

        logger.info("File \"{}\" uploaded", file);

        return uploaded;
    }

}