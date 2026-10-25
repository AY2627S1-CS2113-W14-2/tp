import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Runs the real application in temporary folders so tests never touch the user's saved bookings. */
class EduReserveTest {
    @TempDir
    Path workingDirectory;

    @Test
    void main_validName_printsEduReserveGreeting() throws Exception {
        String response = runApplication("James\nbye\n");

        assertTrue(response.contains("Hello, James! Welcome to EduReserve."), response);
        assertFalse(response.contains("Duke"), response);
        assertTrue(Files.isRegularFile(workingDirectory.resolve("data/bookings.txt")));
    }

    @Test
    void main_noInput_exitsBeforeCreatingData() throws Exception {
        String response = runApplication("");

        assertTrue(response.contains("What is your name?"), response);
        assertFalse(response.contains("Hello,"), response);
        assertFalse(Files.exists(workingDirectory.resolve("data")));
    }

    @Test
    void main_blankName_reportsErrorWithoutCreatingData() throws Exception {
        String response = runApplication("  \t\n");

        assertTrue(response.contains("Name cannot be blank."), response);
        assertFalse(response.contains("Welcome to EduReserve"), response);
        assertFalse(Files.exists(workingDirectory.resolve("data")));
    }

    @Test
    void main_endOfInputAfterName_exitsNormally() throws Exception {
        String response = runApplication("  James  \n");

        assertTrue(response.contains("Hello, James! Welcome to EduReserve."), response);
        assertTrue(response.contains("How to use EduReserve:"), response);
        assertFalse(response.contains("Unable to"), response);
    }

    @Test
    void main_blankLinesAndCommands_persistBookingAndStopAtBye() throws Exception {
        String response = runApplication("James\n\n \t\n"
                + "book r/exam n/Midterm v/LT1 t/05/10/2026 14:00\n"
                + "bye\nbook r/lecture n/Should Not Run v/LT2 t/05/10/2026 15:00\n");

        assertTrue(response.contains("Booking added: [B] exam - Midterm"), response);
        assertFalse(response.contains("is not a valid command"), response);
        assertFalse(response.contains("Should Not Run"), response);
        assertEquals("B | exam | Midterm | LT1 | 05/10/2026 14:00\n",
                Files.readString(workingDirectory.resolve("data/bookings.txt")).replace("\r\n", "\n"));
    }

    @Test
    void main_invalidSavedData_reportsLoadFailureAndPreservesFile() throws Exception {
        Path dataFile = workingDirectory.resolve("data/bookings.txt");
        Files.createDirectories(dataFile.getParent());
        Files.writeString(dataFile, "invalid saved data");

        String response = runApplication("James\nbye\n");

        assertTrue(response.contains("Unable to load your bookings."), response);
        assertFalse(response.contains("How to use EduReserve:"), response);
        assertEquals("invalid saved data", Files.readString(dataFile));
    }

    @Test
    void main_dataFileBecomesUnwritable_reportsSaveFailureAndStillExits() throws Exception {
        Process process = startApplication();
        try {
            process.getOutputStream().write("James\n".getBytes(StandardCharsets.UTF_8));
            process.getOutputStream().flush();
            awaitCommandPrompt(process);

            // Only the child process's temporary data file is replaced, after loading has finished.
            Path dataFile = workingDirectory.resolve("data/bookings.txt");
            Files.delete(dataFile);
            Files.createDirectory(dataFile);
            process.getOutputStream().write("bye\n".getBytes(StandardCharsets.UTF_8));
            process.getOutputStream().close();

            String response = awaitExit(process);
            assertTrue(response.contains("Unable to save your bookings."), response);
            assertTrue(Files.isDirectory(dataFile));
        } finally {
            stopIfRunning(process);
        }
    }

