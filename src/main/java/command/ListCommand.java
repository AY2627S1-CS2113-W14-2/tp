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
        int listSize = bookings.size();
        switch (item) {
        case "bookings":
            for (int i = 0; i < listSize; i++) {
                Booking element = bookings.get(i);
                printElement(i, element);
            }
            break;
        case "lectures":
            int lectureIndex = 0;
            for (int i = 0; i < listSize; i++) {
                Booking element = bookings.get(i);
                if (element.getReason().equals("lecture")) {
                    printElement(lectureIndex, element);
                    lectureIndex++;
                }
            }
            break;
        case "exams":
            int examIndex = 0;
            for (int i = 0; i < listSize; i++) {
                Booking element = bookings.get(i);
                if (element.getReason().equals("exam")) {
                    printElement(examIndex, element);
                    examIndex++;
                }
            }
            break;
        default:
            break;
        }
    }

    private void printElement(int index, Booking element) {
        System.out.println((index + 1) + ". " + element.getReason() + " Title: " + element.getName());
    }

}
