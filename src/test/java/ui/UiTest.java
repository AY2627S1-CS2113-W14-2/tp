package ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import Tasks.Booking;
import Tasks.BookingList;

/**
 * Checks that commands add bookings only after validation and give useful feedback.
 */
class UiTest {
    @Test
    void processCommand_validBookings_addsBothAndConfirms() {
        BookingList bookings = new BookingList();
        Ui ui = new Ui(bookings);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOutput = System.out;
        try (PrintStream capturedOutput = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setOut(capturedOutput);
            assertFalse(ui.processCommand("book r/exam n/CS2113 Midterm v/LT1 t/10:30am"));
            assertFalse(ui.processCommand("book r/lecture n/Week 1 Lecture v/LT2 t/2:00pm"));
        } finally {
            System.setOut(originalOutput);
        }

        assertEquals(2, bookings.size());
        assertEquals("exam", bookings.get(0).getReason());
        assertEquals("CS2113 Midterm", bookings.get(0).getName());
        assertEquals("lecture", bookings.get(1).getReason());
        assertEquals("Week 1 Lecture", bookings.get(1).getName());
        String response = output.toString(StandardCharsets.UTF_8);
        assertTrue(response.contains("Booking added: [B] exam - CS2113 Midterm (venue: LT1, timing: 10:30am)"));
        assertTrue(response.contains("Booking added: [B] lecture - Week 1 Lecture (venue: LT2, timing: 2:00pm)"));
    }

    @Test
    void processCommand_invalidThenValid_recoversWithoutAddingInvalidBooking() {
        BookingList bookings = new BookingList();
        Ui ui = new Ui(bookings);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOutput = System.out;
        try (PrintStream capturedOutput = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setOut(capturedOutput);
            assertFalse(ui.processCommand("book r/meeting n/Meeting v/LT1 t/10:30am"));
            assertFalse(ui.processCommand("book r/exam n/Midterm v/LT1 t/"));
            assertFalse(ui.processCommand("book r/exam n/ v/LT1 t/10:30am"));
            assertTrue(bookings.isEmpty());
            assertFalse(ui.processCommand("book r/lecture n/Week 1 Lecture v/LT2 t/2:00pm"));
        } finally {
            System.setOut(originalOutput);
        }

        assertEquals(1, bookings.size());
        assertEquals("lecture", bookings.get(0).getReason());
        String response = output.toString(StandardCharsets.UTF_8);
        assertTrue(response.contains("Booking reason must be exam or lecture."));
        assertTrue(response.contains(HelpText.COMMAND_LIST));
    }

    @Test
    void processCommand_bye_exitsWithoutAddingBooking() {
        BookingList bookings = new BookingList();

        assertTrue(new Ui(bookings).processCommand("BYE"));
        assertTrue(bookings.isEmpty());
    }

    @Test
    void processCommand_cancelExistingBooking_removesIt() {
        BookingList bookings = new BookingList();
        bookings.add(new Booking("exam", "CS2113 Midterm", "LT1", "10:30am"));
        Ui ui = new Ui(bookings);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOutput = System.out;
        try (PrintStream capturedOutput = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setOut(capturedOutput);
            assertFalse(ui.processCommand("cancel n/cs2113 midterm"));
        } finally {
            System.setOut(originalOutput);
        }

        assertTrue(bookings.isEmpty());
        assertTrue(output.toString(StandardCharsets.UTF_8)
                .contains("Booking cancelled: [B] exam - CS2113 Midterm (venue: LT1, timing: 10:30am)"));
    }

    @Test
    void processCommand_cancelUnknownBooking_keepsListUnchanged() {
        BookingList bookings = new BookingList();
        bookings.add(new Booking("exam", "CS2113 Midterm", "LT1", "10:30am"));
        Ui ui = new Ui(bookings);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOutput = System.out;
        try (PrintStream capturedOutput = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setOut(capturedOutput);
            assertFalse(ui.processCommand("cancel n/Nonexistent"));
        } finally {
            System.setOut(originalOutput);
        }

        assertEquals(1, bookings.size());
        assertTrue(output.toString(StandardCharsets.UTF_8).contains("No booking found with the name: Nonexistent"));
    }

