package command;

import Tasks.Booking;
import Tasks.BookingList;

/**
 * Displays and filters the current list of bookings without changing it
 */
public class ListCommand extends Command {
    /**
     * Displays current bookings in a list by their original order with the option to filter
     * @param bookings Arraylist of Booking read and printed line by line
     * @param item filter used to show only lectures/exams
     */
    @Override
    public void execute(BookingList bookings, String item) {
        assert bookings != null : "Booking list cannot be null";
        int listSize = bookings.size();

        assert "bookings".equals(item)
                || "lectures".equals(item)
                || "exams".equals(item)
                : "Unexpected item in list " + item;

        switch (item) {
        case "bookings":
            for (int i = 0; i < listSize; i++) {
                Booking element = bookings.get(i);
                assert element != null : "No such booking exists at index " + i;
                printElement(i, listSize, element);
            }
            break;
        case "lectures":
            int lectureIndex = 0;
            for (int i = 0; i < listSize; i++) {
                Booking element = bookings.get(i);
                assert element != null : "No such booking exists at index " + i;
                if (element.getReason().equals("lecture")) {
                    printElement(lectureIndex, listSize, element);
                    lectureIndex++;
                }
            }
            break;
        case "exams":
            int examIndex = 0;
            for (int i = 0; i < listSize; i++) {
                Booking element = bookings.get(i);
                assert element != null : "No such booking exists at index " + i;
                if (element.getReason().equals("exam")) {
                    printElement(examIndex, listSize, element);
                    examIndex++;
                }
            }
            break;
        default:
            break;
        }
        assert bookings.size() == listSize : "Listing bookings should not change list size: before: "
                + listSize + ", after " + bookings.size();
    }

    /**
     * Prints a single booking with its display number, reason and name.
     * @param index index zero-based display index, adjusted for filtered results
     * @param bookingsSize total number of bookings
     * @param element booking to display
     */
    private void printElement(int index, int bookingsSize, Booking element) {
        assert index >= 0 && index < bookingsSize : "Booking to be printed is not in the list";
        assert element != null : "Booking to be printed cannot be null";
        System.out.println((index + 1) + ". " + element.getReason() + " Title: " + element.getName());
    }

}
