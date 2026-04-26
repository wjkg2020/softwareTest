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
        // 清理测试用的系统属性，防止干扰
        System.clearProperty("jpass.test.string");
        System.clearProperty("jpass.test.int");
        System.clearProperty("jpass.test.bool");
        System.clearProperty("jpass.test.array");
    }

    // --- 1. String 类型测试 (EP + EG) ---
    @ParameterizedTest
    @MethodSource("provideStringTestCases")
    public void testGetString(String key, String sysValue, String defaultValue, String expected) {
        if (sysValue != null) {
            System.setProperty("jpass." + key, sysValue);
        }
        assertEquals(expected, config.get(key, defaultValue));
    }

    static Stream<Arguments> provideStringTestCases() {
        // 预先生成长字符串
        String longStr = new String(new char[1000]).replace('\0', 'a');

        return Stream.of(
                Arguments.of("test.string", "hello", "default", "hello"),
                Arguments.of("test.string", null, "default", "default"),
                Arguments.of("test.string", null, null, null),
                Arguments.of("test.string", "", "default", ""),
                // 使用 Java 8 兼容的长字符串
                Arguments.of("test.string", longStr, "default", longStr)
        );
    }

    // --- 2. Integer 类型测试 (EP + BA + EG) ---
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
                // EP: 正常数值解析
                Arguments.of("test.int", "123", 0, 123),
                // BA: 边界值
                Arguments.of("test.int", String.valueOf(Integer.MAX_VALUE), 0, Integer.MAX_VALUE),
                Arguments.of("test.int", String.valueOf(Integer.MIN_VALUE), 0, Integer.MIN_VALUE),
                // EG: 非法解析（应返回默认值）
                Arguments.of("test.int", "abc", 999, 999),
                Arguments.of("test.int", "12.3", 999, 999),
                // Fallback
                Arguments.of("test.int", null, 42, 42)
        );
    }

    // --- 3. Boolean 类型测试 (EP + BA) ---
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
                // EP: 正常 True/False
                Arguments.of("test.bool", "true", false, true),
                Arguments.of("test.bool", "false", true, false),
                // BA: 大小写不敏感测试 (Boolean.valueOf 行为)
                Arguments.of("test.bool", "TRUE", false, true),
                Arguments.of("test.bool", "TrUe", false, true),
                // EG: 非法字符串（Boolean.valueOf 默认返回 false）
                Arguments.of("test.bool", "not-a-boolean", true, false),
                // Fallback
                Arguments.of("test.bool", null, true, true)
        );
    }

    // --- 4. Array 类型测试 (Combinatorial / EP) ---
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
                // EP: 标准 CSV
                Arguments.of("test.array", "a,b,c", null, new String[]{"a", "b", "c"}),
                // EP: 单个值
                Arguments.of("test.array", "onlyone", null, new String[]{"onlyone"}),
                // BA: 包含空格 (注意：代码里没做 trim，所以空格应该被保留)
                Arguments.of("test.array", "a, b , c", null, new String[]{"a", " b ", " c"}),
                // BA: 连续逗号
                Arguments.of("test.array", "a,,c", null, new String[]{"a", "", "c"}),
                // Fallback
                Arguments.of("test.array", null, new String[]{"def"}, new String[]{"def"})
        );
    }

    // --- 5. 组合测试 (Combinatorial) ---
    /**
     * 模拟多重环境影响。
     */
    @Test
    public void testPrecedenceOrder() {
        // 虽然单例不好模拟文件加载，但我们可以验证系统属性 100% 覆盖其他设置
        String key = "test.precedence";
        System.setProperty("jpass." + key, "from-system");

        assertEquals("from-system", config.get(key, "from-default"));
    }
}