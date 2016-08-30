package cleb.uploading.validating;

/**
 * This interface contains method to validate uploaded book. Any concrete
 * validator should implement this interface and override its
 * {@code validateBook} method based on book type.
 */
public interface IValidator {

    /**
     * Validates given book.
     *
     * @param fileName {@code String} uploaded book file name.
     *
     * @return {@code Object} validated book if is is a valid book, otherwise -
     *         {@code null}.
     */
    public Object validateBook(String fileName);

}
