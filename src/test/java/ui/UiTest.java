package ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.Test;

import tasks.Booking;
import tasks.BookingList;

/**
 * Checks that commands add bookings only after validation and give useful feedback.
 */
class UiTest {
    @Test
    void processCommand_free_excludesOccupiedVenueWithoutFallthroughOrMutation() {
        BookingList bookings = new BookingList();
        Booking occupied = new Booking("exam", "Occupied", "lt19", "28/10/2026 14:00", 80);
        bookings.add(occupied);
        Ui ui = new Ui(bookings);

        String response = captureListOutput(ui, "free t/28/10/2026 14:00");

        assertTrue(response.contains("Venues with no bookings starting at 28/10/2026 14:00:"));
        assertTrue(response.contains("1. COM1-0201"));
        assertFalse(response.lines().anyMatch(line -> line.matches("\\d+\\. LT19")));
        assertFalse(response.contains("Unknown command"));
        assertFalse(response.contains(HelpText.COMMAND_LIST));
        assertEquals(1, bookings.size());
        assertEquals(occupied, bookings.get(0));
        assertEquals(80, bookings.get(0).getAttendeeCount());
    }

    @Test
    void processCommand_freeInvalidInput_showsHelpAndAllowsNextSearch() {
        BookingList bookings = new BookingList();
        Ui ui = new Ui(bookings);

        for (String command : new String[]{"free", "free t/31/02/2026 14:00"}) {
            String response = captureListOutput(ui, command);
            assertTrue(response.contains(HelpText.COMMAND_LIST), command);
            assertFalse(response.contains("Venues with no bookings starting at"), command);
            assertTrue(bookings.isEmpty());
        }

        String validResponse = captureListOutput(ui, "free t/28/10/2026 14:00");
        assertTrue(validResponse.contains("1. COM1-0201"));
        assertFalse(validResponse.contains(HelpText.COMMAND_LIST));
        assertTrue(bookings.isEmpty());
    }

    @Test
    void processCommand_free_doesNotConsumeUndoHistory() {
        BookingList bookings = new BookingList();
        Ui ui = new Ui(bookings);
        captureListOutput(ui, "book r/exam n/Midterm v/LT19 t/28/10/2026 14:00");
        assertEquals(1, bookings.size());

        captureListOutput(ui, "free t/28/10/2026 14:00");
        captureListOutput(ui, "/undo");

        assertTrue(bookings.isEmpty());
        String response = captureListOutput(ui, "free t/28/10/2026 14:00");
        assertTrue(response.lines().anyMatch(line -> line.matches("\\d+\\. LT19")));
    }

    @Test
    void processCommand_editbooking_routesSelectionAndChangesThenResumesNormalCommands() {
        BookingList bookings = new BookingList();
        bookings.add(new Booking("exam", "Other", "LT0", "05/10/2026 14:00"));
        bookings.add(new Booking("exam", "Midterm", "LT1", "05/10/2026 14:00"));
        bookings.add(new Booking("exam", "Midterm", "LT2", "05/10/2026 14:00"));
        Ui ui = new Ui(bookings);

        assertTrue(runAndCapture(ui, "editbooking n/Midterm").contains("2. [B] exam - Midterm (venue: LT2"));
        assertTrue(runAndCapture(ui, "0").contains("Enter a number from 1 to 2"));
        runAndCapture(ui, "2");
        assertTrue(runAndCapture(ui, "v/LT3").contains("Booking updated:"));

        assertEquals("LT1", bookings.get(1).getVenue());
        assertEquals("LT3", bookings.get(2).getVenue());
        assertTrue(runAndCapture(ui, "viewbooking n/Midterm").contains("LT3"));
        assertTrue(ui.processCommand("bye"));
    }

