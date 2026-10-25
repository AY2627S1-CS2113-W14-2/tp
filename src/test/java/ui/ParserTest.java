package ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import Exceptions.InvalidCommandException;
import Tasks.Booking;
import command.SortOrder;

/**
 * Checks booking reasons, required fields, and preservation of user-supplied details.
 */
class ParserTest {
    private final Parser parser = new Parser();

    @Test
    void parseBooking_invalidDatesAndTimes_reportRequiredFormat() {
        String[] timings = {
            "10:30am", "31/02/2026 14:00", "29/02/2025 14:00", "05/13/2026 14:00",
            "05/10/2026 24:00", "05/10/2026 14:60", "2026-10-05 14:00", "5/10/2026 14:00"
        };
        for (String timing : timings) {
            InvalidCommandException error = assertThrows(InvalidCommandException.class,
                    () -> parser.parseBooking("book r/exam n/Midterm v/LT1 t/" + timing), timing);
            assertEquals("Timing must use dd/MM/yyyy HH:mm, for example 05/10/2026 14:00.",
                    error.getMessage(), timing);
        }
    }

    @Test
    void parseBooking_leapDay_acceptsRealDate() {
        Booking booking = parser.parseBooking("book r/exam n/Midterm v/LT1 t/29/02/2028 00:00");
        assertEquals(java.time.LocalDateTime.of(2028, 2, 29, 0, 0), booking.getStartTime());
    }

    @Test
    void parseBookingAndCancel_nullInput_failPreconditions() {
        assertThrows(AssertionError.class, () -> parser.parseBooking(null));
        assertThrows(AssertionError.class, () -> parser.parseCancel(null));
    }

    @Test
    void specializedParsers_otherRecognizedCommand_rejectWrongCommand() {
        assertThrows(InvalidCommandException.class, () -> parser.parseCancel("list n/Midterm"));
        assertThrows(InvalidCommandException.class, () -> parser.parseFind("list n/Midterm"));
        assertThrows(InvalidCommandException.class, () -> parser.parseViewList("bye"));
        assertThrows(InvalidCommandException.class, () -> parser.parseCalendar("bye"));
    }

    @Test
    void parseCommand_unknownOrBlankInput_reportsOriginalCommand() {
        for (String command : new String[]{"UNKNOWN", "", "   "}) {
            InvalidCommandException error = assertThrows(InvalidCommandException.class,
                    () -> parser.parseCommand(command));
            assertEquals(command.strip() + " is not a valid command! -_-", error.getMessage());
        }
    }

    @Test
    void parseBooking_exam_createsBooking() {
        Booking booking = parser.parseBooking(
                "book r/exam n/CS2113 Midterm v/LT1 t/05/10/2026 10:30");

        assertEquals("exam", booking.getReason());
        assertEquals("CS2113 Midterm", booking.getName());
        assertEquals("LT1", booking.getVenue());
        assertEquals("05/10/2026 10:30", booking.getTiming());
    }

    @Test
    void parseBooking_lecture_preservesSpacesAndDates() {
        Booking booking = parser.parseBooking(
                "book r/lecture n/Part a/b Lecture v/Lecture Theatre 2 t/05/10/2026 14:00");

        assertEquals("lecture", booking.getReason());
        assertEquals("Part a/b Lecture", booking.getName());
        assertEquals("Lecture Theatre 2", booking.getVenue());
        assertEquals("05/10/2026 14:00", booking.getTiming());
    }

    @Test
    void parseBooking_mixedCaseAndWhitespace_normalizesReason() {
        Booking booking = parser.parseBooking(
                "  BOOK\tr/  LeCtUrE\tn/  Week 1 Lecture   v/ LT2   t/ 05/10/2026 14:00  ");

        assertEquals("lecture", booking.getReason());
        assertEquals("Week 1 Lecture", booking.getName());
        assertEquals("LT2", booking.getVenue());
        assertEquals("05/10/2026 14:00", booking.getTiming());
    }

    @Test
    void parseBooking_unsupportedReasons_throws() {
        for (String reason : new String[]{"meeting", "examination", "lectures", "exam lecture", ""}) {
            assertThrows(InvalidCommandException.class,
                    () -> parser.parseBooking("book r/" + reason + " n/Midterm v/LT1 t/10:30am"), reason);
        }
    }

    @Test
    void parseBooking_missingOrBlankFields_throws() {
        String[] commands = {
            "book", "book   ", "book n/Midterm v/LT1 t/10:30am", "book r/exam v/LT1 t/10:30am",
            "book r/exam n/Midterm t/10:30am", "book r/exam n/Midterm v/LT1",
            "book r/ n/Midterm v/LT1 t/10:30am", "book r/exam n/ v/LT1 t/10:30am",
            "book r/exam n/Midterm v/ t/10:30am", "book r/lecture n/Week 1 v/LT1 t/   ",
            "book r/exam v/LT1 t/10:30am n/"
        };
        for (String command : commands) {
            assertThrows(InvalidCommandException.class, () -> parser.parseBooking(command), command);
        }
    }

