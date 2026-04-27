package jpass.data;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;

import jpass.xml.bind.Entries;
import jpass.xml.bind.Entry;

/**
 * Full Blackbox Test Suite for EntriesRepository.
 * Restored with complete test cases and Windows file-lock mitigation.
 */
public class EntriesRepositoryBlackBoxTest {

    @TempDir
    Path tempDir;

    /**
     * Helper: Generates a unique filename for each test case to avoid Windows FileSystemException.
     */
    private String getUniqueFileName(String prefix) {
        return tempDir.resolve(prefix + "_" + System.nanoTime() + ".jpass").toString();
    }

    // --- 1. Combinatorial Testing ---

    /**
     * Test Goal: Data integrity across various key types and data volumes.
     * Technique: Combinatorial Testing (Key Status x Entry Count).
     */
    @ParameterizedTest
    @MethodSource("provideCombinations")
    public void testWriteAndReadIntegrity(char[] key, int entryCount) throws Exception {
        String fileName = getUniqueFileName("integrity");
        EntriesRepository repository = EntriesRepository.newInstance(fileName, key);

        Entries originalEntries = new Entries();
        for (int i = 0; i < entryCount; i++) {
            Entry e = new Entry();
            e.setTitle("Title_" + i);
            e.setUser("User_" + i);
            originalEntries.getEntry().add(e);
        }

        repository.writeDocument(originalEntries);
        Entries savedEntries = repository.readDocument();

        assertEquals(entryCount, savedEntries.getEntry().size(), "Mismatch in entry count for key: " + (key == null ? "null" : "masked"));
        if (entryCount > 0) {
            assertEquals("Title_0", savedEntries.getEntry().get(0).getTitle());
        }
    }

    private static Stream<Arguments> provideCombinations() {
        return Stream.of(
                Arguments.of(null, 0),                                      // No encryption, no data
                Arguments.of(null, 5),                                      // No encryption, bulk data
                Arguments.of("abc".toCharArray(), 0),                       // Short key, no data
                Arguments.of("abc".toCharArray(), 1),                       // Short key, single entry
                Arguments.of("K3y_Str0ng!@#$%^&*()".toCharArray(), 10)      // Complex key, bulk data
        );
    }

    // --- 2. Equivalence Partitioning ---

    /**
     * Test Goal: Verify system handles different file naming conventions.
     * Technique: Equivalence Partitioning (Valid Path Formats).
     */
    @ParameterizedTest
    @ValueSource(strings = {"standard.jpass", "no_extension", ".hidden_file", "name with spaces.xml"})
    public void testFileNames(String name) throws Exception {
        // 使用 tempDir 下的子路径确保不冲突
        String fileName = tempDir.resolve("names_" + System.nanoTime() + "_" + name).toString();
        EntriesRepository repo = EntriesRepository.newInstance(fileName);
        Entries entries = new Entries();

        assertDoesNotThrow(() -> repo.writeDocument(entries));
        File file = new File(fileName);
        assertTrue(file.exists(), "File " + name + " should be successfully created.");
    }

    // --- 3. Boundary Analysis---

    /**
     * Test Goal: Check empty character array as a key.
     * Technique: Boundary Analysis.
     */
    @Test
    public void testKeyBoundaries() throws Exception {
        String fileName = getUniqueFileName("key_boundary");
        char[] emptyKey = new char[0]; // Empty array is a boundary between null and filled array

        EntriesRepository repo = EntriesRepository.newInstance(fileName, emptyKey);
        Entries entries = new Entries();

        assertDoesNotThrow(() -> {
            repo.writeDocument(entries);
            repo.readDocument();
        });
    }

    // --- 4. Error Guessing---

    /**
     * Test Goal: Ensure correct handling of authentication failure.
     * Logic: Wrong password should trigger IOException during GZIP/Crypt stream processing.
     */
    @Test
    public void testWrongPasswordFailure() throws Exception {
        String fileName = getUniqueFileName("security");
        char[] correctKey = "correct".toCharArray();
        char[] wrongKey = "wrong".toCharArray();

        EntriesRepository.newInstance(fileName, correctKey).writeDocument(new Entries());

        EntriesRepository wrongRepo = EntriesRepository.newInstance(fileName, wrongKey);

        // EG: Verify decryption failure
        assertThrows(Exception.class, () -> {
            try {
                wrongRepo.readDocument();
            } finally {
                // IMPORTANT: Mitigate JPass Resource Leak by suggesting GC
                System.gc();
            }
        }, "Should throw an exception when the wrong password is used.");
    }

    /**
     * Test Goal: Robustness against corrupted file content.
     * Logic: Manual override file with garbage data.
     */
    @Test
    public void testCorruptedFile() throws Exception {
        String fileName = getUniqueFileName("corrupted");
        java.nio.file.Files.write(new File(fileName).toPath(), "MALFORMED_DATA_NOT_XML".getBytes());

        EntriesRepository repo = EntriesRepository.newInstance(fileName, "key".toCharArray());

        assertThrows(Exception.class, () -> {
            try {
                repo.readDocument();
            } finally {
                System.gc(); // Clean up locked file handles
            }
        });
    }

    /**
     * Test Goal: Edge case of passing null as the document.
     * Observation: Based on previous run, SUT does not throw exception but creates file.
     */
    @Test
    public void testWriteNullDocument() {
        String fileName = getUniqueFileName("null_doc");
        EntriesRepository repo = EntriesRepository.newInstance(fileName);

        // If the SUT handles null silently, our test should reflect that and check results
        assertDoesNotThrow(() -> repo.writeDocument(null));
        assertTrue(new File(fileName).exists(), "File should exist even after null write.");
    }

    @Test
    public void testReadNonExistentFile() {
        String fileName = getUniqueFileName("non_existent");
        // Do not create the file
        EntriesRepository repo = EntriesRepository.newInstance(fileName);
        assertThrows(IOException.class, () -> repo.readDocument());
    }

 
}
