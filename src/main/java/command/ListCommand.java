package command;

import exceptions.InvalidCommandException;
import tasks.Booking;
import tasks.BookingList;
import ui.Parser;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.logging.Logger;

/**
 * Displays and filters the current list of bookings without changing it
 * @author Kytersaurus
 */
public class ListCommand extends Command {
    public static ArrayList<Booking> lastShownList = new ArrayList<Booking>();

    /** Records diagnostic information about listing requests and results. */
    private static final Logger LOGGER = Logger.getLogger(ListCommand.class.getName());

    /** Parser used to interpret listing options. */
    private final Parser parser;

    /**
     * Creates a listing command with its input parser.
     * @author Kytersaurus
     * @param parser parser used to validate listing options
     */
    public ListCommand(Parser parser) {
        assert parser != null : "Parser cannot be null";
        this.parser = parser;
    }
    /**
     * Parses listing options and displays the selected bookings.
     * @author Kytersaurus
     * @param bookings stored bookings to display
     * @param input complete list command
     */
    @Override
    public void execute(BookingList bookings, String input) {
        assert bookings != null : "Booking list cannot be null";
        assert input != null : "List command input cannot be null";

        Parser.ListOptions options = parser.parseViewList(input);

        System.out.println("Ok, here is the list of "
                + options.item() + " in "
                + options.sortOrder() + " order");

        execute(bookings, options.item(), options.sortOrder());
    }
    /**
     * Displays current bookings in a list by their original order with the option to filter
     * @author Kytersaurus
     * @param bookings Arraylist of Booking read and printed line by line
     * @param item filter used to show only lectures/exams
     * @param sortBy order in which to sort and show the list of bookings
     */
    public void execute(BookingList bookings, String item, SortOrder sortBy) {
        assert bookings != null : "Booking list cannot be null";
        int listSize = bookings.size();

        assert "bookings".equals(item)
                || "lectures".equals(item)
                || "exams".equals(item)
                : "Unexpected item in list " + item;

        LOGGER.fine(() -> "Listing bookings: filter=" + item
                + ", sort=" + sortBy + ", stored count=" + listSize);

        populateLastShownList(bookings, item);

        assert bookings.size() == listSize : "Listing bookings should not change list size: before: "
                + listSize + ", after " + bookings.size();
        sortLastShownList(sortBy);
        printLastShownList();
        LOGGER.fine(() -> "Listing completed: displayed count=" + lastShownList.size());
    }

    /**
     * Rebuilds the display list with all bookings or bookings matching the requested reason.
     * References are copied in their original order without changing stored bookings.
     * @author Kytersaurus
     * @param bookings stored bookings to read
     * @param item validated filter: bookings, lectures or exams
     */
    private void populateLastShownList(BookingList bookings, String item) {
        String reason;
        switch (item) {
        case "lectures":
            reason = "lecture";
            break;
        case "exams":
            reason = "exam";
            break;
        case "bookings":
        default:
            reason = null;
            break;
        }

        lastShownList.clear();
        int listSize = bookings.size();
        for (int i = 0; i < listSize; i++) {
            Booking element = bookings.get(i);
            assert element != null : "No such booking exists at index " + i;
            if (reason == null || reason.equals(element.getReason())) {
                lastShownList.addLast(element);
            }
        }
    }

    /**
     * Sorts the last shown list according to the specified sortorder
     * @author Kytersaurus
     * @param sortBy sort the list by a user specified order
     */
    private void sortLastShownList(SortOrder sortBy) {
        int listSize = lastShownList.size();
        if (sortBy == SortOrder.TIME_INCREASING || sortBy == SortOrder.TIME_DECREASING) {
            if (lastShownList.stream().anyMatch(booking -> booking.getStartTime() == null)) {
                LOGGER.fine(() -> "Time sorting rejected: a selected booking lacks a valid start time"
                        + ", sort=" + sortBy + ", selected count=" + listSize);
                throw new InvalidCommandException("Cannot sort these bookings: their timing must use dd/MM/yyyy HH:mm");
            }
        }
        switch (sortBy) {
        case ALPHABETIC:
            lastShownList.sort(
                    Comparator.comparing(Booking::getName, String.CASE_INSENSITIVE_ORDER));
            break;
        case REVERSE_ALPHABETIC:
            lastShownList.sort(
                    Comparator.comparing(Booking::getName, String.CASE_INSENSITIVE_ORDER)
                            .reversed());
            break;
        case TIME_INCREASING:
            lastShownList.sort(
                    Comparator.comparing(Booking::getStartTime));
            break;
        case TIME_DECREASING:
            lastShownList.sort(
                    Comparator.comparing(Booking::getStartTime).reversed());
            break;
        case ORIGINAL:
        default:
            break;
        }
        assert lastShownList.size() == listSize: "Sorting the list should not change the number of bookings: before: "
                + listSize + " after: " + lastShownList.size();
    }

    /**
     * Prints out the contents of lastShownList line by line, with index, reson and Title of the booking
     * @author Kytersaurus
     */
    private void printLastShownList() {
        for (int i = 0; i < lastShownList.size(); i++) {
            Booking element = lastShownList.get(i);
            assert element != null : "Booking to be printed cannot be null";
            System.out.println((i + 1) + ". " + element.getReason() + " Title: " + element.getName());
        }
    }

}
