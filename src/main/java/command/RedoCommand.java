package command;

import exceptions.InvalidCommandException;
import tasks.BookingList;

/**
 * Reapplies the most recently undone command using the current session's shared history.
 * @author Maxkedd
 */
public class RedoCommand extends Command {
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
            throw new InvalidCommandException("Please use: /redo");
        }
        undoCommand.redo(bookings);
    }
}
