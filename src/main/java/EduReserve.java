import static ui.HelpText.LINE_BREAK;

import java.io.IOException;
import java.util.Scanner;

import Tasks.BookingList;
import storage.Storage;
import ui.HelpText;
import ui.Ui;

/**
 * Runs the interactive EduReserve booking application.
 */
public class EduReserve {
    /**
     * Reads the user's name and processes booking commands until exit or end of input.
     */
    public static void main(String[] args) {
        String banner = " _____       _               ____                                                  \n"
                + "| ____|   __| |   _   _     |  _ \\     ___     ___     ___    _ __   __   __   ___\n"
                + "|  _|    / _` |  | | | |    | |_) |   / _ \\   / __|   / _ \\  | '__|  \\ \\ / /  / _ \\\n"
                + "| |___  | (_| |  | |_| |    |  _ <   |  __/   \\__ \\  |  __/  | |      \\ V /  |  __/\n"
                + "|_____|  \\__,_|   \\__,_|    |_| \\_\\   \\___|   |___/   \\___|  |_|       \\_/    \\___|";

        System.out.println(banner);
        System.out.println("What is your name?");

        try (Scanner in = new Scanner(System.in)) {
            if (!in.hasNextLine()) {
                return;
            }

            String userName = in.nextLine().trim();

            if (userName.isBlank()) {
                System.out.println("Username cannot be blank.");
                return;
            }

            System.out.println("Hello, " + userName + "! Welcome to EduReserve.");

            Storage storage = new Storage(userName);
            BookingList bookingList;

            try {
                bookingList = storage.loadBookings();
            } catch (IOException e) {
                System.out.println("Unable to load your bookings.");
                return;
            }

            Ui command = new Ui(bookingList);

            System.out.println(LINE_BREAK);
            System.out.println(HelpText.COMMAND_LIST);
            System.out.println(LINE_BREAK);

            // Process commands
            while (in.hasNextLine()) {
                String userInput = in.nextLine();

                if (userInput.isBlank()) {
                    continue;
                }

                boolean shouldExit = command.processCommand(userInput);

                try {
                    storage.saveBookings(bookingList);
                } catch (IOException e) {
                    System.out.println("Unable to save your bookings.");
                }

                if (shouldExit) {
                    break;
                }
            }
        }
    }
}
