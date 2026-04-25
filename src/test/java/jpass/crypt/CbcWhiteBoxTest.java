package jpass.crypt;

import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class CbcWhiteBoxTest {

    private final byte[] key = new byte[32];
    private final byte[] iv = new byte[16];

    @Test
    public void testBranchCoverageEarlyReturns() throws Exception {
        // Branch: cover "if (data == null || length <= 0) { return; }"
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Cbc cbc = new Cbc(iv, key, out);

        cbc.encrypt(new byte[10], 0);  // length <= 0
        cbc.encrypt(null, 5);          // data == null

        cbc.finishEncryption();
        assertEquals(16, out.toByteArray().length, "Only padding should be written (16 bytes)");
    }

    @Test
    public void testBranchCoverageIncompleteBlockException() {
        // Branch: cover "if (this._overflowUsed != 0) throw new DecryptException();"
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Cbc cbc = new Cbc(iv, key, out);

        assertThrows(DecryptException.class, () -> {
            // Feed 10 bytes (incomplete block, AES needs 16) and force finish
            cbc.decrypt(new byte[10]);
            cbc.finishDecryption();
        }, "Finishing decryption on an incomplete block must throw DecryptException");
    }

    @Test
    public void testBranchCoverageInvalidPaddingException() throws Exception {
        // Branch: cover "if (pad <= 0 || pad > BLOCK_SIZE) throw new DecryptException();"

        // First, create a valid ciphertext
        ByteArrayOutputStream outEnc = new ByteArrayOutputStream();
        Cbc encryptor = new Cbc(iv, key, outEnc);
        encryptor.encrypt(new byte[16]); // Encrypt 16 bytes of zeros
        encryptor.finishEncryption();
        byte[] ciphertext = outEnc.toByteArray();

        // White-box tampering: Corrupt the very last byte of the ciphertext.
        // Because CBC mode propagates errors to the decrypted block, this will corrupt
        // the padding value, forcing the specific pad validation branch to fail.
        ciphertext[ciphertext.length - 1] ^= 0xFF;

        ByteArrayOutputStream outDec = new ByteArrayOutputStream();
        Cbc decryptor = new Cbc(iv, key, outDec);
        decryptor.decrypt(ciphertext);

        assertThrows(DecryptException.class, decryptor::finishDecryption,
                "Corrupted padding byte must trigger DecryptException");
    }

    @Test
    public void testOutputStreamIntegrationLifecycle() throws Exception {
        // Integration Test: Verify Cbc properly integrates with the underlying OutputStream
        // Specifically, verify it calls close() when finish() is called.

        class MockOutputStream extends ByteArrayOutputStream {
            boolean isClosed = false;
            @Override
            public void close() throws IOException {
                isClosed = true;
                super.close();
            }
        }

        MockOutputStream mockOut = new MockOutputStream();
        Cbc cbc = new Cbc(iv, key, mockOut);

        assertFalse(mockOut.isClosed, "Stream should be open initially");
        cbc.encrypt(new byte[10]);
        cbc.finishEncryption();
        assertTrue(mockOut.isClosed, "finishEncryption() must close the OutputStream");
    }
}