    @Test
    void parseBooking_malformedFields_throws() {
        String[] commands = {
            "book exam Midterm LT1 10:30am", "book unexpected r/exam n/Midterm v/LT1 t/10:30am",
            "book R/exam n/Midterm v/LT1 t/10:30am", "book x/extra r/exam n/Midterm v/LT1 t/10:30am",
            "book r/examn/Midterm v/LT1 t/10:30am", "book r/exam n/Midterm venue/LT1 t/10:30am",
            "book n/Midterm r/exam v/LT1 t/10:30am", "book r/exam n/Midterm t/10:30am v/LT1",
            "book r/exam n/Midterm n/Final v/LT1 t/10:30am"
        };
        for (String command : commands) {
            assertThrows(InvalidCommandException.class, () -> parser.parseBooking(command), command);
        }
    }

    @Test
    void parseBooking_nonBookingCommand_throws() {
        assertThrows(InvalidCommandException.class, () -> parser.parseBooking("bye"));
        assertThrows(InvalidCommandException.class,
                () -> parser.parseBooking("todo r/exam n/Midterm v/LT1 t/10:30am"));
    }

    @Test
    void parseCancel_validName_returnsName() {
        assertEquals("CS2113 Midterm", parser.parseCancel("cancel n/CS2113 Midterm"));
        assertEquals("Part a/b", parser.parseCancel("  CANCEL\tn/  Part a/b  "));
    }

    @Test
    void parseCancel_missingOrBlankName_throws() {
        String[] commands = {"cancel", "cancel   ", "cancel n/", "cancel n/   ", "cancel Midterm", "cancel r/exam"};
        for (String command : commands) {
            assertThrows(InvalidCommandException.class, () -> parser.parseCancel(command), command);
        }
    }

    @Test
    void parseViewList_noOptions_returnsOriginalBookings() {
        for (String command : new String[]{"list", "  list   "}) {
            Parser.ListOptions options = parser.parseViewList(command);

            assertEquals("bookings", options.item(), command);
            assertEquals(SortOrder.ORIGINAL, options.sortOrder(), command);
        }
    }

    @Test
    void parseViewList_filterOnly_returnsOriginalOrder() {
        Parser.ListOptions exams = parser.parseViewList("list r/exams");
        assertEquals("exams", exams.item());
        assertEquals(SortOrder.ORIGINAL, exams.sortOrder());

        Parser.ListOptions lectures = parser.parseViewList("list r/lectures");
        assertEquals("lectures", lectures.item());
        assertEquals(SortOrder.ORIGINAL, lectures.sortOrder());
    }

    @Test
    void parseViewList_sortOnly_returnsAllBookings() {
        Parser.ListOptions alphabetic = parser.parseViewList("list s/alpha");
        assertEquals("bookings", alphabetic.item());
        assertEquals(SortOrder.ALPHABETIC, alphabetic.sortOrder());

        Parser.ListOptions reverse = parser.parseViewList("list s/~alpha");
        assertEquals("bookings", reverse.item());
        assertEquals(SortOrder.REVERSE_ALPHABETIC, reverse.sortOrder());
    }

    @Test
    void parseViewList_filterAndSort_acceptsEitherOrder() {
        Parser.ListOptions expected =
                new Parser.ListOptions("exams", SortOrder.ALPHABETIC);

        assertEquals(expected, parser.parseViewList("list r/exams s/alpha"));
        assertEquals(expected, parser.parseViewList("list s/alpha r/exams"));
    }

    @Test
    void parseViewList_whitespaceAndMixedCaseValues_areAccepted() {
        Parser.ListOptions options =
                parser.parseViewList("  LIST\tr/LeCtUrEs   s/~AlPhA  ");

        assertEquals("lectures", options.item());
        assertEquals(SortOrder.REVERSE_ALPHABETIC, options.sortOrder());
    }

    @Test
    void parseViewList_invalidInputs_throws() {
        String[] commands = {
            "list meetings",
            "list exams",
            "list r/exam",
            "list r/lecture",
            "list r/",
            "list s/",
            "list s/numeric",
            "list r/exams extra",
            "list s/alpha extra",
            "book",
            "bye"
        };

        for (String command : commands) {
            assertThrows(InvalidCommandException.class,
                    () -> parser.parseViewList(command), command);
        }
    }

