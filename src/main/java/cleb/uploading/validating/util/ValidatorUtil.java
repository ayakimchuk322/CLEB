package cleb.uploading.validating.util;

/**
 * This utility class manages supporting multiple types of e-books, it's
 * {@code getValidator} method takes {@code String} parameter representing
 * particular e-book type and returns {@code String} with reference for
 * corresponding validator.
 *
 * Each validator class name should consist of two parts - name of the e-book
 * type, all in uppercase, and word "Validator".
 */
public final class ValidatorUtil {

    /**
     * Returns {@code String} representing reference for particular validator
     * that supports this book type. Slash ("/") included at the beginning of
     * the string so no need to include it in {@code getRequestDispatcher}
     * method.
     *
     * @param type {@code String} representing book type.
     *
     * @return {@code String} reference for the particular validator.
     */
    public static String getValidatorReference(String type) {
        return "/" + type.toUpperCase() + "Validator";
    }

}
