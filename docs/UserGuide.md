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
* An edit is rejected if another booking uses the same venue, ignoring capitalization, at exactly the
  same date and time. Invalid changes leave the original booking unchanged, and you can retry at the edit prompt.
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

## FAQ

**Q**: How do I transfer my data to another computer? 

**A**: {your answer here}

## Command Summary

| Action                         | Command                                                                           |
|--------------------------------|-----------------------------------------------------------------------------------|
| View a booking                 | `viewbooking n/BOOKING_NAME [s/time]`                                             |
| Check attendee counts by venue | `checksize`                                                                       |
| Edit a booking                 | `editbooking n/BOOKING_NAME`, then enter the fields to change at the edit prompt. |
| Cancel a pending edit          | `abort` (during selection or editing)                                             |
| List bookings                  | `list [r/REASON] [s/SORT_ORDER]`                                                  |
| Find available venues          | `free t/dd/MM/yyyy HH:mm`                                                         |

