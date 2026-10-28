# Developer Guide

## Acknowledgements

Base template code forked from https://github.com/NUS-CS2113-AY2627-S1/tp

This project follows the Java coding standard from https://se-education.org/guides/conventions/java/intermediate.html

## Design & implementation

![EduReserve Architecture Diagram](UMLDiagrams/ArchitectureDiagram-EduReserve_architecture.png)

### Creating a booking: book

Adds a named exam or lecture booking.

Format: `book r/BOOKING_REASON n/BOOKING_NAME v/VENUE t/TIMING`

- All four fields are required and must appear in the order `r/`, `n/`, `v/`, `t/`.
- The reason must be `exam` or `lecture` (case-insensitive).
- The name and venue must not be blank and may contain spaces.
- Timing must be a valid date and time in `dd/MM/yyyy HH:mm` format.

Example: `book r/exam n/CS2113 Midterm v/LT1 t/05/10/2026 14:00`

Add an optional attendee count after the timing as `a/ATTENDEES`, for example:
`book r/exam n/CS2113 Midterm v/LT1 t/05/10/2026 14:00 a/80`.
The count must be a positive whole number. Bookings without a count have an unknown size.

`Ui.processCommand` identifies the command and creates a `BookCommand`, passing it the existing
`Parser`. `BookCommand.execute` passes the full input to `Parser.parseBooking`, which validates
the fields and constructs a `Booking`. The command then calls `BookingList.add` and prints the
confirmation. `Ui` prints the separator and returns `false` so the application continues.

![Book command sequence diagram](UMLDiagrams/BookingSequenceDiagram.png)

The diagram shows a successful booking; validation internals and storage are omitted for clarity.
If validation fails, `Parser` throws `InvalidCommandException` before a booking is added.
The exception propagates through `BookCommand` to `Ui`, which displays the error and command help.
After `processCommand` returns, `EduReserve` saves the booking list using `Storage.saveBookings`.

[Editable PlantUML source](UMLDiagrams/BookingSequenceDiagram.puml)

### Listing bookings: list
Displays the bookings, with optional filtering and alphabetical sorting

Format: `list [r/REASON] [s/SORT_ORDER]`

Square brackets indicate optional arguments, do not type them.

- `REASON` must be either exams or lectures
- `SORT_ORDER` must be either `alpha` for A to Z or `~alpha` for Z to A sorting
- Sorting compares booking names, ignoring capitalization
- Omitting `r/REASON` shows all bookings
- Omitting `s/SORT_ORDER` shows the list with original booking orders preserved
- The arguments may be swapped to appear in any order first, and can only be specified once

Examples:

|          Command           |                Result                 |
|:--------------------------:|:-------------------------------------:|
|           `list`           |           Show all bookings           |
|       `list r/exams`       |        Show all exam bookings         |
|       `list s/alpha`       | Show all bookings sorted from A to Z  |
| `list r/lectures s/~alpha` | Show all lectures sorted from Z to A  |       

Parser.parseViewList() validates the optional reason and sorting arguments, returning a ListOptions record. Ui passes these options to ListCommand.execute().

The command clears lastShownList and populates it with all bookings or only those matching the requested reason. It then sorts this display list by name (s/alpha, s/~alpha) or date and time (s/time, s/~time). Without a sorting option, insertion order is preserved.

Finally, the command prints the selected bookings with consecutive numbers starting from 1. Sorting does not change the stored BookingList. Time sorting is rejected if a selected booking lacks a valid parsed date and time.

![List command sequence diagram](UMLDiagrams/ListCommandSequenceDiagram.png)
### List available venues: free
Displays a list of all available venues at a user specified time

Format: `free [t/TIMING]`

- `TIMING` must use the `dd/MM/yyyy HH:mm` format
- `TIMING` represents the start time of the booking

Examples:

|          Command          |                      Result                       |
|:-------------------------:|:-------------------------------------------------:|
| `free t/28/10/2026 14:00` | Show all available venues on 28th Oct 2026 at 2pm |

The free t/dd/MM/yyyy HH:mm command lists preset venues with no booking starting at the requested date and time. Parser.parseFreeTime() validates the input and returns a LocalDateTime. FreeCommand passes this value to VenueCatalog.findAvailableVenues(), which checks each venue using BookingList.isVenueAvailable().
The command prints the matching venues with consecutive numbers, or a message if none are available. It does not modify stored bookings. Availability checks compare venue names without case sensitivity and match exact start times; they do not detect overlapping durations.

