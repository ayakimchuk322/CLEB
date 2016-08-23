package cleb.reading.servlets;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class IndexServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request,
        HttpServletResponse response) throws ServletException, IOException {

        // Show index.html page
        ServletContext servletContext = getServletContext();

        ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(
            servletContext);

        templateResolver.setTemplateMode("HTML5");
        // Prefix and suffix for template
        templateResolver.setPrefix("/WEB-INF/templates/");
        templateResolver.setSuffix(".html");

        TemplateEngine templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);

        WebContext webContext = new WebContext(request, response,
            servletContext, request.getLocale());

        templateEngine.process("index", webContext, response.getWriter());
    }

}