    @Test
    void processCommand_abortOrByeDuringEdit_preservesBooking() {
        BookingList bookings = new BookingList();
        bookings.add(new Booking("exam", "Midterm", "LT1", "05/10/2026 14:00"));
        Ui ui = new Ui(bookings);
        runAndCapture(ui, "editbooking n/Midterm");
        assertTrue(runAndCapture(ui, "abort").contains("Booking edit cancelled."));
        assertTrue(runAndCapture(ui, "viewbooking n/Midterm").contains("LT1"));
        runAndCapture(ui, "editbooking n/Midterm");

        assertTrue(ui.processCommand("BYE"));
        assertEquals("LT1", bookings.get(0).getVenue());
    }

    @Test
    void constructorAndProcessCommand_nullInputs_failPreconditions() {
        assertThrows(AssertionError.class, () -> new Ui(null));
        assertThrows(AssertionError.class, () -> new Ui(new BookingList()).processCommand(null));
    }

    @Test
    void processCommand_unknownCommand_printsHelpAndPreservesBookings() {
        BookingList bookings = new BookingList();
        bookings.add(new Booking("exam", "Midterm", "LT1", "05/10/2026 14:00"));

        String response = runAndCapture(new Ui(bookings), "unknown");

        assertTrue(response.contains("unknown is not a valid command! -_-"), response);
        assertTrue(response.contains(HelpText.COMMAND_LIST), response);
        assertEquals(1, bookings.size());
    }

    @Test
    void processCommand_validBookings_addsBothAndConfirms() {
        BookingList bookings = new BookingList();
        Ui ui = new Ui(bookings);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOutput = System.out;
        try (PrintStream capturedOutput = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setOut(capturedOutput);
            assertFalse(ui.processCommand("book r/exam n/CS2113 Midterm v/LT1 t/05/10/2026 10:30"));
            assertFalse(ui.processCommand("book r/lecture n/Week 1 Lecture v/LT2 t/05/10/2026 14:00"));
        } finally {
            System.setOut(originalOutput);
        }

        assertEquals(2, bookings.size());
        assertEquals("exam", bookings.get(0).getReason());
        assertEquals("CS2113 Midterm", bookings.get(0).getName());
        assertEquals("lecture", bookings.get(1).getReason());
        assertEquals("Week 1 Lecture", bookings.get(1).getName());
        String response = output.toString(StandardCharsets.UTF_8);
        assertTrue(response.contains(
                "Booking added: [B] exam - CS2113 Midterm (venue: LT1, timing: 05/10/2026 10:30)"));
        assertTrue(response.contains(
                "Booking added: [B] lecture - Week 1 Lecture (venue: LT2, timing: 05/10/2026 14:00)"));
        assertFalse(response.contains("Booking clash:"), response);
    }

    @Test
    void processCommand_checksizeReportsMaximumAttendeeCountForEachVenue() {
        BookingList bookings = new BookingList();
        bookings.add(new Booking("exam", "Smaller", "LT1", "05/10/2026 09:00", 40));
        bookings.add(new Booking("exam", "Largest", "LT1", "05/10/2026 10:00", 90));
        bookings.add(new Booking("lecture", "Other Venue", "LT2", "05/10/2026 11:00", 55));
        Ui ui = new Ui(bookings);

        String response = runAndCapture(ui, "checksize");

        assertTrue(response.contains("LT1: 90 attendees"), response);
        assertTrue(response.contains("LT2: 55 attendees"), response);
        assertFalse(response.contains("LT1: 40 attendees"), response);
        assertEquals(3, bookings.size());
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
            assertFalse(ui.processCommand("book r/lecture n/Week 1 Lecture v/LT2 t/05/10/2026 14:00"));
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
                "book r/exam n/CS2113 Lecture 7 v/LT13 t/05/10/2026 16:00");
        captureListOutput(ui,
                "book r/exam n/CG2028 Finals v/MSPH5 t/06/10/2026 14:00");
        captureListOutput(ui,
                "book r/lecture n/EE2028 v/LT7 t/07/10/2026 13:00");

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