    /**
     * Creates the three example bookings through the normal booking command.
     */
    private Ui createUiWithBookings(BookingList bookings) {
        Ui ui = new Ui(bookings);

        captureListOutput(ui,
                "book r/exam n/CS2113 Lecture 7 v/LT13 t/1600-1800");
        captureListOutput(ui,
                "book r/exam n/CG2028 Finals v/MSPH5 t/1400-1530");
        captureListOutput(ui,
                "book r/lecture n/EE2028 v/LT7 t/1300-1400");

        assertEquals(3, bookings.size());
        return ui;
    }

    /**
     * Captures one command's output and checks that it does not exit the app.
     */
    private String captureListOutput(Ui ui, String command) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOutput = System.out;

        try (PrintStream capturedOutput =
                     new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setOut(capturedOutput);
            assertFalse(ui.processCommand(command));
        } finally {
            System.setOut(originalOutput);
        }

        return output.toString(StandardCharsets.UTF_8);
    }

    @Test
    void processCommand_list_printsAllBookingsInOrder() {
        BookingList bookings = new BookingList();
        Ui ui = createUiWithBookings(bookings);

        String response = captureListOutput(ui, "list");
        String expectedRows = String.join(System.lineSeparator(),
                "1. exam Title: CS2113 Lecture 7",
                "2. exam Title: CG2028 Finals",
                "3. lecture Title: EE2028");

        assertTrue(response.contains("Ok, here is the list of bookings:"));
        assertTrue(response.contains(expectedRows));
        assertFalse(response.contains(HelpText.COMMAND_LIST));
        assertFalse(response.contains("Unknown command"));
        assertEquals(3, bookings.size());
    }

    @Test
    void processCommand_listExams_printsOnlyExams() {
        BookingList bookings = new BookingList();
        Ui ui = createUiWithBookings(bookings);

        String response = captureListOutput(ui, "list r/exams");
        String expectedRows = String.join(System.lineSeparator(),
                "1. exam Title: CS2113 Lecture 7",
                "2. exam Title: CG2028 Finals");

        assertTrue(response.contains("Ok, here is the list of exams:"));
        assertTrue(response.contains(expectedRows));
        assertFalse(response.contains("EE2028"));
        assertFalse(response.contains(HelpText.COMMAND_LIST));
        assertFalse(response.contains("Unknown command"));
        assertEquals(3, bookings.size());
    }

    @Test
    void processCommand_listLectures_printsOnlyLecturesStartingAtOne() {
        BookingList bookings = new BookingList();
        Ui ui = createUiWithBookings(bookings);

        String response = captureListOutput(ui, "list r/lectures");

        assertTrue(response.contains("Ok, here is the list of lectures:"));
        assertTrue(response.contains("1. lecture Title: EE2028"));
        assertFalse(response.contains("CS2113 Lecture 7"));
        assertFalse(response.contains("CG2028 Finals"));
        assertFalse(response.contains(HelpText.COMMAND_LIST));
        assertFalse(response.contains("Unknown command"));
        assertEquals(3, bookings.size());
    }

    @Test
    void processCommand_listEmptyBookings_printsNoBookingRows() {
        BookingList bookings = new BookingList();
        Ui ui = new Ui(bookings);

        for (String command : new String[]{"list", "list r/exams", "list r/lectures"}) {
            String response = captureListOutput(ui, command);

            assertTrue(response.contains("Ok, here is the list of"), command);
            assertFalse(response.contains("Title:"), command);
            assertFalse(response.contains(HelpText.COMMAND_LIST), command);
            assertTrue(bookings.isEmpty());
        }
    }

    @Test
    void processCommand_listInvalidFilter_showsHelpWithoutChangingBookings() {
        BookingList bookings = new BookingList();
        Ui ui = createUiWithBookings(bookings);

        String response = captureListOutput(ui, "list meetings");

        assertTrue(response.contains(
                "list must be followed by an empty word, r/exams or r/lectures"));
        assertTrue(response.contains(HelpText.COMMAND_LIST));
        assertFalse(response.contains("Title:"));
        assertEquals(3, bookings.size());
    }

    @Test
    void processCommand_viewbooking_printsExistingDetails() {
        BookingList bookings = new BookingList();
        bookings.add(new Booking("exam", "CS2113 Midterm", "LT1", "10:30am"));
        Ui ui = new Ui(bookings);

        String response = captureListOutput(ui, "viewbooking n/CS2113 Midterm");

        assertTrue(response.contains(
                "Ok, here are the individual bookings relevant to CS2113 Midterm:"));
        assertTrue(response.contains("Booking 1:"));
        assertTrue(response.contains("Booking purpose: exam"));
        assertTrue(response.contains("Booking venue: LT1"));
        assertTrue(response.contains("Booking timing: 10:30am"));
        assertEquals(1, bookings.size());
        assertEquals("CS2113 Midterm", bookings.get(0).getName());
    }

    @Test
    void processCommand_viewbooking_printsCaseInsensitiveMatch() {
        BookingList bookings = new BookingList();
        bookings.add(new Booking("lecture", "Week 1 Lecture", "LT2", "2:00pm"));
        Ui ui = new Ui(bookings);

        String response = captureListOutput(ui, "viewbooking n/week 1 lecture");

        assertTrue(response.contains("Booking purpose: lecture"));
        assertTrue(response.contains("Booking venue: LT2"));
        assertTrue(response.contains("Booking timing: 2:00pm"));
        assertEquals(1, bookings.size());
    }

    @Test
    void processCommand_viewbooking_printsPunctuatedName() {
        BookingList bookings = new BookingList();
        bookings.add(new Booking("exam", "Part a/b: Final Review", "Lecture Theatre 2", "05/10/2026 2:00pm"));
        Ui ui = new Ui(bookings);

        String response = captureListOutput(ui, "viewbooking n/Part a/b: Final Review");

        assertTrue(response.contains(
                "Ok, here are the individual bookings relevant to Part a/b: Final Review:"));
        assertTrue(response.contains("Booking 1:"));
        assertTrue(response.contains("Booking purpose: exam"));
        assertTrue(response.contains("Booking venue: Lecture Theatre 2"));
        assertTrue(response.contains("Booking timing: 05/10/2026 2:00pm"));
        assertEquals(1, bookings.size());
    }

    @Test
    void processCommand_viewbooking_acceptsSurroundingWhitespace() {
        BookingList bookings = new BookingList();
        bookings.add(new Booking("lecture", "Week 1 Lecture", "LT2", "2:00pm"));
        Ui ui = new Ui(bookings);

        String response = captureListOutput(ui, "  viewbooking\tn/  Week 1 Lecture  ");

        assertTrue(response.contains("Booking purpose: lecture"));
        assertTrue(response.contains("Booking venue: LT2"));
        assertTrue(response.contains("Booking timing: 2:00pm"));
        assertEquals(1, bookings.size());
    }

    @Test
    void processCommand_viewbooking_printsAllExactNameMatches() {
        BookingList bookings = new BookingList();
        bookings.add(new Booking("exam", "CS2113 Midterm", "LT1", "10:30am"));
        bookings.add(new Booking("lecture", "CS2113 Midterm", "LT2", "2:00pm"));
        bookings.add(new Booking("exam", "CS2113 Finals", "LT3", "4:00pm"));
        Ui ui = new Ui(bookings);

        String response = captureListOutput(ui, "viewbooking n/cs2113 midterm");

        assertTrue(response.contains("Booking 1:"));
        assertTrue(response.contains("Booking 2:"));
        assertTrue(response.contains("Booking purpose: exam"));
        assertTrue(response.contains("Booking purpose: lecture"));
        assertTrue(response.contains("Booking venue: LT1"));
        assertTrue(response.contains("Booking venue: LT2"));
        assertFalse(response.contains("LT3"));
        assertEquals(3, bookings.size());
    }

    @Test
    void processCommand_viewbooking_unknownBookingShowsError() {
        BookingList bookings = new BookingList();
        bookings.add(new Booking("exam", "CS2113 Midterm", "LT1", "10:30am"));
        Ui ui = new Ui(bookings);

        String response = captureListOutput(ui, "viewbooking n/Nonexistent");

        assertTrue(response.contains("No booking found with the name: Nonexistent"));
        assertTrue(response.contains(HelpText.COMMAND_LIST));
        assertFalse(response.contains("Booking purpose:"));
        assertEquals(1, bookings.size());
    }

    @Test
    void processCommand_viewbooking_missingNameShowsHelp() {
        BookingList bookings = new BookingList();
        bookings.add(new Booking("exam", "CS2113 Midterm", "LT1", "10:30am"));
        Ui ui = new Ui(bookings);

        String response = captureListOutput(ui, "viewbooking");

        assertTrue(response.contains("Please use: viewbooking n/BOOKING_NAME"));
        assertTrue(response.contains(HelpText.COMMAND_LIST));
        assertEquals(1, bookings.size());
    }

    @Test
    void processCommand_viewbooking_blankNameShowsHelp() {
        BookingList bookings = new BookingList();
        bookings.add(new Booking("exam", "CS2113 Midterm", "LT1", "10:30am"));
        Ui ui = new Ui(bookings);

        String response = captureListOutput(ui, "viewbooking n/   ");

        assertTrue(response.contains("Booking name must not be empty."));
        assertTrue(response.contains(HelpText.COMMAND_LIST));
        assertEquals(1, bookings.size());
    }

    @Test
    void processCommand_viewbooking_doesNotExitApplication() {
        BookingList bookings = new BookingList();
        bookings.add(new Booking("exam", "CS2113 Midterm", "LT1", "10:30am"));
        Ui ui = new Ui(bookings);

        assertFalse(captureListOutput(ui, "viewbooking n/CS2113 Midterm").isEmpty());
        assertTrue(ui.processCommand("bye"));
    }

    @Test
    void processCommand_find_showsOnlyMatchingBookings() {
        BookingList bookings = new BookingList();
        bookings.add(new Booking("exam", "CS2113 Midterm", "LT1", "10:30am"));
        bookings.add(new Booking("exam", "CS2445 Final Exam", "LT2", "2:00pm"));
        Ui ui = new Ui(bookings);

        String broadSearch = runAndCapture(ui, "find n/CS2");
        String narrowSearch = runAndCapture(ui, "find n/cs2113");
        String noMatch = runAndCapture(ui, "find n/Tutorial");

        assertTrue(broadSearch.contains("1. [B] exam - CS2113 Midterm"));
        assertTrue(broadSearch.contains("2. [B] exam - CS2445 Final Exam"));
        assertTrue(narrowSearch.contains("1. [B] exam - CS2113 Midterm"));
        assertFalse(narrowSearch.contains("CS2445"));
        assertTrue(noMatch.contains("No bookings found matching: Tutorial"));
        assertEquals(2, bookings.size());
    }

    @Test
    void processCommand_findWithoutKeyword_showsErrorAndKeepsList() {
        BookingList bookings = new BookingList();
        bookings.add(new Booking("exam", "CS2113 Midterm", "LT1", "10:30am"));

        String response = runAndCapture(new Ui(bookings), "find n/");

        assertTrue(response.contains("Search keyword must not be empty."));
        assertEquals(1, bookings.size());
    }

    private String runAndCapture(Ui ui, String command) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOutput = System.out;
        try (PrintStream capturedOutput = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setOut(capturedOutput);
            assertFalse(ui.processCommand(command));
        } finally {
            System.setOut(originalOutput);
        }
        return output.toString(StandardCharsets.UTF_8);
    }
}
