package cleb.uploading.validating;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This servlet class validates new fb2 books.
 */
public class FB2Validator extends HttpServlet implements IValidator {

    private static final long serialVersionUID = 1L;

    // Logger for this class
    private static final Logger logger = LogManager
        .getLogger(FB2Validator.class.getName());

    private String tempFolderPath;

    private String errorDesc;

    @Override
    public void init() throws ServletException {
        // Load properties
        Properties properties = new Properties();

        try (InputStream propIn = getServletContext()
            .getResourceAsStream("/WEB-INF/classes/props.properties")) {
            properties.load(propIn);
        } catch (IOException e) {
            logger.error("Can not load properties", e);
        }

        // Directory for temporary storing uploaded books - till it's checked by
        // this servlet
        tempFolderPath = properties.getProperty("file-temp-upload");

        // Error description when file is not a valid fb2 book
        errorDesc = properties.getProperty("fb2-validator-error");

        logger.info("FB2Validator initialized");
    }

    @Override
    protected void doPost(HttpServletRequest request,
        HttpServletResponse response) throws ServletException, IOException {

        String fileName = (String) request.getAttribute("file");

        Object book = validateBook(fileName);

        if (book != null) {
            request.setAttribute("book", book);
            RequestDispatcher dispatcher = request
                .getRequestDispatcher("/FB2Saver");
            dispatcher.forward(request, response);
        } else {
            // Inform user about error
            request.setAttribute("errordesc", errorDesc);
            request.setAttribute("previouspage", "/upload");

            RequestDispatcher dispatcher = getServletContext()
                .getRequestDispatcher("/error");

            dispatcher.forward(request, response);
        }
    }

    @Override
    public Object validateBook(String fileName) {

        // Currently this method simply builds DOM tree from given file (fb2
        // internally uses XML) and catches exceptions, if no exception is
        // thrown during building - file should be valid
        // TODO add validation against actual xsd schema
        // For now, not all books pass validation against xsd, maybe it is
        // caused by using different schemas while creating those books

        Document book = null;

        File tempBookFile = new File(tempFolderPath + fileName);

        SAXBuilder builder = new SAXBuilder();

        try {
            book = builder.build(tempBookFile);

            logger.info("Book \"{}\" successfully validated", fileName);
        } catch (JDOMException e) {
            logger.error("Book \"{}\" is not a valid fb2 book", fileName, e);

            return null;
        } catch (IOException e) {
            logger.error("Can not validate book \"{}\"", fileName, e);

            return null;
        }

        return book;
    }

}
