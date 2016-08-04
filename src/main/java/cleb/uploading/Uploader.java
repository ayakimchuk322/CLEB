package cleb;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This servlet handles uploading user books onto server.
 */
public class Uploader extends HttpServlet {
    //  UID To satisfy compiler
    private static final long serialVersionUID = 1L;

    private boolean isMultipart;
    private String filePath;
    // Max book size to be uploaded (10MB)
    private int maxFileSize = 10 * 1024 * 1024;
    private File file;

    @Override
    public void init() {
	// Get the file location where it would be stored.
	filePath = getServletContext().getInitParameter("file-upload");
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
	    throws ServletException, IOException {
	// Check that we have a file upload request
	isMultipart = ServletFileUpload.isMultipartContent(request);
	response.setContentType("text/html");
	java.io.PrintWriter out = response.getWriter();
	if (!isMultipart) {
	    out.println("<html>");
	    out.println("<head>");
	    out.println("<title>Servlet upload</title>");
	    out.println("</head>");
	    out.println("<body>");
	    out.println("<p>No file uploaded</p>");
	    out.println("</body>");
	    out.println("</html>");
	    return;
	}

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

	    out.println("<html>");
	    out.println("<head>");
	    out.println("<title>Servlet upload</title>");
	    out.println("</head>");
	    out.println("<body>");

	    while (iterator.hasNext()) {
		FileItem fi = iterator.next();
		if (!fi.isFormField()) {
		    // Get the uploaded file parameters
		    String fieldName = fi.getFieldName();
		    String fileName = fi.getName();
		    String contentType = fi.getContentType();
		    boolean isInMemory = fi.isInMemory();
		    long sizeInBytes = fi.getSize();
		    // Write the file
		    if (fileName.lastIndexOf("\\") >= 0) {
			file = new File(filePath + fileName
				.substring(fileName.lastIndexOf("\\")));
		    } else {
			file = new File(filePath + fileName
				.substring(fileName.lastIndexOf("\\") + 1));
		    }
		    fi.write(file);
		    out.println("Uploaded Filename: " + fileName + "<br>");
		}
	    }
	    out.println("</body>");
	    out.println("</html>");
	} catch (Exception ex) {
	    System.out.println(ex);
	}
    }

}