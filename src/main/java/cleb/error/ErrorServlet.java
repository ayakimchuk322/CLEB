package cleb.error;

import static cleb.security.dao.UserDAO.getUserNameBySubject;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This servlet class handles some exceptional situations with user interaction
 * and shows user page with brief error descripion.
 *
 * Classes that want to show user such information should forward request and
 * response with two additional attributes - brief error description and name of
 * a page or servlet to point back.
 */
public class ErrorServlet extends HttpServlet {

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

        // Retrieve error description
        String errorDesc = (String) request.getAttribute("errordesc");

        // Obtain page from where came error request
        String previousPage = (String) request.getAttribute("previouspage");

        // Set variables for template
        webContext.setVariable("errordesc", errorDesc);
        webContext.setVariable("previouspage", previousPage);

        // Show error.html page with error description and link to previous page
        templateEngine.process("error", webContext, response.getWriter());
    }

    @Override
    protected void doPost(HttpServletRequest request,
        HttpServletResponse response) throws ServletException, IOException {

        // Simply transfers control to doGet method
        doGet(request, response);
    }

}
