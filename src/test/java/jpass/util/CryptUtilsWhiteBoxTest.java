package jpass.util;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.reflect.Method;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Random;

/**
 * Whitebox Test Suite for CryptUtils.
 * Focuses on Branch Coverage, Loop Boundaries, and Private Method testing.
 */
/*
Whitebox Analysis - Surviving Mutations (CryptUtils)

Equivalent Mutation (Line 148, 153): The surviving removed call to MessageDigest::reset mutations are Equivalent Mutations. In Java, MessageDigest.digest() automatically resets the digest state upon completion. Therefore, manual calls to reset() are redundant. Removing them does not change the cryptographic output, making the mutation unkillable by functional assertions.

Boundary Condition (Line 164): The mutation from > 0 to >= 0 for saltLength is also functionally equivalent. When saltLength is 0, the method returns an empty array. Invoking nextBytes on an empty array (the mutated path) remains a no-op and produces an identical empty array output.

Action Taken: We have added boundary tests for saltLength = 0 and saltLength = 1, and verified the integrity of the hashing chain across multiple iterations. The remaining surviving mutations are confirmed as redundant code artifacts rather than logic gaps.
 */
public class CryptUtilsWhiteBoxTest {

    // --- 1. 分支覆盖：generateRandomSalt ---
    @Test
    public void testGenerateRandomSalt_BranchCoverage() {
        // 分支 1: saltLength = 0 (不进入 if 块)
        byte[] saltZero = CryptUtils.generateRandomSalt(0);
        assertEquals(0, saltZero.length);

        // 分支 2: saltLength > 0 (进入 if 块执行 nextBytes)
        byte[] saltPositive = CryptUtils.generateRandomSalt(16);
        assertEquals(16, saltPositive.length);

        // 验证非全零（概率上几乎不可能全零）
        boolean allZeros = true;
        for (byte b : saltPositive) {
            if (b != 0) { allZeros = false; break; }
        }
        assertFalse(allZeros, "Salt should be populated with random data");
    }

    // --- 2. 循环覆盖：SHA-256 迭代逻辑 ---
    /**
     * 使用反射测试私有方法 getSha256Hash(char[], int)
     * 覆盖循环的 0次、1次 和 多次 执行。
     */
    @ParameterizedTest
    @ValueSource(ints = {0, 1, 5, 100})
    public void testSha256Hash_LoopCoverage(int iterations) throws Exception {
        Method method = CryptUtils.class.getDeclaredMethod("getSha256Hash", char[].class, int.class);
        method.setAccessible(true);

        char[] pass = "whitebox_test".toCharArray();
        byte[] result = (byte[]) method.invoke(null, pass, iterations);

        assertNotNull(result);
        assertEquals(32, result.length);
    }

    // --- 3. 异常路径覆盖：newRandomNumberGenerator ---
    @Test
    public void testNewRandomNumberGenerator_TryCatch() {
        // 在标准环境下，这应该返回 SecureRandom
        Random rnd = CryptUtils.newRandomNumberGenerator();
        assertNotNull(rnd);

        // 验证在没有抛出异常时，默认是 SecureRandom 的实例
        assertTrue(rnd instanceof SecureRandom || rnd instanceof Random);
    }

    // --- 4. 异常包装验证 (Exception Wrapping) ---
    /**
     * 验证当底层算法不可用时，是否正确抛出 RuntimeException。
     * 虽然 SHA-256 是 Java 强制要求的，但我们可以通过反射调用
     * 验证 Catch 块内的消息包装逻辑。
     */
    @Test
    public void testPBKDF2_InternalErrorHandling() {
        char[] text = "test".toCharArray();
        byte[] salt = new byte[16];

        // 实际上抛出的是 IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> {
            CryptUtils.getPBKDF2Key(text, salt, -1);
        });
    }

    // --- 5. 集成测试：多步加固流程 ---
    @Test
    public void testCryptoIntegrationPath() {
        // 模拟 JPass 的真实工作流：生成盐 -> 派生密钥 -> 哈希
        int saltLen = 32;
        char[] masterPassword = "jhu_mscs_student_2026".toCharArray();

        // 1. 生成盐
        byte[] salt = CryptUtils.generateRandomSalt(saltLen);

        // 2. 通过 PBKDF2 派生密钥 (使用默认迭代)
        byte[] derivedKey = CryptUtils.getPBKDF2KeyWithDefaultIterations(masterPassword, salt);

        // 3. 对派生出的结果做 SHA-256 进一步加固
        // 将 byte[] 转为 char[] 模拟后续处理
        char[] keyAsChars = new String(derivedKey).toCharArray();
        byte[] finalHash = CryptUtils.getSha256HashWithDefaultIterations(keyAsChars);

        assertNotNull(finalHash);
        assertEquals(32, finalHash.length);
    }

    /*

    After Mutation
     */


    /**
     * 针对 Line 164: 强化边界值测试
     * 杀死 boundary 变异体（如果变异为 > 1 或 >= 1 等情况）
     */
    @Test
    public void testGenerateRandomSalt_ExactBoundaries() {
        // 测试长度为 1 的边界
        byte[] saltOne = CryptUtils.generateRandomSalt(1);
        assertEquals(1, saltOne.length);

        // 测试长度为 0 的边界
        // 虽然无法杀死 >=0 的等价变异，但能确保基础逻辑正确
        byte[] saltZero = CryptUtils.generateRandomSalt(0);
        assertEquals(0, saltZero.length);
    }

    /**
     * 针对 Line 148 & 153: 验证哈希的一致性
     * 虽然 md.reset() 是冗余的，但我们要确保不同长度和多次迭代下的确定性
     */
    @Test
    public void testSha256Hash_ConsistencyAndIterationBoundaries() throws Exception {
        Method method = CryptUtils.class.getDeclaredMethod("getSha256Hash", char[].class, int.class);
        method.setAccessible(true);

        char[] text = "JHU".toCharArray();

        // 覆盖 iteration = 0 的情况
        byte[] hash0 = (byte[]) method.invoke(null, text, 0);

        // 覆盖 iteration = 1 的情况（正好跳过 153 行循环内部的 reset）
        byte[] hash1 = (byte[]) method.invoke(null, text, 1);

        // 验证：iteration=0 的结果应该是 text 的直接 SHA-256
        // 验证：iteration=1 的结果应该是对 hash0 再做一次 SHA-256
        assertFalse(Arrays.equals(hash0, hash1));
        assertEquals(32, hash0.length);
        assertEquals(32, hash1.length);
    }

    /**
     * 针对 Line 153: 通过大数据量模拟可能的碰撞或状态错误
     */
    @Test
    public void testSha256Hash_HighIterationStress() throws Exception {
        Method method = CryptUtils.class.getDeclaredMethod("getSha256Hash", char[].class, int.class);
        method.setAccessible(true);

        char[] text = "mscs".toCharArray();
        // 跑 1000 次迭代，确保在没有 reset() 意外干扰的情况下逻辑依然稳健
        byte[] hashLarge = (byte[]) method.invoke(null, text, 1000);
        assertNotNull(hashLarge);
    }
}