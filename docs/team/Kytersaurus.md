# Kytersaurus - Project Portfolio Page

## Overview

EduReserve is a Java command-line application that helps school administrators manage lecture and exam venue bookings.
My main contributions are listing and sorting bookings, finding available preset venues, and documenting these features
with usage examples and UML diagrams.

## Summary of contributions

### New feature: Listing bookings (`list`)

- **What it does:** Displays all bookings or filters them by exam or lecture, with consecutive numbering starting at 1.
- **Justification:** Helps administrators review bookings and focus on the type of event they are managing.
- **Highlights:** Implemented `ListCommand` and integrated it with `Parser` and `Ui`. Added the `r/exams` and
  `r/lectures` options and maintained a separate `lastShownList` so filtering does not change the stored bookings.
  Updated command help and validation for the listing syntax.
- **Code:** [ListCommand](../../src/main/java/command/ListCommand.java),
  [Parser](../../src/main/java/ui/Parser.java), [Ui](../../src/main/java/ui/Ui.java).
  Commits: `199ad65`, `fc90b1a`.

### Enhancement: Sorting listed bookings

- **What it does:** Supports alphabetical and reverse alphabetical sorting with `s/alpha` and `s/~alpha`, and
  chronological and reverse chronological sorting with `s/time` and `s/~time`. Without a sorting option, bookings
  retain their original order.
- **Justification:** Lets administrators locate bookings by name and review upcoming bookings in time order.
- **Highlights:** Added `SortOrder` and used comparators to sort the display list. Alphabetical comparisons ignore
  capitalization. Filtering and sorting can be combined, and the optional arguments can appear in either order.
  The parser returns a `ListOptions` record containing both selections and rejects duplicate or unsupported options.
  Time sorting reports an error if a selected booking has an older timing value without a valid date and time.
- **Code:** [ListCommand](../../src/main/java/command/ListCommand.java),
  [SortOrder](../../src/main/java/command/SortOrder.java), [Parser](../../src/main/java/ui/Parser.java).
  Commits: `a58cc2a`, `2ca271c`.

### New feature: Finding available venues (`free`)

- **What it does:** Lists preset venues available for a one-hour slot starting at the date and time specified by
  `free t/dd/MM/yyyy HH:mm`. Displays numbered results or a message when no venues are available.
- **Justification:** Helps administrators choose a venue before creating a booking, rather than checking each
  existing booking manually.
- **Highlights:** Implemented `FreeCommand`, date/time parsing, UI dispatch and `VenueCatalog`. The catalog contains
  computing seminar rooms and labs, lecture theatres and MPSH venues. Availability searches leave stored bookings
  unchanged. Updated `BookingList.isVenueAvailable` to reuse the existing `Booking.overlaps` helper so availability
  follows the same one-hour rule as booking clash prevention. The overlap helper and clash prevention feature were
  implemented by a teammate; my contribution was integrating the rule into the availability search.
- **Code:** [FreeCommand](../../src/main/java/command/FreeCommand.java),
  [VenueCatalog](../../src/main/java/tasks/VenueCatalog.java),
  [BookingList](../../src/main/java/tasks/BookingList.java). Commit: `47bac08` introduced the free command.

### Defensive programming and testing

- Added assertions to listing code to check valid filters, non-null bookings and unchanged list sizes after
  listing and sorting. Added precondition assertions to venue availability code. Invalid user input is reported
  through exceptions rather than assertions.
- Added and updated JUnit tests in [ListCommandTest](../../src/test/java/command/ListCommandTest.java),
  [ParserTest](../../src/test/java/ui/ParserTest.java) and [UiTest](../../src/test/java/ui/UiTest.java) for filtering,
  sorting, numbering, invalid options, empty lists and preservation of the stored booking order.
- Added [FreeCommandTest](../../src/test/java/command/FreeCommandTest.java) and
  [VenueCatalogTest](../../src/test/java/tasks/VenueCatalogTest.java), with additional availability tests in
  [BookingListTest](../../src/test/java/tasks/BookingListTest.java), `ParserTest` and `UiTest`. These cover occupied
  venues, empty results, invalid dates and times, case-insensitive venue matching and searches that preserve bookings
  and undo history.
- Extended availability tests for the one-hour booking duration, covering
    partial overlaps before and after an existing booking's start, adjacent
    slots exactly one hour apart, and slots crossing midnight. Updated command
    and UI tests to check the displayed time range and preservation of stored
    bookings.
### Other functional contributions

- Created the command abstract class and method for all other commands to inherit from and act as a template for all command functions

### Documentation and diagrams

- Documented the `list` and `free` commands in the [User Guide](../UserGuide.md), including syntax and examples.
- Wrote the corresponding implementation explanations in the [Developer Guide](../DeveloperGuide.md) and updated
  the user stories for listing, sorting and venue availability.
- Created and refined the [list sequence diagram](../UMLDiagrams/ListCommandSequenceDiagram.puml) and
  [free sequence diagram](../UMLDiagrams/FreeCommandSequenceDiagram.puml), showing parsing, command execution,
  filtering or availability checks, and output to the user.
- Created the [architecture diagram](../UMLDiagrams/ArchitectureDiagram.puml) and simplified it to show the main
  application, UI, command, model and storage dependencies.
- Added a linked table of contents to the Developer Guide to make its sections easier to navigate.

### Integration and code quality

- Resolved merge conflicts while retaining teammates' tests and preserving their author attribution in Git history.
- Fixed coding style and Javadoc issues identified by CI and integrated listing and availability commands with the
  existing command handling structure.
- Built and verified jar file for milestone 1 submission
