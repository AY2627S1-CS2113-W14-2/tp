package command;

import Tasks.Booking;
import Tasks.BookingList;

public class ListCommand extends Command {
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
