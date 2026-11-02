package command;

import exceptions.InvalidCommandException;
import tasks.BookingList;

import java.util.logging.Logger;

/**
 * Reapplies the most recently undone command using the current session's shared history.
 * @author Maxkedd
 */
public class RedoCommand extends Command {
    /** Records diagnostic details about redo requests and input validation. */
    private static final Logger LOGGER = Logger.getLogger(RedoCommand.class.getName());

    private final UndoCommand undoCommand;

    /**
     * Uses the same UndoCommand instance that records and undoes this session's commands.
     * @author Maxkedd
     */
    public RedoCommand(UndoCommand undoCommand) {
        assert undoCommand != null : "Redo command requires undo history";
        this.undoCommand = undoCommand;
    }

    /**
     * Validates /redo and restores the next redo state; the application saves the restored list.
     * @author Maxkedd
     */
    @Override
    public void execute(BookingList bookings, String item) {
        if (!item.strip().equalsIgnoreCase("/redo")) {
            LOGGER.fine(() -> "Redo rejected: expected /redo without arguments");
            throw new InvalidCommandException("Please use: /redo");
        }
        LOGGER.fine(() -> "Redo requested: stored count=" + bookings.size());
        undoCommand.redo(bookings);
    }
}
