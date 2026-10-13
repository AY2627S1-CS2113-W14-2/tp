package ui;

import static ui.HelpText.LINE_BREAK;

import Exceptions.InvalidCommandException;
import Tasks.Booking;
import Tasks.BookingList;
import command.ListCommand;

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
                Booking booking = parser.parseBooking(userInput);
                bookingList.add(booking);
                System.out.println("Booking added: " + booking);
                System.out.println(LINE_BREAK);
                break;
            case "list":
                String item = parser.parseViewList(userInput);
                System.out.println("Ok, here is the list of " + item +":");
                new ListCommand().execute(bookingList, item);
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
                String bookingName = parser.parseViewBooking(userInput);
                int bookingIndex = bookingList.findByName(bookingName);
                if (bookingIndex == -1) {
                    throw new InvalidCommandException("No booking found with the name: " + bookingName);
                }

                assert bookingIndex >= 0 && bookingIndex < bookingList.size()
                        : "Booking index must be within the list size";
                Booking targetBooking = bookingList.get(bookingIndex);

                assert targetBooking != null : "Booking must be valid";
                assert targetBooking.getName().equalsIgnoreCase(bookingName)
                        : "View booking must match the name";
                assert targetBooking.getReason() != null : "Reason for booking cannot be empty";
                assert targetBooking.getVenue() != null : "Venue in booking cannot be empty";
                assert targetBooking.getTiming() != null : "Timing of venue cannot be empty";

                System.out.println("Here are the booking details for " + bookingName + ":");
                System.out.println("Booking purpose: " + targetBooking.getReason());
                System.out.println("Booking venue: " + targetBooking.getVenue());
                System.out.println("Booking timing: " + targetBooking.getTiming());
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
