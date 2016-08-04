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
 * This servlet checks if newly uploading books is not already in library.
 */
public class DuplicateChecker extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private String tempFolderPath;
    private String folderPath;

    @Override
    public void init() {
	// Directory for temporary storing uploaded books - till it's checked by
	// DuplicateChecker servlet
	tempFolderPath = getServletContext().getInitParameter("file-temp-upload");
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
	String newBookMd5sum = null;

	String tempBookPath = tempFolderPath + request.getParameter("book");
	File tempBookFile = new File(tempBookPath);

	try (FileInputStream fileIn = new FileInputStream(tempBookFile);
		BufferedInputStream bufferIn = new BufferedInputStream(fileIn);) {

	    // md5Hex converts an array of bytes into an array of characters
	    // representing the hexadecimal values of each byte in order.
	    // The returned array will be double the length of the passed array,
	    // as it takes two characters to represent any given byte.
	    newBookMd5sum = DigestUtils
		    .md5Hex(IOUtils.toByteArray(bufferIn));

	    System.out.println(newBookMd5sum);
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

}
