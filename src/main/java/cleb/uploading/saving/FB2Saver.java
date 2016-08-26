package cleb.uploading.saving;

import static cleb.book.dao.BookDAO.addPaths;
import static cleb.book.dao.BookDAO.storeInDB;

import org.apache.logging.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.apache.logging.log4j.LogManager;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.List;
import java.util.Properties;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This servlet stores basic information about fb2 book in database and places
 * the book into storing directory.
 */
public class FB2Saver extends HttpServlet implements ISaver {

    private static final long serialVersionUID = 1L;

    // Logger for this class
    private static final Logger logger = LogManager
        .getLogger(FB2Saver.class.getName());

    private String tempFolderPath;
    private String folderPath;
    private String coversPath;

    private String fileName;

    private String errorDesc;

    @Override
    public void init() throws ServletException {
        // Load properties
        Properties properties = new Properties();

        try (InputStream propIn = getServletContext()
            .getResourceAsStream("/WEB-INF/classes/props.properties")) {
            properties.load(propIn);
        } catch (IOException e) {
            logger.error("Can not load properties", e);
        }

        // Directory for temporary storing uploaded books
        tempFolderPath = properties.getProperty("file-temp-upload");
        // Directory to store uploaded books
        folderPath = properties.getProperty("book-store");
        // Directory to store books covers
        coversPath = properties.getProperty("book-covers");

        // Error description when book can not be uploaded
        errorDesc = properties.getProperty("saver-error");

        logger.info("FB2Saver initialized");
    }

    @Override
    protected void doGet(HttpServletRequest request,
        HttpServletResponse response) throws ServletException, IOException {
        // Redirect back to upload page
        response.sendRedirect("upload");
    }

    @Override
    protected void doPost(HttpServletRequest request,
        HttpServletResponse response) throws ServletException, IOException {

        fileName = (String) request.getAttribute("file");

        String tempBookPath = tempFolderPath + fileName;
        String bookPath = folderPath + fileName;

        Object book = request.getAttribute("book");

        if (getBasicInfo(request, book)) {
            // Try to save cover, book, and information aboub book in database
            String coverPath = saveCover(book, fileName);
            storeInDir(tempBookPath, bookPath);
            addPaths(fileName, bookPath, coverPath);

            logger.info("Book \"{}\" successfully saved", fileName);

            // Redirect back to upload page
            doGet(request, response);
        } else {
            // Inform user about error
            request.setAttribute("errordesc", errorDesc);
            request.setAttribute("previouspage", "/upload");

            RequestDispatcher dispatcher = getServletContext()
                .getRequestDispatcher("/error");

            dispatcher.forward(request, response);
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

        // Uploaded by a user (email)
        Subject currentUser = SecurityUtils.getSubject();
        String uploadedBy = currentUser.getPrincipal().toString();

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

    @Override
    public String saveCover(Object book, String name) {
        // Necessary cast to process with book
        Document doc = (Document) book;

        // Document root and namespaces
        Element root = doc.getRootElement();
        Namespace ns = root.getNamespace();
        Namespace l = root.getNamespace("l");

        Element descEl = null;
        Element titleInfoEl = null;
        Element coverpageEl = null;
        Element imageEl = null;

        // Get the base64 encoded cover image
        Element binaryEl = null;
        String binaryText;

        try {
            descEl = root.getChild("description", ns);
            titleInfoEl = descEl.getChild("title-info", ns);
            coverpageEl = titleInfoEl.getChild("coverpage", ns);
            imageEl = coverpageEl.getChild("image", ns);

            // Get the cover reference
            String imageHref = imageEl.getAttributeValue("href", l);

            List<Element> binaries = root.getChildren("binary", ns);

            // Find the binary element that contains cover
            for (Element e : binaries) {
                if (imageHref.contains(e.getAttributeValue("id"))) {
                    binaryEl = e;
                    break;
                }
            }

            binaryText = binaryEl.getText();
            // Get rid of all spaces, otherwise Base64 decoder won't be able to
            // decode it
            binaryText = binaryText.replaceAll("\\s+", "");
        } catch (NullPointerException e) {
            // This book has no cover
            logger.warn("Book \"{}\" has no cover", fileName);

            return "";
        }

        // Get the file type (jpeg/png)
        String coverType = binaryEl.getAttributeValue("content-type");
        String extension = "";

        switch (coverType) {
            case "image/jpeg":
                extension = ".jpeg";
                break;
            case "image/png":
                extension = ".png";
                break;
            // Handle some possible exotic extension
            default:
                extension = "." + coverType.substring(6);
        }

        // Decode it into byte array
        Decoder decoder;
        byte[] bytes;
        try {
            decoder = Base64.getDecoder();
            bytes = decoder.decode(binaryText);
        } catch (Exception e) {
            logger.error("Can not decode cover for book \"{}\"", fileName, e);

            return "";
        }

        // Write out decoded image into appropriate file
        File cover = new File(coversPath + name + extension);
        try (FileOutputStream fileOut = new FileOutputStream(cover);
             BufferedOutputStream bufferOut = new BufferedOutputStream(
                 fileOut);) {
            bufferOut.write(bytes);
        } catch (IOException e) {
            logger.error("Can not save cover for book\"{}\"", fileName, e);
        }

        return name + extension;
    }

}
