package cleb.uploading.validating;

import java.io.File;

/**
 * This interface contains method to validate uploaded book. Any concrete
 * validator should implement this interface and override its
 * {@code validateBook} method based on book type.
 */
public interface IValidator {

    /**
     * Validates given book.
     *
     * @param file book to validate.
     *
     * @return {@code true}, if given file is valid book, otherwise -
     *         {@code false}.
     */
    public boolean validateBook(File file);

}
