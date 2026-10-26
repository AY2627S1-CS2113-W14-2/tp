package Tasks;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Locale;

/**
 * Owns booking list and provides operations for managing them.
 * @author Maxkedd
 */
public class BookingList {
    private final ArrayList<Booking> bookings = new ArrayList<>();

    /**
     * Adds a booking and checks that it was appended successfully.
     * @author Maxkedd
     */
    public void add(Booking booking) {
        assert booking != null : "Cannot add a null booking";
        int previousSize = bookings.size();
        bookings.add(booking);
        assert bookings.size() == previousSize + 1 : "Adding a booking must increase the size by one";
        assert bookings.getLast() == booking : "The added booking must be the last booking";
    }
    /**
     * Returns the booking at the given index.
     * The caller must pass a valid index, for example one returned by {@link #findByName(String)}.
     * @author Pratham-tech
     */
    public Booking get(int index) {
        assert index >= 0 && index < bookings.size() : "Booking index must be within the list";
        return bookings.get(index);
    }
    /**
     * Removes the booking at the given index and checks that exactly one booking was removed.
     * The caller must pass a valid index, for example one returned by {@link #findByName(String)}.
     * @author Pratham-tech
     */
    public void remove(int index) {
        assert index >= 0 && index < bookings.size() : "Cannot remove a booking outside the list";
        int previousSize = bookings.size();
        bookings.remove(index);
        assert bookings.size() == previousSize - 1 : "Removing a booking must decrease the size by one";
    }

    /** Replaces the selected booking in place, preserving the number and order of bookings. */
    public void replace(Booking original, Booking replacement) {
        int index = bookings.indexOf(original);
        assert index >= 0 : "The original booking must still be in the list";
        assert replacement != null : "Cannot replace a booking with null";
        bookings.set(index, replacement);
    }
    public int size() {
        return bookings.size();
    }
    public boolean isEmpty() {
        return bookings.isEmpty();
    }
    public Booking getLast() {
        return bookings.getLast();
    }
    /**
     * Returns the index of the first booking with the given name (ignoring case),
     * or -1 if there is no such booking.
     * The name must already be validated by the parser, so it is never null or blank.
     * @author Pratham-tech
     */
    public int findByName(String name) {
        assert name != null && !name.isBlank() : "Booking name to search for must already be validated";
        for (int i = 0; i < bookings.size(); i++) {
            if (bookings.get(i).getName().equalsIgnoreCase(name)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns a list of bookings with the exact given name,
     * or an empty list if there is no such booking.
     * The name must already be validated by the parser, so it is never null or blank.
     * @param name to look for in order to get matches
     * @author produdepunk
     */
    public ArrayList<Booking> findAllByName(String name) {
        assert name != null && !name.isBlank()
                : "Booking name must already be validated";

        ArrayList<Booking> exactMatches = new ArrayList<>();

        for (Booking booking : bookings) {
            if (booking.getName().equalsIgnoreCase(name)) {
                exactMatches.add(booking);
            }
        }

        return exactMatches;
    }

    /**
     * Returns the bookings whose name contains the keyword, ignoring case, in their original order.
     * The keyword must already be validated by the parser, so it is never null or blank.
     * The returned list is a copy, so changing it does not change the bookings.
     * @author Pratham-tech
     * @param keyword text to look for inside booking names
     * @return matching bookings, empty if none match
     */
    public ArrayList<Booking> findByKeyword(String keyword) {
        assert keyword != null && !keyword.isBlank() : "Search keyword must already be validated";
        String lowerCaseKeyword = keyword.toLowerCase(Locale.ROOT);
        ArrayList<Booking> matches = new ArrayList<>();
        for (Booking booking : bookings) {
            if (booking.getName().toLowerCase(Locale.ROOT).contains(lowerCaseKeyword)) {
                matches.add(booking);
            }
        }
        assert matches.size() <= bookings.size() : "Cannot find more bookings than exist";
        return matches;
    }

    /**
     * Returns an existing booking that clashes with the new booking, or null if the venue is free.
     * A clash means the same venue (ignoring case) starting at exactly the same date and time.
     * Older saved bookings without a full date are skipped because their time cannot be compared.
     * @author Pratham-tech
     * @param newBooking booking about to be added, never null and with a valid start time
     * @return the booking already using that venue and time, or null if there is none
     */
    public Booking findClash(Booking newBooking) {
        assert newBooking != null : "Booking to check for a clash must not be null";
        assert newBooking.getStartTime() != null : "Booking to check for a clash must have a start time";
        for (Booking existing : bookings) {
            boolean sameVenue = existing.getVenue().equalsIgnoreCase(newBooking.getVenue());
            boolean sameTime = newBooking.getStartTime().equals(existing.getStartTime());
            if (sameVenue && sameTime) {
                return existing;
            }
        }
        return null;
    }

    /**
     * Returns the bookings that start on the given date, ordered by start time.
     * Older saved bookings without a full date are skipped because they have no start time.
     * The returned list is a copy, so changing it does not change the bookings.
     * @author Pratham-tech
     * @param date day to look for, never null
     * @return bookings starting on that day, earliest first, empty if there are none
     */
    public ArrayList<Booking> findByDate(LocalDate date) {
        assert date != null : "Date to search for must not be null";
        ArrayList<Booking> matches = new ArrayList<>();
        for (Booking booking : bookings) {
            if (booking.getStartTime() != null && booking.getStartTime().toLocalDate().equals(date)) {
                matches.add(booking);
            }
        }
        matches.sort(Comparator.comparing(Booking::getStartTime));
        assert matches.size() <= bookings.size() : "Cannot find more bookings than exist";
        return matches;
    }
}
