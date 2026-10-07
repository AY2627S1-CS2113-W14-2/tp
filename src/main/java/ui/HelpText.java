package ui;

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

            cancel n/BOOKING_NAME: cancel an existing booking so the venue is free again.
                e.g. cancel n/CS2113 Midterm
                
            list BOOKING_REASON (optional):
                BOOKING_REASON must be blank, exams or lectures.
                e.g. list exams   
                
            viewbooking n/BOOKING_NAME: views an existing booking and shows the details.
                e.g. viewbooking n/CS2113 Midterm
                
            bye: exit EduReserve
            """;

    /**
     * Separates command responses.
     */
    public static final String LINE_BREAK = "-".repeat(60);

    /**
     * Prevents construction because this class is only for CONSTANTS.
     */
    private HelpText() {}
}
