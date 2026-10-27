package ui;

import static ui.HelpText.LINE_BREAK;

import exceptions.InvalidCommandException;
import tasks.Booking;
import tasks.BookingList;
import command.BookCommand;
import command.EditCommand;
import command.ListCommand;
import command.SizeCommand;
import command.FindCommand;
import command.ViewCommand;
import command.CalendarCommand;
import command.UndoCommand;
import command.RedoCommand;

/**
 * Executes commands and displays booking confirmations or input errors.
 */
public class Ui {

    private final BookingList bookingList;
    private final Parser parser = new Parser();
    private final UndoCommand undoCommand = new UndoCommand();
    private final RedoCommand redoCommand = new RedoCommand(undoCommand);
    /** Retained between input lines while the user selects a booking and enters changes. */
    private final EditCommand editCommand = new EditCommand();

    public Ui(BookingList bookingList) {
        assert bookingList != null : "Ui requires a booking list";
        this.bookingList = bookingList;
    }

    /**
     * Processes one command and returns whether the user requested to exit.
     * @author Maxkedd
     */
    public boolean processCommand(String userInput) {
        assert userInput != null : "User input must not be null";
        try {
            System.out.println(LINE_BREAK);
            if (editCommand.isPending()) {
                if (userInput.strip().equalsIgnoreCase("bye")) {
                    editCommand.cancel();
                    return true;
                }
                editCommand.processInput(bookingList, userInput);
                System.out.println(LINE_BREAK);
                return false;
            }
            String command = parser.parseCommand(userInput);

            switch (command) {
            case "bye":
                return true;
            case "book":
                undoCommand.executeAndRecord(command, userInput, bookingList,
                        () -> new BookCommand(parser).execute(bookingList, userInput));
                System.out.println(LINE_BREAK);
                break;
            case "editbooking":
                editCommand.execute(bookingList, parser.parseEdit(userInput));
                System.out.println(LINE_BREAK);
                break;
            case "checksize":
                new SizeCommand().execute(bookingList, userInput);
                System.out.println(LINE_BREAK);
                break;
            case "list":
                ui.Parser.ListOptions listOptions = parser.parseViewList(userInput);
                System.out.println("Ok, here is the list of " + listOptions.item() +" in "
                        + listOptions.sortOrder().toString() + " order");
                new ListCommand().execute(bookingList, listOptions.item(), listOptions.sortOrder());
                break;
            case "cancel":
                undoCommand.executeAndRecord(command, userInput, bookingList, () -> cancelBooking(userInput));
                System.out.println(LINE_BREAK);
                break;
            case "/undo":
                undoCommand.execute(bookingList, userInput);
                System.out.println(LINE_BREAK);
                break;
            case "/redo":
                redoCommand.execute(bookingList, userInput);
                System.out.println(LINE_BREAK);
                break;
            case "viewbooking":
                ViewOptions viewOptions = parser.parseViewBookingOptions(userInput);
                System.out.println("Ok, here are the individual bookings relevant to "
                        + viewOptions.getName() + ":");
                new ViewCommand().execute(
                        bookingList, viewOptions.getName(), viewOptions.isSortByTime());
                System.out.println(LINE_BREAK);
                break;
            case "find":
                String keyword = parser.parseFind(userInput);
                new FindCommand().execute(bookingList, keyword);
                System.out.println(LINE_BREAK);
                break;
            case "calendar":
                parser.parseCalendar(userInput);
                new CalendarCommand().execute(bookingList, "today");
                System.out.println(LINE_BREAK);
                break;
            default:
                throw new InvalidCommandException("Unknown command: " + command);
            }
        } catch (InvalidCommandException e) {
            System.out.println(e.getMessage() + HelpText.COMMAND_LIST);
        }
        return false;
    }

    /**
     * Cancels the first booking matching the supplied name after validating the input.
     * @author Pratham-tech
     */
    private void cancelBooking(String userInput) {
        String name = parser.parseCancel(userInput);
        int index = bookingList.findByName(name);
        if (index == -1) {
            throw new InvalidCommandException("No booking found with the name: " + name);
        }
        Booking cancelled = bookingList.get(index);
        bookingList.remove(index);
        assert cancelled.getName().equalsIgnoreCase(name) : "Cancelled booking must match the name";
        System.out.println("Booking cancelled: " + cancelled);
    }
}
