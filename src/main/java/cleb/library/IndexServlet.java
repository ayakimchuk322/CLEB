package cleb.library;

import static cleb.book.dao.BookDAO.getLatestBooks;
import static cleb.security.dao.UserDAO.getUserNameBySubject;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cleb.book.Book;

/**
 * This class serves index.html page to users.
 */
public class IndexServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    // Logger for this class
    private static final Logger logger = LogManager
        .getLogger(IndexServlet.class.getName());

    private String annotationsPath;

    private ServletContext servletContext;
    private ServletContextTemplateResolver templateResolver;
    private TemplateEngine templateEngine;

    private List<Element> quotesEl;

    private Random random;

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

        // Directory to store books annotations
        annotationsPath = properties.getProperty("book-annotations");

        // Initialize Thymeleaf for this servlet
        servletContext = getServletContext();
        templateResolver = new ServletContextTemplateResolver(servletContext);
        templateResolver.setTemplateMode("HTML5");
        // Prefix and suffix for template
        templateResolver.setPrefix("/WEB-INF/templates/");
        templateResolver.setSuffix(".html");
        templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);

        // Load quotes from xml file
        InputStream quotesIn = getServletContext()
            .getResourceAsStream("/WEB-INF/quotes.xml");

        SAXBuilder builder = new SAXBuilder();
        try {
            Document quotesDoc = builder.build(quotesIn);
            Element rootEl = quotesDoc.getRootElement();
            quotesEl = rootEl.getChildren();

        } catch (JDOMException e) {
        } catch (IOException e) {
        }

        // Initialize random generator
        random = new Random();
    }

    @Override
    protected void doGet(HttpServletRequest request,
        HttpServletResponse response) throws ServletException, IOException {

        WebContext webContext = new WebContext(request, response,
            servletContext, request.getLocale());

        // Get current user
        Subject currentUser = SecurityUtils.getSubject();

        if (currentUser.isAuthenticated()) {
            String userName = getUserNameBySubject(currentUser);
            // Set username variable
            webContext.setVariable("username", userName);
        }

        // Get random quote from list
        int quotesSize = quotesEl.size();
        Element quote = quotesEl.get(random.nextInt(quotesSize));

        String quoteText = quote.getChildText("text");
        String quoteAuthor = quote.getChildText("author");

        // Set variables for template
        webContext.setVariable("quote", quoteText);
        webContext.setVariable("author", quoteAuthor);

        // Load latest books from database
        Book[] latestBooks = getLatestBooks();
        // Set books variable
        webContext.setVariable("books", latestBooks);

        // Set annotations for latest books
        webContext.setVariable("annotation1",
            loadAnnotation(latestBooks[0].getFileName()));
        webContext.setVariable("annotation2",
            loadAnnotation(latestBooks[1].getFileName()));
        webContext.setVariable("annotation3",
            loadAnnotation(latestBooks[2].getFileName()));

        // For correct display of cyrillic charachters
        response.setCharacterEncoding("UTF-8");

        // Show index.html page
        templateEngine.process("index", webContext, response.getWriter());
    }

    /**
     * Loads annotation into {@code String} from file for {@code bookName} book.
     *
     * @param bookName {@code String} book file name.
     * @return {@code String} with loaded annotation.
     */
    private String loadAnnotation(String bookName) {
        String annotation = "";

        File annoFile = new File(annotationsPath + bookName + ".txt");

        try {
            annotation = FileUtils.readFileToString(annoFile, "UTF-8");
        } catch (IOException e) {
            logger.error("Can not load annotation for book \"{}\"", bookName,
                e);
        }

        return annotation;
    }

}
