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
    void loadBookings_missingDataFile_createsEmptyFile()
            throws IOException {
        Storage storage = new Storage(tempDirectory);

        BookingList loadedBookings = storage.loadBookings();

        assertTrue(loadedBookings.isEmpty());
        assertTrue(Files.exists(tempDirectory.resolve("bookings.txt")));

        try (Stream<Path> files = Files.list(tempDirectory)) {
            assertEquals(1, files.count());
        }
    }

    @Test
    void saveBookings_existingBookings_loadsSameBookings()
            throws IOException {
        Storage storage = new Storage(tempDirectory);

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
    void loadBookings_separateStorageInstances_shareSameDataFile()
            throws IOException {
        Storage firstStorage = new Storage(tempDirectory);
        Storage secondStorage = new Storage(tempDirectory);

        BookingList bookings = new BookingList();

        Booking exam = new Booking(
                "exam",
                "James Exam",
                "LT1",
                "10AM");

        bookings.add(exam);
        firstStorage.saveBookings(bookings);

        BookingList loadedBookings = secondStorage.loadBookings();

        assertEquals(1, loadedBookings.size());
        assertEquals(
                "James Exam",
                loadedBookings.get(0).getName());

        Booking lecture = new Booking(
                "lecture",
                "james Lecture",
                "LT2",
                "2PM");

        loadedBookings.add(lecture);
        secondStorage.saveBookings(loadedBookings);

        BookingList reloadedBookings = firstStorage.loadBookings();

        assertEquals(2, reloadedBookings.size());

        assertEquals(
                "james Lecture",
                reloadedBookings.get(1).getName());

        try (Stream<Path> files = Files.list(tempDirectory)) {
            assertEquals(1, files.count());
        }
    }
}
