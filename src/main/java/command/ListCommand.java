package command;

import exceptions.InvalidCommandException;
import tasks.Booking;
import tasks.BookingList;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Displays and filters the current list of bookings without changing it
 */
public class ListCommand extends Command {
    public static ArrayList<Booking> lastShownList = new ArrayList<Booking>();

    /**
     * Override method which overloads the abstract function with an additional SortOrder parameter
     * @param bookings Arraylist of Booking changed/read by command
     * @param item Additional variable used by command to select elements from bookings
     */
    @Override
    public void execute (BookingList bookings, String item) {
        execute(bookings, item, SortOrder.ORIGINAL);
    }
    /**
     * Displays current bookings in a list by their original order with the option to filter
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

        lastShownList.clear();

        switch (item) {
        case "bookings":
            for (int i = 0; i < listSize; i++) {
                Booking element = bookings.get(i);
                assert element != null : "No such booking exists at index " + i;
                lastShownList.addLast(element);
            }
            break;
        case "lectures":
            for (int i = 0; i < listSize; i++) {
                Booking element = bookings.get(i);
                assert element != null : "No such booking exists at index " + i;
                if (element.getReason().equals("lecture")) {
                    lastShownList.addLast(element);
                }
            }
            break;
        case "exams":
            for (int i = 0; i < listSize; i++) {
                Booking element = bookings.get(i);
                assert element != null : "No such booking exists at index " + i;
                if (element.getReason().equals("exam")) {
                    lastShownList.addLast(element);
                }
            }
            break;
        default:
            break;
        }

        assert bookings.size() == listSize : "Listing bookings should not change list size: before: "
                + listSize + ", after " + bookings.size();
        sortLastShownList(sortBy);
        printLastShownList();
    }

    /**
     * Sorts the last shown list according to the specified sortorder
     * @param sortBy sort the list by a user specified order
     */
    private void sortLastShownList(SortOrder sortBy) {
        int listSize = lastShownList.size();
        if (sortBy == SortOrder.TIME_INCREASING || sortBy == SortOrder.TIME_DECREASING) {
            if (lastShownList.stream().anyMatch(booking -> booking.getStartTime() == null)) {
                throw new InvalidCommandException("Cannot sort these bookings: their timing must use dd/MM/yyyy HH:mm");
            }
        }
        switch (sortBy) {
        case ORIGINAL:
            break;
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
        default:
            throw new IllegalArgumentException("Unexpected sort order: " + sortBy.toString());
        }
        assert lastShownList.size() == listSize: "Sorting the list should not change the number of bookings: before: "
                + listSize + " after: " + lastShownList.size();
    }

    /**
     * Prints out the contents of lastShownList line by line, with index, reson and Title of the booking
     */
    private void printLastShownList() {
        for (int i = 0; i < lastShownList.size(); i++) {
            Booking element = lastShownList.get(i);
            assert element != null : "Booking to be printed cannot be null";
            System.out.println((i + 1) + ". " + element.getReason() + " Title: " + element.getName());
        }
    }

}
