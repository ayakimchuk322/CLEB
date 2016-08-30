package cleb.library.reading.util;

/**
 * This utility class manages supporting multiple types of e-books, it's
 * {@code getReaderReference} method takes {@code String} parameter representing
 * particular e-book type and returns {@code String} with reference for
 * corresponding reader.
 *
 * Each reader class name should consist of two parts - name of the e-book type,
 * all in uppercase, and word "Reader".
 */
public final class ReaderUtil {

    /**
     * Returns {@code String} representing reference for particular reader that
     * supports this book type. Slash ("/") included at the beginning of the
     * string so no need to include it in {@code getRequestDispatcher} method.
     *
     * @param type {@code String} representing book type.
     *
     * @return {@code String} reference for the particular reader.
     */
    public static String getReaderReference(String type) {
        return "/" + type.toUpperCase() + "Reader";
    }

}