![Free command sequence diagram](UMLDiagrams/FreeCommandSequenceDiagram.png)

### View booking: viewbooking
Displays the details of the bookings under the same name, with optional time filtering

Format: viewbooking [n/BOOKING_NAME] [s/time]

Second argument is optional

- BOOKING_NAME must be a valid name in the booking list
- Sorting compares booking timings
- Omitting s/time shows the list with original booking orders preserved

Examples:

|             Command              |                               Result                                |
|:--------------------------------:|:-------------------------------------------------------------------:|
|    `viewbooking n/mybooking`     |    Show all bookings under the name "mybooking" in booking order    |
| `viewbooking n/mybooking s/time` | Show all bookings under the name "mybooking" in the order of timing |

![View command sequence diagram](UMLDiagrams/ViewCommandSequenceDiagram.png)

### Editing a booking: editbooking

Format: `editbooking n/BOOKING_NAME`

- Matches the full booking name, ignoring case; partial names are not matched.
- If multiple bookings match, a numbered list shows their reason, venue, and timing.
  Enter the number from this filtered list to select the booking to edit.
- With one match, the booking is selected automatically.
- At the editing prompt, enter one or more fields: `r/REASON`, `n/NAME`, `v/VENUE`, `t/TIMING`.
  These fields may appear in any order, once each. Omitted fields keep their existing values.
- Add optional `a/ATTENDEES` at the end to update the booking size.
- Reasons must be `exam` or `lecture`; timing must use `dd/MM/yyyy HH:mm`.
- Invalid input or a venue/time clash leaves the original booking unchanged and allows a retry.
- Type `abort` to cancel editing, or `bye` to exit without applying changes.

Example session (two bookings named "Midterm"):

```text
editbooking n/Midterm
2
v/LT3 t/06/10/2026 15:00
```

`Ui` retains an `EditCommand` between input lines. The command uses `BookingList.findAllByName`
to build the filtered selection and retains the selected booking object, so filtered numbers
cannot accidentally select an unrelated booking in the full list. `Parser.parseEditedBooking`
validates a replacement using the same constraints as creating a booking. Clash checking excludes
the selected original booking, and `BookingList.replace` applies the change in its original position
only after validation succeeds. The existing application loop saves the updated list.

[Editable edit booking sequence diagram](UMLDiagrams/EditBookingSequenceDiagram.puml)

### Checking attendee counts: checksize

The `checksize` command reports the largest recorded attendee count for each venue. Bookings without
an attendee count are omitted, and no venue capacity values are required.

Format: `checksize`

The command groups bookings by venue and prints the largest attendee count recorded for each venue.
Bookings without `a/ATTENDEES` do not contribute to the results.

![Checksize sequence diagram](UMLDiagrams/CheckSizeSequenceDiagram.png)

### Saving and loading bookings: storage

Saves one username and the booking list to text files. It loads them again when EduReserve starts.

The first time EduReserve starts, `loadUserName` returns `null` because `data/username.txt` does not exist.
EduReserve asks for a username and calls `saveUserName`. Later runs load this username and skip the question.
Only one username is stored because EduReserve is a single-user application.

Data format:

`B | BOOKING_REASON | BOOKING_NAME | VENUE | TIMING | ATTENDEES`

Example:

`B | exam | Midterm | LT1 | 05/10/2026 14:00 | 80`

- Each booking is stored on one line.
- The attendee count is optional.
- Bookings are stored in `data/bookings.txt`.
- The file uses UTF-8.

When EduReserve starts, it creates a `Storage` object and calls `loadBookings`. If the data file
does not exist, an empty file is created. `Storage` reads each nonblank line and passes it to
`parseBooking`. The method checks the fields and creates a `Booking`. The booking is then added
to the `BookingList`.

After a command is processed, EduReserve calls `saveBookings`. The method goes through the current
booking list and passes each booking to `bookingToLine`. This method gets the reason, name, venue,
timing and attendee count from the booking. The completed lines are then written to
`data/bookings.txt`. `saveBookings` reads the file's last modified time after writing and returns it.
EduReserve formats this time and displays it so the user can confirm that the booking file was saved.

The storage also supports booking files created before attendee counts were added. An older line
has five parts while a new line has six parts. For an older booking, the attendee count is set to
`null`. When the booking list is saved again, it is written using the new format. A saved attendee
count must be a positive whole number. Invalid booking data causes an `IOException`, which
EduReserve catches and reports to the user.

