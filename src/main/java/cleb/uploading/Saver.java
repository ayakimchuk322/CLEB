package cleb.uploading;

import org.apache.commons.io.FileUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This servlet stores basic information about book in database and places the
 * book into storing directory.
 */
// TODO remove e.printstacktraces
public class Saver extends HttpServlet implements ISaver {
    private static final long serialVersionUID = 1L;

    private String tempFolderPath;
    private String folderPath;

    private Document document;

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

    /**
     * Cleans temporary directory in case there are left some uploaded but
     * unprocessed books.
     */
    @Override
    public void destroy() {
        try {
            FileUtils.cleanDirectory(new File(tempFolderPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        String tempBookPath = tempFolderPath
                + (String) request.getAttribute("book");
        String bookPath = folderPath + (String) request.getAttribute("book");

        document = (Document) request.getAttribute("doc");

        getBasicInfo(request, document);

        storeInDir(tempBookPath, bookPath);
    }

    // TODO add javadoc
    @Override
    public synchronized void getBasicInfo(HttpServletRequest request,
            Document doc) {

        // Information about file, will go into db
        String fileName = (String) request.getAttribute("book");
        String fileType = (String) request.getAttribute("type");
        Long fileSize = (Long) request.getAttribute("size");
        String md5 = (String) request.getAttribute("md5");

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

        // Information about book, will go into db
        String genre = "";
        String authorFirstName = "";
        String authorLastName = "";
        String title = "";
        String seqName = "";
        String seqNumber = "";
        String published = "";
        // String uploadedBy;

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
    }

    // TODO add method to store this book in db

    /**
     * Moves book from temporary to storing directory.
     *
     * @param srcFile
     *        Path to file in temporary directory
     * @param destFile
     *        Path to file in storing directory
     */
    private void storeInDir(String srcFile, String destFile) {
        try {
            FileUtils.moveFile(new File(srcFile), new File(destFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
