import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

/**
 * Checks the startup greeting shown by EduReserve.
 */
class EduReserveTest {
    @Test
    void main_validName_printsEduReserveGreeting() {
        InputStream originalInput = System.in;
        PrintStream originalOutput = System.out;
        ByteArrayInputStream input = new ByteArrayInputStream(
                "James\nbye\n".getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try (PrintStream capturedOutput =
                     new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setIn(input);
            System.setOut(capturedOutput);
            EduReserve.main(new String[0]);
        } finally {
            System.setIn(originalInput);
            System.setOut(originalOutput);
        }

        String response = output.toString(StandardCharsets.UTF_8);
        assertTrue(response.contains("EduReserve"));
        assertTrue(response.contains("Hello, James! Welcome to EduReserve."));
        assertFalse(response.contains("Duke"));
    }
}
