package cleb.reading;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Random;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This class serves index.html page to users.
 */
public class IndexServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private ServletContext servletContext;
    private ServletContextTemplateResolver templateResolver;
    private TemplateEngine templateEngine;

    private List<Element> quotesEl;

    private Random random;

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

        // Load quotes from xml file
        InputStream quotesIn = getServletContext()
            .getResourceAsStream("/WEB-INF/quotes.xml");

        SAXBuilder builder = new SAXBuilder();
        try {
            Document quotesDoc = builder.build(quotesIn);
            Element rootEl = quotesDoc.getRootElement();
            quotesEl = rootEl.getChildren();

        } catch (JDOMException e) {
        } catch (IOException e) {
        }

        // Initialize random generator
        random = new Random();
    }

    @Override
    protected void doGet(HttpServletRequest request,
        HttpServletResponse response) throws ServletException, IOException {

        // Get random quote from list
        int quotesSize = quotesEl.size();
        Element quote = quotesEl.get(random.nextInt(quotesSize));

        String quoteText = quote.getChildText("text");
        String quoteAuthor = quote.getChildText("author");

        // Show index.html page
        WebContext webContext = new WebContext(request, response,
            servletContext, request.getLocale());

        webContext.setVariable("quote", quoteText);
        webContext.setVariable("author", quoteAuthor);

        templateEngine.process("index", webContext, response.getWriter());
    }

}
