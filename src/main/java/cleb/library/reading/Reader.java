package cleb.library.reading;

import static cleb.library.reading.util.ReaderUtil.getReaderReference;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// TODO add javadoc
// TODO replace printstacktrace
public class Reader extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private String folderPath;
    private String annotationsPath;
    private String coversPath;

    @Override
    public void init() throws ServletException {
        // Load properties
        Properties properties = new Properties();

        try (InputStream propIn = getServletContext()
            .getResourceAsStream("/WEB-INF/classes/props.properties")) {
            properties.load(propIn);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Directory with books
        folderPath = properties.getProperty("book-store");
        // Directory with annotations
        annotationsPath = properties.getProperty("book-annotations");
        // Directory with covers
        coversPath = properties.getProperty("book-covers");
    }

    @Override
    protected void doPost(HttpServletRequest request,
        HttpServletResponse response) throws ServletException, IOException {

        String fileName = request.getParameter("filename");
        String fileType = FilenameUtils.getExtension(fileName);

        File bookFile = new File(folderPath + fileName);

        String coverName = request.getParameter("covername");

        request.setAttribute("bookfile", bookFile);
        request.setAttribute("covername", coverName);

        // Get the string reference for concrete reader
        String reader = getReaderReference(fileType);

        // Forward request with file reference to corresponding reader
        RequestDispatcher dispatcher = request.getRequestDispatcher(reader);
        dispatcher.forward(request, response);
    }

}
