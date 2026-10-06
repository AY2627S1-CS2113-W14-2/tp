package command;

import Tasks.BookingList;

public abstract class Command {
    public abstract void execute(BookingList bookings, String item);
}
