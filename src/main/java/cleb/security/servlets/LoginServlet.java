package cleb.security.servlets;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// TODO add loggin to file
public class LoginServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request,
        HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");

        if (request.getParameter("logout") != null) {
            org.apache.shiro.subject.Subject currentUser = SecurityUtils
                .getSubject();
            currentUser.logout();
        }

        // Show login.jsp page
        RequestDispatcher dispatcher = request
            .getRequestDispatcher("/login.jsp");
        dispatcher.include(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request,
        HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");

        // Get parameters
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        Boolean rememberMe = Boolean
            .valueOf(request.getParameter("rememberMe"));

        // Check for both email and password being not null
        if (email == null || password == null) {
            // Inform user about wrong parameters
            request.setAttribute("message", "wrong parameters");
        } else {
            // Try to login
            boolean logedIn = tryLogin(email, password, rememberMe);

            if (logedIn) {
                // Inform user about successfull login
                request.setAttribute("message",
                    "Login successful - Welcome! Open <a href='/index.jsp'>Welcome page</a>");
            } else {
                // Inform user about error
                request.setAttribute("message", "Wrong email/password.");
            }
        }

        // Show index.jsp page
        RequestDispatcher dispatcher = request
            .getRequestDispatcher("/index.jsp");
        dispatcher.include(request, response);
    }

    /**
     * Tries to login user with provided email and password.
     *
     * @param email
     *        String with user entered email
     * @param password
     *        String with user entered password
     * @param rememberMe
     *        Boolean with true if remember me was checked and false otherwise.
     * @return True if user was successfully loged in and false otherwise.
     */
    private boolean tryLogin(String email, String password,
        Boolean rememberMe) {
        // Get the currently executing user:
        Subject currentUser = SecurityUtils.getSubject();

        if (!currentUser.isAuthenticated()) {
            // Collect user principals and credentials
            UsernamePasswordToken token = new UsernamePasswordToken(email,
                password);
            // Set remember me status
            token.setRememberMe(rememberMe);

            try {
                currentUser.login(token);

                // Log to console
                System.out
                    .println("User [" + currentUser.getPrincipal().toString()
                        + "] logged in successfully.");

                // Save current username in the session
                currentUser.getSession().setAttribute("username", email);

                return true;
            } catch (UnknownAccountException uae) {
                // Log to console
                System.err.println("There is no user with username of "
                    + token.getPrincipal());
            } catch (IncorrectCredentialsException ice) {
                // Log to console
                System.err.println("Password for account "
                    + token.getPrincipal() + " was incorrect!");
            } catch (LockedAccountException lae) {
                // Log to console
                System.err.println("The account for username "
                    + token.getPrincipal() + " is locked.");
            }
        } else {
            // Alredy logged in
            return true;
        }

        return false;
    }

}
