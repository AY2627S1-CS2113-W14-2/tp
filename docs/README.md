# EduReserve

[**Home**](README.md) | [User Guide](UserGuide.md) | [Developer Guide](DeveloperGuide.md) | [About Us](AboutUs.md)

## Venue booking, made simple

EduReserve is a command-line application that helps school administrators manage lecture and exam venue bookings.
Create bookings, check availability, and keep venue schedules organised from one place.

Built with Java for CS2113 at the [National University of Singapore](https://www.nus.edu.sg/).

[Get started](UserGuide.md#quick-start) · [Explore the features](UserGuide.md#features) · [Meet the team](AboutUs.md)

## What you can do

- **Create bookings** for exams and lectures, with optional attendee counts.
- **Check venue availability** for a time or browse available hourly slots for a venue and date.
- **View and edit bookings** by their full event name, including sorting matching bookings by time.
- **Review attendee counts** to see the largest recorded group for each venue.
- **Save your schedule automatically** and load it again the next time you start EduReserve.

See the [User Guide](UserGuide.md) for command formats and examples.

## Get started

1. Install Java 25 and check it with `java -version`.
2. Download `EduReserve.jar` from the [EduReserve releases page](https://github.com/AY2627S1-CS2113-W14-2/tp/releases).
3. Put the JAR in the folder where you want to keep your booking data, open a terminal in that folder, and run:

   ```text
   java -jar EduReserve.jar
   ```

4. Enter your name when prompted, then try these commands one at a time:

   ```text
   book r/exam n/CS2113 Midterm v/LT1 t/05/11/2026 14:00 a/80
   list
   viewbooking n/CS2113 Midterm
   checksize
   ```

EduReserve saves bookings after each command. The [Quick Start](UserGuide.md#quick-start) explains the first run and
how to resume with your saved data.

## Project links

- [User Guide](UserGuide.md) — installation, quick start and command reference.
- [Developer Guide](DeveloperGuide.md) — design, implementation and project setup.
- [About Us](AboutUs.md) — meet the team behind EduReserve.
- [Project repository](https://github.com/AY2627S1-CS2113-W14-2/tp)
