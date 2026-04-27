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

        byte[] shortData = new byte[]{1, 2, 3, 4, 5};
        ByteArrayInputStream shortStream = new ByteArrayInputStream(shortData);

        Exception exception = assertThrows(IOException.class, () -> {
            new CryptInputStream(shortStream, dummyKey);
        }, "Should throw IOException when parent stream lacks full 16-byte IV");

        assertEquals("No initial values in stream.", exception.getMessage());
    }

    @Test
    public void testIntegrationRandomIvGeneration() throws Exception {

        ByteArrayOutputStream parentOut = new ByteArrayOutputStream();
        CryptOutputStream cryptOut = new CryptOutputStream(parentOut, dummyKey);
        assertEquals(16, parentOut.toByteArray().length,
                "Constructor should write exactly 16 bytes (the random IV) to the parent stream upon initialization");

        cryptOut.close();
    }

    @Test
    public void testIntegrationStreamClosure() throws Exception {

        class MockInputStream extends ByteArrayInputStream {
            boolean isClosed = false;
            public MockInputStream(byte[] buf) { super(buf); }
            @Override
            public void close() throws IOException {
                isClosed = true;
                super.close();
            }
        }

        MockInputStream mockIn = new MockInputStream(new byte[16]);
        CryptInputStream cryptIn = new CryptInputStream(mockIn, dummyKey);

        assertFalse(mockIn.isClosed, "Parent stream should be open");
        cryptIn.close();
        assertTrue(mockIn.isClosed, "CryptInputStream.close() must propagate to the parent stream");
    }

    @Test
    public void testBranchCatchDecryptException() throws Exception {

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        CryptOutputStream cryptOut = new CryptOutputStream(outStream, dummyKey);
        cryptOut.write("Data".getBytes());
        cryptOut.close();
        byte[] data = outStream.toByteArray();
        data[data.length - 1] = 0x00;
        ByteArrayInputStream inStream = new ByteArrayInputStream(data);
        CryptInputStream cryptIn = new CryptInputStream(inStream, dummyKey);
        IOException exception = assertThrows(IOException.class, () -> {
            while (cryptIn.read() != -1) {
            }
        });

        assertEquals("can't decrypt", exception.getMessage(),
                "Must hit the catch(DecryptException) block and wrap it in IOException");
    }
}
