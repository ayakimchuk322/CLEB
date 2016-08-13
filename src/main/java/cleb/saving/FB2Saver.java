package cleb.saving;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This servlet stores basic information about fb2 book in database and places
 * the book into storing directory.
 */
// TODO remove e.printstacktraces
public class FB2Saver extends HttpServlet implements ISaver {

    private static final long serialVersionUID = 1L;

    private String tempFolderPath;

    private String folderPath;

    private Object book;

    /**
     * Initializes temporary and storing directories.
     */
    @Override
    public void init() throws ServletException {
        // Directory for temporary storing uploaded books
        tempFolderPath = getServletContext()
                .getInitParameter("file-temp-upload");
        // Directory to store uploaded books
        folderPath = getServletContext().getInitParameter("file-store");
    }

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        String tempBookPath = tempFolderPath
                + (String) request.getAttribute("file");
        String bookPath = folderPath + (String) request.getAttribute("file");

        book = request.getAttribute("book");

        if (getBasicInfo(request, book)) {
            storeInDir(tempBookPath, bookPath);
        } else {
            // TODO show user error page
        }

    }

    @Override
    public synchronized boolean getBasicInfo(HttpServletRequest request,
            Object book) {

        // Information about file, will go into db
        String fileName = (String) request.getAttribute("file");
        String md5 = (String) request.getAttribute("md5");
        Long fileSize = (Long) request.getAttribute("size");
        String fileType = (String) request.getAttribute("type");

        // Information about book, will go into db
        String genre = "";
        String authorFirstName = "";
        String authorLastName = "";
        String title = "";
        String seqName = "";
        String seqNumber = "";
        String published = "";
        // FIXME add logic to identify uploader
        String uploadedBy = "ADMIN";

        // Necessary cast to process with book
        Document doc = (Document) book;

        // Document root and namespace
        Element root = doc.getRootElement();
        Namespace ns = root.getNamespace();

        // Get required elements to extract necessary information about book
        Element descEl = null;
        Element titleInfoEl = null;
        Element genreEl = null;
        Element authorEl = null;
        Element firstNameEl = null;
        Element lastNameEl = null;
        Element bookTitleEl = null;
        Element sequenceEl = null;
        Element publishInfoEl = null;
        Element yearEl = null;

        // Not all books have all these items, that's why every one statement
        // below covered in try catch block
        try {
            descEl = root.getChild("description", ns);
        } catch (NullPointerException e) {
        }

        try {
            titleInfoEl = descEl.getChild("title-info", ns);
        } catch (NullPointerException e) {
        }

        try {
            genreEl = titleInfoEl.getChild("genre", ns);
            genre = genreEl.getText();
        } catch (NullPointerException e) {
        }

        try {
            authorEl = titleInfoEl.getChild("author", ns);
        } catch (NullPointerException e) {
        }

        try {
            firstNameEl = authorEl.getChild("first-name", ns);
            authorFirstName = firstNameEl.getText();
        } catch (NullPointerException e) {
        }

        try {
            lastNameEl = authorEl.getChild("last-name", ns);
            authorLastName = lastNameEl.getText();
        } catch (NullPointerException e) {
        }

        try {
            bookTitleEl = titleInfoEl.getChild("book-title", ns);
            title = bookTitleEl.getText();
        } catch (NullPointerException e) {
        }

        try {
            sequenceEl = titleInfoEl.getChild("sequence", ns);
            seqName = sequenceEl.getAttributeValue("name");
            seqNumber = sequenceEl.getAttributeValue("number");
        } catch (NullPointerException e) {
        }

        try {
            publishInfoEl = descEl.getChild("publish-info", ns);
        } catch (NullPointerException e) {
        }

        try {
            yearEl = publishInfoEl.getChild("year", ns);
            published = yearEl.getText();
        } catch (NullPointerException e) {
        }

        return storeInDB(fileName, md5, fileSize, fileType, genre,
                authorFirstName, authorLastName, title, seqName, seqNumber,
                published, uploadedBy);
    }

}
