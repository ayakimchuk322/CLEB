package cleb.validating;

import java.io.File;

/**
 * This interface contains necessary method to validate uploaded books. Any
 * concrete validator should implement this interface and override its
 * validateBook method based on book type.
 */
// FIXME improve javadoc comments throughout all project!!!
public interface BookValidator {

    public boolean validateBook(File file);

}
