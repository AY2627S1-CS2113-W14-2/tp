import static ui.HelpText.LINE_BREAK;

import java.io.IOException;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

import tasks.BookingList;
import storage.Storage;
import ui.HelpText;
import ui.Ui;

/**
 * Runs the interactive EduReserve booking application.
 */
public class EduReserve {
    private static final DateTimeFormatter SAVE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

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

        try (Scanner in = new Scanner(System.in)) {
            Storage storage = new Storage();
            String userName = getUserName(in, storage);

            if (userName == null) {
                return;
            }

            System.out.println("Hello, " + userName + "! Welcome to EduReserve.");

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
                    FileTime saveTime = storage.saveBookings(bookingList);
                    printSaveTime(saveTime);
                } catch (IOException e) {
                    System.out.println("Unable to save your bookings.");
                }

                if (shouldExit) {
                    break;
                }
            }
        }
    }

    /**
     * Loads the username or asks for it on the first run.
     */
    private static String getUserName(Scanner in, Storage storage) {
        String userName;

        try {
            userName = storage.loadUserName();
        } catch (IOException e) {
            System.out.println("Unable to load your username.");
            return null;
        }

        if (userName != null) {
            return userName;
        }

        System.out.println("What is your name?");

        if (!in.hasNextLine()) {
            return null;
        }

        userName = in.nextLine().trim();

        if (userName.isBlank()) {
            System.out.println("Name cannot be blank.");
            return null;
        }

        try {
            storage.saveUserName(userName);
        } catch (IOException e) {
            System.out.println("Unable to save your username.");
            return null;
        }

        return userName;
    }

    /**
     * Shows when the bookings file was last saved.
     */
    private static void printSaveTime(FileTime saveTime) {
        LocalDateTime localTime = LocalDateTime.ofInstant(
                saveTime.toInstant(),
                ZoneId.systemDefault());

        String formattedTime = localTime.format(SAVE_TIME_FORMAT);

        System.out.println("Bookings saved at: " + formattedTime);
    }
}
