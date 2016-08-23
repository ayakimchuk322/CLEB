package cleb.error;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// TODO add javadoc
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

        // Retrieve error description
        String errorDesc = (String) request.getAttribute("errordesc");

        // Obtain page from where came error request
        String previousPage = (String) request
            .getAttribute("javax.servlet.forward.request_uri");

        // Get rid of app name
        previousPage = previousPage.replaceFirst("/cleb", "");

        // Show error.html page with error description and link to previous page
        WebContext webContext = new WebContext(request, response,
            servletContext, request.getLocale());

        webContext.setVariable("errordesc", errorDesc);
        webContext.setVariable("previouspage", previousPage);

        templateEngine.process("error", webContext, response.getWriter());
    }

    @Override
    protected void doPost(HttpServletRequest request,
        HttpServletResponse response) throws ServletException, IOException {
        // Simply transfers control to doGet method
        doGet(request, response);
    }

}
