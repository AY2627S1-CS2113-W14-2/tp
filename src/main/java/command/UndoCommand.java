package command;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import Exceptions.InvalidCommandException;
import Tasks.Booking;
import Tasks.BookingList;

/**
 * Stores successful undoable commands for this session and restores their previous booking state.
 * @author Maxkedd
 */
public class UndoCommand extends Command {
    /** Most recent successful command is at the top of the stack. */
    private final Deque<UndoEntry> history = new ArrayDeque<>();

    /**
     * Declares which commands can be undone. Add future commands that change bookings here.
     * Commands must be passed in the lowercase form returned by Parser.parseCommand.
     * @author Maxkedd
     */
    public boolean isUndoable(String command) {
        return switch (command) {
        case "book", "cancel" -> true;
        default -> false;
        };
    }

    /**
     * Runs a command and records its previous state only if it is undoable and succeeds.
     * Every future undoable operation must also be executed through this method.
     * Booking objects have immutable fields, so copying their references preserves their state.
     *
     * @author Maxkedd
     * @param command parsed command name
     * @param userInput full command text entered by the user, including its arguments
     * @param bookings booking list changed by the operation
     * @param action operation to execute; validation failures must throw before changing bookings
     */
    public void executeAndRecord(String command, String userInput, BookingList bookings, Runnable action) {
        if (!isUndoable(command)) {
            action.run();
            return;
        }

        List<Booking> previousBookings = new ArrayList<>();
        for (int i = 0; i < bookings.size(); i++) {
            previousBookings.add(bookings.get(i));
        }
        action.run();
        history.push(new UndoEntry(userInput.strip(), previousBookings));
    }

    /**
     * Restores the state before the latest successful undoable command, including booking order.
     * The input must be /undo without arguments; undo itself is never added to history.
     * The application's command loop saves the restored list through Storage.saveBookings.
     * @author Maxkedd
     */
    @Override
    public void execute(BookingList bookings, String item) {
        if (!item.strip().equalsIgnoreCase("/undo")) {
            throw new InvalidCommandException("Please use: /undo");
        }
        if (history.isEmpty()) {
            System.out.println("No commands to undo.");
            return;
        }

        UndoEntry lastCommand = history.peek();
        while (!bookings.isEmpty()) {
            bookings.remove(bookings.size() - 1);
        }
        for (Booking booking : lastCommand.previousBookings()) {
            bookings.add(booking);
        }
        history.pop();
        System.out.println("Undid command: " + lastCommand.userInput());
    }

    /**
     * Stores the full command text and the bookings in their original order before it ran.
     * @author Maxkedd
     */
    private record UndoEntry(String userInput, List<Booking> previousBookings) {
    }
}
