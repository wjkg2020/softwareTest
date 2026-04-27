package jpass.crypt;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class Aes256WhiteBoxTest {

    // UNIT TESTS: Control Flow & Exception Handling

    @Test
    public void testConstructorShortKeyException() {
        // Control flow: System.arraycopy throws exception for short keys
        byte[] shortKey = new byte[16];
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> new Aes256(shortKey),
                "Constructor should throw exception for key length < 32");
    }

    @Test
    public void testEncryptInputBlockException() {
        // Control flow: Force out-of-bounds read in encrypt()
        Aes256 aes = new Aes256(new byte[32]);
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> {
            aes.encrypt(new byte[10], 0, new byte[16], 0);
        }, "Input block < 16 bytes should throw exception");
    }

    @Test
    public void testDecryptOutputBlockException() {
        // Control flow: Force out-of-bounds write in decrypt()
        Aes256 aes = new Aes256(new byte[32]);
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> {
            aes.decrypt(new byte[16], 0, new byte[10], 0);
        }, "Output buffer < 16 bytes should throw exception");
    }

    @Test
    public void testInvalidIndexOffsets() {
        // Control flow: Test index offset math (inIndex + 16 > length)
        Aes256 aes = new Aes256(new byte[32]);
        byte[] largeInput = new byte[20];
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> {
            aes.encrypt(largeInput, 10, new byte[16], 0); // Reads beyond array end
        }, "Invalid inIndex offset should throw exception");
    }

    // UNIT TESTS: Internal Branch Coverage (Data Flow)

    @Test
    public void testInternalMathBranchCoverage() {
        // Branch Coverage Goal: Maximize coverage in private times2() and mul() methods.
        // times2() has a crucial branch: if ((b & 0x80) != 0).
        // To guarantee we hit both TRUE and FALSE paths of this branch:
        // - 0xFF (11111111 in binary) forces the MSB to 1 (hits TRUE branch).
        // - 0x00 forces the MSB to 0 (hits FALSE branch).

        byte[] key = new byte[32];
        Aes256 aes = new Aes256(key);

        byte[] highBitsInput = new byte[16];
        byte[] lowBitsInput = new byte[16];
        byte[] outBuffer = new byte[16];

        for (int i = 0; i < 16; i++) {
            highBitsInput[i] = (byte) 0xFF; // All 1s
            lowBitsInput[i] = 0x00;         // All 0s
        }

        // Processing these specific byte patterns ensures maximum branch coverage
        // inside the private mixColumns / Galois Field multiplication logic.
        aes.encrypt(highBitsInput, 0, outBuffer, 0);
        aes.encrypt(lowBitsInput, 0, outBuffer, 0);

        // Asserting it doesn't crash is enough, correctness is verified in black-box tests.
    }

    // INTEGRATION TEST

    @Test
    public void testSequentialMultiBlockIntegration() {
        // Integration Goal: Test how Aes256 integrates with an external block-processing loop.
        // Real-world apps don't encrypt 16 bytes; they encrypt streams.
        // This simulates a basic ECB (Electronic Codebook) mode integration.

        Aes256 aes = new Aes256(new byte[32]);
        int blocks = 3;
        int totalSize = blocks * 16; // 48 bytes

        byte[] multiBlockPlaintext = new byte[totalSize];
        for(int i = 0; i < totalSize; i++) {
            multiBlockPlaintext[i] = (byte) i; // Fill with dummy data 0, 1, 2...
        }

        byte[] ciphertext = new byte[totalSize];
        byte[] decrypted = new byte[totalSize];

        // Simulate external Integration: Loop through buffer and encrypt chunk by chunk
        for (int i = 0; i < totalSize; i += 16) {
            aes.encrypt(multiBlockPlaintext, i, ciphertext, i);
        }

        // Simulate external Integration: Loop through buffer and decrypt chunk by chunk
        for (int i = 0; i < totalSize; i += 16) {
            aes.decrypt(ciphertext, i, decrypted, i);
        }

        // Verify the entire chain integrated successfully
        assertArrayEquals(multiBlockPlaintext, decrypted, "Multi-block integration workflow failed");
    }
}
