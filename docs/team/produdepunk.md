# Heu Yu Hui - Project Portfolio Page

## Overview

EduReserve is a Java command-line application that helps school administrators manage lecture and exam venue
bookings. Users can create, view and update bookings, record attendee counts and save their bookings between
sessions. My main contributions are the `editbooking`, `viewbooking` and `checksize` features, together with
their tests and documentation.

## Summary of Contributions
* **Code contributed**: [RepoSense link](https://nus-cs2113-ay2627-s1.github.io/tp-dashboard/?search=produdepunk&breakdown=true)

### New features implemented

#### Editing a booking: `editbooking`

* **What it does:** Updates a booking's purpose, name, venue, timing or attendee count. Users select among
  matching names, change only the required fields and can cancel with `abort`.
* **Justification:** Lets administrators correct or reschedule bookings without recreating them.
* **Implementation highlights:** Integrated `EditCommand` into the UI loop with filtered selection and
  partial-update validation. Invalid input or clashes preserve the original booking; successful edits
  retain its list position.
* **Code:** [EditCommand](../../src/main/java/command/EditCommand.java),
  [Parser](../../src/main/java/ui/Parser.java) and [BookingList](../../src/main/java/tasks/BookingList.java).
  Main commits: `444034b`, `92d5f32`.

#### Viewing bookings: `viewbooking`

* **What it does:** Displays the purpose, venue and timing of all full-name matches, ignoring capitalization.
  Optional `s/time` sorts them chronologically.
* **Justification:** Helps administrators inspect bookings without searching through the full list.
* **Implementation highlights:** Extracted `ViewCommand` and added `ViewOptions` and parsing for sorting.
  Preserved original order by default and handled missing bookings, invalid options and unsortable older
  timings without changing stored bookings.
* **Code:** [ViewCommand](../../src/main/java/command/ViewCommand.java),
  [ViewOptions](../../src/main/java/ui/ViewOptions.java).
  Main commits: `f0e2024`, `bd7325c`, `4011674`.

#### Checking attendee counts: `checksize`

* **What it does:** Reports the largest recorded attendee count per venue, ignoring bookings without counts.
* **Justification:** Supports resource planning using recorded group sizes; these values are not venue capacities.
* **Implementation highlights:** Used a case-insensitive `TreeMap` to group venues and display maximum counts
  alphabetically. Added optional `a/ATTENDEES` support to booking creation and editing, with positive whole-number
  validation and empty-result handling.
* **Code:** [SizeCommand](../../src/main/java/command/SizeCommand.java),
  [Booking](../../src/main/java/tasks/Booking.java).
  Main commit: `92d5f32`. Saving attendee counts to the data file was contributed by a teammate.

### Defensive programming and testing

* Added input and booking-invariant assertions, including checks that viewing preserves the list size.
  Reported user input errors through `InvalidCommandException` to allow correction.
* Wrote [EditCommandTest](../../src/test/java/command/EditCommandTest.java),
  [ViewCommandTest](../../src/test/java/command/ViewCommandTest.java) and
  [SizeCommandTest](../../src/test/java/command/SizeCommandTest.java), covering selection, partial updates,
  clashes, cancellation, display order, venue grouping and invalid input.
* Added parser, UI and application integration tests for name matching, sorting, attendee counts and
  saving and reloading edited bookings.

### Documentation contributions

* Documented the three commands' formats, behavior and implementation in the [Developer Guide](../DeveloperGuide.md).
* Created sequence diagrams and rendered PNGs for [viewbooking](../UMLDiagrams/ViewCommandSequenceDiagram.puml),
  [editbooking](../UMLDiagrams/EditBookingSequenceDiagram.puml) and
  [checksize](../UMLDiagrams/CheckSizeSequenceDiagram.puml).
* Updated the [User Guide](../UserGuide.md) introduction, Quick Start, feature examples and command summary.
  Main commit: `45ee537`.
