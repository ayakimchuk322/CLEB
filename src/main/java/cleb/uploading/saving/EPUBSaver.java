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
import java.util.Properties;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;

/**
 * This servlet class stores basic information about epub book in database and
 * places the book into storing directory.
 */
public class EPUBSaver extends HttpServlet implements ISaver {

    private static final long serialVersionUID = 1L;

    // Logger for this class
    private static final Logger logger = LogManager
        .getLogger(EPUBSaver.class.getName());

    private String tempFolderPath;
    private String folderPath;
    private String annotationsPath;
    private String coversPath;

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
        // Directory to store books annotations
        annotationsPath = properties.getProperty("book-annotations");
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

        String fileName = (String) request.getAttribute("file");

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

        // Information about file, will go into database
        String fileName = (String) request.getAttribute("file");
        String md5 = (String) request.getAttribute("md5");
        Long fileSize = (Long) request.getAttribute("size");
        String fileType = (String) request.getAttribute("type");

        // Information about book, will go into database
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

        Document contentopfDoc = null;

        try {
            // Get container.xml to find content.opf file inside book
            FileHeader containerxmlFH = zip
                .getFileHeader("META-INF/container.xml");

            try (InputStream containerxmlIn = zip
                .getInputStream(containerxmlFH)) {

                SAXBuilder builder = new SAXBuilder();

                Document containerxmlDoc = builder.build(containerxmlIn);

                // container.xml root and namespace
                Element containerxmlRoot = containerxmlDoc.getRootElement();
                Namespace containerxmlNs = containerxmlRoot.getNamespace();

                // content.opf location inside epub book
                String contentopfPath = containerxmlRoot
                    .getChild("rootfiles", containerxmlNs)
                    .getChild("rootfile", containerxmlNs)
                    .getAttributeValue("full-path");

                // Get content.opf with information about book
                FileHeader contentopfFH = zip.getFileHeader(contentopfPath);

                try (InputStream contentopIn = zip
                    .getInputStream(contentopfFH);) {

                    contentopfDoc = builder.build(contentopIn);
                }
            }

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

            // Save annotation
            saveAnnotation(contentopfDoc, fileName);

        } catch (ZipException e) {
            logger.error("Can not extract information from book \"{}\"",
                fileName, e);
        } catch (JDOMException e) {
            logger.error("Can not parse information about book \"{}\"",
                fileName, e);
        } catch (IOException e) {
            logger.error("Can not read information about book \"{}\"", fileName,
                e);
        }

        return storeInDB(fileName, md5, fileSize, fileType, genre,
            authorFirstName, authorLastName, title, seqName, seqNumber,
            published, uploadedBy);
    }

    @Override
    public void saveAnnotation(Object annotationHolder, String fileName) {
        // Necessary cast to process with annotation extraction
        Document doc = (Document) annotationHolder;

        try {
            // Document root and namespace
            Element contentopfRoot = doc.getRootElement();
            Namespace contentopfNs = contentopfRoot.getNamespace();
            Namespace dc = contentopfRoot.getNamespace("dc");

            Element metadataEl = contentopfRoot.getChild("metadata",
                contentopfNs);

            // Get annotation element
            Element annoEl = metadataEl.getChild("description", dc);

            // Get annotation text
            String annotation = annoEl.getValue().replaceAll("\\s+", " ")
                .trim();

            // File to store extracted annotation
            File annoFile = new File(annotationsPath + fileName + ".txt");

            // Write out
            FileUtils.writeStringToFile(annoFile, annotation, "UTF-8");
        } catch (IOException e) {
            logger.error("Can not save annotation fot book \"{}\"", fileName,
                e);
        } catch (NullPointerException e) {
            logger.warn("Book \"{}\" has no annotation", fileName);
        }
    }

    @Override
    public String saveCover(Object book, String fileName) {
        // Necessary cast to process with book
        ZipFile zip = (ZipFile) book;

        // To not overheat system with deep search for cover (since different
        // epub books have it under different names and in different folders -
        // which will result in additional parsing of 2 xml files)
        // simply try extract it with a few most used names
        try {
            zip.extractFile("cover.jpeg", coversPath, null, fileName + ".jpeg");
            return fileName + ".jpeg";
        } catch (ZipException e) {
        }

        try {
            zip.extractFile("OPS/cover.jpeg", coversPath, null,
                fileName + ".jpeg");
            return fileName + ".jpeg";
        } catch (ZipException e) {
        }

        try {
            zip.extractFile("OPS/images/cover.jpeg", coversPath, null,
                fileName + ".jpeg");
            return fileName + ".jpeg";
        } catch (ZipException e) {
        }

        try {
            zip.extractFile("cover.png", coversPath, null, fileName + ".png");
            return fileName + ".png";
        } catch (ZipException e) {
        }

        try {
            zip.extractFile("OPS/cover.png", coversPath, null,
                fileName + ".png");
            return fileName + ".png";
        } catch (ZipException e) {
        }

        try {
            zip.extractFile("OPS/images/cover.png", coversPath, null,
                fileName + ".png");
            return fileName + ".png";
        } catch (ZipException e) {
            logger.warn("Book \"{}\" has no cover", fileName);
        }

        return "";
    }

}
