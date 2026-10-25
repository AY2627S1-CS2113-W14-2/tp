package ui;

import static ui.HelpText.LINE_BREAK;

import Exceptions.InvalidCommandException;
import Tasks.Booking;
import Tasks.BookingList;
import command.BookCommand;
import command.ListCommand;
import command.FindCommand;
import command.ViewCommand;
import command.CalendarCommand;

/**
 * Executes commands and displays booking confirmations or input errors.
 */
public class Ui {

    private final BookingList bookingList;
    private final Parser parser = new Parser();

    public Ui(BookingList bookingList) {
        assert bookingList != null : "Ui requires a booking list";
        this.bookingList = bookingList;
    }

    /**
     * Processes one command and returns whether the user requested to exit.
     */
    public boolean processCommand(String userInput) {
        assert userInput != null : "User input must not be null";
        try {
            System.out.println(LINE_BREAK);
            String command = parser.parseCommand(userInput);

            switch (command) {
            case "bye":
                return true;
            case "book":
                new BookCommand(parser).execute(bookingList, userInput);
                Booking booking = parser.parseBooking(userInput);
                Booking clash = bookingList.findClash(booking);
                if (clash != null) {
                    throw new InvalidCommandException("Booking clash: " + clash.getVenue() + " is already booked at "
                            + clash.getTiming() + " for " + clash.getName() + ".");
                }
                bookingList.add(booking);
                System.out.println("Booking added: " + booking);
                System.out.println(LINE_BREAK);
                break;
            case "list":
                ui.Parser.ListOptions listOptions = parser.parseViewList(userInput);
                System.out.println("Ok, here is the list of " + listOptions.item() +" in "
                        + listOptions.sortOrder().toString() + " order");
                new ListCommand().execute(bookingList, listOptions.item(), listOptions.sortOrder());
                break;
            case "cancel":
                String name = parser.parseCancel(userInput);
                int index = bookingList.findByName(name);
                if (index == -1) {
                    throw new InvalidCommandException("No booking found with the name: " + name);
                }
                Booking cancelled = bookingList.get(index);
                bookingList.remove(index);
                assert cancelled.getName().equalsIgnoreCase(name) : "Cancelled booking must match the name";
                System.out.println("Booking cancelled: " + cancelled);
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
}
