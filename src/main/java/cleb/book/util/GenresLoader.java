package cleb.book.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.servlet.http.HttpServlet;

// TODO add javadoc
public class GenresLoader extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static Properties properties;

    @Override
    public void init() {
        // Load genres from properties file
        properties = new Properties();

        try (InputStream propIn = getServletContext()
            .getResourceAsStream("/WEB-INF/classes/genres.properties")) {
            properties.load(propIn);
        } catch (IOException e) {
        }
    }

    public static String getGenreNormal(String genreAbbr) {
        if (genreAbbr == null || genreAbbr.isEmpty()) {
            return "Other";
        }

        String genreNormal = properties.getProperty(genreAbbr);

        return (genreNormal != null) ? genreNormal : "Other";
    }

}
