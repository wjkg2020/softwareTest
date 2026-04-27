package jpass.crypt.io;

import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class CryptIoBlackBoxTest {

    private final byte[] dummyKey = new byte[32];
    
    private byte[] readAllBytes(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[1024];
        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        return buffer.toByteArray();
    }

    @Test
    public void testEndToEndEquivalence() throws Exception {

        byte[] originalData = "Hello, JPass Security!".getBytes();
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        CryptOutputStream cryptOut = new CryptOutputStream(outStream, dummyKey);
        cryptOut.write(originalData);
        cryptOut.close();

        byte[] encryptedFileContent = outStream.toByteArray();
        ByteArrayInputStream inStream = new ByteArrayInputStream(encryptedFileContent);
        CryptInputStream cryptIn = new CryptInputStream(inStream, dummyKey);
        byte[] decryptedData = readAllBytes(cryptIn);
        cryptIn.close();

        assertArrayEquals(originalData, decryptedData, "End-to-end stream encryption/decryption failed");
    }

    @Test
    public void testBoundaryFileSizes() throws Exception {

        int[] fileSizes = {0, 1, 1000};
        Random rand = new Random(42);

        for (int size : fileSizes) {
            byte[] data = new byte[size];
            rand.nextBytes(data);
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            CryptOutputStream cryptOut = new CryptOutputStream(outStream, dummyKey);
            cryptOut.write(data);
            cryptOut.close();
            ByteArrayInputStream inStream = new ByteArrayInputStream(outStream.toByteArray());
            CryptInputStream cryptIn = new CryptInputStream(inStream, dummyKey);
            byte[] decrypted = readAllBytes(cryptIn);
            assertArrayEquals(data, decrypted, "Failed boundary test for file size: " + size);
        }
    }

    @Test
    public void testCombinatorialWriteMethods() throws Exception {

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        CryptOutputStream cryptOut = new CryptOutputStream(outStream, dummyKey);
        cryptOut.write(65);
        cryptOut.write("BCDE".getBytes()); 
        cryptOut.close();

        ByteArrayInputStream inStream = new ByteArrayInputStream(outStream.toByteArray());
        CryptInputStream cryptIn = new CryptInputStream(inStream, dummyKey);
        byte[] decrypted = readAllBytes(cryptIn);
        assertArrayEquals("ABCDE".getBytes(), decrypted, "Mixed write methods should combine correctly");
    }

    @Test
    public void testErrorGuessingCorruptedStream() throws Exception {

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        CryptOutputStream cryptOut = new CryptOutputStream(outStream, dummyKey);
        cryptOut.write("Sensitive Data".getBytes());
        cryptOut.close();

        byte[] corruptedFile = outStream.toByteArray();
        corruptedFile[corruptedFile.length - 1] ^= 0xFF;
        ByteArrayInputStream inStream = new ByteArrayInputStream(corruptedFile);
        CryptInputStream cryptIn = new CryptInputStream(inStream, dummyKey);
        assertThrows(IOException.class, () -> readAllBytes(cryptIn),
                "Reading a corrupted stream should throw an IOException");
    }
}
