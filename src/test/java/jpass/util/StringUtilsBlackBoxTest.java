package jpass.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class StringUtilsBlackBoxTest {

    @Test
    public void testStripStringBoundaries() {
        // BA (Boundary Analysis): Testing string lengths exactly at, below, and above the target length of 10
        String text10 = "1234567890";

        // Under boundary (9 chars)
        assertEquals("123456789", StringUtils.stripString("123456789", 10), "Should not strip short strings");

        // Exact boundary (10 chars)
        assertEquals(text10, StringUtils.stripString(text10, 10), "Should not strip exact length strings");

        // Over boundary (11 chars)
        assertEquals("1234567890...", StringUtils.stripString(text10 + "1", 10), "Should append ... when over length");
    }

    @Test
    public void testStripStringErrorGuessing() {
        // EG (Error Guessing): Null strings and negative lengths
        assertNull(StringUtils.stripString(null, 10), "Null input should return null");

        // A negative length should logically throw a StringIndexOutOfBoundsException
        assertThrows(StringIndexOutOfBoundsException.class, () -> {
            StringUtils.stripString("test", -1);
        }, "Negative length should cause out of bounds exception");
    }

    @Test
    public void testCombinatorialXmlFilter() {
        // Combinatorial: Mix valid XML characters with invalid ones in the same string
        char validChar = 'A'; // 0x41 (Valid)
        char invalidChar = 0x01; // Not in the allowed XML 1.0 ranges

        String mixed = "Test" + invalidChar + validChar;
        String result = StringUtils.stripNonValidXMLCharacters(mixed);

        // EP: Valid chars stay, invalid chars become '?'
        assertEquals("Test?A", result, "Invalid chars should be replaced by ?, valid chars kept");

        // EP: Empty and Null inputs
        assertEquals("", StringUtils.stripNonValidXMLCharacters(""));
        assertNull(StringUtils.stripNonValidXMLCharacters(null));
    }
}