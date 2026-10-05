import static Ui.HelpText.LINE_BREAK;

import java.util.Scanner;

import Tasks.BookingList;
import Ui.HelpText;
import Ui.Ui;

/**
 * Runs the interactive EduReserve booking application.
 */
public class EduReserve {
    /**
     * Reads the user's name and processes booking commands until exit or end of input.
     */
    public static void main(String[] args) {
        String banner = " ____        _        \n"
                + "|  _ \\ _   _| | _____ \n"
                + "| | | | | | | |/ / _ \\\n"
                + "| |_| | |_| |   <  __/\n"
                + "|____/ \\__,_|_|\\_\\___|\n";
        System.out.println(banner);
        System.out.println("What is your name?");

        BookingList bookingList = new BookingList();
        Ui command = new Ui(bookingList);

        try (Scanner in = new Scanner(System.in)) {
            if (!in.hasNextLine()) {
                return;
            }
            System.out.println("Hello, welcome to eduReserve " + in.nextLine());
            System.out.println(LINE_BREAK);
            System.out.println(HelpText.COMMAND_LIST);
            System.out.println(LINE_BREAK);

            // Process Commands
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
