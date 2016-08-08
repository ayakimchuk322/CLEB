package cleb.uploading;

import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import java.io.File;
import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This servlet validates new books.
 */
// TODO remove e.prinstacktraces
public class BookValidator extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private String tempFolderPath;

    private Document document;

    /**
     * Initializes temporary directory.
     */
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

	if (validateBook(tempBookFile)) {
	    request.setAttribute("doc", document);
	    RequestDispatcher dispatcher = request
		    .getRequestDispatcher("/Saver");
	    dispatcher.forward(request, response);
	} else {
	    // TODO inform user about malformed book
	}
    }

    /**
     * This method validates given book to be valid xml file.
     *
     * @param file
     *        fb2 book (xml file) to validate
     * @return true, if given file is valid xml and false - otherwise
     */
    // Currently this method simply builds DOM tree from given file and catches
    // exceptions, if no exception is thrown during building - file should be
    // valid
    // TODO add validation against actual xsd schema
    // For now, not all books pass validation against xsd, maybe it is caused by
    // using different schemas while creating those books
    private boolean validateBook(File file) {
	boolean validated = false;

	SAXBuilder builder = new SAXBuilder();
	try {
	    document = builder.build(file);

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
