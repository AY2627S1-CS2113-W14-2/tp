package command;

import java.util.ArrayList;

import exceptions.InvalidCommandException;
import tasks.Booking;
import tasks.BookingList;
import ui.Parser;

/**
 * Guides the user through selecting an exact-name match and updating its details.
 * Pending input is handled by the existing UI loop so no extra console reader is needed.
 * @author produdepunk
 */
public class EditCommand extends Command {
    private final Parser parser = new Parser();
    private ArrayList<Booking> matches = new ArrayList<>();
    /** Null means the user still needs to select a booking from the filtered matches. */
    private Booking selectedBooking;

    /** Finds exact-name matches and prompts for a selection or new field values. */
    @Override
    public void execute(BookingList bookings, String item) {
        assert bookings != null : "Booking list cannot be null";
        assert item != null && !item.isBlank() : "Edit name must already be validated";
        int listSize = bookings.size();
        ArrayList<Booking> exactBookings = bookings.findAllByName(item);
        assert exactBookings.size() <= listSize : "Cannot find more bookings than exist";
        assert bookings.size() == listSize : "Finding bookings must not change the list size";
        matches = exactBookings;
        selectedBooking = null;
        if (matches.isEmpty()) {
            throw new InvalidCommandException("No booking found with the name: " + item);
        }
        if (matches.size() == 1) {
            selectedBooking = matches.getFirst();
            printEditPrompt();
            return;
        }
        System.out.println("Multiple bookings found. Select a booking from this list:");
        for (int i = 0; i < matches.size(); i++) {
            System.out.println((i + 1) + ". " + matches.get(i));
        }
        System.out.println("Enter a number from 1 to " + matches.size() + ", or type abort to cancel editing.");
    }

    /** Returns whether the UI should treat the next line as editing input. */
    public boolean isPending() {
        return !matches.isEmpty();
    }

    /**
     * Handles a filtered-list selection or applies edited fields after validation and clash checks.
     * Invalid input leaves the edit pending and the original booking unchanged so the user can retry.
     */
    public void processInput(BookingList bookings, String input) {
        assert bookings != null : "Booking list cannot be null";
        assert input != null : "Edit input must not be null";
        assert isPending() : "An edit must be started before continuing it";
        if (input.strip().equalsIgnoreCase("abort")) {
            cancel();
            System.out.println("Booking edit cancelled.");
            return;
        }
        if (selectedBooking == null) {
            selectBooking(input);
            printEditPrompt();
            return;
        }
        Booking replacement = parser.parseEditedBooking(selectedBooking, input);
        Booking clash = findClashExceptSelected(bookings, replacement);
        if (clash != null) {
            throw new InvalidCommandException("Booking clash: " + clash.getVenue() + " is already booked at "
                    + clash.getTiming() + " for " + clash.getName() + ". Enter different changes or type abort.");
        }
        bookings.replace(selectedBooking, replacement);
        cancel();
        System.out.println("Booking updated: " + replacement);
    }

    /** Checks the edited booking against every other booking, excluding its current list entry. */
    private Booking findClashExceptSelected(BookingList bookings, Booking replacement) {
        for (int i = 0; i < bookings.size(); i++) {
            Booking existing = bookings.get(i);
            if (existing == selectedBooking) {
                continue;
            }
            boolean sameVenue = existing.getVenue().equalsIgnoreCase(replacement.getVenue());
            boolean sameTime = replacement.getStartTime() != null
                    && replacement.getStartTime().equals(existing.getStartTime());
            if (sameVenue && sameTime) {
                return existing;
            }
        }
        return null;
    }

    /** Resolves a one-based selection against the filtered matches, rather than the full booking list. */
    private void selectBooking(String input) {
        int selection;
        try {
            selection = Integer.parseInt(input.strip());
        } catch (NumberFormatException e) {
            throw new InvalidCommandException("Enter a number from 1 to " + matches.size() + ", or type abort.");
        }
        if (selection < 1 || selection > matches.size()) {
            throw new InvalidCommandException("Enter a number from 1 to " + matches.size() + ", or type abort.");
        }
        selectedBooking = matches.get(selection - 1);
    }

    /** Displays the chosen booking and explains how to preserve fields. */
    private void printEditPrompt() {
        System.out.println("Editing: " + selectedBooking);
        System.out.println("Enter r/REASON, n/NAME, v/VENUE, or t/TIMING changes in any order.");
        System.out.println("Add optional a/ATTENDEES at the end.");
        System.out.println("Omit fields to keep them. Timing must use dd/MM/yyyy HH:mm. Type abort to cancel editing.");
    }

    /** Clears pending selection and changes without modifying any booking. */
    public void cancel() {
        matches.clear();
        selectedBooking = null;
    }
}
