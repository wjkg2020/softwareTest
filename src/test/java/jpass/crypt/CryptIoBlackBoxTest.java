package jpass.crypt.io;

import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class CryptIoBlackBoxTest {

    private final byte[] dummyKey = new byte[32]; // Standard 256-bit key

    // Helper method to read an entire InputStream into a byte array
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
        // EP (Equivalence Partitioning): Standard valid data flow
        byte[] originalData = "Hello, JPass Security!".getBytes();

        // 1. Write data using CryptOutputStream
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        // Using the constructor that auto-generates the IV and writes it to the stream
        CryptOutputStream cryptOut = new CryptOutputStream(outStream, dummyKey);
        cryptOut.write(originalData);
        cryptOut.close();

        byte[] encryptedFileContent = outStream.toByteArray();

        // 2. Read data using CryptInputStream
        ByteArrayInputStream inStream = new ByteArrayInputStream(encryptedFileContent);
        // Using the constructor that automatically reads the IV from the first 16 bytes
        CryptInputStream cryptIn = new CryptInputStream(inStream, dummyKey);
        byte[] decryptedData = readAllBytes(cryptIn);
        cryptIn.close();

        assertArrayEquals(originalData, decryptedData, "End-to-end stream encryption/decryption failed");
    }

    @Test
    public void testBoundaryFileSizes() throws Exception {
        // BA (Boundary Analysis): Testing extreme file sizes (Empty, 1 byte, and large)
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
        // Combinatorial: Test mixing single-byte writes and array writes
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        CryptOutputStream cryptOut = new CryptOutputStream(outStream, dummyKey);

        cryptOut.write(65); // Write single byte 'A'
        cryptOut.write("BCDE".getBytes()); // Write array
        cryptOut.close();

        ByteArrayInputStream inStream = new ByteArrayInputStream(outStream.toByteArray());
        CryptInputStream cryptIn = new CryptInputStream(inStream, dummyKey);
        byte[] decrypted = readAllBytes(cryptIn);

        assertArrayEquals("ABCDE".getBytes(), decrypted, "Mixed write methods should combine correctly");
    }

    @Test
    public void testErrorGuessingCorruptedStream() throws Exception {
        // EG (Error Guessing): What happens if the encrypted file is corrupted during download?
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        CryptOutputStream cryptOut = new CryptOutputStream(outStream, dummyKey);
        cryptOut.write("Sensitive Data".getBytes());
        cryptOut.close();

        byte[] corruptedFile = outStream.toByteArray();
        corruptedFile[corruptedFile.length - 1] ^= 0xFF; // Flip bits in the last byte (padding area)

        ByteArrayInputStream inStream = new ByteArrayInputStream(corruptedFile);
        CryptInputStream cryptIn = new CryptInputStream(inStream, dummyKey);

        // The read() method should catch the internal DecryptException and throw an IOException
        assertThrows(IOException.class, () -> readAllBytes(cryptIn),
                "Reading a corrupted stream should throw an IOException");
    }
}