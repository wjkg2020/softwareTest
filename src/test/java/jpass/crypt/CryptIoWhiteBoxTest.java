package jpass.crypt.io;

import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

public class CryptIoWhiteBoxTest {

    private final byte[] dummyKey = new byte[32];

    @Test
    public void testBranchMissingIvException() {
        // Branch Coverage: CryptInputStream constructor
        // "if (cur < 0) throw new IOException("No initial values in stream.");"

        // Provide a stream with only 5 bytes, but the constructor expects 16 bytes for the IV
        byte[] shortData = new byte[]{1, 2, 3, 4, 5};
        ByteArrayInputStream shortStream = new ByteArrayInputStream(shortData);

        Exception exception = assertThrows(IOException.class, () -> {
            new CryptInputStream(shortStream, dummyKey);
        }, "Should throw IOException when parent stream lacks full 16-byte IV");

        assertEquals("No initial values in stream.", exception.getMessage());
    }

    @Test
    public void testIntegrationRandomIvGeneration() throws Exception {
        // Integration Test: Verify CryptOutputStream constructor behavior
        // When NOT provided an IV, it should generate a random 16-byte IV
        // and write it to the parent stream immediately.

        ByteArrayOutputStream parentOut = new ByteArrayOutputStream();

        // Instantiate without IV. It should write exactly 16 bytes to parentOut right away.
        CryptOutputStream cryptOut = new CryptOutputStream(parentOut, dummyKey);

        assertEquals(16, parentOut.toByteArray().length,
                "Constructor should write exactly 16 bytes (the random IV) to the parent stream upon initialization");

        cryptOut.close();
    }

    @Test
    public void testIntegrationStreamClosure() throws Exception {
        // Integration Test: Verify CryptInputStream properly closes its parent stream

        class MockInputStream extends ByteArrayInputStream {
            boolean isClosed = false;
            public MockInputStream(byte[] buf) { super(buf); }
            @Override
            public void close() throws IOException {
                isClosed = true;
                super.close();
            }
        }

        // We need 16 bytes just to satisfy the IV reading in the constructor
        MockInputStream mockIn = new MockInputStream(new byte[16]);
        CryptInputStream cryptIn = new CryptInputStream(mockIn, dummyKey);

        assertFalse(mockIn.isClosed, "Parent stream should be open");
        cryptIn.close();
        assertTrue(mockIn.isClosed, "CryptInputStream.close() must propagate to the parent stream");
    }

    @Test
    public void testBranchCatchDecryptException() throws Exception {
        // Branch Coverage: CryptInputStream.read()
        // "catch (DecryptException ex) { throw new IOException("can't decrypt"); }"

        // 1. Create a valid encrypted stream
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        CryptOutputStream cryptOut = new CryptOutputStream(outStream, dummyKey);
        cryptOut.write("Data".getBytes());
        cryptOut.close();

        byte[] data = outStream.toByteArray();
        // Corrupt the ciphertext to trigger DecryptException internally during padding check
        data[data.length - 1] = 0x00;

        ByteArrayInputStream inStream = new ByteArrayInputStream(data);
        CryptInputStream cryptIn = new CryptInputStream(inStream, dummyKey);

        // Read through the stream until it attempts to finish decryption and fails
        IOException exception = assertThrows(IOException.class, () -> {
            while (cryptIn.read() != -1) {
                // consume stream
            }
        });

        assertEquals("can't decrypt", exception.getMessage(),
                "Must hit the catch(DecryptException) block and wrap it in IOException");
    }
}