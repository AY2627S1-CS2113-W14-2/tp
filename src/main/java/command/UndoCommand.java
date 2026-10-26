package command;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import Exceptions.InvalidCommandException;
import Tasks.Booking;
import Tasks.BookingList;

/**
 * Shares this session's undo and redo history and restores saved booking states.
 * @author Maxkedd
 */
public class UndoCommand extends Command {
    /** Most recent successful command is at the top of the stack. */
    private final Deque<UndoEntry> history = new ArrayDeque<>();
    /** Most recently undone command is the next command available for redo. */
    private final Deque<UndoEntry> redoHistory = new ArrayDeque<>();

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
     * A successful new undoable command discards all remaining redo entries.
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

        List<Booking> previousBookings = copyBookings(bookings);
        action.run();
        history.push(new UndoEntry(userInput.strip(), previousBookings, copyBookings(bookings)));
        redoHistory.clear();
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
        restoreBookings(bookings, lastCommand.previousBookings());
        history.pop();
        redoHistory.push(lastCommand);
        System.out.println("Undid command: " + lastCommand.userInput());
    }

    /**
     * Restores the most recently undone command and makes it available for undo again.
     * Called by RedoCommand after it validates the user's input.
     * @author Maxkedd
     */
    void redo(BookingList bookings) {
        if (redoHistory.isEmpty()) {
            System.out.println("No commands to redo.");
            return;
        }

        UndoEntry lastUndone = redoHistory.peek();
        restoreBookings(bookings, lastUndone.resultingBookings());
        redoHistory.pop();
        history.push(lastUndone);
        System.out.println("Redid command: " + lastUndone.userInput());
    }

    /**
     * Copies booking references in order; Booking fields are immutable.
     * @author Maxkedd
     */
    private List<Booking> copyBookings(BookingList bookings) {
        List<Booking> snapshot = new ArrayList<>();
        for (int i = 0; i < bookings.size(); i++) {
            snapshot.add(bookings.get(i));
        }
        return snapshot;
    }

    /**
     * Replaces the current bookings with a saved state, preserving their original order.
     * @author Maxkedd
     */
    private void restoreBookings(BookingList bookings, List<Booking> snapshot) {
        while (!bookings.isEmpty()) {
            bookings.remove(bookings.size() - 1);
        }
        for (Booking booking : snapshot) {
            bookings.add(booking);
        }
    }

    /**
     * Stores the full command text and ordered booking states before and after it ran.
     * @author Maxkedd
     */
    private record UndoEntry(String userInput, List<Booking> previousBookings, List<Booking> resultingBookings) {
    }
}
