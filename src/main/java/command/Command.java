package command;

import Tasks.BookingList;

public abstract class Command {
    /**
     * Executes current commands using BookingList and additional variable
     * @param bookings Arraylist of Booking changed/read by command
     * @param item Additional variable used by command to select elements from bookings
     */
    public abstract void execute(BookingList bookings, String item);
}
