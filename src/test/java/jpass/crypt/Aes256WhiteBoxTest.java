package jpass.crypt;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class Aes256WhiteBoxTest {

    @Test
    public void testConstructorShortKeyException() {

        byte[] shortKey = new byte[16];
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> new Aes256(shortKey),
                "Constructor should throw exception for key length < 32");
    }

    @Test
    public void testEncryptInputBlockException() {

        Aes256 aes = new Aes256(new byte[32]);
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> {
            aes.encrypt(new byte[10], 0, new byte[16], 0);
        }, "Input block < 16 bytes should throw exception");
    }

    @Test
    public void testDecryptOutputBlockException() {

        Aes256 aes = new Aes256(new byte[32]);
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> {
            aes.decrypt(new byte[16], 0, new byte[10], 0);
        }, "Output buffer < 16 bytes should throw exception");
    }

    @Test
    public void testInvalidIndexOffsets() {

        Aes256 aes = new Aes256(new byte[32]);
        byte[] largeInput = new byte[20];
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> {
            aes.encrypt(largeInput, 10, new byte[16], 0); // Reads beyond array end
        }, "Invalid inIndex offset should throw exception");
    }


    @Test
    public void testInternalMathBranchCoverage() {

        byte[] key = new byte[32];
        Aes256 aes = new Aes256(key);

        byte[] highBitsInput = new byte[16];
        byte[] lowBitsInput = new byte[16];
        byte[] outBuffer = new byte[16];

        for (int i = 0; i < 16; i++) {
            highBitsInput[i] = (byte) 0xFF;
            lowBitsInput[i] = 0x00;
        }

        aes.encrypt(highBitsInput, 0, outBuffer, 0);
        aes.encrypt(lowBitsInput, 0, outBuffer, 0);
        // Asserting it doesn't crash is enough, correctness is verified in black-box tests.
    }


    @Test
    public void testSequentialMultiBlockIntegration() {

        Aes256 aes = new Aes256(new byte[32]);
        int blocks = 3;
        int totalSize = blocks * 16; // 48 bytes
        byte[] multiBlockPlaintext = new byte[totalSize];
        for(int i = 0; i < totalSize; i++) {
            multiBlockPlaintext[i] = (byte) i;
        }
        byte[] ciphertext = new byte[totalSize];
        byte[] decrypted = new byte[totalSize];
        for (int i = 0; i < totalSize; i += 16) {
            aes.encrypt(multiBlockPlaintext, i, ciphertext, i);
        }
        for (int i = 0; i < totalSize; i += 16) {
            aes.decrypt(ciphertext, i, decrypted, i);
        }
        assertArrayEquals(multiBlockPlaintext, decrypted, "Multi-block integration workflow failed");
    }
}
