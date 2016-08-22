package cleb.uploading.validating.factory;

/**
 * This factory class manages supporting multiple types of e-books, it's
 * {@code getValidator} method takes {@code String} parameter representing
 * particular e-book type and returns {@code String} with reference for
 * corresponding validator.
 *
 * Each validator class name should consist of two parts - name of the e-book
 * type, all in uppercase, and word "Validator".
 */
public final class ValidatorFactory {

    // One of the supported types
    private String type;

    // TODO replace with static method, no need to create object

    public ValidatorFactory(String type) {
        // Uppercase type to match with validator name
        this.type = type.toUpperCase();
    }

    /**
     * Returns {@code String} representing reference for particular validator
     * that supports this book type. Slash ("/") included at the beginning of
     * the string so no need to include it in {@code getRequestDispatcher}
     * method.
     *
     * @return {@code String} reference for the particular validator
     */
    public String getValidator() {
        return "/" + type + "Validator";
    }
}
