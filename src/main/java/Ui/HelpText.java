package Ui;

/**
 * Supplies the shared command guide shown at startup and after input errors.
 */
public final class HelpText {
    /**
     * Command syntax displayed at startup and after input errors.
     */
    public static final String COMMAND_LIST = """
            \n
            How to use EduReserve:
            book r/BOOKING_REASON v/VENUE t/TIMING:
                BOOKING_REASON must be exam or lecture (case-insensitive).
                Supply non-empty fields in the order r/, v/, t/.
                Examples:
                book r/exam v/LT1 t/10:30am
                book r/lecture v/LT2 t/2:00pm
            
            bye: exit EduReserve
            """;

    /**
     * Separates command responses.
     */
    public static final String LINE_BREAK = "─".repeat(60);

    /**
     * Prevents construction because this class only holds shared text.
     */
    private HelpText() {}
}
