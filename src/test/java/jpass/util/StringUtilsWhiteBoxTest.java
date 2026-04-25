package jpass.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class StringUtilsWhiteBoxTest {

    @Test
    public void testStripStringBranchCoverage() {
        // Branch 1: text == null (Fails first condition of the 'if')
        assertNull(StringUtils.stripString(null, 5));

        // Branch 2: text != null is TRUE, but text.length() > length is FALSE
        assertEquals("abc", StringUtils.stripString("abc", 5));

        // Branch 3: Both conditions are TRUE (Executes the substring block)
        assertEquals("ab...", StringUtils.stripString("abcde", 2));
    }

    @Test
    public void testStripNonValidXmlBranchCoverage() {
        // Branch 1: Early return conditions (if in == null || in.isEmpty())
        assertNull(StringUtils.stripNonValidXMLCharacters(null));
        assertEquals("", StringUtils.stripNonValidXMLCharacters(""));

        // Branch 2: Hit the massive 'if' condition (Valid Chars)
        // Picking specific hex values straight from the source code's IF statement to force true paths
        char tab = 0x9;
        char standardChar = 0x20;
        char highValidChar = 0xE000;

        String validString = "" + tab + standardChar + highValidChar;
        assertEquals(validString, StringUtils.stripNonValidXMLCharacters(validString));

        // Branch 3: Hit the 'else' block (Invalid Chars)
        // Pick a hex value explicitly excluded by the IF conditions (e.g., 0x0B)
        char invalidChar = 0xB;
        assertEquals("?", StringUtils.stripNonValidXMLCharacters("" + invalidChar));
    }
}