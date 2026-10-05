package Ui;

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
     * Returns a recognised command in lowercase, or reports an unknown command.
     */
    public String parseCommand(String input) {
        String originalCommand = separateInput(input)[0];
        String command = originalCommand.toLowerCase(Locale.ROOT);
        switch (command) {
        case "book":
        case "bye":
            return command;
        default:
            throw new InvalidCommandException(originalCommand + " is not a valid command! -_-");
        }
    }

    /**
     * Creates an exam or lecture booking using the field order shown in HelpText.
     * Venue and timing are kept as text, so values may contain spaces.
     */
    public Booking parseBooking(String input) {
        String command = parseCommand(input);
        String[] separatedInput = separateInput(input);
        if (!command.equals("book") || separatedInput.length < 2) {
            throw new InvalidCommandException("Please use: book r/BOOKING_REASON v/VENUE t/TIMING");
        }

        // Split only before a field prefix, preserving spaces and dates inside values.
        String[] fields = separatedInput[1].strip().split("\\p{javaWhitespace}+(?=[a-zA-Z]/)");
        if (fields.length != 3 || !fields[0].startsWith("r/")
                || !fields[1].startsWith("v/") || !fields[2].startsWith("t/")) {
            throw new InvalidCommandException("Please use: book r/BOOKING_REASON v/VENUE t/TIMING");
        }

        String reason = fields[0].substring(2).strip().toLowerCase(Locale.ROOT);
        String venue = fields[1].substring(2).strip();
        String timing = fields[2].substring(2).strip();
        if (!reason.equals("exam") && !reason.equals("lecture")) {
            throw new InvalidCommandException("Booking reason must be exam or lecture.");
        }
        if (venue.isBlank() || timing.isBlank()) {
            throw new InvalidCommandException("Venue and timing must not be empty.");
        }
        return new Booking(reason, venue, timing);
    }
}