A plain-text file was used because the booking data has a small and fixed format. It is also easy
to read and does not need another library. JSON was considered, but it would add extra parsing for
the current amount of data. Requiring only the new format was also considered, but older booking
files would no longer load.

![Storage sequence diagram](UMLDiagrams/StorageSequenceDiagram.png)

The diagram shows the loading and saving of bookings. File operations, validation details and
exception handling are omitted for clarity.

[Editable PlantUML source](UMLDiagrams/StorageSequenceDiagram.puml)

### Cancelling a booking: cancel
Removes a booking so that its venue and time can be used again

Format: cancel n/BOOKING_NAME

- BOOKING_NAME is not case sensitive and may contain spaces
- If several bookings share the same name, only the first one in the list is cancelled. `book` rejects duplicate names, so this mainly affects older saved data.
- An error is shown if no booking has that name, and the list is left unchanged

Examples:

|            Command             |                          Result                           |
|:------------------------------:|:---------------------------------------------------------:|
|   `cancel n/CS2113 Midterm`    |        Cancel the booking named "CS2113 Midterm"          |
|   `cancel n/cs2113 midterm`    | Same booking as above, as capitalization is ignored       |

Implementation: `Ui#cancelBooking` calls `Parser#parseCancel` to extract the name, `BookingList#findByName` to get the
index of the first match (or -1), and `BookingList#remove` to delete it. Assertions check that the index is inside the
list and that the list shrinks by exactly one, while user mistakes such as a missing name throw
`InvalidCommandException`.

Design consideration: cancelling by name instead of by list number. `viewbooking`, `editbooking` and `find` already
identify bookings by name, so `cancel` works the same way. A list number is risky because it changes whenever the list
is filtered, sorted, cancelled from or undone, so a number read from an old `list` could cancel the wrong booking.
The trade-off is that the full name must be typed, which is acceptable because `book` requires names to be unique.
![Cancel command sequence diagram](UMLDiagrams/CancelCommandSequenceDiagram.png)

### Finding bookings: find
Shows every booking whose name contains a keyword

Format: find n/KEYWORD

- KEYWORD is not case sensitive and may contain spaces
- A booking matches if its name contains the keyword anywhere, not just at the start
- Matches are shown in their original order, with their venue and timing
- If nothing matches, a message says so. The booking list is never changed

Examples:

|        Command         |                                        Result                                         |
|:----------------------:|:-------------------------------------------------------------------------------------:|
|     `find n/CS2`       | Show both "CS2113 Midterm" and "CS2445 Final Exam"                                    |
|    `find n/CS2113`     | Show only "CS2113 Midterm"                                                            |
|   `find n/final exam`  | Show only "CS2445 Final Exam"                                                         |

Implementation: `Parser#parseFind` extracts the keyword, `Ui` passes it to `FindCommand`, and `FindCommand` calls
`BookingList#findByKeyword` and prints each match. Assertions check that the keyword is not blank, that every result
contains the keyword and that the list size is unchanged.

Design consideration: partial, case-insensitive matching instead of matching the full name. Admins often remember only
part of a booking name, such as the module code `CS2113`, and exact lookup by full name is already covered by
`viewbooking`, `cancel` and `editbooking`. The trade-off is that a very short keyword can match many bookings, which is
acceptable because the results are only displayed and the list is never changed.
![Find command sequence diagram](UMLDiagrams/FindCommandSequenceDiagram.png)

### Preventing booking clashes: book
A new booking is rejected if its venue is already booked at an overlapping time

Format: book r/BOOKING_REASON n/BOOKING_NAME v/VENUE t/TIMING (same as before)

- Every booking lasts 1 hour from its start time, so a booking at 12:00 occupies 12:00 to 13:00
- A clash means an existing booking with the same venue (not case sensitive) whose hour overlaps the new booking's hour
- A booking that starts exactly when another ends does not clash, so 12:00 and 13:00 can both be booked
- Older saved bookings without a full dd/MM/yyyy HH:mm timing are not checked
- Cancelling the existing booking frees the venue and time again
- `editbooking` and `free` use the same rule, so all three commands always agree

Examples, assuming `book r/exam n/CS2113 Midterm v/LT1 t/05/10/2026 12:00` already exists (it occupies 12:00 to 13:00):

