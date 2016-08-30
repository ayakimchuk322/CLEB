package cleb.library.reading;

import static cleb.security.dao.UserDAO.getUserNameBySubject;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class FB2Reader extends HttpServlet implements IReader {

    private static final long serialVersionUID = 1L;

    // Logger for this class
    private static final Logger logger = LogManager
        .getLogger(FB2Reader.class.getName());

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
        bookText = normalizeTags(bookText);

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
    public String read(File bookFile) {
        String bookText = "";

        Document book = null;

        SAXBuilder builder = new SAXBuilder();

        try {
            book = builder.build(bookFile);
        } catch (JDOMException e) {
            logger.error("Book \"{}\" is corrupted", bookFile.getName(), e);
        } catch (IOException e) {
            logger.error("Can not read book \"{}\"", bookFile.getName(), e);
        }

        // Document root and namespace
        Element root = book.getRootElement();
        Namespace ns = root.getNamespace();

        // Get element with book text
        Element body = root.getChild("body", ns);

        XMLOutputter xmlOutputter = new XMLOutputter();

        xmlOutputter.setFormat(Format.getPrettyFormat());

        // Output text to String
        try (StringWriter stringOut = new StringWriter()) {
            xmlOutputter.output(body, stringOut);
            bookText = stringOut.toString();
        } catch (IOException e) {
            logger.error("Can not output book text", e);
        }

        return bookText;
    }

    /**
     * Replaces tags specific to fb2 format with html tags.
     *
     * @param text {@code String} to normalize.
     *
     * @return {@code String} with replaced tags.
     */
    private String normalizeTags(String text) {
        text = StringUtils.replace(text, "<body>", "", 1);
        text = StringUtils.replace(text, "</body>", "", 1);

        text = StringUtils.replace(text, "<empty-line />", "<br>");

        text = StringUtils.replace(text, "<title>", "<br><div>");
        text = StringUtils.replace(text, "</title>", "</div><br>");

        text = StringUtils.replace(text, "<epigraph>", "<br><div>");
        text = StringUtils.replace(text, "</epigraph>", "</div><br>");

        text = StringUtils.replace(text, "<text-author>", "<div>");
        text = StringUtils.replace(text, "</text-author>", "</div>");

        text = StringUtils.replacePattern(text, "<a.+?<\\/a>", "");

        return text;
    }

}
