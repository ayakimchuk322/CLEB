package cleb.library.reading;

import static cleb.security.dao.UserDAO.getUserNameBySubject;

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

// TODO add javadoc
// TODO replace printstacktrace
public class FB2Reader extends HttpServlet implements IReader {

    private static final long serialVersionUID = 1L;

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

        doGet(request, response);
    }

    @Override
    public String read(File bookFile) {
        String bookText = "";

        Document bookDoc = null;

        SAXBuilder builder = new SAXBuilder();

        try {
            bookDoc = builder.build(bookFile);
        } catch (JDOMException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Document root and namespace
        Element root = bookDoc.getRootElement();
        Namespace ns = root.getNamespace();

        // Get element with book text
        Element body = root.getChild("body", ns);

        XMLOutputter xmlOutputter = new XMLOutputter();

        xmlOutputter.setFormat(Format.getPrettyFormat());
        // xmlOutputter.getFormat().setTextMode(Format.TextMode.NORMALIZE);

        // Output text to String
        try (StringWriter stringOut = new StringWriter()) {
            xmlOutputter.output(body, stringOut);
            bookText = stringOut.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return bookText;
    }

}
