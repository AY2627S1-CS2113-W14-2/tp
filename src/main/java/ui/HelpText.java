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
                
            list r/BOOKING_REASON (optional) s/SORT_ORDER (optional):
                BOOKING_REASON must be exams or lectures.
                Omit r/BOOKING_REASON to list both exams and lectures
                e.g. list r/exams   
                
                SORT_ORDER must be alpha or ~alpha
                alpha for alphabetical order and ~alpha for the reverse
                Omit s/SORT_ORDER to show the list in original order
                e.g list r/exams s/alpha
                
            viewbooking n/BOOKING_NAME: views an existing booking and shows the details.
                e.g. viewbooking n/CS2113 Midterm
            
            find n/KEYWORD: finds bookings whose name contains the keyword (not case sensitive).
                e.g. find n/CS2
                
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
