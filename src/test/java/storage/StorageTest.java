package storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import Tasks.Booking;
import Tasks.BookingList;

/**
 * Checks saving and loading bookings.
 */
class StorageTest {
    @TempDir
    Path tempDirectory;

    @Test
    void loadBookings_newUser_createsEmptyFile()
            throws IOException {
        Storage storage = new Storage(
                tempDirectory,
                "NewUser");

        BookingList loadedBookings = storage.loadBookings();

        assertTrue(loadedBookings.isEmpty());

        try (Stream<Path> files = Files.list(tempDirectory)) {
            assertEquals(1, files.count());
        }
    }

    @Test
    void saveBookings_existingBookings_loadsSameBookings()
            throws IOException {
        Storage storage = new Storage(
                tempDirectory,
                "James");

        BookingList bookings = new BookingList();

        Booking exam = new Booking(
                "exam",
                "CS2113 Final Exam",
                "COM1-02-06",
                "10AM-12PM");

        Booking lecture = new Booking(
                "lecture",
                "CS2113 Lecture",
                "LT19",
                "2PM-4PM");

        bookings.add(exam);
        bookings.add(lecture);

        storage.saveBookings(bookings);

        BookingList loadedBookings = storage.loadBookings();

        assertEquals(2, loadedBookings.size());

        assertEquals(
                "exam",
                loadedBookings.get(0).getReason());
        assertEquals(
                "CS2113 Final Exam",
                loadedBookings.get(0).getName());
        assertEquals(
                "COM1-02-06",
                loadedBookings.get(0).getVenue());
        assertEquals(
                "10AM-12PM",
                loadedBookings.get(0).getTiming());

        assertEquals(
                "lecture",
                loadedBookings.get(1).getReason());
        assertEquals(
                "CS2113 Lecture",
                loadedBookings.get(1).getName());
        assertEquals(
                "LT19",
                loadedBookings.get(1).getVenue());
        assertEquals(
                "2PM-4PM",
                loadedBookings.get(1).getTiming());
    }

    @Test
    void loadBookings_differentLetterCase_keepsUsersSeparate()
            throws IOException {
        Storage upperCaseStorage = new Storage(
                tempDirectory,
                "James");

        Storage lowerCaseStorage = new Storage(
                tempDirectory,
                "james");

        BookingList upperCaseBookings = new BookingList();

        Booking exam = new Booking(
                "exam",
                "James Exam",
                "LT1",
                "10AM");

        upperCaseBookings.add(exam);
        upperCaseStorage.saveBookings(upperCaseBookings);

        BookingList lowerCaseBookings =
                lowerCaseStorage.loadBookings();

        assertTrue(lowerCaseBookings.isEmpty());

        Booking lecture = new Booking(
                "lecture",
                "james Lecture",
                "LT2",
                "2PM");

        lowerCaseBookings.add(lecture);
        lowerCaseStorage.saveBookings(lowerCaseBookings);

        BookingList loadedUpperCaseBookings =
                upperCaseStorage.loadBookings();

        BookingList loadedLowerCaseBookings =
                lowerCaseStorage.loadBookings();

        assertEquals(1, loadedUpperCaseBookings.size());
        assertEquals(1, loadedLowerCaseBookings.size());

        assertEquals(
                "James Exam",
                loadedUpperCaseBookings.get(0).getName());

        assertEquals(
                "james Lecture",
                loadedLowerCaseBookings.get(0).getName());

        try (Stream<Path> files = Files.list(tempDirectory)) {
            assertEquals(2, files.count());
        }
    }
}
