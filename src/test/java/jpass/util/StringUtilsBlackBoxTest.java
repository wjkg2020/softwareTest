package jpass.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class StringUtilsBlackBoxTest {

    @Test
    public void testStripStringBoundaries() {

        String text10 = "1234567890";
        assertEquals("123456789", StringUtils.stripString("123456789", 10), "Should not strip short strings");
        assertEquals(text10, StringUtils.stripString(text10, 10), "Should not strip exact length strings");
        assertEquals("1234567890...", StringUtils.stripString(text10 + "1", 10), "Should append ... when over length");
    }

    @Test
    public void testStripStringErrorGuessing() {

        assertNull(StringUtils.stripString(null, 10), "Null input should return null");
        assertThrows(StringIndexOutOfBoundsException.class, () -> {
            StringUtils.stripString("test", -1);
        }, "Negative length should cause out of bounds exception");
    }

    @Test
    public void testCombinatorialXmlFilter() {

        char validChar = 'A';
        char invalidChar = 0x01;
        String mixed = "Test" + invalidChar + validChar;
        String result = StringUtils.stripNonValidXMLCharacters(mixed);
        assertEquals("Test?A", result, "Invalid chars should be replaced by ?, valid chars kept");
        assertEquals("", StringUtils.stripNonValidXMLCharacters(""));
        assertNull(StringUtils.stripNonValidXMLCharacters(null));
    }
}