    /** Starts a separate Java process with an isolated working directory and optional coverage instrumentation. */
    private Process startApplication() throws Exception {
        ArrayList<String> command = new ArrayList<>();
        command.add(Path.of(System.getProperty("java.home"), "bin", "java").toString());
        // Append child-process coverage to the current JaCoCo session when coverage is enabled.
        for (String argument : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
            if (argument.startsWith("-javaagent:") && argument.contains("jacoco")) {
                command.add(coverageAgentWithAbsoluteDestination(argument));
            }
        }
        command.add("-ea");
        command.add("-cp");
        command.add(Path.of(EduReserve.class.getProtectionDomain().getCodeSource().getLocation().toURI()).toString());
        command.add("EduReserve");
        return new ProcessBuilder(command)
                .directory(workingDirectory.toFile())
                .redirectErrorStream(true)
                .redirectOutput(outputFile().toFile())
                .start();
    }

    /** Keeps the coverage destination in the parent project even though the child uses a temporary folder. */
    private String coverageAgentWithAbsoluteDestination(String argument) {
        String option = "destfile=";
        int start = argument.indexOf(option) + option.length();
        int end = argument.indexOf(',', start);
        String destination = argument.substring(start, end < 0 ? argument.length() : end);
        return argument.replace(option + destination, option + Path.of(destination).toAbsolutePath())
                .replace("append=false", "append=true");
    }

    /** Feeds a complete session to the child and ensures it terminates even when an assertion fails. */
    private String runApplication(String input) throws Exception {
        Process process = startApplication();
        try {
            process.getOutputStream().write(input.getBytes(StandardCharsets.UTF_8));
            process.getOutputStream().close();
            return awaitExit(process);
        } finally {
            stopIfRunning(process);
        }
    }

    /** Waits for normal exit with a timeout so a broken input loop cannot hang the test suite. */
    private String awaitExit(Process process) throws Exception {
        assertTrue(process.waitFor(10, TimeUnit.SECONDS), "Application did not exit within 10 seconds");
        String response = Files.readString(outputFile());
        assertEquals(0, process.exitValue(), response);
        return response;
    }

    /** Waits until loading is complete before deliberately making the temporary save destination invalid. */
    private void awaitCommandPrompt(Process process) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (System.nanoTime() < deadline && process.isAlive()) {
            if (Files.readString(outputFile()).contains("bye: exit EduReserve")) {
                return;
            }
            Thread.sleep(20);
        }
        throw new AssertionError("Application did not reach its command prompt:\n" + Files.readString(outputFile()));
    }

    private Path outputFile() {
        return workingDirectory.resolve("output.txt");
    }

    @Test
    void main_editSelectedDuplicate_persistsOnlySelectedChangeAcrossRestart() throws Exception {
        String response = runApplication("James\n"
                + "book r/exam n/Other v/LT0 t/05/10/2026 14:00\n"
                + "book r/exam n/Midterm v/LT1 t/05/10/2026 14:00\n"
                + "book r/exam n/Midterm v/LT2 t/05/10/2026 14:00\n"
                + "editbooking n/midterm\n2\nv/LT3\nbye\n");

        assertTrue(response.contains("Booking updated: [B] exam - Midterm (venue: LT3"), response);
        assertEquals("B | exam | Other | LT0 | 05/10/2026 14:00\n"
                + "B | exam | Midterm | LT1 | 05/10/2026 14:00\n"
                + "B | exam | Midterm | LT3 | 05/10/2026 14:00\n",
                Files.readString(workingDirectory.resolve("data/bookings.txt")).replace("\r\n", "\n"));

        String restarted = runApplication("James\nviewbooking n/Midterm\nbye\n");
        assertTrue(restarted.contains("venue: LT3"), restarted);
        assertFalse(restarted.contains("venue: LT2"), restarted);
    }

    /** Stops and reaps a timed-out child before JUnit removes its temporary directory. */
    private void stopIfRunning(Process process) throws InterruptedException, IOException {
        if (process.isAlive()) {
            process.destroyForcibly();
            assertTrue(process.waitFor(10, TimeUnit.SECONDS), "Application process could not be stopped");
        }
        process.getInputStream().close();
        process.getErrorStream().close();
        process.getOutputStream().close();
    }
}
