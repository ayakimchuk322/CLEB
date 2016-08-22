package cleb.security.servlets;

import static cleb.security.dao.UserDAO.register;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This servlet registers new users.
 */
public class RegisterServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    // Logger for this class
    private static final Logger logger = LogManager
        .getLogger(RegisterServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request,
        HttpServletResponse response) throws ServletException, IOException {
        // Show register.html page
        RequestDispatcher dispatcher = request
            .getRequestDispatcher("/register.html");
        dispatcher.include(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request,
        HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");

        // Get parameters
        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        // Check for name, email and password being not empty and not null
        if (name.length() == 0 || email.length() == 0 || password.length() == 0
            || name == null || email == null || password == null) {
            // Inform user about wrong parameters

            logger.warn("Empty user name, email and/or password");
        } else {
            // Proceed with saving in db
            if (!register(name, email, password)) {
                // TODO inform user about failed register
                doGet(request, response);
            }
        }

        // Show login.jsp page
        RequestDispatcher dispatcher = request
            .getRequestDispatcher("/login.html");
        dispatcher.include(request, response);
    }

}
