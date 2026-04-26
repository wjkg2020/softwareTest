package jpass.util;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Blackbox Test Suite for CryptUtils.
 * Covers EP, BA, EG, and Combinatorial Testing.
 */
/*
Defect Report: Improper Input Validation in Crypto Utility

Description: The method getPBKDF2Key lacks input validation for the salt parameter. While it handles null, it fails to intercept zero-length arrays (byte[0]).

Root Cause: The underlying JCE provider throws an InvalidKeySpecException for empty salts, which is then wrapped into an unhandled RuntimeException.

Impact: Potential Application Denial of Service (DoS). If a user or a corrupted configuration file provides an empty salt, the application will terminate unexpectedly.

 */
public class CryptUtilsBlackBoxTest {

    // --- 1. PBKDF2 组合测试 (Combinatorial Testing) ---
    /**
     * 测试目标：验证不同参数组合下的 PBKDF2 密钥生成。
     * 使用笛卡尔积思想覆盖多种输入组合。
     */
    @ParameterizedTest
    @MethodSource("providePbkdf2Combinations")
    public void testPBKDF2Key_Combinatorial(char[] text, byte[] salt, int iterations) {
        if (salt == null || salt.length == 0 || iterations <= 0) {
            // 预期崩溃的情况
            assertThrows(Exception.class, () -> CryptUtils.getPBKDF2Key(text, salt, iterations));
        } else {
            // 正常运行的情况（包含 text == null）
            byte[] key1 = CryptUtils.getPBKDF2Key(text, salt, iterations);
            assertNotNull(key1);
            assertEquals(32, key1.length);

            // 验证确定性
            byte[] key2 = CryptUtils.getPBKDF2Key(text, salt, iterations);
            assertArrayEquals(key1, key2);
        }
    }

    static Stream<Arguments> providePbkdf2Combinations() {
        char[][] texts = {
                "".toCharArray(),
                "password".toCharArray(),
                "P@ssw0rd123!@#".toCharArray(),
                "你好世界".toCharArray() // UTF-8 字符
        };
        byte[][] salts = {
                new byte[0],
                new byte[]{0, 1, 2, 3, 4, 5, 6, 7}, // 64 bits
                "salt_constant_str".getBytes()
        };
        int[] iterations = {1, 1000, 310000};

        // 生成排列组合
        Stream.Builder<Arguments> builder = Stream.builder();
        for (char[] t : texts) {
            for (byte[] s : salts) {
                for (int i : iterations) {
                    builder.add(Arguments.of(t, s, i));
                }
            }
        }
        // 增加 Error Guessing 案例
        builder.add(Arguments.of(null, new byte[16], 1000));
        builder.add(Arguments.of("p".toCharArray(), null, 1000));
        builder.add(Arguments.of("p".toCharArray(), new byte[16], -1));

        return builder.build();
    }

    // --- 2. SHA-256 边界值与等价类测试 (BA + EP) ---
    @ParameterizedTest
    @MethodSource("provideShaTestCases")
    public void testSha256Hash_Variations(char[] text) {
        byte[] hash1 = CryptUtils.getSha256Hash(text);
        byte[] hash2 = CryptUtils.getSha256Hash(text);

        assertNotNull(hash1);
        assertEquals(32, hash1.length, "SHA-256 must be 32 bytes");
        assertArrayEquals(hash1, hash2, "Hashing must be deterministic");
    }

    static Stream<Arguments> provideShaTestCases() {
        // 生成长字符串 (Java 8 兼容方式)
        char[] longCharArr = new String(new char[1024]).replace('\0', 'z').toCharArray();

        return Stream.of(
                Arguments.of((Object) "".toCharArray()),
                Arguments.of((Object) " ".toCharArray()),
                Arguments.of((Object) "a".toCharArray()),
                Arguments.of((Object) longCharArr),
                Arguments.of((Object) "!@#$%^&*()_+~`|\\".toCharArray()),
                Arguments.of((Object) "1234567890".toCharArray())
        );
    }

    // --- 3. 随机盐生成测试 (BA + EG) ---
    @ParameterizedTest
    @ValueSource(ints = {-1, 0, 1, 8, 16, 32, 1024})
    public void testGenerateRandomSalt_Boundary(int length) {
        if (length < 0) {
            // 错误猜测：负数长度可能抛出异常
            assertThrows(NegativeArraySizeException.class, () -> CryptUtils.generateRandomSalt(length));
        } else {
            byte[] salt = CryptUtils.generateRandomSalt(length);
            assertEquals(length, salt.length);

            if (length > 0) {
                byte[] anotherSalt = CryptUtils.generateRandomSalt(length);
                // 验证随机性：两次生成的盐不应相同 (极低概率碰撞)
                assertFalse(Arrays.equals(salt, anotherSalt));
            }
        }
    }

    // --- 4. 随机数生成器质量测试 ---
    @Test
    public void testRandomGeneratorUniqueness() {
        // 验证返回的 Random 对象不是同一个实例，或者至少能产生不同序列
        Set<Integer> numbers = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            numbers.add(CryptUtils.newRandomNumberGenerator().nextInt());
        }
        // 如果 100 个随机数都一样，那肯定是挂了
        assertTrue(numbers.size() > 95, "Random numbers should be unique enough");
    }

    // --- 5. 默认迭代次数合规性测试 ---
    @Test
    public void testDefaultIterationsIntegrity() {
        char[] pass = "admin".toCharArray();
        byte[] salt = "fixed_salt".getBytes();

        byte[] keyDefault = CryptUtils.getPBKDF2KeyWithDefaultIterations(pass, salt);
        byte[] keyManual = CryptUtils.getPBKDF2Key(pass, salt, 310000);

        assertArrayEquals(keyDefault, keyManual, "Default iterations should match OWASP 310,000");
    }
}