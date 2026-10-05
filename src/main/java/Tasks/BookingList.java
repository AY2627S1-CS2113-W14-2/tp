package Tasks;

import java.util.ArrayList;

/** Owns booking list and provides operations for managing them. */
public class BookingList {
    /**
     * Booking in display order; internal indexes begin at zero.
     */
    private final ArrayList<Booking> bookings = new ArrayList<>();

    /**
     * Appends a task to the end of the list.
     */
    public void add(Booking booking) {
        bookings.add(booking);
    }

    /**
     * Returns the task at a zero-based index.
     */
    public Booking get(int index) {
        return bookings.get(index);
    }

    /**
     * Removes the task at a zero-based index, shifting later Booking forward.
     */
    public void remove(int index) {
        bookings.remove(index);
    }

    /**
     * Returns the number of Booking currently in the list.
     */
    public int size() {
        return bookings.size();
    }

    /**
     * Returns whether the list contains no Booking.
     */
    public boolean isEmpty() {
        return bookings.isEmpty();
    }

    /**
     * Returns the most recently appended task; the list must not be empty.
     */
    public Booking getLast() {
        return bookings.getLast();
    }
}
