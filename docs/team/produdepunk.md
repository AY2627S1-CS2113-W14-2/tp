# Heu Yu Hui - Project Portfolio Page

## Overview

EduReserve is a Java command-line application that helps school administrators manage lecture and exam venue
bookings. Users can create, view and update bookings, record attendee counts and save their bookings between
sessions. My main contributions are the `editbooking`, `viewbooking` and `checksize` features, together with
their tests and documentation.

### Summary of Contributions

#### Editing a booking: `editbooking`

* **What it does:** Updates an existing booking's purpose, name, venue, timing or attendee count. Users start
  with `editbooking n/BOOKING_NAME`, select a booking if several names match, then enter the fields to change.
  Omitted fields retain their existing values, and `abort` cancels the pending edit.
* **Justification:** Allows administrators to correct or reschedule a booking without cancelling it and
  entering all its details again.
* **Implementation highlights:** Implemented `EditCommand` and integrated the selection and editing steps
  into the existing UI loop. Selection numbers refer to the filtered matching list, so users edit the booking
  they selected. Added parsing and validation for partial updates, retained the original booking when input
  is invalid or a venue/time clash is detected, and replaced a successfully edited booking in its original
  list position. Added support for updating attendee counts alongside the other fields.
* **Code:** [EditCommand](../../src/main/java/command/EditCommand.java),
  [Parser](../../src/main/java/ui/Parser.java), [Ui](../../src/main/java/ui/Ui.java) and
  [BookingList](../../src/main/java/tasks/BookingList.java).
  Main commits: `444034b`, `92d5f32`.

#### Viewing bookings: `viewbooking`

* **What it does:** Displays the purpose, venue and timing of every booking matching a full name, ignoring
  capitalization. The optional `s/time` argument sorts matching bookings from earliest to latest.
* **Justification:** Helps administrators inspect a specific booking or compare bookings sharing the same
  name without searching through the full list.
* **Implementation highlights:** Extracted viewing into `ViewCommand` and supported displaying all exact-name
  matches. Added `ViewOptions`, parser support and chronological sorting while preserving the original
  booking order when no sort option is supplied. Added error handling for missing bookings, invalid options
  and older saved timings that cannot be sorted. Viewing leaves the stored booking list unchanged.
* **Code:** [ViewCommand](../../src/main/java/command/ViewCommand.java),
  [ViewOptions](../../src/main/java/ui/ViewOptions.java) and [Parser](../../src/main/java/ui/Parser.java).
  Main commits: `f0e2024`, `bd7325c`, `4011674`.

#### Checking attendee counts: `checksize`

* **What it does:** Reports the largest recorded attendee count for each venue. Bookings without attendee
  counts are ignored, and venue names are grouped without regard to capitalization.
* **Justification:** Gives administrators a summary of recorded group sizes by venue to support resource
  planning. The reported values describe booking attendance rather than the venue's actual capacity.
* **Implementation highlights:** Implemented `SizeCommand` using a case-insensitive `TreeMap` to group venues,
  keep the maximum count and display results alphabetically. Added the optional attendee-count field to
  bookings and parsing support for `a/ATTENDEES` when creating or editing a booking. Validated counts as
  positive whole numbers and provided a message when no booking has a recorded count.
* **Code:** [SizeCommand](../../src/main/java/command/SizeCommand.java),
  [Booking](../../src/main/java/tasks/Booking.java) and [Parser](../../src/main/java/ui/Parser.java).
  Main commit: `92d5f32`. Saving attendee counts to the data file was contributed by a teammate.

#### Defensive programming and testing

* Added assertions for valid command inputs and booking invariants, including checks that viewing and
  finding matching bookings do not change the list size. User input errors are reported through
  `InvalidCommandException` so users can correct their input.
* Wrote [EditCommandTest](../../src/test/java/command/EditCommandTest.java) to cover selection among matching
  names, partial updates, invalid selections, invalid changes, clashes, cancellation and missing bookings.
* Wrote [ViewCommandTest](../../src/test/java/command/ViewCommandTest.java) to check original display order
  and assertions, and added parser and UI tests for name matching, whitespace, punctuation, chronological
  sorting and invalid input.
* Wrote [SizeCommandTest](../../src/test/java/command/SizeCommandTest.java) to check maximum counts per venue,
  case-insensitive grouping, bookings without counts, empty results and rejected arguments.
* Added integration tests in [ParserTest](../../src/test/java/ui/ParserTest.java),
  [UiTest](../../src/test/java/ui/UiTest.java) and [EduReserveTest](../../src/test/java/EduReserveTest.java),
  including selecting, editing, saving and reloading a booking.

#### Documentation contributions

* Wrote the `viewbooking`, `editbooking` and `checksize` sections in the [Developer Guide](../DeveloperGuide.md),
  explaining command formats, behavior and implementation.
* Created sequence diagrams for [viewing bookings](../UMLDiagrams/ViewCommandSequenceDiagram.puml),
  [editing bookings](../UMLDiagrams/EditBookingSequenceDiagram.puml) and
  [checking attendee counts](../UMLDiagrams/CheckSizeSequenceDiagram.puml), with rendered PNG versions.
* Updated the [User Guide](../UserGuide.md) introduction and Quick Start, documented the three features with
  examples and input requirements, and updated the command summary. Main commit: `45ee537`.
