package jpass.util;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.stream.Stream;

/**
 * Blackbox Testing for Configuration.
 * Focusing on Priority Logic (System Prop vs Default) and Type Parsing.
 */
public class ConfigurationBlackBoxTest {

    private Configuration config;

    @BeforeEach
    public void setup() {
        config = Configuration.getInstance();
    }

    @AfterEach
    public void tearDown() {
        System.clearProperty("jpass.test.string");
        System.clearProperty("jpass.test.int");
        System.clearProperty("jpass.test.bool");
        System.clearProperty("jpass.test.array");
    }

    // --- 1. String  (EP + EG) ---
    @ParameterizedTest
    @MethodSource("provideStringTestCases")
    public void testGetString(String key, String sysValue, String defaultValue, String expected) {
        if (sysValue != null) {
            System.setProperty("jpass." + key, sysValue);
        }
        assertEquals(expected, config.get(key, defaultValue));
    }

    static Stream<Arguments> provideStringTestCases() {
        String longStr = new String(new char[1000]).replace('\0', 'a');

        return Stream.of(
                Arguments.of("test.string", "hello", "default", "hello"),
                Arguments.of("test.string", null, "default", "default"),
                Arguments.of("test.string", null, null, null),
                Arguments.of("test.string", "", "default", ""),
                Arguments.of("test.string", longStr, "default", longStr)
        );
    }

    // --- 2. Integer  (EP + BA + EG) ---
    @ParameterizedTest
    @MethodSource("provideIntegerTestCases")
    public void testGetInteger(String key, String sysValue, Integer defaultValue, Integer expected) {
        if (sysValue != null) {
            System.setProperty("jpass." + key, sysValue);
        }
        assertEquals(expected, config.getInteger(key, defaultValue));
    }

    static Stream<Arguments> provideIntegerTestCases() {
        return Stream.of(
                // EP:
                Arguments.of("test.int", "123", 0, 123),
                // BA: 
                Arguments.of("test.int", String.valueOf(Integer.MAX_VALUE), 0, Integer.MAX_VALUE),
                Arguments.of("test.int", String.valueOf(Integer.MIN_VALUE), 0, Integer.MIN_VALUE),
                // EG: 
                Arguments.of("test.int", "abc", 999, 999),
                Arguments.of("test.int", "12.3", 999, 999),
                // Fallback
                Arguments.of("test.int", null, 42, 42)
        );
    }

    // --- 3. Boolean  (EP + BA) ---
    @ParameterizedTest
    @MethodSource("provideBooleanTestCases")
    public void testIsBoolean(String key, String sysValue, Boolean defaultValue, Boolean expected) {
        if (sysValue != null) {
            System.setProperty("jpass." + key, sysValue);
        }
        assertEquals(expected, config.is(key, defaultValue));
    }

    static Stream<Arguments> provideBooleanTestCases() {
        return Stream.of(
                // EP:  True/False
                Arguments.of("test.bool", "true", false, true),
                Arguments.of("test.bool", "false", true, false),
                // BA:  (Boolean.valueOf )
                Arguments.of("test.bool", "TRUE", false, true),
                Arguments.of("test.bool", "TrUe", false, true),
                // EG: （Boolean.valueOf  false）
                Arguments.of("test.bool", "not-a-boolean", true, false),
                // Fallback
                Arguments.of("test.bool", null, true, true)
        );
    }

    // --- 4. Array  (Combinatorial / EP) ---
    @ParameterizedTest
    @MethodSource("provideArrayTestCases")
    public void testGetArray(String key, String sysValue, String[] defaultValue, String[] expected) {
        if (sysValue != null) {
            System.setProperty("jpass." + key, sysValue);
        }
        assertArrayEquals(expected, config.getArray(key, defaultValue));
    }

    static Stream<Arguments> provideArrayTestCases() {
        return Stream.of(
                // EP:  CSV
                Arguments.of("test.array", "a,b,c", null, new String[]{"a", "b", "c"}),
                // EP: 
                Arguments.of("test.array", "onlyone", null, new String[]{"onlyone"}),
                // BA:  
                Arguments.of("test.array", "a, b , c", null, new String[]{"a", " b ", " c"}),
                // BA: 
                Arguments.of("test.array", "a,,c", null, new String[]{"a", "", "c"}),
                // Fallback
                Arguments.of("test.array", null, new String[]{"def"}, new String[]{"def"})
        );
    }

    
    @Test
    public void testPrecedenceOrder() {
       
        String key = "test.precedence";
        System.setProperty("jpass." + key, "from-system");

        assertEquals("from-system", config.get(key, "from-default"));
    }
}
