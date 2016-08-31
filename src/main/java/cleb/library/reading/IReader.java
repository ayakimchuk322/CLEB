package cleb.library.reading;

import java.io.File;

/**
 * This interface contains method to get text from book and return it as a
 * {@code String} to caller. Any concrete reader class should implement this
 * interface and provide specific implementation for {@code read} method based
 * on book type.
 */
public interface IReader {

    /**
     * Implementation of this method should parse {@code bookFile} book and
     * return it's text as a {@code String}.
     *
     * @param bookFile {@code File} book to parse.
     *
     * @return {@code String} book text.
     */
    public String read(File bookFile);
}
