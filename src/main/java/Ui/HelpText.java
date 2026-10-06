package Ui;

/**
 * HelpText for Users to understand how to use EduReserve
 */
public final class HelpText {
    public static final String COMMAND_LIST = """
            \n
            How to use EduReserve:
            book r/BOOKING_REASON n/BOOKING_NAME v/VENUE t/TIMING:
                BOOKING_REASON must be exam or lecture.
                e.g. book r/exam n/CS2113 Midterm v/LT1 t/10:30am

            bye: exit EduReserve
            """;

    /**
     * Separates command responses.
     */
    public static final String LINE_BREAK = "─".repeat(60);

    /**
     * Prevents construction because this class is only for CONSTANTS.
     */
    private HelpText() {}
}
