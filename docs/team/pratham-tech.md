# Pratham Dang - Project Portfolio Page

## Overview

EduReserve is a command-line application that helps school administrators book and manage lecture and exam venues.
Administrators type short commands to create bookings, cancel them when a class is cancelled, search for them and see
what is happening today, while the app stops two classes from using the same venue at the same time. It is written in
Java and saves each user's bookings to a file.

### Summary of Contributions
* **Code contributed**: [RepoSense link](https://nus-cs2113-ay2627-s1.github.io/tp-dashboard/?search=pratham-tech&breakdown=true)

#### Contributions to team-based tasks

* **Merged teammates' pull requests** into `master`, checking that the build and Checkstyle still passed after
  each merge.
* **Coordinated with the team** on who owned which commands and files, and on shared rules between commands.
* **Helped teammates with their implementations**, including their questions and ideas related to their commands and tasks.
* **Contributed ideas during brainstorming**, including the design considerations for the features.
* **Wrote the Developer Guide glossary for the whole project**, covering all commands and key terms, not only my
  own features.

* Review/ mentoring contributions: 
* Reviewed PR [#113](https://github.com/AY2627S1-CS2113-W14-2/tp/pull/113) Fixed DG formatting issues
* Reviewed PR [#96](https://github.com/AY2627S1-CS2113-W14-2/tp/pull/96) Fixed class diagram and added functions in user guide
* Reviewed PR [#86](https://github.com/AY2627S1-CS2113-W14-2/tp/pull/86) Class diagram, standardised sequence diagrams and PPP
* Reviewed PR [#69](https://github.com/AY2627S1-CS2113-W14-2/tp/pull/69) Added sorting by time in list command
* Reviewed PR [#57](https://github.com/AY2627S1-CS2113-W14-2/tp/pull/57) Updated developer guide user stories
* Reviewed PR [#41](https://github.com/AY2627S1-CS2113-W14-2/tp/pull/41) Implemented additional sort option for multiple bookings under same name
* Reviewed PR [#30](https://github.com/AY2627S1-CS2113-W14-2/tp/pull/30) Added assertions for view booking features
* Reviewed PR [#27](https://github.com/AY2627S1-CS2113-W14-2/tp/pull/27) Assertions - Add Bookings
* Reviewed PR [#3](https://github.com/AY2627S1-CS2113-W14-2/tp/pull/3) changed the about us

#### Features implemented

* **Cancel a booking** (`cancel n/BOOKING_NAME`): removes a booking by its name, ignoring capitalization, so that the
  venue and time can be used again. If no booking has that name, an error is shown and the list is left unchanged.
  This supports the user story of cancelling a booking when a class is cancelled.
* **Find bookings** (`find n/KEYWORD`): lists every booking whose name contains the keyword anywhere in it, ignoring
  capitalization, with its venue and timing. For example, `find n/CS2` shows both "CS2113 Midterm" and
  "CS2445 Final Exam", while `find n/CS2113` shows only the first. The list is never changed by a search.
* **Booking clash prevention** (part of `book`): rejects a new booking when the same venue is already booked at an
  overlapping time. Every booking lasts 1 hour from its start time, so a booking at 12:00 occupies 12:00 to 13:00 and
  a booking at 12:01 in the same venue is rejected, while one at 13:00 is allowed. I defined the rule once in
  `Booking#overlaps` and reused it in `findClash` (used by `book`), `isVenueAvailable` (used by `free`) and the clash
  check in `editbooking`, so all three commands always agree.
* **Daily calendar** (`calendar`): shows the bookings that start today in order of start time, with the start time,
  purpose, name and venue of each booking.

#### Defensive programming and testing

* Added assertions to the cancel, find, clash and calendar code, for example checking that an index is inside the
  list, that a keyword is not blank, that a removal shrinks the list by exactly one, and that read-only commands
  leave the list unchanged. Mistakes the user can make, such as a missing name, are still reported as
  `InvalidCommandException`.
* Wrote JUnit tests for these features, including `FindCommandTest` and `CalendarCommandTest`, tests for
  `Booking#overlaps` and `BookingList#findClash` that check the boundaries of the 1-hour slot, and tests for the
  `cancel`, `find`, `calendar` and clash cases in `ParserTest` and `UiTest`.

#### Contributions to the Developer Guide

* Wrote the sections for `cancel`, `find`, booking clash prevention and `calendar`, including examples and the
  implementation of each, and a design consideration for each of cancel, find and clash prevention.
* Drew the sequence diagram for the `calendar`, `find`, `cancel` commands along with `clash prevention` too.
* Added the user stories for finding bookings, preventing clashes and the daily calendar.

#### Contributions to the User Guide

* Wrote the sections for `cancel`, `find`, `calendar` and booking clash prevention, each with its format, rules and
  examples, and added them to the Command Summary.
* Filled in the Developer Guide glossary for the whole project.