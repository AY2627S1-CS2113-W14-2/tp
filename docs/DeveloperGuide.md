# Developer Guide

## Acknowledgements

{list here sources of all reused/adapted ideas, code, documentation, and third-party libraries -- include links to the original source as well}

## Design & implementation

{Describe the design and implementation of the product. Use UML diagrams and short code snippets where applicable.}

### Creating a booking: book

Adds a named exam or lecture booking.

Format: `book r/BOOKING_REASON n/BOOKING_NAME v/VENUE t/TIMING`

- All four fields are required and must appear in the order `r/`, `n/`, `v/`, `t/`.
- The reason must be `exam` or `lecture` (case-insensitive).
- The name and venue must not be blank and may contain spaces.
- Timing must be a valid date and time in `dd/MM/yyyy HH:mm` format.

Example: `book r/exam n/CS2113 Midterm v/LT1 t/05/10/2026 14:00`

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

Format: list [r/REASON] [s/SORT_ORDER]

Square brackets indicate optional arguments, do not type them.

- REASON must be either exams or lectures
- SORT_ORDER must be either alpha for A to Z or ~alpha for Z to A sorting
- Sorting compares booking names, ignoring capitalization
- Omitting r/REASON shows all bookings
- Omitting s/SORT_ORDER shows the list with original booking orders preserved
- The arguments may be swapped to appear in any order first, and can only be specified once

Examples:

|          Command           |                Result                 |
|:--------------------------:|:-------------------------------------:|
|           `list`           |           Show all bookings           |
|       `list r/exams`       |        Show all exam bookings         |
|       `list s/alpha`       | Show all bookings sorted from A to Z  |
| `list r/lectures s/~alpha` | Show all lectures sorted from Z to A  |       

![List command sequence diagram](UMLDiagrams/ListCommandSequenceDiagram.png)

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

### Cancelling a booking: cancel
Removes a booking so that its venue and time can be used again

Format: cancel n/BOOKING_NAME

- BOOKING_NAME is not case sensitive and may contain spaces
- If several bookings share the same name, only the first one in the list is cancelled
- An error is shown if no booking has that name, and the list is left unchanged

Examples:

|            Command             |                          Result                           |
|:------------------------------:|:---------------------------------------------------------:|
|   `cancel n/CS2113 Midterm`    |        Cancel the booking named "CS2113 Midterm"          |
|   `cancel n/cs2113 midterm`    | Same booking as above, as capitalization is ignored       |

Implementation: `Parser#parseCancel` extracts the name, `BookingList#findByName` returns the index of the first
match (or -1), and `BookingList#remove` deletes it. Assertions check that the index is inside the list and that the
list shrinks by exactly one, while user mistakes such as a missing name throw `InvalidCommandException`.
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
![Find command sequence diagram](UMLDiagrams/FindCommandSequenceDiagram.png)

### Preventing booking clashes: book
A new booking is rejected if its venue is already booked at the same time

Format: book r/BOOKING_REASON n/BOOKING_NAME v/VENUE t/TIMING (same as before)

- A clash means an existing booking with the same venue (not case sensitive) and exactly the same TIMING
- A booking only has a start time, so two bookings in the same venue that start at different times never clash
- Older saved bookings without a full dd/MM/yyyy HH:mm timing are not checked
- Cancelling the existing booking frees the venue and time again

Examples, assuming `book r/exam n/CS2113 Midterm v/LT1 t/05/10/2026 14:00` already exists:

|                       Command                       |            Result             |
|:---------------------------------------------------:|:-----------------------------:|
| `book r/lecture n/Week 1 v/lt1 t/05/10/2026 14:00`  | Rejected: LT1 is already booked |
| `book r/lecture n/Week 1 v/LT1 t/05/10/2026 15:00`  |            Added              |
| `book r/lecture n/Week 1 v/LT2 t/05/10/2026 14:00`  |            Added              |

Implementation: after `Parser#parseBooking` succeeds, `Ui` calls `BookingList#findClash`. It returns the booking that
already uses the venue and time, or null. A clash is reported as an `InvalidCommandException`, so the new booking is
never added.
![Book command sequence diagram](UMLDiagrams/BookCommandSequenceDiagram.png)

### Daily calendar: calendar
Shows the bookings that start today, with their start times

Format: calendar

- Takes no arguments, and extra words are rejected
- Today is read from the computer's clock
- Bookings are shown earliest first, with start time, purpose, name and venue
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
| v1.0    | School Admin | Edit a booking                                                       | I can change my booking details if needed                                             |
| v1.0    | School Admin | Check size and equipment                                             | Decide whether I have enough resources to hold an event in the venue                  |
| v2.0    | School Admin | find a to-do item by name                                            | locate a to-do without having to go through the entire list                           |
| v2.0    | School Admin | Add booking notes                                                    | I have an alternative method to convey any messages to students beforehand            |
| v2.0    | School Admin | Undo the previous action                                             | I can rewind accidental actions                                                       |
| v2.0    | School Admin | Redo last undo                                                       | I can redo an action if I accidentally undid it                                       |
| v2.0    | School Admin | Be stopped from booking a venue that is already booked for that time | Avoid double-booking a venue and clashing with other classes                          |
| v2.0    | School Admin | Get a calendar view of today's bookings                              | Quickly be updated on what's scheduled for today without going through the whole list |
| v2.0    | School Admin | View a list of the bookings sorted by time                           | I can clearly see which bookings come after which time                                |
| v2.0    | School Admin | View a list of available venues                                      | I can clearly see which booking slots are free                                        |


## Non-Functional Requirements

{Give non-functional requirements}

## Glossary

* *glossary item* - Definition

## Instructions for manual testing

{Give instructions on how to do a manual product testing e.g., how to load sample data to be used for testing}