    /**
     * Checks the heading, displayed rows and preservation of stored booking order.
     */
    private void assertListing(String command, String heading, String... expectedRows) {
        BookingList bookings = new BookingList();
        Ui ui = createUiWithBookings(bookings);

        String response = captureListOutput(ui, command);
        List<String> actualRows = response.lines()
                .filter(line -> line.matches("\\d+\\. .* Title: .*"))
                .toList();

        assertTrue(response.contains(heading), response);
        assertEquals(List.of(expectedRows), actualRows, command);
        assertFalse(response.contains(HelpText.COMMAND_LIST), response);
        assertFalse(response.contains("Unknown command"), response);

        // Sorting the displayed list must preserve the stored bookings and order.
        assertEquals(3, bookings.size());
        assertEquals("CS2113 Lecture 7", bookings.get(0).getName());
        assertEquals("CG2028 Finals", bookings.get(1).getName());
        assertEquals("EE2028", bookings.get(2).getName());
    }

    @Test
    void processCommand_list_printsAllBookingsInOriginalOrder() {
        assertListing("list",
                "Ok, here is the list of bookings in ORIGINAL order",
                "1. exam Title: CS2113 Lecture 7",
                "2. exam Title: CG2028 Finals",
                "3. lecture Title: EE2028");
    }

    @Test
    void processCommand_listExams_printsOnlyExams() {
        assertListing("list r/exams",
                "Ok, here is the list of exams in ORIGINAL order",
                "1. exam Title: CS2113 Lecture 7",
                "2. exam Title: CG2028 Finals");
    }

    @Test
    void processCommand_listLectures_startsNumberingAtOne() {
        assertListing("list r/lectures",
                "Ok, here is the list of lectures in ORIGINAL order",
                "1. lecture Title: EE2028");
    }

    @Test
    void processCommand_listAlphabetic_sortsAllBookings() {
        assertListing("list s/alpha",
                "Ok, here is the list of bookings in ALPHABETIC order",
                "1. exam Title: CG2028 Finals",
                "2. exam Title: CS2113 Lecture 7",
                "3. lecture Title: EE2028");
    }

    @Test
    void processCommand_listReverseAlphabetic_sortsAllBookingsBackwards() {
        assertListing("list s/~alpha",
                "Ok, here is the list of bookings in REVERSE_ALPHABETIC order",
                "1. lecture Title: EE2028",
                "2. exam Title: CS2113 Lecture 7",
                "3. exam Title: CG2028 Finals");
    }

    @Test
    void processCommand_listExamsAlphabetic_filtersThenSorts() {
        assertListing("list r/exams s/alpha",
                "Ok, here is the list of exams in ALPHABETIC order",
                "1. exam Title: CG2028 Finals",
                "2. exam Title: CS2113 Lecture 7");
    }

    @Test
    void processCommand_listTimeIncreasing_filtersAndPreservesStoredOrder() {
        assertListing("list r/exams s/time",
                "Ok, here is the list of exams in TIME_INCREASING order",
                "1. exam Title: CS2113 Lecture 7",
                "2. exam Title: CG2028 Finals");
    }

    @Test
    void processCommand_listTimeDecreasing_printsLatestFirst() {
        assertListing("list s/~time",
                "Ok, here is the list of bookings in TIME_DECREASING order",
                "1. lecture Title: EE2028",
                "2. exam Title: CG2028 Finals",
                "3. exam Title: CS2113 Lecture 7");
    }

    @Test
    void processCommand_listTimeWithLegacyTiming_showsErrorAndPreservesBookings() {
        BookingList bookings = new BookingList();
        Booking legacy = new Booking("exam", "Old booking", "LT1", "10AM");
        Booking dated = new Booking("exam", "Dated booking", "LT2", "25/10/2026 09:00");
        bookings.add(legacy);
        bookings.add(dated);
        Ui ui = new Ui(bookings);

        for (String command : new String[]{"list s/time", "list s/~time"}) {
            String response = captureListOutput(ui, command);

            assertTrue(response.contains(
                    "Cannot sort these bookings: their timing must use dd/MM/yyyy HH:mm"), command);
            assertTrue(response.contains(HelpText.COMMAND_LIST), command);
            assertFalse(response.contains("Title:"), command);
            assertEquals(2, bookings.size());
            assertEquals(legacy, bookings.get(0));
            assertEquals(dated, bookings.get(1));
        }
    }

