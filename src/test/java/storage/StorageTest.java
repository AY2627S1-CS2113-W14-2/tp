package storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import tasks.Booking;
import tasks.BookingList;

/**
 * Checks saving and loading bookings.
 */
class StorageTest {
    @TempDir
    Path tempDirectory;

    @Test
    void constructor_nullDirectory_rejectsInvalidConfiguration() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> new Storage(null));
        assertEquals("Storage directory must not be null", error.getMessage());
    }

    @Test
    void loadUserName_missingFile_returnsNull() throws IOException {
        Storage storage = new Storage(tempDirectory);

        assertNull(storage.loadUserName());
        try (Stream<Path> files = Files.list(tempDirectory)) {
            assertEquals(0, files.count());
        }
    }

    @Test
    void saveAndLoadUserName_validName_returnsSameName() throws IOException {
        Storage storage = new Storage(tempDirectory);

        storage.saveUserName("  James  ");

        assertEquals("James", storage.loadUserName());
        assertEquals("James", Files.readString(tempDirectory.resolve("username.txt")));
    }

    @Test
    void saveUserName_blankName_rejectsInvalidName() {
        Storage storage = new Storage(tempDirectory);

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> storage.saveUserName("  "));

        assertEquals("Username must not be blank", error.getMessage());
    }

    @Test
    void saveBookings_nullList_preservesExistingFile() throws IOException {
        Path dataFile = tempDirectory.resolve("bookings.txt");
        String original = "B | exam | Midterm | LT1 | 05/10/2026 14:00\n";
        Files.writeString(dataFile, original);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> new Storage(tempDirectory).saveBookings(null));

        assertEquals("Booking list must not be null", error.getMessage());
        assertEquals(original, Files.readString(dataFile));
    }

    @Test
    void loadBookings_blankLines_areSkipped() throws IOException {
        Files.writeString(tempDirectory.resolve("bookings.txt"),
                "\n  \t\nB | exam | Midterm | LT1 | 05/10/2026 14:00\n\n");

        BookingList bookings = new Storage(tempDirectory).loadBookings();

        assertEquals(1, bookings.size());
        assertEquals("Midterm", bookings.get(0).getName());
    }

    @Test
    void loadBookings_invalidFields_reportPhysicalLineNumberAndPreserveFile() throws IOException {
        String[] invalidLines = {
            "B | exam | Midterm | LT1",
            "B | exam | Midterm | LT1 | 14:00 | extra",
            "X | exam | Midterm | LT1 | 14:00",
            "B | meeting | Midterm | LT1 | 14:00",
            "B | exam |   | LT1 | 14:00",
            "B | exam | Midterm |   | 14:00",
            "B | exam | Midterm | LT1 | ",
            "B | exam | Midterm | LT1 | 14:00 | 0",
            "B | exam | Midterm | LT1 | 14:00 | twenty"
        };
        Path dataFile = tempDirectory.resolve("bookings.txt");
        Storage storage = new Storage(tempDirectory);
        for (String line : invalidLines) {
            String contents = "B | lecture | Valid | LT2 | 10AM\n\n" + line + "\n";
            Files.writeString(dataFile, contents);

            IOException error = assertThrows(IOException.class, storage::loadBookings, line);

            assertEquals("Invalid booking data at line 3", error.getMessage(), line);
            assertEquals(contents, Files.readString(dataFile), line);
        }
    }

    @Test
    void saveBookings_emptyList_clearsPreviouslySavedBookings() throws IOException {
        Storage storage = new Storage(tempDirectory);
        Files.writeString(tempDirectory.resolve("bookings.txt"), "B | exam | Old | LT1 | 10AM\n");

        storage.saveBookings(new BookingList());

        assertEquals("", Files.readString(tempDirectory.resolve("bookings.txt")));
        assertTrue(storage.loadBookings().isEmpty());
    }

    @Test
    void saveBookings_validList_returnsFileModifiedTime() throws IOException {
        Storage storage = new Storage(tempDirectory);

        FileTime saveTime = storage.saveBookings(new BookingList());

        FileTime fileTime = Files.getLastModifiedTime(
                tempDirectory.resolve("bookings.txt"));
        assertEquals(fileTime, saveTime);
    }

    @Test
    void loadBookings_nestedDirectory_createsParents() throws IOException {
        Path nested = tempDirectory.resolve("nested/data");

        assertTrue(new Storage(nested).loadBookings().isEmpty());
        assertTrue(Files.isRegularFile(nested.resolve("bookings.txt")));
    }

    @Test
    void loadAndSave_directoryOccupiedByFile_reportIoFailure() throws IOException {
        Path occupied = tempDirectory.resolve("occupied");
        Files.writeString(occupied, "keep this file");
        Storage storage = new Storage(occupied);

        assertThrows(IOException.class, storage::loadBookings);
        assertThrows(IOException.class, () -> storage.saveBookings(new BookingList()));
        assertEquals("keep this file", Files.readString(occupied));
    }

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
                "10AM-12PM",
                30);

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
                30,
                loadedBookings.get(0).getAttendeeCount());

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
        assertNull(
                loadedBookings.get(1).getAttendeeCount());
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
