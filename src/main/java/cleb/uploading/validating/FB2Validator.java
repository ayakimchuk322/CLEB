package cleb.uploading.validating;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
 * This servlet validates new fb2 books.
 */
public class FB2Validator extends HttpServlet implements IValidator {

    private static final long serialVersionUID = 1L;

    private static final String ERROR_DESC = "The file is not a valid fb2 book";

    // Logger for this class
    private static final Logger logger = LogManager
        .getLogger(FB2Validator.class.getName());

    private String tempFolderPath;

    private String fileName;

    private Document book;

    @Override
    public void init() throws ServletException {
        // Directory for temporary storing uploaded books - till it's checked by
        // this servlet
        tempFolderPath = getServletContext()
            .getInitParameter("file-temp-upload");

        logger.info("FB2Validator initialized");
    }

    @Override
    protected void doPost(HttpServletRequest request,
        HttpServletResponse response) throws ServletException, IOException {

        fileName = (String) request.getAttribute("file");

        String tempBookPath = tempFolderPath + fileName;
        File tempBookFile = new File(tempBookPath);

        if (validateBook(tempBookFile)) {
            request.setAttribute("book", book);
            RequestDispatcher dispatcher = request
                .getRequestDispatcher("/FB2Saver");
            dispatcher.forward(request, response);
        } else {
            // Inform user about error
            request.setAttribute("errordesc", ERROR_DESC);
            request.setAttribute("previouspage", "/upload");

            RequestDispatcher dispatcher = getServletContext()
                .getRequestDispatcher("/error");

            dispatcher.forward(request, response);
        }
    }

    @Override
    public boolean validateBook(File file) {

        // Currently this method simply builds DOM tree from given file (fb2
        // internally uses XML) and catches exceptions, if no exception is
        // thrown during building - file should be valid
        // TODO add validation against actual xsd schema
        // For now, not all books pass validation against xsd, maybe it is
        // caused by using different schemas while creating those books

        boolean validated = false;

        SAXBuilder builder = new SAXBuilder();
        try {
            book = builder.build(file);

            validated = true;

            logger.info("Book \"{}\" successfully validated", fileName);
        } catch (JDOMException e) {
            validated = false;

            logger.error("Book \"{}\" is not a valid fb2 book", fileName, e);
        } catch (IOException e) {
            validated = false;

            logger.error("Can not validate book \"{}\"", fileName, e);
        }

        return validated;
    }

}
