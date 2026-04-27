package jpass.crypt;

import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class CbcBlackBoxTest {

    private final byte[] dummyKey = new byte[32]; // 256-bit key
    private final byte[] dummyIv = new byte[16];  // 128-bit IV

    @Test
    public void testReversibilityEquivalence() throws Exception {
        // EP: Valid inputs of various standard lengths should be perfectly reversible
        Random rand = new Random(42);
        int[] testLengths = {10, 50, 1000};

        for (int len : testLengths) {
            byte[] original = new byte[len];
            rand.nextBytes(original);

            // Encrypt
            ByteArrayOutputStream outEnc = new ByteArrayOutputStream();
            Cbc encryptor = new Cbc(dummyIv, dummyKey, outEnc);
            encryptor.encrypt(original);
            encryptor.finishEncryption();
            byte[] ciphertext = outEnc.toByteArray();

            // Decrypt
            ByteArrayOutputStream outDec = new ByteArrayOutputStream();
            Cbc decryptor = new Cbc(dummyIv, dummyKey, outDec);
            decryptor.decrypt(ciphertext);
            decryptor.finishDecryption();
            byte[] decrypted = outDec.toByteArray();

            assertArrayEquals(original, decrypted, "Decrypted text should match original for length " + len);
        }
    }

    @Test
    public void testPaddingBoundaries() throws Exception {

        int[] boundaries = {15, 16, 17};

        for (int len : boundaries) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Cbc cbc = new Cbc(dummyIv, dummyKey, out);
            cbc.encrypt(new byte[len]);
            cbc.finishEncryption();
            byte[] ciphertext = out.toByteArray();
            assertTrue(ciphertext.length % 16 == 0, "Ciphertext length must be a multiple of 16");
            assertTrue(ciphertext.length > len, "Ciphertext must include padding");
        }
    }

    @Test
    public void testErrorGuessingNullInputs() throws Exception {
        // EG: Guessing that developers might forget to handle nulls
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Cbc cbc = new Cbc(dummyIv, dummyKey, out);

        // Should not crash, should return safely or handle it
        assertDoesNotThrow(() -> cbc.encrypt(null));
        assertDoesNotThrow(() -> cbc.decrypt(null));
    }

    @Test
    public void testCombinatorialChunking() throws Exception {

        byte[] data = new byte[32];
        new Random(123).nextBytes(data);
        ByteArrayOutputStream out1 = new ByteArrayOutputStream();
        Cbc cbc1 = new Cbc(dummyIv, dummyKey, out1);
        cbc1.encrypt(data);
        cbc1.finishEncryption();
        ByteArrayOutputStream out2 = new ByteArrayOutputStream();
        Cbc cbc2 = new Cbc(dummyIv, dummyKey, out2);
        for (byte b : data) {
            cbc2.encrypt(new byte[]{b}); // Feed 1 byte at a time
        }
        cbc2.finishEncryption();
        assertArrayEquals(out1.toByteArray(), out2.toByteArray(),
                "Combinatorial chunking should yield identical ciphertexts");
    }
}
