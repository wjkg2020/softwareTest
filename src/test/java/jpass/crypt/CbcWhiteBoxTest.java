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
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Cbc cbc = new Cbc(iv, key, out);
        cbc.encrypt(new byte[10], 0);
        cbc.encrypt(null, 5);
        cbc.finishEncryption();
        assertEquals(16, out.toByteArray().length, "Only padding should be written (16 bytes)");
    }

    @Test
    public void testBranchCoverageIncompleteBlockException() {
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Cbc cbc = new Cbc(iv, key, out);
        assertThrows(DecryptException.class, () -> {
            cbc.decrypt(new byte[10]);
            cbc.finishDecryption();
        }, "Finishing decryption on an incomplete block must throw DecryptException");
    }

    @Test
    public void testBranchCoverageInvalidPaddingException() throws Exception {

        ByteArrayOutputStream outEnc = new ByteArrayOutputStream();
        Cbc encryptor = new Cbc(iv, key, outEnc);
        encryptor.encrypt(new byte[16]);
        encryptor.finishEncryption();
        byte[] ciphertext = outEnc.toByteArray();

        ciphertext[ciphertext.length - 1] ^= 0xFF;
        ByteArrayOutputStream outDec = new ByteArrayOutputStream();
        Cbc decryptor = new Cbc(iv, key, outDec);
        decryptor.decrypt(ciphertext);
        assertThrows(DecryptException.class, decryptor::finishDecryption,
                "Corrupted padding byte must trigger DecryptException");
    }

    @Test
    public void testOutputStreamIntegrationLifecycle() throws Exception {

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
