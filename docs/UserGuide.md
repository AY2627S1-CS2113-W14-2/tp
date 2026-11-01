# User Guide

## Introduction

EduReserve is a single user platform for admins to book venues based on the name of the user
and input relevant details of the bookings such as the booking purpose, venue, timing or optionally attendee count.
The following guide will introduce the functions to utilize the platform.

## Quick Start

1. Ensure that Java 25 is installed. Open a terminal and run `java -version` to check that it reports version 25.
2. Download the application JAR from the [EduReserve releases page](https://github.com/AY2627S1-CS2113-W14-2/tp/releases).
   The JAR is named `EduReserve.jar`.
3. Place `EduReserve.jar` in a folder where you want to keep your booking data.
4. Open a terminal in that folder and run:

   ```text
   java -jar EduReserve.jar
   ```

5. The first time you run EduReserve, enter your name when prompted with `What is your name?`. EduReserve saves
   one username in `data/username.txt`. Later runs use the saved username automatically.
6. Try the following commands, pressing Enter after each line:

   ```text
   book r/exam n/CS2113 Midterm v/LT1 t/05/11/2026 14:00 a/80
   list
   viewbooking n/CS2113 Midterm
   checksize
   ```

   These commands create an exam booking for 80 attendees, list your bookings, display the booking's purpose,
   venue and timing, and show the largest recorded attendee count for each venue. Booking times use
   `dd/MM/yyyy HH:mm` with a 24-hour clock. New booking names must be unique, ignoring capitalization.
7. After each command, EduReserve saves your bookings to `data/bookings.txt` and displays the time of the save.
   Enter `bye` to exit. Run the application from the same folder next time to load the saved username and bookings.

## Features 

Words in uppercase, such as `BOOKING_NAME`, are placeholders to replace with your own values.
Square brackets indicate optional fields; do not type the brackets.

### Saving and loading data

EduReserve saves one username and all bookings automatically.
- Enter your username on the first launch. The same username is used the next time EduReserve starts.
- After every command, the bookings are saved and the latest save time is shown.
- Run EduReserve from the same folder again to load the saved username and bookings.

### Creating a booking: `book`

Creates an exam or lecture booking for a named event at a venue.

Format: `book r/BOOKING_REASON n/BOOKING_NAME v/VENUE t/TIMING [a/ATTENDEES]`

* The required fields must appear once each, in the order `r/`, `n/`, `v/`, `t/`.
  The optional `a/ATTENDEES` field must go last. Use lowercase field prefixes.
* `BOOKING_REASON` must be `exam` or `lecture`.
* Names and venues cannot be blank and may contain spaces. The booking name must be unique,
  ignoring capitalization. A duplicate name is rejected without adding a booking.
* `TIMING` must use `dd/MM/yyyy HH:mm` with a 24-hour clock. Each booking lasts one hour.
* A booking is rejected if its hour overlaps another booking in the same venue, ignoring
  capitalization. Starting exactly when the other booking ends is allowed.
* `ATTENDEES`, if supplied, must be a positive whole number.
* A successful booking displays `Booking added:` followed by its details and is saved automatically.
  You can undo it with `/undo` during the same session.

Examples, assuming the names and venue slots are available:

| Command | Result |
|---------|--------|
| `book r/exam n/CS2113 Midterm v/LT1 t/05/11/2026 14:00 a/80` | Books LT1 from 14:00 to 15:00 on 5 November 2026 for an exam with 80 attendees. |
| `book r/lecture n/Week 1 Lecture v/LT1 t/05/11/2026 15:00` | Books the next hour in LT1 for a lecture without an attendee count. |

### Listing bookings: `list`
Displays the bookings, with optional filtering and sorting by name or time and date

Format: `list [r/REASON] [s/SORT_ORDER]`

Square brackets indicate optional arguments, do not type them.

- `REASON` must be either exams or lectures
- `SORT_ORDER` must be the following options `alpha` for A to Z, `~alpha` for Z to A, `time` for earliest to latest time and `~time` for latest to earliest time.
- Alphabetical sorting compares booking names, ignoring capitalization.
- Omitting `r/REASON` shows all bookings.
- Omitting `s/SORT_ORDER` shows the list with original booking orders preserved.
- The arguments may be swapped to appear in any order first, and can only be specified once.
- Time sorting is unavailable if a selected booking lacks a valid date and time.

Examples:

|          Command           |                    Result                     |
|:--------------------------:|:---------------------------------------------:|
|           `list`           |               Show all bookings               |
|       `list r/exams`       |            Show all exam bookings             |
|       `list s/alpha`       |     Show all bookings sorted from A to Z      |
| `list r/lectures s/~alpha` |     Show all lectures sorted from Z to A      |
|   `list r/exams s/time`    | Show all exams sorted from earliest to latest |

### List available venues: `free`
Displays a list of all available venues at a user specified time

Format: `free t/TIMING`

- `TIMING` must use the `dd/MM/yyyy HH:mm` format.
- `TIMING` is the start of the one-hour slot being checked.
- A venue is excluded if an existing booking overlaps the requested hour.
- Bookings ending exactly when the requested slot starts are allowed.

Examples:

|          Command          |                              Result                              |
|:-------------------------:|:----------------------------------------------------------------:|
| `free t/28/10/2026 14:00` | Show all available venues from 14:00 to 15:00 on 28 October 2026 |

### Viewing a booking: `viewbooking`

Displays the purpose, venue and timing of every booking with the specified full name.

Format: `viewbooking n/BOOKING_NAME [s/time]`

* The name is required and may contain spaces or punctuation. Matching ignores capitalization but requires
  the full name, rather than part of it.
* If several bookings share the same name, all matches are displayed in their original booking order.
* Add `s/time` to display matches in chronological order, earliest first. This is the only supported sort
  option for `viewbooking`.
* If a matching booking has an older saved timing without a full date and time, sorting shows an error.
  Omit `s/time` to view it, or use `editbooking` to update its timing.
* If no booking matches, the application displays `No booking found with the name: BOOKING_NAME`.
  Viewing does not change any bookings.

Examples:

| Command | Result |
|---------|--------|
| `viewbooking n/CS2113 Midterm` | Shows all bookings named "CS2113 Midterm" in booking order. |
| `viewbooking n/cs2113 midterm s/time` | Shows the same matches in chronological order. |

### Checking attendee counts by venue: `checksize`

Shows the largest recorded attendee count for each venue across your saved bookings. This value describes
the recorded group sizes; it does not indicate the venue's actual capacity or available seats.

Format: `checksize`

* The command takes no arguments.
* Record an attendee count using the optional `a/ATTENDEES` field when creating or editing a booking.
  The count must be a positive whole number.
* Bookings without an attendee count are ignored. Venues with no recorded counts are omitted.
* Venue names are grouped without regard to capitalization, so `LT1` and `lt1` refer to the same venue.
  Results are displayed alphabetically by venue.
* If no booking has an attendee count, the application displays `No bookings include an attendee count.`

Example: if LT1 has bookings for 40 and 80 attendees, and LT2 has a booking for 65 attendees, entering
`checksize` displays:

```text
Largest recorded attendee count by venue:
LT1: 80 attendees
LT2: 65 attendees
```

### Editing a booking: `editbooking`

Updates an existing booking's purpose, name, venue, timing or attendee count.

Start editing with: `editbooking n/BOOKING_NAME`

1. Enter the full booking name. Matching ignores capitalization. If no booking matches, an error is displayed.
2. If several bookings match, enter the number of the booking from the displayed matching list and press Enter.
   If only one booking matches, it is selected automatically.
3. At the edit prompt, enter at least one of `r/`, `n/`, `v/` or `t/` on a single line and press Enter.
   You may also append an attendee count:

   ```text
   [r/BOOKING_REASON] [n/NEW_NAME] [v/VENUE] [t/TIMING] [a/ATTENDEES]
   ```

   The `r/`, `n/`, `v/` and `t/` fields may appear in any order, at most once each. If supplied,
   `a/ATTENDEES` must go last and cannot be entered on its own. To change only the attendee count,
   repeat an existing field, for example `v/LT1 a/100`. Omitted fields keep their existing values.

* `BOOKING_REASON` must be `exam` or `lecture`.
* Names, venues and timings cannot be blank. Timings must use `dd/MM/yyyy HH:mm` with a 24-hour clock,
  and attendee counts must be positive whole numbers.
* Each booking lasts one hour from its start time. An edit is rejected if that hour overlaps another booking
  in the same venue, ignoring capitalization. For example, a booking from 14:00 to 15:00 prevents an edit to
  14:30 in that venue, but allows one starting at 15:00. Invalid changes leave the original booking unchanged,
  and you can retry at the edit prompt.
* Type `abort` during selection or editing to cancel without applying changes. Type `bye` to exit without
  applying the pending changes. Finish or abort editing before entering other commands.
* A successful edit displays `Booking updated:` followed by the updated booking and is saved automatically.

Example, assuming the booking from Quick Start exists: enter each line separately.

```text
editbooking n/CS2113 Midterm
v/LT2 t/06/11/2026 15:00 a/100
```

This moves the booking to LT2 on 6 November 2026 at 15:00 and changes the attendee count to 100.
Its name and purpose stay the same.

### Cancelling a booking: `cancel`

Removes a booking so that its venue and time can be used again, for example when a class is cancelled.

Format: `cancel n/BOOKING_NAME`

* The name is required and may contain spaces. Matching ignores capitalization but requires the full name.
* If several bookings share the same name, only the first one in the list is cancelled.
* If no booking has that name, the application displays `No booking found with the name: BOOKING_NAME`
  and your bookings are left unchanged.
* A successful cancellation displays `Booking cancelled:` followed by the cancelled booking. You can restore it
  with `/undo` during the same session.

Examples:

| Command | Result |
|---------|--------|
| `cancel n/CS2113 Midterm` | Cancels the booking named "CS2113 Midterm". |
| `cancel n/cs2113 midterm` | Cancels the same booking, as capitalization is ignored. |
| `cancel n/Unknown Name` | Displays `No booking found with the name: Unknown Name`. Your bookings are unchanged. |

### Undoing a command: `/undo`

Restores the booking list to its state before the latest successful `book` or `cancel` command.

Format: `/undo`

* Include the leading `/`. The command takes no arguments.
* Only successful `book` and `cancel` commands are recorded. Failed commands and viewing commands
  do not add entries to the history. `editbooking` is not recorded.
* Repeat `/undo` to undo earlier recorded commands, most recent first. Cancelled bookings return
  to their original positions in the list.
* Undo restores the entire saved booking list. Any edits made with `editbooking` after that
  saved state are also lost; edits cannot be undone individually.
* A successful undo displays `Undid command:` followed by the original command and saves the restored list.
* If the history is empty, the application displays `No commands to undo.` and leaves bookings unchanged.
* History is available only during the current session and is cleared when you exit. Finish or abort
  any pending edit before using `/undo`.

Examples (each row is a separate scenario):

| Command | Result |
|---------|--------|
| `/undo` after a successful `book` | Restores the list from before that booking was added. |
| `/undo` after `cancel n/CS2113 Midterm` succeeds | Restores the list including the cancelled booking. |

### Redoing a command: `/redo`

Restores the booking list to its state after the most recently undone command.

Format: `/redo`

* Include the leading `/`. The command takes no arguments.
* Use `/undo` first. Repeat `/redo` to reapply multiple undone commands in the reverse order
  in which they were undone. A redone command can be undone again.
* A successful new `book` or `cancel` clears all remaining redo history. Failed commands and
  viewing commands do not clear it.
* Redo restores the entire saved booking list, including the booking order. Edits made with
  `editbooking` since the saved state can be overwritten; editing does not clear redo history.
* A successful redo displays `Redid command:` followed by the original command and saves the restored list.
* If there is nothing to redo, the application displays `No commands to redo.` and leaves bookings unchanged.
* Redo history is available only during the current session. Finish or abort any pending edit
  before using `/redo`.

Examples (each row is a separate scenario):

| Command | Result |
|---------|--------|
| `/redo` after undoing a successful `book` | Restores the list including the added booking. |
| `/redo` after undoing a successful `cancel` | Restores the list with that booking removed again. |

### Finding bookings: `find`

Shows every booking whose name contains a keyword. Use it when you only remember part of a name.

Format: `find n/KEYWORD`

* The keyword is required and may contain spaces. Matching ignores capitalization.
* A booking matches if its name contains the keyword anywhere, not only at the start. This differs from
  `viewbooking`, which needs the full name.
* Matches are numbered and shown in their original booking order, with each booking's details.
* If nothing matches, the application displays `No bookings found matching: KEYWORD`.
  Finding never changes your bookings.

Examples, assuming bookings named "CS2113 Midterm" and "CS2445 Final Exam" exist:

| Command | Result |
|---------|--------|
| `find n/CS2` | Shows both "CS2113 Midterm" and "CS2445 Final Exam". |
| `find n/CS2113` | Shows only "CS2113 Midterm". |
| `find n/final exam` | Shows only "CS2445 Final Exam". |
| `find n/no such module` | Displays `No bookings found matching: no such module`. |

### Viewing today's bookings: `calendar`

Shows the bookings that start today, so you can see the day at a glance.

Format: `calendar`

* The command takes no arguments. Extra words are rejected.
* Today's date is read from your computer's clock.
* Bookings are shown earliest first, with their start time, purpose, name and venue.
* A booking appears on the day it starts, even if its one-hour slot runs past midnight.
* Bookings saved with an older timing that has no full date are not shown.
* If nothing starts today, the application displays `No bookings on dd/MM/yyyy.`

Example: if you have two bookings today, entering `calendar` displays:

```text
Calendar for 05/10/2026:
1. 09:00 - lecture - Week 1 Lecture (venue: LT2)
2. 14:00 - exam - CS2113 Midterm (venue: LT1)
```

Examples:

| Command | Result |
|---------|--------|
| `calendar` | Shows today's bookings, earliest first. |
| `calendar` on a day with no bookings | Displays `No bookings on dd/MM/yyyy.` |
| `calendar tomorrow` | Rejected, because `calendar` takes no extra words. Use `calendar` on its own. |

### Avoiding booking clashes

EduReserve stops two bookings from using the same venue at the same time when you use `book`.

* Every booking lasts one hour from its start time. A booking at 12:00 uses its venue from 12:00 to 13:00.
* A new booking is rejected if it is in the same venue, ignoring capitalization, and its hour overlaps an
  existing booking's hour. The application displays `Booking clash:` followed by the venue, time and name of the
  existing booking, and nothing is added.
* A booking that starts exactly when another ends is allowed, so 12:00 and 13:00 can both be booked in `LT1`.
* The same rule applies when you change a booking with `editbooking`, and `free` uses it when it lists
  available venues.
* Cancelling the existing booking frees its venue and time again.

Examples, assuming `book r/exam n/CS2113 Midterm v/LT1 t/05/10/2026 12:00` already exists:

| Command | Result |
|---------|--------|
| `book r/lecture n/Week 1 v/lt1 t/05/10/2026 12:00` | Rejected: LT1 is already booked at that time. |
| `book r/lecture n/Week 1 v/LT1 t/05/10/2026 12:01` | Rejected: overlaps the 12:00 to 13:00 booking. |
| `book r/lecture n/Week 1 v/LT1 t/05/10/2026 13:00` | Added: starts as the first booking ends. |
| `book r/lecture n/Week 1 v/LT2 t/05/10/2026 12:00` | Added: a different venue. |

## FAQ

**Q**: How do I transfer my data to another computer? 

**A**: {your answer here}

## Command Summary

| Action                         | Command                                                                           |
|--------------------------------|-----------------------------------------------------------------------------------|
| Create a booking               | `book r/BOOKING_REASON n/BOOKING_NAME v/VENUE t/TIMING [a/ATTENDEES]`                 |
| Undo a booking or cancellation | `/undo`                                                                           |
| Redo an undone command         | `/redo`                                                                           |
| View a booking                 | `viewbooking n/BOOKING_NAME [s/time]`                                             |
| Check attendee counts by venue | `checksize`                                                                       |
| Edit a booking                 | `editbooking n/BOOKING_NAME`, then enter the fields to change at the edit prompt. |
| Cancel a pending edit          | `abort` (during selection or editing)                                             |
| List bookings                  | `list [r/REASON] [s/SORT_ORDER]`                                                  |
| Find available venues          | `free t/dd/MM/yyyy 
| Cancel a booking               | `cancel n/BOOKING_NAME`                                                           |
| Find bookings by name          | `find n/KEYWORD`                                                                  |
| View today's bookings          | `calendar`                                                                        |HH:mm`                                                         |

