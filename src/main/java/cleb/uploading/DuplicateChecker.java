package cleb.uploading;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This servlet checks if newly uploading book is not already in library.
 */
public class DuplicateChecker extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private String tempFolderPath;
    private String folderPath;

    /**
     * Initializes two parameters - temporary folder and storing folder.
     */
    @Override
    public void init() {
	// Directory for temporary storing uploaded books - till it's checked by
	// this servlet
	tempFolderPath = getServletContext()
		.getInitParameter("file-temp-upload");
	// Directory to store uploaded books
	folderPath = getServletContext().getInitParameter("file-upload");
    }

    @Override
    protected void doGet(HttpServletRequest request,
	    HttpServletResponse response) throws ServletException, IOException {
    }

    @Override
    protected void doPost(HttpServletRequest request,
	    HttpServletResponse response) throws ServletException, IOException {

	String tempBookPath = tempFolderPath + request.getParameter("book");
	File tempBookFile = new File(tempBookPath);

	getBookMd5sum(tempBookFile);
    }

    /**
     * This method calculates temporarily uploaded book MD5 sum.
     *
     * @param file
     *        Previously uploaded book in temp folder
     * @return String representing this book MD5 sum value
     */
    private String getBookMd5sum(File file) {
	String newBookMd5sum = null;

	try (FileInputStream fileIn = new FileInputStream(file);
	     BufferedInputStream bufferIn = new BufferedInputStream(fileIn);) {

	    newBookMd5sum = DigestUtils.md5Hex(IOUtils.toByteArray(bufferIn));
	} catch (IOException e) {
	    e.printStackTrace();
	}

	return newBookMd5sum;
    }

    /**
     * This method connects to database and checks for books with same md5 sum.
     *
     * @param md5sum
     *        String representing new book md5 sum to check among already
     *        uploaded books
     */
    private void checkMd5sum(String md5sum) {

    }
}
