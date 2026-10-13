package Tasks;

import java.util.ArrayList;

/** Owns booking list and provides operations for managing them. */
public class BookingList {
    private final ArrayList<Booking> bookings = new ArrayList<>();

    /** Adds a booking and checks that it was appended successfully. */
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
     */
    public Booking get(int index) {
        assert index >= 0 && index < bookings.size() : "Booking index must be within the list";
        return bookings.get(index);
    }
    /**
     * Removes the booking at the given index and checks that exactly one booking was removed.
     * The caller must pass a valid index, for example one returned by {@link #findByName(String)}.
     */
    public void remove(int index) {
        assert index >= 0 && index < bookings.size() : "Cannot remove a booking outside the list";
        int previousSize = bookings.size();
        bookings.remove(index);
        assert bookings.size() == previousSize - 1 : "Removing a booking must decrease the size by one";
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
}
