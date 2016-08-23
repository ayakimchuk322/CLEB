package cleb.security.login;

import static cleb.security.dao.UserDAO.register;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This servlet registers new users.
 */
public class RegisterServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final String ERROR_DESC = "Can not register, please try again";

    private ServletContext servletContext;
    private ServletContextTemplateResolver templateResolver;
    private TemplateEngine templateEngine;

    // Logger for this class
    private static final Logger logger = LogManager
        .getLogger(RegisterServlet.class.getName());

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

        // Show register.html page
        WebContext webContext = new WebContext(request, response,
            servletContext, request.getLocale());
        templateEngine.process("register", webContext, response.getWriter());
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
            // Inform user about wrong parameters

            logger.warn("Empty user name, email and/or password");
        } else {
            // Proceed with saving in db
            if (register(name, email, password)) {
                // Redirect to login page
                response.sendRedirect("login");
            } else {
                // Inform user about error
                request.setAttribute("errordesc", ERROR_DESC);

                RequestDispatcher dispatcher = getServletContext()
                    .getRequestDispatcher("/error");

                dispatcher.forward(request, response);
            }
        }
    }

}
