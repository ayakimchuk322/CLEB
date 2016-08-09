package cleb.uploading.factory;

/**
 * This factory class manages supporting multiple types of e-books, it's
 * getSaver method takes String parameter representing particular e-book type
 * and returns String URL for corresponding saver.
 *
 * Each saver class name should consist of two parts - name of the e-book type,
 * all in uppercase, and word "Saver".
 */
public final class SaverFactory {

    // One of the supported types
    private String type;

    public SaverFactory(String type) {
        // Uppercase type to match with validator name
        this.type = type.toUpperCase();
    }

    /**
     * This method simply returns String representing URL for particular saver
     * that supports this book type. Slash ("/") included at the beginning of
     * the string so no need to include it in getRequestDispatcher method.
     *
     * @return String URL for the particular saver
     */
    public String getSaver() {
        return "/" + type + "Saver";
    }

}
