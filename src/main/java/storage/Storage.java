package storage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import tasks.Booking;
import tasks.BookingList;

/**
 * Saves bookings for the single user of the application.
 */
public class Storage {
    private static final String DELIMITER = " | ";
    private static final String FILE_NAME = "bookings.txt";

    private final Path filePath;

    /**
     * Uses the default data folder.
     */
    public Storage() {
        this(Path.of("data"));
    }

    /**
     * Uses the given data folder.
     *
     * @param dataDirectory the data folder
     */
    public Storage(Path dataDirectory) {
        if (dataDirectory == null) {
            throw new IllegalArgumentException(
                    "Storage directory must not be null");
        }

        filePath = dataDirectory.resolve(FILE_NAME);
    }

    /**
     * Loads the saved bookings.
     *
     * @return the saved bookings
     * @throws IOException if the file cannot be read
     */
    public BookingList loadBookings() throws IOException {
        BookingList bookings = new BookingList();

        if (!Files.exists(filePath)) {
            createEmptyDataFile();
            return bookings;
        }

        List<String> lines = Files.readAllLines(
                filePath,
                StandardCharsets.UTF_8);

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);

            if (line.isBlank()) {
                continue;
            }

            int lineNumber = i + 1;

            Booking booking = parseBooking(
                    line,
                    lineNumber);

            bookings.add(booking);
        }

        return bookings;
    }

    /**
     * Saves the bookings.
     *
     * @param bookings the bookings to save
     * @throws IOException if the file cannot be written
     */
    public void saveBookings(BookingList bookings)
            throws IOException {
        if (bookings == null) {
            throw new IllegalArgumentException(
                    "Booking list must not be null");
        }

        Path dataFolder = filePath.getParent();

        Files.createDirectories(dataFolder);

        List<String> lines = new ArrayList<>();

        for (int i = 0; i < bookings.size(); i++) {
            Booking booking = bookings.get(i);

            String line = bookingToLine(booking);

            lines.add(line);
        }

        Files.write(
                filePath,
                lines,
                StandardCharsets.UTF_8);
    }

    /**
     * Creates the data file when the application is run for the first time.
     *
     * @throws IOException if the file cannot be created
     */
    private void createEmptyDataFile() throws IOException {
        Path dataFolder = filePath.getParent();

        Files.createDirectories(dataFolder);
        Files.createFile(filePath);
    }

    /**
     * Changes a booking into one line of text.
     *
     * @param booking the booking to change
     * @return one line for the data file
     */
    private String bookingToLine(Booking booking) {
        String line = "B";

        line = line + DELIMITER;
        line = line + booking.getReason();

        line = line + DELIMITER;
        line = line + booking.getName();

        line = line + DELIMITER;
        line = line + booking.getVenue();

        line = line + DELIMITER;
        line = line + booking.getTiming();

        Integer attendeeCount = booking.getAttendeeCount();

        line = line + DELIMITER;

        if (attendeeCount != null) {
            line = line + attendeeCount;
        }

        return line;
    }

    /**
     * Changes one saved line into a booking.
     *
     * @param line one line from the data file
     * @param lineNumber its line number
     * @return the loaded booking
     * @throws IOException if the line is invalid
     */
    private Booking parseBooking(
            String line,
            int lineNumber) throws IOException {
        String[] parts = line.split(
                " \\| ",
                -1);

        if (parts.length != 5 && parts.length != 6) {
            throwInvalidDataError(lineNumber);
        }

        String type = parts[0];
        String reason = parts[1];
        String name = parts[2];
        String venue = parts[3];
        String timing = parts[4];

        Integer attendeeCount = null;

        if (parts.length == 6) {
            String attendeeText = parts[5];

            if (!attendeeText.isBlank()) {
                try {
                    attendeeCount = Integer.parseInt(attendeeText);
                } catch (NumberFormatException e) {
                    throwInvalidDataError(lineNumber);
                }

                if (attendeeCount <= 0) {
                    throwInvalidDataError(lineNumber);
                }
            }
        }

        if (!type.equals("B")) {
            throwInvalidDataError(lineNumber);
        }

        if (!isValidReason(reason)) {
            throwInvalidDataError(lineNumber);
        }

        if (name.isBlank()) {
            throwInvalidDataError(lineNumber);
        }

        if (venue.isBlank()) {
            throwInvalidDataError(lineNumber);
        }

        if (timing.isBlank()) {
            throwInvalidDataError(lineNumber);
        }

        Booking booking = new Booking(
                reason,
                name,
                venue,
                timing,
                attendeeCount);

        return booking;
    }

    /**
     * Checks the booking reason.
     *
     * @param reason the reason to check
     * @return true for an exam or lecture
     */
    private boolean isValidReason(String reason) {
        if (reason.equals("exam")) {
            return true;
        }

        if (reason.equals("lecture")) {
            return true;
        }

        return false;
    }

    /**
     * Reports a bad line in the data file.
     *
     * @param lineNumber the bad line number
     * @throws IOException for invalid saved data
     */
    private void throwInvalidDataError(int lineNumber)
            throws IOException {
        String message = "Invalid booking data at line "
                + lineNumber;

        throw new IOException(message);
    }
}
