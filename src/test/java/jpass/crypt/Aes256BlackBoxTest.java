package jpass.crypt;

import org.junit.jupiter.api.Test;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class Aes256BlackBoxTest {

    // Helper: Convert hex string to byte array
    private byte[] hexStringToByteArray(String s) {
        s = s.replaceAll("\\s+", "");
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    @Test
    public void testNistKnownAnswer() {
        // KAT: Check against standard NIST FIPS 197 vectors
        byte[] key = hexStringToByteArray("000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f");
        byte[] plaintext = hexStringToByteArray("00112233445566778899aabbccddeeff");
        byte[] expected = hexStringToByteArray("8ea2b7ca516745bfeafc49904b496089");

        Aes256 aes = new Aes256(key);
        byte[] ciphertext = new byte[16];
        byte[] decrypted = new byte[16];

        aes.encrypt(plaintext, 0, ciphertext, 0);
        assertArrayEquals(expected, ciphertext, "Ciphertext should match NIST standard");

        aes.decrypt(ciphertext, 0, decrypted, 0);
        assertArrayEquals(plaintext, decrypted, "Decrypted text should match original plaintext");
    }

    @Test
    public void testReversibility() {
        // Property-based: encrypt(decrypt(x)) == x for random inputs
        Random random = new Random(42);
        for (int i = 0; i < 100; i++) {
            byte[] key = new byte[32];
            byte[] plaintext = new byte[16];
            byte[] ciphertext = new byte[16];
            byte[] decrypted = new byte[16];

            random.nextBytes(key);
            random.nextBytes(plaintext);

            Aes256 aes = new Aes256(key);
            aes.encrypt(plaintext, 0, ciphertext, 0);
            aes.decrypt(ciphertext, 0, decrypted, 0);

            assertArrayEquals(plaintext, decrypted, "Decryption failed on random input iteration " + i);
        }
    }

    @Test
    public void testKeyLengthBoundaries() {
        // BA/EP: Test invalid key boundaries (31 bytes)
        byte[] key31 = new byte[31];
        assertThrows(Exception.class, () -> new Aes256(key31), "Should throw exception for key < 32 bytes");

        // BA/EP: Test boundary just above standard (33 bytes)
        byte[] key33 = new byte[33];
        new Aes256(key33); // Should not crash (will likely truncate to 32 bytes internally)
    }

    @Test
    public void testBlockSizeBoundaries() {
        // BA/EP: Test invalid block sizes
        Aes256 aes = new Aes256(new byte[32]);
        byte[] validOut = new byte[16];

        assertThrows(Exception.class, () -> aes.encrypt(new byte[15], 0, validOut, 0), "Should fail for 15-byte block");
        assertThrows(Exception.class, () -> aes.encrypt(new byte[0], 0, validOut, 0), "Should fail for empty block");

        // 16-byte block should work without throwing exceptions
        aes.encrypt(new byte[16], 0, validOut, 0);
    }

    @Test
    public void testErrorGuessing() {
        Aes256 aes = new Aes256(new byte[32]);

        // EG 1: Null pointer injection
        assertThrows(NullPointerException.class, () -> aes.encrypt(null, 0, new byte[16], 0));

        // EG 2: In-place encryption (input and output are the same array)
        byte[] data = hexStringToByteArray("00112233445566778899aabbccddeeff");
        byte[] expected = new byte[16];
        aes.encrypt(data, 0, expected, 0); // Get baseline

        aes.encrypt(data, 0, data, 0); // Encrypt in-place
        assertArrayEquals(expected, data, "In-place encryption should not corrupt data");
    }

    @Test
    public void testCombinatorialOffsets() {
        // Combinatorial: Test combinations of valid/invalid array offsets
        Aes256 aes = new Aes256(new byte[32]);
        byte[] buffer = new byte[100];
        byte[] outBuffer = new byte[100];

        byte[] secret = hexStringToByteArray("00112233445566778899aabbccddeeff");
        System.arraycopy(secret, 0, buffer, 50, 16);

        // Valid offset combination
        aes.encrypt(buffer, 50, outBuffer, 20);
        byte[] decrypted = new byte[16];
        aes.decrypt(outBuffer, 20, decrypted, 0);
        assertArrayEquals(secret, decrypted, "Offset encryption/decryption failed");

        // Invalid offset combinations
        assertThrows(Exception.class, () -> aes.encrypt(buffer, -1, outBuffer, 0), "Negative index should fail");
        assertThrows(Exception.class, () -> aes.encrypt(buffer, 90, outBuffer, 0), "Out of bounds index should fail");
    }
}