package ui;

import java.util.Locale;

import Exceptions.InvalidCommandException;
import Tasks.Booking;

/**
 * Interprets command text without printing or changing the booking list.
 */
public class Parser {

    /**
     * Splits once so spaces within booking fields are preserved.
     */
    private String[] separateInput(String input) {
        input = input.replace("\t", " ");
        return input.strip().split("\\p{javaWhitespace}+", 2);
    }

    /**
     * Parse command in lowercase and catch unknown commands.
     */
    public String parseCommand(String input) {
        String originalCommand = separateInput(input)[0];
        String command = originalCommand.toLowerCase(Locale.ROOT);
        switch (command) {
        case "book", "cancel", "list", "bye":
            return command;
        default:
            throw new InvalidCommandException(originalCommand + " is not a valid command! -_-");
        }
    }

    /**
     * Creates a named exam or lecture booking in the order r/, n/, v/, t/.
     * Field values are kept as text, so names, venues, and timings may contain spaces.
     */
    public Booking parseBooking(String input) {
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
        return new Booking(reason, name, venue, timing);
    }

    /**
     * Extracts the booking name from a command in the form: cancel n/BOOKING_NAME.
     * The name is kept as text, so it may contain spaces.
     */
    public String parseCancel(String input) {
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
     * @param input whole string parsed by program
     */
    public String parseViewList(String input) {
        String command = parseCommand(input);
        String[] separatedInput = separateInput(input);

        if (!command.equals("list") || separatedInput.length > 2) {
            throw new InvalidCommandException("Please only input the command and items you want to view");
        }

        if (separatedInput.length == 1) {
            return "bookings";
        }

        String item = separatedInput[1].strip();

        switch (item) {
            case "lectures":
                return "lectures";
            case "exams":
                return "exams";
            default:
                throw new InvalidCommandException("list must be followed by an empty word, exams or lectures");
        }
    }
}
