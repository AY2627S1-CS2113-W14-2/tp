package command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.junit.jupiter.api.Test;

import tasks.BookingList;
import ui.Ui;

/** Checks diagnostic messages through real dispatch, including failed operations and history changes. */
class BookingHistoryLoggingTest {
    @Test
    void processCommand_loggingEnabled_reportsOutcomesWithoutChangingHistory() {
        List<String> messages = new ArrayList<>();
        Handler handler = new Handler() {
            @Override
            public void publish(LogRecord record) {
                messages.add(record.getMessage());
                assertEquals(Level.FINE, record.getLevel());
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() {
            }
        };
        List<Logger> loggers = List.of(Logger.getLogger(BookCommand.class.getName()),
                Logger.getLogger(UndoCommand.class.getName()), Logger.getLogger(RedoCommand.class.getName()),
                Logger.getLogger(Ui.class.getName()), Logger.getLogger(BookingList.class.getName()));
        List<Level> previousLevels = new ArrayList<>();
        for (Logger logger : loggers) {
            previousLevels.add(logger.getLevel());
            logger.addHandler(handler);
            logger.setLevel(Level.FINE);
        }
        try {
            BookingList bookings = new BookingList();
            Ui ui = new Ui(bookings);
            String first = "book r/exam n/First v/LT1 t/05/10/2026 14:00";
            for (String input : new String[]{"/undo", "/redo", "/undo extra", "/redo extra",
                "book", first, first, "book r/exam n/Clash v/LT1 t/05/10/2026 14:00",
                "cancel n/Missing", "/undo", "/redo"}) {
                assertFalse(ui.processCommand(input));
            }
            assertEquals(1, bookings.size());
            assertEquals("First", bookings.get(0).getName());
            assertFalse(ui.processCommand("cancel n/First"));
            assertTrue(bookings.isEmpty());
            for (String expected : List.of(
                    "Undo skipped: no commands to undo", "Redo skipped: no commands to redo",
                    "Undo rejected: expected /undo without arguments",
                    "Redo rejected: expected /redo without arguments",
                    "Book rejected: Please use:", "Book rejected: duplicate name=First",
                    "Book rejected: name=Clash, clashes with=First, venue=LT1",
                    "Booking added: First, stored count=1",
                    "Undo history recorded: command=book, undo count=1, redo history cleared",
                    "Undo completed: command=" + first + ", stored count=0, undo count=0, redo count=1",
                    "Redo requested: stored count=0",
                    "Redo completed: command=" + first + ", stored count=1, undo count=1, redo count=0",
                    "Cancel rejected: no booking named Missing",
                    "Cancelled booking: First, bookings left=0", "Clash found: Clash overlaps First")) {
                assertTrue(messages.stream().anyMatch(message -> message.startsWith(expected)), expected);
            }
        } finally {
            for (int i = 0; i < loggers.size(); i++) {
                loggers.get(i).removeHandler(handler);
                loggers.get(i).setLevel(previousLevels.get(i));
            }
            handler.close();
        }
    }
}
