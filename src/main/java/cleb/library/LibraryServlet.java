package cleb.library;

import static cleb.book.dao.BookDAO.getAllBooks;
import static cleb.book.dao.BookDAO.getBooks;
import static cleb.security.dao.UserDAO.getUserNameBySubject;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cleb.book.Book;

/**
 * This servlet class serves library.html page to users.
 */
public class LibraryServlet extends HttpServlet {

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

        List<Book> books = null;

        String searchRequest = request.getParameter("searchrequest");

        // Check if user requested search
        if (searchRequest != null && !searchRequest.isEmpty()) {
            // Load requested books
            books = getBooks(searchRequest);
        } else {
            // Load all books from database
            books = getAllBooks();
        }

        // Set books variable
        webContext.setVariable("books", books);

        // Set encoding for correct display of cyrillic charachters
        response.setCharacterEncoding("UTF-8");

        // Show library.html page
        templateEngine.process("library", webContext, response.getWriter());
    }

    @Override
    protected void doPost(HttpServletRequest request,
        HttpServletResponse response) throws ServletException, IOException {

        // Transfer contol to doGet method
        doGet(request, response);
    }

}
