package cleb.uploading.saving;

import static cleb.book.dao.BookDAO.addCoverName;
import static cleb.book.dao.BookDAO.storeInDB;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.temporal.ChronoField;
import java.util.Properties;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

/**
 * This servlet stores basic information about epub book in database and places
 * the book into storing directory.
 */
public class EPUBSaver extends HttpServlet implements ISaver {

    private static final long serialVersionUID = 1L;

    // Logger for this class
    private static final Logger logger = LogManager
        .getLogger(EPUBSaver.class.getName());

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

        logger.info("EPUBSaver initialized");
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
            String coverName = saveCover(book, fileName);
            storeInDir(tempBookPath, bookPath);
            // If coverName is empty (there is no cover for this book) no point
            // to update it in database - it's empty by default
            if (!coverName.isEmpty()) {
                addCoverName(fileName, coverName);
            }

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
        // file attribute extracted in doPost method
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
        ZipFile zip = (ZipFile) book;

        // Add random suffix to temporary directory based on current time
        String suffix = String
            .valueOf(Instant.now().get(ChronoField.MILLI_OF_SECOND));

        // Extract META-INF/container.xml and get the location of content.opf
        // It's location inside book differs from book to book
        // content.opf has the neccessary information about the book
        String containerxmlPath = tempFolderPath + "/" + suffix + "/";

        try {
            zip.extractFile("META-INF/container.xml", containerxmlPath);

            SAXBuilder builder = new SAXBuilder();

            Document containerxmlDoc = builder
                .build(new File(containerxmlPath + "/META-INF/container.xml"));

            // container.xml root and namespace
            Element containerxmlRoot = containerxmlDoc.getRootElement();
            Namespace containerxmlNs = containerxmlRoot.getNamespace();

            // content.opf location inside epub book
            String contentopfPath = containerxmlRoot
                .getChild("rootfiles", containerxmlNs)
                .getChild("rootfile", containerxmlNs)
                .getAttributeValue("full-path");

            zip.extractFile(contentopfPath, containerxmlPath);

            Document contentopfDoc = builder
                .build(new File(containerxmlPath + contentopfPath));

            // content.opf root and namespace
            Element contentopfRoot = contentopfDoc.getRootElement();
            Namespace contentopfNs = contentopfRoot.getNamespace();
            Namespace dc = contentopfRoot.getNamespace("dc");

            // Get required elements to extract necessary information about book
            Element metadataEl = null;
            Element titleEl = null;
            Element creatorEl = null;

            // Not all books have all these items, that's why every one
            // statement below covered in try catch block
            try {
                metadataEl = contentopfRoot.getChild("metadata", contentopfNs);
            } catch (NullPointerException e) {
            }

            try {
                titleEl = metadataEl.getChild("title", dc);
                title = titleEl.getText();
            } catch (NullPointerException e) {
            }

            try {
                creatorEl = metadataEl.getChild("creator", dc);
                authorFirstName = creatorEl.getText();
            } catch (NullPointerException e) {
            }

        } catch (ZipException e) {
            logger.error("Can not extract information from book \"{}\"",
                fileName, e);
        } catch (JDOMException e) {
            logger.error("Can not parse information about book \"{}\"",
                fileName, e);
        } catch (IOException e) {
            logger.error("Can not read information about book \"{}\"", fileName,
                e);
        } finally {
            cleanTmpDir(containerxmlPath);
        }

        return storeInDB(fileName, md5, fileSize, fileType, genre,
            authorFirstName, authorLastName, title, seqName, seqNumber,
            published, uploadedBy);
    }

    @Override
    public String saveCover(Object book, String name) {
        // Necessary cast to process with book
        ZipFile zip = (ZipFile) book;

        // To not overheat system with deep search for cover (since different
        // epub books have it under different names and in different folders -
        // which will result in additional parsing of 2 xml files)
        // simply try extract it with a few most used names
        try {
            zip.extractFile("cover.jpeg", coversPath, null, name + ".jpeg");
            return name + ".jpeg";
        } catch (ZipException e) {
        }

        try {
            zip.extractFile("OPS/cover.jpeg", coversPath, null, name + ".jpeg");
            return name + ".jpeg";
        } catch (ZipException e) {
        }

        try {
            zip.extractFile("OPS/images/cover.jpeg", coversPath, null,
                name + ".jpeg");
            return name + ".jpeg";
        } catch (ZipException e) {
        }

        try {
            zip.extractFile("cover.png", coversPath, null, name + ".png");
            return name + ".png";
        } catch (ZipException e) {
        }

        try {
            zip.extractFile("OPS/cover.png", coversPath, null, name + ".png");
            return name + ".png";
        } catch (ZipException e) {
        }

        try {
            zip.extractFile("OPS/images/cover.png", coversPath, null,
                name + ".png");
            return name + ".png";
        } catch (ZipException e) {
            logger.warn("Book \"{}\" has no cover", fileName);
        }

        return "";
    }

    /**
     * Deletes temporarily extracted files from epub book.
     *
     * @param path Directory to delete.
     */
    private void cleanTmpDir(String path) {
        FileUtils.deleteQuietly(new File(path));
    }
}