    @Test
    void parseViewList_repeatedOptions_throws() {
        String[] commands = {
            "list r/exams r/lectures",
            "list r/exams r/exams",
            "list s/alpha s/~alpha",
            "list s/alpha s/alpha"
        };

        for (String command : commands) {
            assertThrows(InvalidCommandException.class,
                    () -> parser.parseViewList(command), command);
        }
    }

    @Test
    void parseViewBooking_validName_returnsName() {
        assertEquals("CS2113 Midterm", parser.parseViewBooking("viewbooking n/CS2113 Midterm"));
    }

    @Test
    void parseViewBookingOptions_withoutSortOption_disablesChronologicalSorting() {
        ViewOptions options = parser.parseViewBookingOptions("viewbooking n/CS2113 Midterm");

        assertEquals("CS2113 Midterm", options.getName());
        assertFalse(options.isSortByTime());
    }

    @Test
    void parseViewBookingOptions_timeSortOption_enablesChronologicalSorting() {
        ViewOptions options = parser.parseViewBookingOptions("viewbooking n/CS2113 Midterm s/TIME");

        assertEquals("CS2113 Midterm", options.getName());
        assertTrue(options.isSortByTime());
    }

    @Test
    void parseViewBookingOptions_unsupportedSortOption_throws() {
        assertThrows(InvalidCommandException.class,
                () -> parser.parseViewBookingOptions("viewbooking n/CS2113 Midterm s/alpha"));
    }

    @Test
    void parseViewBooking_nameWithPunctuationAndSpaces_isPreserved() {
        assertEquals("Part a/b: Final Review", parser.parseViewBooking(
                "viewbooking n/Part a/b: Final Review"));
    }

    @Test
    void parseViewBooking_surroundingWhitespaceAndTabs_areIgnored() {
        assertEquals("Week 1 Lecture", parser.parseViewBooking(
                "  viewbooking\tn/  Week 1 Lecture  "));
    }

    @Test
    void parseViewBooking_lowercaseCommand_returnsName() {
        assertEquals("CS2113 Midterm", parser.parseViewBooking("viewbooking n/CS2113 Midterm"));
    }

    @Test
    void parseViewBooking_missingOrBlankName_throws() {
        String[] commands = {
            "viewbooking",
            "viewbooking   ",
            "viewbooking n/",
            "viewbooking n/   ",
            "viewbooking CS2113 Midterm",
            "viewbooking r/exam"
        };

        for (String command : commands) {
            assertThrows(InvalidCommandException.class,
                    () -> parser.parseViewBooking(command), command);
        }
    }

    @Test
    void parseViewBooking_wrongCommand_throws() {
        String[] commands = {
            "list n/CS2113 Midterm",
            "book n/CS2113 Midterm",
            "cancel n/CS2113 Midterm",
            "bye",
            "view n/CS2113 Midterm"
        };

        for (String command : commands) {
            assertThrows(InvalidCommandException.class,
                    () -> parser.parseViewBooking(command), command);
        }
    }

    @Test
    void parseViewBooking_unknownCommand_throws() {
        assertThrows(InvalidCommandException.class,
                () -> parser.parseViewBooking("search n/CS2113 Midterm"));
    }

    @Test
    void parseViewBooking_nameWithOnlyWhitespaceAfterPrefix_throws() {
        assertThrows(InvalidCommandException.class,
                () -> parser.parseViewBooking("viewbooking\tn/\t\t"));
    }

    @Test
    void parseFind_validKeyword_returnsKeyword() {
        assertEquals("CS2", parser.parseFind("find n/CS2"));
        assertEquals("final exam", parser.parseFind("  FIND\tn/  final exam  "));
    }

    @Test
    void parseFind_missingOrBlankKeyword_throws() {
        String[] commands = {"find", "find   ", "find n/", "find n/   ", "find CS2", "find r/exam"};
        for (String command : commands) {
            assertThrows(InvalidCommandException.class, () -> parser.parseFind(command), command);
        }
    }

    @Test
    void parseFind_nullInput_throwsAssertionError() {
        assertThrows(AssertionError.class, () -> parser.parseFind(null));
    }

    @Test
    void parseCalendar_commandOnly_isAccepted() {
        parser.parseCalendar("calendar");
        parser.parseCalendar("  CALENDAR  ");
    }

    @Test
    void parseCalendar_extraWords_throws() {
        String[] commands = {"calendar today", "calendar d/05/10/2026", "calendar 5"};
        for (String command : commands) {
            assertThrows(InvalidCommandException.class, () -> parser.parseCalendar(command), command);
        }
    }

    @Test
    void parseCalendar_nullInput_throwsAssertionError() {
        assertThrows(AssertionError.class, () -> parser.parseCalendar(null));
    }
}
