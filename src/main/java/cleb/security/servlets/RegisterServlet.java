package cleb.security.servlets;

import static cleb.security.dao.UserDAO.registrate;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// TODO add javadoc
// FIXME add check before registering if not registered already
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
            request.setAttribute("message",
                "Wrong parameters: fill all required fields.");
        } else {
            // Proceed with saving in db
            registrate(name, email, password);

            // Inform user about successfull registering
            request.setAttribute("message", "User created. "
                + "You can now <a href='login.jsp'>login</a>.");
        }

        // Show register.jsp page
        RequestDispatcher dispatcher = request
            .getRequestDispatcher("/register.jsp");
        dispatcher.include(request, response);
    }

}
