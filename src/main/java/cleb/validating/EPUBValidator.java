package cleb.validating;

import java.io.File;
import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This servlet validates new epub books.
 */
public class EPUBValidator extends HttpServlet implements IValidator {
    private static final long serialVersionUID = 1L;

    private String tempFolderPath;

    // XXX zip object?
    // TODO rename attributes passing by servlets (esp book)
    private Object document;

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
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        String tempBookPath = tempFolderPath
                + (String) request.getAttribute("file");
        File tempBookFile = new File(tempBookPath);

        if (validateBook(tempBookFile)) {
            request.setAttribute("book", document);
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

        return validated;
    }

}
