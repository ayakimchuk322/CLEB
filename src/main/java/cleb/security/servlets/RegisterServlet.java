package cleb.security.servlets;

import static cleb.security.dao.UserDAO.register;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RegisterServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request,
        HttpServletResponse response) throws ServletException, IOException {
        // Show register.jsp page
        RequestDispatcher dispatcher = request
            .getRequestDispatcher("/register.jsp");
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

        // Check for name, email and password being not null
        if (name == null || email == null || password == null) {
            // Inform user about wrong parameters
        } else {
            // Proceed with saving in db
            // TODO add if check
            register(name, email, password);
        }

        // Show login.jsp page
        RequestDispatcher dispatcher = request
            .getRequestDispatcher("/login.jsp");
        dispatcher.include(request, response);
    }

}
