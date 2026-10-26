package ui;

import java.time.format.DateTimeParseException;
import java.util.Locale;

import Exceptions.InvalidCommandException;
import Tasks.Booking;
import command.SortOrder;

/**
 * Interprets command text without printing or changing the booking list.
 * @author Maxkedd
 */
public class Parser {

    /**
     * Splits once so spaces within booking fields are preserved.
     * @author Maxkedd
     */
    private String[] separateInput(String input) {
        input = input.replace("\t", " ");
        return input.strip().split("\\p{javaWhitespace}+", 2);
    }

    /**
     * Parse command in lowercase and catch unknown commands.
     * @author Maxkedd
     * @author Maxkedd
     */
    public String parseCommand(String input) {
        String originalCommand = separateInput(input)[0];
        String command = originalCommand.toLowerCase(Locale.ROOT);
        switch (command) {
        case "book", "cancel", "list", "bye", "find", "viewbooking", "calendar", "/undo", "/redo":
            return command;
        default:
            throw new InvalidCommandException(originalCommand + " is not a valid command! -_-");
        }
    }

    /**
     * Creates a named exam or lecture booking in the order r/, n/, v/, t/.
     * Field values are kept as text, so names, venues, and timings may contain spaces.
     * @author Maxkedd
     */
    public Booking parseBooking(String input) {
        assert input != null : "Booking command input must not be null";
        String command = parseCommand(input);
        String[] separatedInput = separateInput(input);
        if (!command.equals("book") || separatedInput.length < 2) {
            throw new InvalidCommandException("Please use: book r/BOOKING_REASON n/BOOKING_NAME v/VENUE t/TIMING");
        }

        // checks for r/, n/, v/, t/
        String[] fields = separatedInput[1].strip().split("\\p{javaWhitespace}+(?=[rntv]/)");
        if (fields.length != 4 || !fields[0].startsWith("r/") || !fields[1].startsWith("n/")
                || !fields[2].startsWith("v/") || !fields[3].startsWith("t/")) {
            throw new InvalidCommandException("Please use: book r/BOOKING_REASON n/BOOKING_NAME v/VENUE t/TIMING");
        }

        String reason = fields[0].substring(2).strip().toLowerCase(Locale.ROOT);
        String name = fields[1].substring(2).strip();
        String venue = fields[2].substring(2).strip();
        String timing = fields[3].substring(2).strip();
        if (!reason.equals("exam") && !reason.equals("lecture")) {
            throw new InvalidCommandException("Booking reason must be exam or lecture.");
        }
        if (name.isBlank() || venue.isBlank() || timing.isBlank()) {
            throw new InvalidCommandException("Booking name, venue and timing must not be empty.");
        }
        try {
            Booking.parseStartTime(timing);
        } catch (DateTimeParseException e) {
            throw new InvalidCommandException(
                    "Timing must use dd/MM/yyyy HH:mm, for example 05/10/2026 14:00.");
        }
        return new Booking(reason, name, venue, timing);
    }

    /**
     * Extracts the booking name from a command in the form: cancel n/BOOKING_NAME.
     * The name is kept as text, so it may contain spaces.
     * @author Pratham-tech
     * @param input whole command string entered by the user, never null
     * @return the booking name to cancel
     * @throws InvalidCommandException if the command is not a valid cancel command
     */
    public String parseCancel(String input) {
        assert input != null : "Cancel command input must not be null";
        String command = parseCommand(input);
        String[] separatedInput = separateInput(input);
        if (!command.equals("cancel") || separatedInput.length < 2
                || !separatedInput[1].strip().startsWith("n/")) {
            throw new InvalidCommandException("Please use: cancel n/BOOKING_NAME");
        }

        String name = separatedInput[1].strip().substring(2).strip();
        if (name.isBlank()) {
            throw new InvalidCommandException("Booking name must not be empty.");
        }
        return name;
    }

