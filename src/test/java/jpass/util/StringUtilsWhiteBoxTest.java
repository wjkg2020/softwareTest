package jpass.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class StringUtilsWhiteBoxTest {

    @Test
    public void testStripStringBranchCoverage() {
        
        assertNull(StringUtils.stripString(null, 5));
        assertEquals("abc", StringUtils.stripString("abc", 5));
        assertEquals("ab...", StringUtils.stripString("abcde", 2));
    }

    @Test
    public void testStripNonValidXmlBranchCoverage() {

        assertNull(StringUtils.stripNonValidXMLCharacters(null));
        assertEquals("", StringUtils.stripNonValidXMLCharacters(""));
        char tab = 0x9;
        char standardChar = 0x20;
        char highValidChar = 0xE000;
        String validString = "" + tab + standardChar + highValidChar;
        assertEquals(validString, StringUtils.stripNonValidXMLCharacters(validString));
        char invalidChar = 0xB;
        assertEquals("?", StringUtils.stripNonValidXMLCharacters("" + invalidChar));
    }
}
