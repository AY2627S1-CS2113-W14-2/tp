# Peiyi Zhou - Project Portfolio Page

## Overview

EduReserve is a Java command-line application for managing exam and lecture venue bookings. My main contributions are booking data storage, saved display names and save-time reporting, together with their tests and documentation.

## Summary of Contributions

### Code contributed

* [RepoSense code dashboard](https://nus-cs2113-ay2627-s1.github.io/tp-dashboard/?search=JamesZ1110&breakdown=true)

### New feature: Booking storage

* **What it does:** Saves bookings to `data/bookings.txt` and loads them when EduReserve starts again.
* **Justification:** Lets users keep their booking data between application sessions.
* **Highlights:** Implemented the `Storage` class and connected it to the application loop. Missing folders and files are created automatically, while invalid saved data is reported without starting the application with incomplete data. Changed the initial name-based design so that all names use the same booking file, as EduReserve is a single-user application.
* **Code:** [Storage](../../src/main/java/storage/Storage.java), [EduReserve](../../src/main/java/EduReserve.java). Main commits: `05e0ccd`, `7f9fcf5`.

### Enhancement: Saved display name and save-time feedback

* **What it does:** Remembers one display name and shows when the booking file was last saved.
* **Justification:** Avoids asking the same user for their name on every launch and confirms that booking changes were saved.
* **Highlights:** Stored the display name separately in `data/username.txt`. This is only a display name and does not create accounts or separate user profiles. Updated `saveBookings` to return the file's last-modified time for EduReserve to display.
* **Code:** [Storage](../../src/main/java/storage/Storage.java), [EduReserve](../../src/main/java/EduReserve.java). Main commit: `8522682`.

### Enhancement: Saving attendee counts

* **What it does:** Saves and reloads the optional attendee count of each booking.
* **Justification:** Prevents attendee information from being lost after the application exits.
* **Highlights:** Extended the saved format with an attendee-count field and validated saved counts as positive whole numbers. Older booking files without attendee counts can still be loaded.
* **Code:** [Storage](../../src/main/java/storage/Storage.java). Main commit: `70c58cb`.

### Other functional contributions

* Added the startup banner, personalised greeting and related application test.
* Made EduReserve save the current booking list automatically after processing commands.
* Added error handling for problems encountered while loading or saving files.
* **Code:** [EduReserve](../../src/main/java/EduReserve.java), [HelpText](../../src/main/java/ui/HelpText.java). Main commit: `57aa784`.

### Testing contributions

* Created and expanded [StorageTest](../../src/test/java/storage/StorageTest.java) to cover creating the data file, saving and loading bookings, using one shared file, invalid saved data, attendee counts, saved display names and save timestamps.
* Added and updated [EduReserveTest](../../src/test/java/EduReserveTest.java) for the startup greeting and storage-related application behaviour.
* Main commits: `57aa784`, `05e0ccd`, `7f9fcf5`, `70c58cb`, `8522682`.

### Documentation contributions

* Documented the storage design, saved format and compatibility with older booking files in the [Developer Guide](../DeveloperGuide.md).
* Created and updated the [storage sequence diagram](../UMLDiagrams/StorageSequenceDiagram.puml).
* Updated the [User Guide](../UserGuide.md) and the Developer Guide's manual-testing instructions.
* Main commits: `b4a1dc6`, `8522682`, `60fb13f`, `f62ec4b`.

### Team-based contributions

* Tracked the booking-storage work in [issue #38](https://github.com/AY2627S1-CS2113-W14-2/tp/issues/38)
  and the saved-username and save-time work in
  [issue #83](https://github.com/AY2627S1-CS2113-W14-2/tp/issues/83).
* Added the shared manual-testing instructions and storage section in
  [PR #94](https://github.com/AY2627S1-CS2113-W14-2/tp/pull/94).

### Review contributions

* Reviewed team PRs
  [#109](https://github.com/AY2627S1-CS2113-W14-2/tp/pull/109),
  [#82](https://github.com/AY2627S1-CS2113-W14-2/tp/pull/82),
  [#78](https://github.com/AY2627S1-CS2113-W14-2/tp/pull/78),
  [#16](https://github.com/AY2627S1-CS2113-W14-2/tp/pull/16), and
  [#1](https://github.com/AY2627S1-CS2113-W14-2/tp/pull/1).