    /**
     * Parses a view command into 3 separate cases,
     * View lectures and exams, view lectures only and view exams only
     * View the specified lectures or exams by alphabetical order
     * @param input whole string parsed by program
     */
    public ListOptions parseViewList(String input) {
        String command = parseCommand(input);

        if (!command.equals("list")) {
            throw new InvalidCommandException("Please use list r/exams or r/lectures and optional s/SORT_ORDER");
        }

        String[] separatedInput = separateInput(input);

        if (separatedInput.length == 1) {
            return new ListOptions("bookings", SortOrder.ORIGINAL);
        }

        String[] options = separatedInput[1].strip()
                .split("\\p{javaWhitespace}+");

        boolean reasonSeen = false;
        boolean sortOrderSeen = false;
        String item = "bookings";
        SortOrder sortOrder = SortOrder.ORIGINAL;
        for (String option : options) {
            if (option.startsWith("r/")) {
                if (reasonSeen) {
                    throw new InvalidCommandException("Reason should only be specified once");
                }
                reasonSeen = true;
                if (option.equalsIgnoreCase("r/lectures")) {
                    item = "lectures";
                } else if (option.equalsIgnoreCase("r/exams")) {
                    item = "exams";
                } else {
                    throw new InvalidCommandException("Reason should be lectures or exams");
                }
            } else if (option.startsWith("s/")) {
                if (sortOrderSeen) {
                    throw new InvalidCommandException("Sort order should only be specified once");
                }
                sortOrderSeen = true;
                if (option.equalsIgnoreCase("s/alpha")) {
                    sortOrder = SortOrder.ALPHABETIC;
                } else if (option.equalsIgnoreCase("s/~alpha")) {
                    sortOrder = SortOrder.REVERSE_ALPHABETIC;
                } else {
                    throw new InvalidCommandException("Sorted order should be alpha or ~alpha");
                }
            } else {
                throw new InvalidCommandException("list must be followed by a reason or sort order");
            }
        }
        return new ListOptions(item, sortOrder);
    }

    /**
     * Extracts the booking name from a command in the form: viewBooking n/BOOKING_NAME.
     * The name is kept as text, so it may contain spaces.
     * @param input whole string parsed by program
     * @return the booking name to view
     * @throws InvalidCommandException if the command is not a valid view booking command
     */
    public String parseViewBooking(String input) {
        return parseViewBookingOptions(input).getName();
    }

    /**
     * Parses a view booking command with an optional chronological sort option.
     *
     * @param input whole command string entered by the user
     * @return the booking name and whether to sort by time
     */
    public ViewOptions parseViewBookingOptions(String input) {
        String command = parseCommand(input);
        String[] separatedInput = separateInput(input);
        if (!command.equals("viewbooking") || separatedInput.length < 2
                || !separatedInput[1].strip().startsWith("n/")) {
            throw new InvalidCommandException("Please use: viewbooking n/BOOKING_NAME [s/time]");
        }

        String details = separatedInput[1].strip().substring(2).strip();
        boolean sortByTime = false;
        int sortOptionIndex = details.indexOf(" s/");
        if (sortOptionIndex >= 0) {
            String sortOption = details.substring(sortOptionIndex + 1).strip();
            if (!sortOption.equalsIgnoreCase("s/time")) {
                throw new InvalidCommandException("Sort order must be time.");
            }
            details = details.substring(0, sortOptionIndex).strip();
            sortByTime = true;
        }

        String name = details;
        if (name.isBlank()) {
            throw new InvalidCommandException("Booking name must not be empty.");
        }
        return new ViewOptions(name, sortByTime);
    }

    /**
     * Extracts the search keyword from a command in the form: find n/KEYWORD.
     * The keyword is kept as text, so it may contain spaces.
     * @author Pratham-tech
     * @param input whole command string entered by the user, never null
     * @return the keyword to search for in booking names
     * @throws InvalidCommandException if the command is not a valid find command
     */
    public String parseFind(String input) {
        assert input != null : "Find command input must not be null";
        String command = parseCommand(input);
        String[] separatedInput = separateInput(input);
        if (!command.equals("find") || separatedInput.length < 2
                || !separatedInput[1].strip().startsWith("n/")) {
            throw new InvalidCommandException("Please use: find n/KEYWORD");
        }

        String keyword = separatedInput[1].strip().substring(2).strip();
        if (keyword.isBlank()) {
            throw new InvalidCommandException("Search keyword must not be empty.");
        }
        return keyword;
    }
    /**
     * Checks that a calendar command has no extra words after it, because it always shows today's bookings.
     * @author Pratham-tech
     * @param input whole command string entered by the user, never null
     * @throws InvalidCommandException if the command is not exactly: calendar
     */
    public void parseCalendar(String input) {
        assert input != null : "Calendar command input must not be null";
        String command = parseCommand(input);
        String[] separatedInput = separateInput(input);
        if (!command.equals("calendar") || separatedInput.length > 1) {
            throw new InvalidCommandException("Please use: calendar");
        }
    }

    /**
     * Holds the booking filter and sorting options for the list command
     */
    public record ListOptions(String item, SortOrder sortOrder) {
    }
}
