package cleb.security.login;

import static cleb.security.dao.UserDAO.register;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This servlet class registers new users.
 */
public class RegisterServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    // Logger for this class
    private static final Logger logger = LogManager
        .getLogger(RegisterServlet.class.getName());

    private String errorDesc;

    private ServletContext servletContext;
    private ServletContextTemplateResolver templateResolver;
    private TemplateEngine templateEngine;

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

        // Error description when user can not be logged-in
        errorDesc = properties.getProperty("register-servlet-error");

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
            // Users are not permitted to register while already logged-in
            // Show index.html page
            response.sendRedirect("index");
        } else {
            // Show register.html page
            templateEngine.process("register", webContext,
                response.getWriter());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request,
        HttpServletResponse response) throws ServletException, IOException {

        // Get parameters
        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        // Check for name, email and password being not empty and not null
        // This is almost reduntant with using "required" attribute on each
        // input tag
        if (name.length() == 0 || email.length() == 0 || password.length() == 0
            || name == null || email == null || password == null) {

            logger.warn("Empty user name, email and/or password");
        } else {
            // Proceed with saving to database
            if (register(name, email, password)) {
                // Redirect to login page
                response.sendRedirect("login");
            } else {
                // Inform user about error
                request.setAttribute("errordesc", errorDesc);
                request.setAttribute("previouspage", "/register");

                RequestDispatcher dispatcher = getServletContext()
                    .getRequestDispatcher("/error");

                dispatcher.forward(request, response);
            }
        }
    }

}
