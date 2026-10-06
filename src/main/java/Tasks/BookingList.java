package Tasks;

import java.util.ArrayList;

/** Owns booking list and provides operations for managing them. */
public class BookingList {
    private final ArrayList<Booking> bookings = new ArrayList<>();
    public void add(Booking booking) {
        bookings.add(booking);
    }
    public Booking get(int index) {
        return bookings.get(index);
    }
    public void remove(int index) {
        bookings.remove(index);
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
}
