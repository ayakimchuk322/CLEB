package cleb.uploading;

import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This servlet validates new books against XSD.
 */
// TODO remove e.prinstacktraces
public class BookValidator extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private String tempFolderPath;

    @Override
    public void init() throws ServletException {
	// Directory for temporary storing uploaded books - till it's checked by
	// this servlet
	tempFolderPath = getServletContext()
		.getInitParameter("file-temp-upload");
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

	validateBook(tempBookFile);
    }

    private boolean validateBook(File file) {
	boolean validated = false;

	SAXBuilder builder = new SAXBuilder();
	try {
	    builder.build(file);

	    validated = true;
	} catch (JDOMException e) {
	    e.printStackTrace();
	    validated = false;
	} catch (IOException e) {
	    e.printStackTrace();
	}

	return validated;
    }

}
