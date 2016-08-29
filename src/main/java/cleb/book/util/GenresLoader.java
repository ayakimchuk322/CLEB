package cleb.book.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.servlet.http.HttpServlet;

/**
 * This class loads at server startup list of genres abbreviations along with
 * their normal, full names from properties file. Loaded propertiies should be
 * used to normalize genres names in database during saving information about
 * book.
 */
public class GenresLoader extends HttpServlet {

    private static final long serialVersionUID = 1L;

    // Logger for this class
    private static final Logger logger = LogManager
        .getLogger(GenresLoader.class.getName());

    private static Properties properties;

    @Override
    public void init() {
        // Load genres from properties file
        properties = new Properties();

        try (InputStream propIn = getServletContext()
            .getResourceAsStream("/WEB-INF/classes/genres.properties")) {
            properties.load(propIn);
        } catch (IOException e) {
            logger.error("Can not load properties", e);
        }
    }

    /**
     * Returns associated full name for a given {@code genreAbbr} genre
     * abbreviation.
     *
     * @param genreAbbr {@code String} genre abbreviation.
     *
     * @return {@code String} with full genre name.
     */
    public static String getGenreNormal(String genreAbbr) {
        if (genreAbbr == null || genreAbbr.isEmpty()) {
            return "Other";
        }

        String genreNormal = properties.getProperty(genreAbbr);

        return (genreNormal != null) ? genreNormal : "Other";
    }

}
