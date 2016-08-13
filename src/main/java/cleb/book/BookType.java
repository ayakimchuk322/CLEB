package cleb.book;

/**
 * Enumeration of all supported e-book types by this library.
 */
public enum BookType {
    FB2("fb2"), EPUB("epub");

    private final String type;

    private BookType(String type) {
        this.type = type;
    }

    public String type() {
        return this.type;
    }
}
