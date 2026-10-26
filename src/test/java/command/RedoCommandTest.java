package command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import Tasks.BookingList;
import ui.Ui;

/**
 * Checks redo through the command interface, including history invalidation and repeated undo/redo.
 * @author Maxkedd
 */
class RedoCommandTest {
    private static final String FIRST = "book r/exam n/First v/LT1 t/05/10/2026 14:00";
    private static final String SECOND = "book r/exam n/Second v/LT2 t/05/10/2026 14:00";
    private static final String THIRD = "book r/exam n/Third v/LT3 t/05/10/2026 14:00";
    private static final String FOURTH = "book r/exam n/Fourth v/LT4 t/05/10/2026 14:00";

    /**
     * Redoes commands in reverse undo order and makes each redone command undoable again.
     * @author Maxkedd
     */
    @Test
    void processCommand_repeatedRedo_restoresOrderAndCanBeUndone() {
        BookingList bookings = new BookingList();
        Ui ui = new Ui(bookings);
        run(ui, FIRST);
        run(ui, SECOND);
        run(ui, "/undo");
        run(ui, "/undo");
        assertTrue(bookings.isEmpty());
        assertTrue(run(ui, "/undo").contains("No commands to undo."));

        assertTrue(run(ui, "  /REDO\t ").contains("Redid command: " + FIRST));
        assertEquals(1, bookings.size());
        assertEquals("First", bookings.get(0).getName());
        assertTrue(run(ui, "/redo").contains("Redid command: " + SECOND));
        assertEquals(2, bookings.size());
        assertEquals("Second", bookings.get(1).getName());
        assertTrue(run(ui, "/redo").contains("No commands to redo."));

        assertTrue(run(ui, "/undo").contains("Undid command: " + SECOND));
        assertEquals(1, bookings.size());
        run(ui, "/redo");
        assertEquals(2, bookings.size());
    }

    /**
     * Reapplying cancellation removes the same booking and undo restores its original position.
     * @author Maxkedd
     */
    @Test
    void processCommand_redoCancellation_restoresCancelledState() {
        BookingList bookings = new BookingList();
        Ui ui = new Ui(bookings);
        run(ui, FIRST);
        run(ui, SECOND);
        run(ui, "cancel n/First");
        run(ui, "/undo");

        assertTrue(run(ui, "/redo").contains("Redid command: cancel n/First"));
        assertEquals(1, bookings.size());
        assertEquals("Second", bookings.get(0).getName());
        run(ui, "/undo");
        assertEquals(2, bookings.size());
        assertEquals("First", bookings.get(0).getName());
        assertEquals("Second", bookings.get(1).getName());
    }

    /**
     * A new booking or cancellation after undo discards the previous redo path.
     * @author Maxkedd
     */
    @Test
    void processCommand_newUndoableCommandAfterUndo_clearsRedoHistory() {
        for (String newCommand : new String[]{THIRD, "cancel n/First"}) {
            BookingList bookings = new BookingList();
            Ui ui = new Ui(bookings);
            run(ui, FIRST);
            run(ui, SECOND);
            run(ui, "/undo");
            run(ui, newCommand);

            assertTrue(run(ui, "/redo").contains("No commands to redo."), newCommand);
            assertEquals(-1, bookings.findByName("Second"));
            assertTrue(run(ui, "/undo").contains("Undid command: " + newCommand));
            assertEquals(1, bookings.size());
            assertEquals("First", bookings.get(0).getName());
            run(ui, "/redo");
            assertTrue(run(ui, "/redo").contains("No commands to redo."), newCommand);
            assertEquals(-1, bookings.findByName("Second"));
        }
    }

    /**
     * A new booking or cancellation after a partial redo discards all remaining redo entries.
     * @author Maxkedd
     */
    @Test
    void processCommand_newUndoableCommandAfterRedo_clearsRemainingRedoHistory() {
        for (String newCommand : new String[]{FOURTH, "cancel n/First"}) {
            BookingList bookings = new BookingList();
            Ui ui = new Ui(bookings);
            run(ui, FIRST);
            run(ui, SECOND);
            run(ui, THIRD);
            run(ui, "/undo");
            run(ui, "/undo");
            run(ui, "/redo");
            run(ui, newCommand);

            assertTrue(run(ui, "/redo").contains("No commands to redo."), newCommand);
            assertEquals(-1, bookings.findByName("Third"));
            assertTrue(run(ui, "/undo").contains("Undid command: " + newCommand));
            assertEquals(2, bookings.size());
            assertTrue(run(ui, "/undo").contains("Undid command: " + SECOND));
            assertEquals(1, bookings.size());
        }
    }

    /**
     * Read-only commands and validation failures preserve the next available redo entry.
     * @author Maxkedd
     */
    @Test
    void processCommand_failedAndReadOnlyCommands_preserveRedoHistory() {
        BookingList bookings = new BookingList();
        Ui ui = new Ui(bookings);
        run(ui, FIRST);
        run(ui, SECOND);
        run(ui, THIRD);
        run(ui, "/undo");
        run(ui, "/undo");
        run(ui, "/redo");
        for (String command : new String[]{"list", "find n/First", "viewbooking n/First", "calendar",
            FIRST, "book r/exam n/Clash v/LT1 t/05/10/2026 14:00", "book", "cancel n/Missing", "unknown"}) {
            run(ui, command);
        }
        assertTrue(run(ui, "/redo extra").contains("Please use: /redo"));
        assertTrue(run(ui, "/undo extra").contains("Please use: /undo"));

        assertTrue(run(ui, "/redo").contains("Redid command: " + THIRD));
        assertEquals(3, bookings.size());
        assertEquals("Third", bookings.get(2).getName());
    }

    /**
     * A session has nothing to redo until it undoes a command, and history is not shared across sessions.
     * @author Maxkedd
     */
    @Test
    void processCommand_redoWithoutHistory_leavesBookingsUnchanged() {
        BookingList bookings = new BookingList();
        Ui ui = new Ui(bookings);
        assertTrue(run(ui, "/redo").contains("No commands to redo."));
        run(ui, FIRST);
        assertTrue(run(ui, "/redo").contains("No commands to redo."));
        assertEquals(1, bookings.size());
        run(ui, "/undo");
        assertTrue(run(new Ui(bookings), "/redo").contains("No commands to redo."));
        assertTrue(bookings.isEmpty());
        run(ui, "/redo");
        assertEquals(1, bookings.size());
    }

    /**
     * Captures the command response while checking that the application remains open.
     * @author Maxkedd
     */
    private String run(Ui ui, String input) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOutput = System.out;
        try (PrintStream capturedOutput = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setOut(capturedOutput);
            assertFalse(ui.processCommand(input));
        } finally {
            System.setOut(originalOutput);
        }
        return output.toString(StandardCharsets.UTF_8);
    }
}
