package command;

import java.util.ArrayList;
import java.util.Locale;

import tasks.Booking;
import tasks.BookingList;

/**
 * Searches the bookings for names containing a keyword without changing the list.
 * @author Pratham-tech
 */
public class FindCommand extends Command {
    /**
     * Prints every booking whose name contains the keyword, ignoring case, in their original order.
     * @author Pratham-tech
     * @param bookings Arraylist of Booking to search, never null
     * @param item keyword to look for in booking names, never null or blank
     */
    @Override
    public void execute(BookingList bookings, String item) {
        assert bookings != null : "Booking list cannot be null";
        assert item != null && !item.isBlank() : "Find keyword must already be validated";
        int listSize = bookings.size();

        ArrayList<Booking> matches = bookings.findByKeyword(item);
        assert matches.size() <= listSize : "Cannot find more bookings than exist";

        if (matches.isEmpty()) {
            System.out.println("No bookings found matching: " + item);
        } else {
            System.out.println("Here are the matching bookings:");
            for (int i = 0; i < matches.size(); i++) {
                Booking match = matches.get(i);
                assert match.getName().toLowerCase(Locale.ROOT).contains(item.toLowerCase(Locale.ROOT))
                        : "Every result must contain the keyword";
                System.out.println((i + 1) + ". " + match);
            }
        }
        assert bookings.size() == listSize : "Finding bookings should not change list size: before: "
                + listSize + ", after " + bookings.size();
    }
}
