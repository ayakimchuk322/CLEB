package cleb.validating.factory;

/**
 * This factory class manages supporting multiple types of e-books, it's
 * getValidator method takes String parameter representing particular e-book
 * type and returns String URL for corresponding validator.
 *
 * Each validator class name should consist of two parts - name of the e-book
 * type, all in uppercase, and word "Validator".
 */
public final class ValidatorFactory {

    // One of the supported types
    private String type;

    public ValidatorFactory(String type) {
        // Uppercase type to match with validator name
        this.type = type.toUpperCase();
    }

    /**
     * This method simply returns String representing URL for particular
     * validator that supports this book type. Slash ("/") included at the
     * beginning of the string so no need to include it in getRequestDispatcher
     * method.
     *
     * @return String URL for the particular validator
     */
    public String getValidator() {
        return "/" + type + "Validator";
    }
}