    @Test
    void processCommand_listEmptyBookings_printsNoBookingRows() {
        Ui ui = new Ui(new BookingList());
        String[] commands = {
            "list",
            "list r/exams",
            "list r/lectures",
            "list s/alpha",
            "list r/exams s/~alpha",
            "list s/time",
            "list r/exams s/~time"
        };

        for (String command : commands) {
            String response = captureListOutput(ui, command);

            assertTrue(response.contains("Ok, here is the list of"), command);
            assertFalse(response.contains("Title:"), command);
            assertFalse(response.contains(HelpText.COMMAND_LIST), command);
        }
    }

    @Test
    void processCommand_listInvalidOptions_showsHelpWithoutChangingBookings() {
        BookingList bookings = new BookingList();
        Ui ui = createUiWithBookings(bookings);

        String[] commands = {
            "list meetings",
            "list r/meeting",
            "list s/numeric",
            "list r/exams r/lectures",
            "list s/alpha s/~alpha"
        };

        for (String command : commands) {
            String response = captureListOutput(ui, command);

            assertTrue(response.contains(HelpText.COMMAND_LIST), command);
            assertFalse(response.contains("Title:"), command);
            assertEquals(3, bookings.size(), command);
        }
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
    void processCommand_viewbooking_sortsMatchingBookingsByTime() {
        BookingList bookings = new BookingList();
        bookings.add(new Booking("exam", "CS2113 Midterm", "LT1", "05/10/2026 14:00"));
        bookings.add(new Booking("lecture", "Other Booking", "LT2", "05/10/2026 08:00"));
        bookings.add(new Booking("lecture", "CS2113 Midterm", "LT3", "05/10/2026 09:00"));
        Ui ui = new Ui(bookings);

        String response = captureListOutput(ui, "viewbooking n/CS2113 Midterm s/time");

        int earlierBooking = response.indexOf("Booking timing: 05/10/2026 09:00");
        int laterBooking = response.indexOf("Booking timing: 05/10/2026 14:00");
        assertTrue(earlierBooking >= 0, response);
        assertTrue(laterBooking >= 0, response);
        assertTrue(earlierBooking < laterBooking, response);
        assertFalse(response.contains("Other Booking"));
        assertEquals(3, bookings.size());
    }

    @Test
    void processCommand_viewbooking_timeSortWithLegacyTimingShowsError() {
        BookingList bookings = new BookingList();
        bookings.add(new Booking("exam", "CS2113 Midterm", "LT1", "10:30am"));
        Ui ui = new Ui(bookings);

        String response = captureListOutput(ui, "viewbooking n/CS2113 Midterm s/time");

        assertTrue(response.contains(
                "Cannot sort these bookings: their timing must use dd/MM/yyyy HH:mm."));
        assertTrue(response.contains(HelpText.COMMAND_LIST));
        assertFalse(response.contains("Booking purpose:"));
        assertEquals(1, bookings.size());
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

    /**
     * Checks that /undo reaches the latest successful change despite intervening commands.
     * @author Maxkedd
     */
    @Test
    void processCommand_undo_skipsReadOnlyAndInvalidCommands() {
        BookingList bookings = new BookingList();
        Ui ui = new Ui(bookings);
        runAndCapture(ui, "book r/exam n/Midterm v/LT1 t/05/10/2026 14:00");
        runAndCapture(ui, "list");
        runAndCapture(ui, "find n/Midterm");
        runAndCapture(ui, "viewbooking n/Midterm");
        runAndCapture(ui, "calendar");
        runAndCapture(ui, "book r/exam n/Clash v/LT1 t/05/10/2026 14:00");
        runAndCapture(ui, "cancel n/Missing");
        runAndCapture(ui, "unknown");

        String response = runAndCapture(ui, "  /UNDO\t ");

        assertTrue(response.lines().anyMatch(line -> line.equals(
                "Undid command: book r/exam n/Midterm v/LT1 t/05/10/2026 14:00")), response);
        assertTrue(bookings.isEmpty());
        assertTrue(runAndCapture(ui, "/undo").contains("No commands to undo."));
    }

    /**
     * Checks that undoing cancellation restores the exact booking at its original position.
     * @author Maxkedd
     */
    @Test
    void processCommand_undoCancellation_restoresOriginalPositionAndDuplicateNames() {
        BookingList bookings = new BookingList();
        Booking first = new Booking("exam", "Midterm", "LT1", "05/10/2026 14:00");
        Booking second = new Booking("exam", "Midterm", "LT2", "05/10/2026 14:00");
        bookings.add(first);
        bookings.add(second);
        Ui ui = new Ui(bookings);
        runAndCapture(ui, "cancel n/Midterm");
        assertEquals(1, bookings.size());

        String response = runAndCapture(ui, "/undo");

        assertTrue(response.lines().anyMatch(line -> line.equals("Undid command: cancel n/Midterm")), response);
        assertEquals(2, bookings.size());
        assertEquals(first, bookings.get(0));
        assertEquals(second, bookings.get(1));
        assertTrue(runAndCapture(ui, "/undo").contains("No commands to undo."));
        assertEquals(2, bookings.size());
    }

    /**
     * Checks that an invalid undo request leaves history available for a valid request.
     * @author Maxkedd
     */
    @Test
    void processCommand_undoWithArguments_keepsHistoryForValidUndo() {
        BookingList bookings = new BookingList();
        Ui ui = new Ui(bookings);
        runAndCapture(ui, "book r/exam n/Midterm v/LT1 t/05/10/2026 14:00");

        assertTrue(runAndCapture(ui, "/undo extra").contains("Please use: /undo"));
        assertEquals(1, bookings.size());
        runAndCapture(ui, "/undo");
        assertTrue(bookings.isEmpty());
    }

    /**
     * Checks history when a new booking is added after an undo.
     * @author Maxkedd
     */
    @Test
    void processCommand_undoAfterNewCommand_followsRemainingHistory() {
        BookingList bookings = new BookingList();
        Ui ui = new Ui(bookings);
        runAndCapture(ui, "book r/exam n/First v/LT1 t/05/10/2026 14:00");
        runAndCapture(ui, "book r/exam n/Second v/LT2 t/05/10/2026 14:00");
        runAndCapture(ui, "/undo");
        runAndCapture(ui, "book r/exam n/Third v/LT3 t/05/10/2026 14:00");
        runAndCapture(ui, "/undo");
        assertEquals(1, bookings.size());
        assertEquals("First", bookings.get(0).getName());
        runAndCapture(ui, "/undo");
        assertTrue(bookings.isEmpty());
    }

    /**
     * Checks that a new session does not inherit another session's undo history.
     * @author Maxkedd
     */
    @Test
    void processCommand_newSession_hasNoUndoHistory() {
        BookingList bookings = new BookingList();
        runAndCapture(new Ui(bookings), "book r/exam n/Midterm v/LT1 t/05/10/2026 14:00");

        assertTrue(runAndCapture(new Ui(bookings), "/undo").contains("No commands to undo."));
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

    /**
     * Checks that duplicate-name rejection keeps the last successful command available for undo.
     * @author Maxkedd
     */
    @Test
    void processCommand_duplicateBookingName_isRejectedWithoutChangingUndoHistory() {
        BookingList bookings = new BookingList();
        Ui ui = new Ui(bookings);
        String originalInput = "book r/exam n/Midterm v/LT1 t/05/10/2026 14:00";
        runAndCapture(ui, originalInput);

        String response = runAndCapture(ui, "book r/lecture n/midterm v/LT2 t/06/10/2026 09:00");

        assertTrue(response.contains("A booking with the name midterm already exists."), response);
        assertFalse(response.contains("Booking added:"), response);
        assertEquals(1, bookings.size());
        assertEquals("LT1", bookings.get(0).getVenue());
        assertTrue(runAndCapture(ui, "/undo").contains("Undid command: " + originalInput));
        assertTrue(bookings.isEmpty());
        assertTrue(runAndCapture(ui, "/undo").contains("No commands to undo."));

        // Once undo removes the original booking, its name is available again.
        assertTrue(runAndCapture(ui, originalInput).contains("Booking added:"));
        assertEquals(1, bookings.size());
    }

    @Test
    void processCommand_bookSameVenueAndTime_isRejectedEvenInDifferentCase() {
        BookingList bookings = new BookingList();
        Ui ui = new Ui(bookings);
        runAndCapture(ui, "book r/exam n/CS2113 Midterm v/LT1 t/05/10/2026 14:00");

        String response = runAndCapture(ui, "book r/lecture n/Week 1 Lecture v/lt1 t/05/10/2026 14:00");

        assertTrue(response.contains(
                "Booking clash: LT1 is already booked at 05/10/2026 14:00 for CS2113 Midterm."), response);
        assertFalse(response.contains("Booking added:"), response);
        assertEquals(1, bookings.size());
    }

    @Test
    void processCommand_bookDifferentTimeOrVenue_isAccepted() {
        BookingList bookings = new BookingList();
        Ui ui = new Ui(bookings);

        runAndCapture(ui, "book r/exam n/CS2113 Midterm v/LT1 t/05/10/2026 14:00");
        runAndCapture(ui, "book r/exam n/CS2113 Quiz v/LT1 t/05/10/2026 15:00");
        runAndCapture(ui, "book r/lecture n/Week 1 Lecture v/LT2 t/05/10/2026 14:00");

        assertEquals(3, bookings.size());
    }

    @Test
    void processCommand_calendar_handlesEmptyDayAndExtraWords() {
        BookingList bookings = new BookingList();
        bookings.add(new Booking("exam", "Old Exam", "LT1", "05/10/2020 14:00"));
        Ui ui = new Ui(bookings);

        String emptyDay = runAndCapture(ui, "calendar");
        String extraWords = runAndCapture(ui, "calendar tomorrow");

        assertTrue(emptyDay.contains("No bookings on "), emptyDay);
        assertTrue(extraWords.contains("Please use: calendar"), extraWords);
        assertEquals(1, bookings.size());
    }

    @Test
    void processCommand_bookOverlappingHour_isRejectedButBackToBackIsAccepted() {
        BookingList bookings = new BookingList();
        Ui ui = new Ui(bookings);
        runAndCapture(ui, "book r/exam n/CS2113 Midterm v/LT1 t/05/10/2026 14:00");

        String overlapping = runAndCapture(ui, "book r/lecture n/Week 1 Lecture v/LT1 t/05/10/2026 14:30");
        String earlierOverlap = runAndCapture(ui, "book r/lecture n/Week 2 Lecture v/LT1 t/05/10/2026 13:01");
        runAndCapture(ui, "book r/lecture n/Week 3 Lecture v/LT1 t/05/10/2026 15:00");
        runAndCapture(ui, "book r/lecture n/Week 4 Lecture v/LT1 t/05/10/2026 13:00");

        assertTrue(overlapping.contains("Booking clash: LT1 is already booked at 05/10/2026 14:00"), overlapping);
        assertTrue(earlierOverlap.contains("Booking clash:"), earlierOverlap);
        assertEquals(3, bookings.size());
    }
}
