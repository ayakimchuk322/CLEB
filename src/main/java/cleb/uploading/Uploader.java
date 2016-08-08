package cleb.uploading;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;

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
// TODO remove e.printstacktraces
public class Uploader extends HttpServlet {
    // UID To satisfy compiler
    private static final long serialVersionUID = 1L;

    private boolean isMultipart;
    private String tempFolderPath;
    // Max book size to be uploaded (10MB)
    private int maxFileSize = 10 * 1024 * 1024;
    private File file;

    private String fileName;
    private String fileType;

    @Override
    public void init() {
	// Directory for temporary storing uploaded books - till it's checked by
	// DuplicateChecker servlet
	tempFolderPath = getServletContext()
		.getInitParameter("file-temp-upload");
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
	    throws ServletException, IOException {
	// Check that we have a file upload request
	isMultipart = ServletFileUpload.isMultipartContent(request);
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
	String bookParameter = "book=" + fileName;
	RequestDispatcher dispatcher = getServletContext()
		.getRequestDispatcher("/DuplicateChecker?" + bookParameter);
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

	DiskFileItemFactory factory = new DiskFileItemFactory();

	// Create a new file upload handler
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
		    } catch (IllegalArgumentException
			    | NullPointerException e) {
			uploaded = false;

			return uploaded;
		    }

		    // File type supported, OK to write the file
		    // Add random prefix to file name based on current time
		    String prefix = String.valueOf(
			    Instant.now().get(ChronoField.MILLI_OF_SECOND))
			    + "-";

		    if (fileName.lastIndexOf("\\") >= 0) {
			file = new File(tempFolderPath + prefix + fileName
				.substring(fileName.lastIndexOf("\\")));
		    } else {
			file = new File(tempFolderPath + prefix + fileName
				.substring(fileName.lastIndexOf("\\") + 1));
		    }

		    fi.write(file);
		    fileName = prefix + fileName;

		    uploaded = true;
		}
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    uploaded = false;
	}

	return uploaded;
    }

}