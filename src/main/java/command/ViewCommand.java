package command;

import Exceptions.InvalidCommandException;
import Tasks.Booking;
import Tasks.BookingList;

import java.util.ArrayList;

/**
 * Displays all the relevant bookings details under the same name
 */
public class ViewCommand extends Command {
    /**
     * Displays all the details of separate bookings linked to the exact name
     * @param bookings Arraylist of Booking checked for a specific name
     * @param item filter used to show only the bookings with same name
     */
    @Override
    public void execute(BookingList bookings, String item) {
        assert bookings != null : "Booking list cannot be null";
        assert item != null && !item.isBlank() : "Find keyword must already be validated";
        int listSize = bookings.size();

        ArrayList<Booking> exactBookings = bookings.findAllByName(item);

        if (exactBookings.isEmpty()) {
            throw new InvalidCommandException("No booking found with the name: " + item);
        }

        int index = 1;
        for (Booking bookingDetails : exactBookings) {
            assert bookingDetails != null : "Booking must be valid";
            assert bookingDetails.getName().equalsIgnoreCase(item)
                    : "View booking must match the name";
            assert bookingDetails.getReason() != null : "Reason for booking cannot be empty";
            assert bookingDetails.getVenue() != null : "Venue in booking cannot be empty";
            assert bookingDetails.getTiming() != null : "Timing of venue cannot be empty";

            System.out.println("\nBooking " + index + ":");
            System.out.println("Booking purpose: " + bookingDetails.getReason());
            System.out.println("Booking venue: " + bookingDetails.getVenue());
            System.out.println("Booking timing: " + bookingDetails.getTiming());
            index++;
        }

        assert bookings.size() == listSize
                : "Viewing a booking must not change the list size";
    }
}
