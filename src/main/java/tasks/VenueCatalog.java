package tasks;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores the list of available venues for booking, including the functions to check for venue availability
 * @author Kytersaurus
 */
public final class VenueCatalog {
    private static final List<String> VENUES = List.of(
            // COM1 seminar rooms
            "COM1-0201",
            "COM1-0203",
            "COM1-0204",
            "COM1-0206",
            "COM1-0207",
            "COM1-0208",
            "COM1-0209",
            "COM1-0210",
            "COM1-0212",

            // COM1 programming labs
            "COM1-B108",
            "COM1-B109",
            "COM1-B110",
            "COM1-B111",
            "COM1-B112",

            // COM3 seminar rooms
            "COM3-01-20",
            "COM3-01-21",
            "COM3-01-22",
            "COM3-01-23",
            "COM3-01-25",
            "COM3-02-60",

            // COM4 workstation labs
            "COM4-02-03",
            "COM4-02-04",
            "COM4-02-05",

            // COM4 seminar rooms
            "COM4SR31",
            "COM4SR32",
            "COM4SR33",

            //MPSH exam venues
            "MPSH1",
            "MPSH2",
            "MPSH3",
            "MPSH4",
            "MPSH5",
            "MPSH6",

            // Arts-area lecture theatres
            "LT8",
            "LT9",
            "LT10",
            "LT11",
            "LT12",
            "LT13",
            "LT14",
            "LT15",

            //Computing-area lecture theatres
            "LT17",
            "LT19",
            "I3-LT38",
            "COM3-MPH",
            "SR_LT19",

            //U town lecture theaters
            "UT-AUD1",
            "UT-AUD2",
            "UT-AUD3",
            "UTSRC-GLR",
            "UTSRC-LT50",
            "UTSRC-LT51",
            "UTSRC-LT52",
            "UTSRC-LT53",
            "UTSRC-SR1",
            "UTSRC-SR2",
            "UTSRC-SR3",
            "UTSRC-SR4",
            "UTSRC-SR5",
            "UTSRC-SR6",
            "UTSRC-SR7",
            "UTSRC-SR8",
            "UTSRC-SR9"
            );

    /**
     * Creates a list of all available venues at the specified time
     * @author Kytersaurus
     * @param bookings list of existing bookings
     * @param startTime time specified
     * @return list of bookings that are available at the specified time
     */
    public static List<String> findAvailableVenues(BookingList bookings, LocalDateTime startTime) {
        assert bookings != null : "Booking list cannot be null";
        assert startTime != null : "Requested time cannot be null";

        List<String> available = new ArrayList<>();

        for (String venue : VENUES) {
            if (bookings.isVenueAvailable(venue, startTime)) {
                available.add(venue);
            }
        }

        return available;
    }

    /**
     * Finds available one-hour slots starting on each whole hour of a date.
     * The final slot ends at midnight on the following day.
     * @author Kytersaurus
     * @param bookings existing bookings
     * @param venue preset venue to check
     * @param date date to search
     * @return available start times in chronological order
     */
    public static List<LocalDateTime> findAvailableSlots(
            BookingList bookings, String venue, LocalDate date) {
        assert bookings != null : "Booking list cannot be null";
        assert venue != null && !venue.isBlank() : "Venue cannot be blank";
        assert date != null : "Requested date cannot be null";
        assert findVenueInCatalog(venue) : "Venue must exist in the catalog";

        List<LocalDateTime> available = new ArrayList<>();
        for (int hour = 0; hour < 24; hour++) {
            LocalDateTime start = date.atTime(hour, 0);
            if (bookings.isVenueAvailable(venue, start)) {
                available.add(start);
            }
        }
        return available;
    }

    /**
     * Checks if a venue requested by the user is in the list of available venues
     * @author Kytersaurus
     * @param venue String name of the venue requested
     * @return true if the venue exists in the catalog, false otherwise
     */
    public static boolean findVenueInCatalog(String venue) {
        assert venue != null : "Venue cannot be null";
        return VENUES.stream()
                    .anyMatch(preset -> preset.equalsIgnoreCase(venue.strip()));
    }
}
