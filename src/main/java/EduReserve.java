import static ui.HelpText.LINE_BREAK;

import java.util.Scanner;

import Tasks.BookingList;
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

        BookingList bookingList = new BookingList();
        Ui command = new Ui(bookingList);

        try (Scanner in = new Scanner(System.in)) {
            if (!in.hasNextLine()) {
                return;
            }

            String userName = in.nextLine().trim();
            System.out.println("Hello, " + userName + "! Welcome to EduReserve.");
            System.out.println(LINE_BREAK);
            System.out.println(HelpText.COMMAND_LIST);
            System.out.println(LINE_BREAK);

            // Process commands
            while (in.hasNextLine()) {
                String userInput = in.nextLine();

                if (userInput.isBlank()) {
                    continue;
                }

                if (command.processCommand(userInput)) {
                    break;
                }
            }
        }
    }
}
