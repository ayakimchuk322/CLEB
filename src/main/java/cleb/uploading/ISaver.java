package cleb.uploading;

import javax.servlet.http.HttpServletRequest;

/**
 * This interface contains necessary method to get information about uploaded
 * book. Any concrete saver class should implement this interface and override
 * its getBasicInfo method based on book type.
 */
public interface ISaver {

    public void getBasicInfo(HttpServletRequest request, Object book);

}
