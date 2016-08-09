package cleb.uploading;

import org.jdom2.Document;

import javax.servlet.http.HttpServletRequest;

/**
 * This interface contains necessary method to get information about uploaded
 * book. Any concrete saver class should implement this interface and override
 * its getBasicInfo method based on book type.
 */
public interface ISaver {

    public void getBasicInfo(HttpServletRequest request, Document doc);

}
