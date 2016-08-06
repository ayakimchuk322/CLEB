package cleb.uploading;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This servlet stores basic information about book in database and places the
 * book into storing directory.
 */
public class Saver extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private Document document;

    @Override
    protected void doGet(HttpServletRequest request,
	    HttpServletResponse response) throws ServletException, IOException {
    }

    @Override
    protected void doPost(HttpServletRequest request,
	    HttpServletResponse response) throws ServletException, IOException {
	document = (Document) request.getAttribute("doc");

	getBasicInfo(document);
    }

    // TODO get basic info about book from this Document
    private void getBasicInfo(Document doc) {
	Element root = doc.getRootElement();
	Namespace ns = root.getNamespace();

    }

    // TODO add method to store this book in db and folder
}
