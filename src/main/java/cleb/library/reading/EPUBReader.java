package cleb.library.reading;

import static cleb.security.dao.UserDAO.getUserNameBySubject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;

/**
 * This class reads epub books.
 */
public class EPUBReader extends HttpServlet implements IReader {

    private static final long serialVersionUID = 1L;

    // Logger for this class
    private static final Logger logger = LogManager
        .getLogger(EPUBReader.class.getName());

    private ServletContext servletContext;
    private ServletContextTemplateResolver templateResolver;
    private TemplateEngine templateEngine;

    @Override
    public void init() throws ServletException {
        // Initialize Thymeleaf for this servlet
        servletContext = getServletContext();
        templateResolver = new ServletContextTemplateResolver(servletContext);
        templateResolver.setTemplateMode("HTML5");
        templateResolver.setCharacterEncoding("UTF-8");
        // Prefix and suffix for template
        templateResolver.setPrefix("/WEB-INF/templates/");
        templateResolver.setSuffix(".html");
        templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);
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

        File bookFile = (File) request.getAttribute("bookfile");
        String coverName = (String) request.getAttribute("covername");

        // Get book text
        String bookText = read(bookFile);

        // Set book text variable
        webContext.setVariable("booktext", bookText);
        // Set cover variable
        webContext.setVariable("covername", coverName);

        // For correct display of cyrillic charachters
        response.setCharacterEncoding("UTF-8");

        // Show reading.html page
        templateEngine.process("reading", webContext, response.getWriter());
    }

    @Override
    protected void doPost(HttpServletRequest request,
        HttpServletResponse response) throws ServletException, IOException {

        // Forward request to doGet method
        doGet(request, response);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public String read(File bookFile) {
        String bookText = "";

        StringBuilder tempText = new StringBuilder();

        ZipFile book = null;

        List fileHeaders = null;

        try {
            book = new ZipFile(bookFile);

            fileHeaders = book.getFileHeaders();

            for (Object o : fileHeaders) {
                FileHeader fileHeader = (FileHeader) o;

                // Chapters in epub book is XHTML files
                if (fileHeader.getFileName().contains(".xhtml")) {
                    // Get chapter text
                    InputStream chapterIn = book.getInputStream(fileHeader);
                    tempText.append(getChapterText(chapterIn));
                }
            }
        } catch (ZipException e) {
            logger.error("Book \"{}\" is corrupted", bookFile.getName(), e);

            return bookText;
        }

        bookText = tempText.toString();

        return bookText;
    }

    /**
     * Returns {@code String} with text for chapter from {@code chapterIn}
     * stream obtained from zip (epub) file.
     *
     * @param chapterIn {@code InputStream} for chapter in epub book.
     *
     * @return {@code String} with chapter text.
     */
    private String getChapterText(InputStream chapterIn) {
        String chapterText = "";

        Document chapterDoc = null;

        SAXBuilder builder = new SAXBuilder();

        try {
            chapterDoc = builder.build(chapterIn);
        } catch (JDOMException e) {
            logger.error("Can not read chapter", e);

            return chapterText;
        } catch (IOException e) {
            logger.error("Can not read chapter", e);

            return chapterText;
        }

        // Document root and namespace
        Element root = chapterDoc.getRootElement();
        Namespace ns = root.getNamespace();

        // Get element with chapter text
        Element body = root.getChild("body", ns);

        XMLOutputter xmlOutputter = new XMLOutputter();

        // Output chapter text to String
        try (StringWriter stringOut = new StringWriter()) {
            xmlOutputter.output(body, stringOut);
            chapterText = stringOut.toString();
        } catch (IOException e) {
            logger.error("Can not output chapter text", e);
        }

        return chapterText;
    }
}