|                       Command                       |                Result                |
|:---------------------------------------------------:|:------------------------------------:|
| `book r/lecture n/Week 1 v/lt1 t/05/10/2026 12:00`  |  Rejected: LT1 is already booked     |
| `book r/lecture n/Week 1 v/LT1 t/05/10/2026 12:01`  |  Rejected: overlaps 12:00 to 13:00   |
| `book r/lecture n/Week 1 v/LT1 t/05/10/2026 11:01`  |  Rejected: runs into 12:00           |
| `book r/lecture n/Week 1 v/LT1 t/05/10/2026 13:00`  |  Added: starts as the first ends     |
| `book r/lecture n/Week 1 v/LT1 t/05/10/2026 11:00`  |  Added: ends as the first starts     |
| `book r/lecture n/Week 1 v/LT2 t/05/10/2026 12:00`  |  Added: different venue              |

Implementation: `BookCommand` calls `BookingList#findClash` once `Parser#parseBooking` succeeds. `findClash` asks each
existing booking whether `Booking#overlaps(venue, startTime)`, which is true when the venue matches (ignoring case) and
the new start is before the existing end and the existing start is before the new end. A booking's end is its start plus
`Booking.DEFAULT_DURATION` (1 hour). The first overlapping booking is returned, or null if the venue is free. A clash is
reported as an `InvalidCommandException`, so the new booking is never added. `BookingList#isVenueAvailable` (used by
`free`) and `EditCommand` call the same `Booking#overlaps` method, so the rule is defined in one place.

Design consideration: a fixed 1-hour duration instead of an end time entered by the user. A booking only stores its
start time, so letting the user type an end time or duration would change the `book` format and the saved file format,
and older saved bookings would need converting. With a fixed duration the end time is calculated, nothing new is saved,
and `Booking.DEFAULT_DURATION` is the only value to change if the length ever changes. The trade-off is that a longer
exam cannot block its venue for its full length. An optional duration field could be added later by storing it in
`Booking` and using it in `Booking#getEndTime`.
![Book command sequence diagram](UMLDiagrams/BookCommandSequenceDiagram.png)

### Daily calendar: calendar
Shows the bookings that start today, with their start times

Format: calendar

- Takes no arguments, and extra words are rejected
- Today is read from the computer's clock
- Bookings are shown earliest first, with start time, purpose, name and venue
- A booking appears on the day it starts, even if its 1-hour slot runs past midnight
- Older saved bookings without a full date are skipped
- If nothing starts today, "No bookings on dd/MM/yyyy." is shown

Example output:

```
Calendar for 05/10/2026:
1. 09:00 - lecture - Week 1 Lecture (venue: LT2)
2. 14:00 - exam - CS2113 Midterm (venue: LT1)
```

Implementation: `Parser#parseCalendar` checks there are no extra words and `Ui` calls
`CalendarCommand#execute(bookings, "today")`. That method calls `CalendarCommand#execute(bookings, LocalDate)`, which
asks `BookingList#findByDate` for the bookings starting on that date, sorted by start time, and prints them. The
`LocalDate` version exists so that tests can check the output for a fixed date instead of depending on today's date.

![Calendar command sequence diagram](UMLDiagrams/CalendarCommandSequenceDiagram.png)

### Undoing a command: /undo

Restores the booking list to its state before the most recent successful `book` or `cancel` command.

Format: `/undo`

`Ui` keeps one `UndoCommand` instance for the session. Its `isUndoable` method declares the eligible
commands. Before executing one of these commands, `executeAndRecord` copies the current booking list.
Only after the command succeeds does it push an `UndoEntry` containing the before and after snapshots and
the full command text onto `history`, then clear `redoHistory`. Failed commands and read-only commands
do not add entries or clear redo history.

When `/undo` is entered, `Ui.processCommand` calls `UndoCommand.execute`. It reads the latest history entry,
removes the current bookings and adds the saved bookings back in their original order. It then pops the
history entry, pushes it onto `redoHistory`, and prints the full command that was undone, for example:

```text
Undid command: book r/exam n/Midterm v/LT1 t/05/10/2026 14:00
```

If history is empty, it prints `No commands to undo.` and leaves the bookings unchanged. Extra arguments
such as `/undo extra` are rejected with `InvalidCommandException`, which `Ui` catches and displays with help.
Repeated `/undo` commands restore earlier states; undo itself is never recorded.

After `processCommand` returns, `EduReserve` calls `Storage.saveBookings` to write the current list to
`data/bookings.txt`. The restored bookings persist across restarts, but undo history lasts only for the
current session. A save failure is reported by `EduReserve` as `Unable to save your bookings.`

