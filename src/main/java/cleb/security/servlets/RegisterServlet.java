package cleb.security.servlets;

import org.apache.shiro.crypto.RandomNumberGenerator;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cleb.security.tables.User;
import cleb.security.tables.UserRole;

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
            SessionFactory factory = new Configuration().configure()
                .buildSessionFactory();

            Transaction transaction = null;

            try (Session session = factory.openSession()) {
                transaction = session.beginTransaction();

                registrate(session, name, email, password);

                // Inform user about successfull registering
                request.setAttribute("message", "User created. "
                    + "You can now <a href='login.jsp'>login</a>.");

                transaction.commit();
            } catch (Exception e) {
                if (transaction != null) {
                    transaction.rollback();
                }

                e.printStackTrace();
            }
        }

        // Show register.jsp page
        RequestDispatcher dispatcher = request
            .getRequestDispatcher("/register.jsp");
        dispatcher.include(request, response);
    }

    private void registrate(Session session, String name, String email,
        String plainTextPassword) {
        // Create new user
        User user = new User();
        user.setUserName(name);
        user.setEmail(email);

        generatePassword(user, plainTextPassword);

        // Save user in db
        session.save(user);

        // Create 'reader' role
        UserRole role = new UserRole();
        role.setEmail(email);
        role.setRoleName("reader");

        // Save role in db
        session.save(role);
    }

    private void generatePassword(User user, String plainTextPassword) {
        RandomNumberGenerator rng = new SecureRandomNumberGenerator();
        Object salt = rng.nextBytes();

        // Hash the plain-text password with the random salt and multiple
        // iterations and then Base64-encode the value
        String hashedPasswordBase64 = new Sha256Hash(plainTextPassword, salt,
            1024).toBase64();

        // Update user object password and salt
        user.setPassword(hashedPasswordBase64);
        user.setSalt(salt.toString());
    }

}
