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
                BOOKING_NAME must be unique (not case sensitive).
                TIMING must use dd/MM/yyyy HH:mm.
                e.g. book r/exam n/CS2113 Midterm v/LT1 t/05/10/2026 14:00
                Add optional a/ATTENDEES after the timing to record the expected group size.
                e.g. book r/exam n/CS2113 Midterm v/LT1 t/05/10/2026 14:00 a/80
                Each booking lasts 1 hour from its start time, e.g. 14:00 to 15:00.
                A booking is rejected if that venue is already booked at any time during that hour.

            editbooking n/BOOKING_NAME: edit an existing booking (full name, not case sensitive).
                If several bookings match, enter the number from the displayed filtered list.
                Then enter r/REASON, n/NAME, v/VENUE, and t/TIMING changes in any order.
                Add optional a/ATTENDEES at the end to change the attendee count.
                Omit fields to keep them. Type abort to cancel editing, or bye to exit.
                e.g. editbooking n/CS2113 Midterm, then v/LT2 t/06/10/2026 14:00

            cancel n/BOOKING_NAME: cancel an existing booking so the venue is free again.
                e.g. cancel n/CS2113 Midterm
                
            list r/BOOKING_REASON (optional) s/SORT_ORDER (optional):
                BOOKING_REASON must be exams or lectures.
                Omit r/BOOKING_REASON to list both exams and lectures
                e.g. list r/exams   
                
                SORT_ORDER must be alpha, ~alpha, time or ~time
                alpha for alphabetical order and ~alpha for the reverse
                time for time ascending and ~time for time descending
                Omit s/SORT_ORDER to show the list in original order
                e.g list r/exams s/alpha
            free t/TIMING:
                TIMING must use dd/MM/yyyy HH:mm.
                Limited to check bookings free for 1 hr within the start time.
                e.g free t/06/10/2026 14:00

            checksize: shows the largest recorded attendee count for each venue.
                Bookings without an attendee count are ignored.
                e.g. checksize
                
            viewbooking n/BOOKING_NAME (optional) s/time:
                Views matching bookings and optionally sorts them chronologically.
                e.g. viewbooking n/CS2113 Midterm s/time
            
            find n/KEYWORD: finds bookings whose name contains the keyword (not case sensitive).
                e.g. find n/CS2

            calendar: shows today's bookings in order of start time.
                e.g. calendar

            /undo: undo the last successful book or cancel command in this session.
                Repeat /undo to undo earlier commands.

            /redo: redo the most recently undone command in this session.
                Repeat /redo to redo further undone commands.
                A successful new book or cancel command clears all remaining redo history.
                
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
