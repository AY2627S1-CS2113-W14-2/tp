package command;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import tasks.Booking;
import tasks.BookingList;

/**
 * Shows the bookings that start on one day, earliest first, without changing the list.
 * @author Pratham-tech
 */
public class CalendarCommand extends Command {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/uuuu");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Shows today's bookings, using the computer's current date.
     * @author Pratham-tech
     * @param bookings bookings to read, never null
     * @param item must be "today", which is the only calendar view supported
     */
    @Override
    public void execute(BookingList bookings, String item) {
        assert "today".equals(item) : "Calendar only supports today, but got " + item;
        execute(bookings, LocalDate.now());
    }

    /**
     * Prints the bookings starting on the given date with their start time, earliest first.
     * Taking the date as a parameter lets tests check the output without depending on today's date.
     * @author Pratham-tech
     * @param bookings bookings to read, never null
     * @param date day to show, never null
     */
    public void execute(BookingList bookings, LocalDate date) {
        assert bookings != null : "Booking list cannot be null";
        assert date != null : "Calendar date cannot be null";
        int listSize = bookings.size();

        ArrayList<Booking> dayBookings = bookings.findByDate(date);
        String dateText = date.format(DATE_FORMAT);

        if (dayBookings.isEmpty()) {
            System.out.println("No bookings on " + dateText + ".");
        } else {
            System.out.println("Calendar for " + dateText + ":");
            for (int i = 0; i < dayBookings.size(); i++) {
                Booking booking = dayBookings.get(i);
                assert booking.getStartTime().toLocalDate().equals(date) : "Every result must start on the date";
                System.out.println((i + 1) + ". " + booking.getStartTime().format(TIME_FORMAT) + " - "
                        + booking.getReason() + " - " + booking.getName() + " (venue: " + booking.getVenue() + ")");
            }
        }
        assert bookings.size() == listSize : "Showing the calendar should not change list size: before: "
                + listSize + ", after " + bookings.size();
    }
}
