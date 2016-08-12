package cleb.saving;

import org.jdom2.Document;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.lingala.zip4j.core.ZipFile;

/**
 * This servlet stores basic information about epub book in database and places
 * the book into storing directory.
 */
// TODO remove e.printstacktraces
public class EPUBSaver extends HttpServlet implements ISaver {

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

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        String tempBookPath = tempFolderPath
                + (String) request.getAttribute("file");
        String bookPath = folderPath + (String) request.getAttribute("file");

        document = (Document) request.getAttribute("book");

        if (getBasicInfo(request, document)) {
            storeInDir(tempBookPath, bookPath);
        } else {
            // TODO show user error page
        }

    }

    @Override
    public boolean getBasicInfo(HttpServletRequest request, Object book) {

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
        ZipFile zip = (ZipFile) book;

        return storeInDB(fileName, md5, fileSize, fileType, genre,
                authorFirstName, authorLastName, title, seqName, seqNumber,
                published, uploadedBy);
    }

}
