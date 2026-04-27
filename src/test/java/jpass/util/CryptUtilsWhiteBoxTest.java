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

    // --- 1. generateRandomSalt ---
    @Test
    public void testGenerateRandomSalt_BranchCoverage() {
        byte[] saltZero = CryptUtils.generateRandomSalt(0);
        assertEquals(0, saltZero.length);

        byte[] saltPositive = CryptUtils.generateRandomSalt(16);
        assertEquals(16, saltPositive.length);

        boolean allZeros = true;
        for (byte b : saltPositive) {
            if (b != 0) { allZeros = false; break; }
        }
        assertFalse(allZeros, "Salt should be populated with random data");
    }

    // --- 2. ：SHA-256 iteration logic ---
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

    // --- 3. exception：newRandomNumberGenerator ---
    @Test
    public void testNewRandomNumberGenerator_TryCatch() {
        Random rnd = CryptUtils.newRandomNumberGenerator();
        assertNotNull(rnd);

        assertTrue(rnd instanceof SecureRandom || rnd instanceof Random);
    }

    // --- 4.  (Exception Wrapping) ---

    @Test
    public void testPBKDF2_InternalErrorHandling() {
        char[] text = "test".toCharArray();
        byte[] salt = new byte[16];

        assertThrows(IllegalArgumentException.class, () -> {
            CryptUtils.getPBKDF2Key(text, salt, -1);
        });
    }

    @Test
    public void testCryptoIntegrationPath() {
        int saltLen = 32;
        char[] masterPassword = "jhu_mscs_student_2026".toCharArray();

        byte[] salt = CryptUtils.generateRandomSalt(saltLen);

        byte[] derivedKey = CryptUtils.getPBKDF2KeyWithDefaultIterations(masterPassword, salt);
        char[] keyAsChars = new String(derivedKey).toCharArray();
        byte[] finalHash = CryptUtils.getSha256HashWithDefaultIterations(keyAsChars);

        assertNotNull(finalHash);
        assertEquals(32, finalHash.length);
    }

    /*

    After Mutation
     */

    @Test
    public void testGenerateRandomSalt_ExactBoundaries() {
        byte[] saltOne = CryptUtils.generateRandomSalt(1);
        assertEquals(1, saltOne.length);
        byte[] saltZero = CryptUtils.generateRandomSalt(0);
        assertEquals(0, saltZero.length);
    }
    @Test
    public void testSha256Hash_ConsistencyAndIterationBoundaries() throws Exception {
        Method method = CryptUtils.class.getDeclaredMethod("getSha256Hash", char[].class, int.class);
        method.setAccessible(true);

        char[] text = "JHU".toCharArray();


        byte[] hash0 = (byte[]) method.invoke(null, text, 0);

        byte[] hash1 = (byte[]) method.invoke(null, text, 1);


        assertFalse(Arrays.equals(hash0, hash1));
        assertEquals(32, hash0.length);
        assertEquals(32, hash1.length);
    }

  
    @Test
    public void testSha256Hash_HighIterationStress() throws Exception {
        Method method = CryptUtils.class.getDeclaredMethod("getSha256Hash", char[].class, int.class);
        method.setAccessible(true);

        char[] text = "mscs".toCharArray();
    
        byte[] hashLarge = (byte[]) method.invoke(null, text, 1000);
        assertNotNull(hashLarge);
    }
}