![Undo command sequence diagram](UMLDiagrams/UndoCommandSequenceDiagram.png)

The diagram covers a valid `/undo` request, including empty history, and a successful save. Validation
details, separator output, collection accessors and storage internals are omitted for clarity.

[Editable PlantUML source](UMLDiagrams/UndoCommandSequenceDiagram.puml)

### Redoing a command: /redo

Format: `/redo`

`Ui` keeps a `RedoCommand` linked to the same `UndoCommand` instance used for recording and undoing commands.
`RedoCommand.execute` validates that there are no arguments, then calls `UndoCommand.redo`. This restores
the after snapshot of the most recently undone command, moves its entry from `redoHistory` back to
`history`, and prints `Redid command: ` followed by the full original command. It can then be undone again.
`EduReserve` saves the restored booking list through the same `Storage.saveBookings` call used after undo.

Repeated `/redo` commands reapply commands in reverse undo order. If there is no available entry, the
response is `No commands to redo.` and bookings stay unchanged. Redo history lasts only for the current session.

A successful new `book` or `cancel` command clears **all remaining redo history**, including after a partial
redo. For example, after booking A, B and C, undoing C and B, and redoing B, a new booking D discards the
option to redo C. A failed command or a read-only command such as `list` leaves that option available.

![Redo command sequence diagram](UMLDiagrams/RedoCommandSequenceDiagram.png)

The diagram covers a valid `/redo` request, including empty redo history, and a successful save.
The two loops expand `restoreBookings`: first clear the current list, then restore the saved **after**
state from `resultingBookings()`. Undo instead restores the **before** state from `previousBookings()`.
Input validation, snapshot helper calls, separator output and storage internals are omitted for clarity.

[Editable PlantUML source](UMLDiagrams/RedoCommandSequenceDiagram.puml)

## Product scope
### Target user profile

{Describe the target user profile}

### Value proposition

{Describe the value proposition: what problem does it solve?}

## User Stories

| Version | As a ...     | I want to ...                                                        | So that I can ...                                                                     |
|---------|--------------|----------------------------------------------------------------------|---------------------------------------------------------------------------------------|
| v1.0    | School Admin | know what features the app has and how to use them                   | I can start to use the app seamlessly                                                 |
| v1.0    | School Admin | Have simple commands                                                 | It’s easier to remember what commands to use and navigate                             |
| v1.0    | School Admin | Book lecture and exam venues                                         | I can keep track of venues that are being used                                        |
| v1.0    | School Admin | Cancel a booking                                                     | Keep the bookings updated in case a class is cancelled                                |
| v1.0    | School Admin | Find bookings by a keyword in their name                             | Quickly locate specific bookings without scrolling through the whole list             |
| v1.0    | School Admin | View a booking                                                       | Check the details of the booking under a name                                         |
| v1.0    | School Admin | List bookings filtered by type and sorted alphebetically             | I can view all bookings clearly in an organised manner                                |
| v2.0    | School Admin | Edit a booking                                                       | I can change my booking details if needed                                             |
| v2.0    | School Admin | Check size of venue in terms of attendees                            | Determine I have enough resources to hold the event in the venue                      |
| v2.0    | School Admin | find a to-do item by name                                            | locate a to-do without having to go through the entire list                           |
| v2.0    | School Admin | Add booking notes                                                    | I have an alternative method to convey any messages to students beforehand            |
| v2.0    | School Admin | Undo the previous action                                             | I can rewind accidental actions                                                       |
| v2.0    | School Admin | Redo last undo                                                       | I can redo an action if I accidentally undid it                                       |
| v2.0    | School Admin | Be stopped from booking a venue that is already booked for that time | Avoid double-booking a venue and clashing with other classes                          |
| v2.0    | School Admin | Get a calendar view of today's bookings                              | Quickly be updated on what's scheduled for today without going through the whole list |
| v2.0    | School Admin | View a list of the bookings sorted by time                           | I can clearly see which bookings come after which time                                |
| v2.0    | School Admin | View a list of available venues                                      | I can clearly see which booking slots are free                                        |
| v2.0    | School Admin | Store my username                                                     | I do not have to enter my username every time the program starts                      |
| v2.0    | School Admin | Keep track of when the storage file was last changed                  | I can confirm that my booking changes were saved                                      |

## Non-Functional Requirements

{Give non-functional requirements}

## Glossary

* *glossary item* - Definition

## Instructions for manual testing

{Give instructions on how to do a manual product testing e.g., how to load sample data to be used for testing}
