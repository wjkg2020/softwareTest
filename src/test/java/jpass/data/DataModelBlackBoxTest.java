package jpass.data;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;

import jpass.xml.bind.Entries;
import jpass.xml.bind.Entry;

/**
 * Test suite for DataModel using Blackbox Testing techniques.
 * Techniques applied: Equivalence Partitioning (EP), Boundary Analysis (BA), Error Guessing (EG).
 */
public class DataModelBlackBoxTest {

    private DataModel dataModel;

    @BeforeEach
    public void setUp() {
        dataModel = DataModel.getInstance();
        // 关键操作：每个测试前清空单例状态，模拟独立环境
        dataModel.clear();
    }

    // --- 1. Equivalence Partitioning (EP) ---

    /**
     * Test Goal: Verify retrieval of an existing entry by its title.
     * Technique: Equivalence Partitioning (Valid Class).
     */
    @Test
    public void testGetEntryByTitle_ValidTitle() {
        Entry entry = new Entry();
        entry.setTitle("GitHub");
        dataModel.getEntries().getEntry().add(entry);

        Entry result = dataModel.getEntryByTitle("GitHub");
        assertNotNull(result, "Should find the entry with title 'GitHub'");
        assertEquals("GitHub", result.getTitle());
    }

    /**
     * Test Goal: Verify behavior when searching for a non-existent title.
     * Technique: Equivalence Partitioning (Invalid Class).
     */
    @Test
    public void testGetEntryByTitle_NonExistentTitle() {
        Entry result = dataModel.getEntryByTitle("NonExistent");
        assertNull(result, "Should return null for a title that does not exist");
    }

    /**
     * Test Goal: Verify modified state toggle.
     * Technique: Equivalence Partitioning.
     */
    @Test
    public void testModifiedState_Toggle() {
        dataModel.setModified(true);
        assertTrue(dataModel.isModified());
        dataModel.setModified(false);
        assertFalse(dataModel.isModified());
    }

    // --- 2. Boundary Analysis (BA) ---

    /**
     * Test Goal: Verify getTitles() when the list is empty.
     * Technique: Boundary Analysis (Lower Bound: 0 entries).
     */
    @Test
    public void testGetTitles_EmptyList() {
        List<String> titles = dataModel.getTitles();
        assertTrue(titles.isEmpty(), "Titles list should be empty when no entries are added");
    }

    /**
     * Test Goal: Verify password handling with an empty character array.
     * Technique: Boundary Analysis (Empty array).
     */
    @Test
    public void testSetPassword_EmptyArray() {
        char[] emptyPass = new char[0];
        dataModel.setPassword(emptyPass);
        assertEquals(0, dataModel.getPassword().length);
    }

    // --- 3. Error Guessing (EG) ---

    /**
     * Test Goal: Check for NullPointerException when passing null to getEntryByTitle.
     * Technique: Error Guessing (Robustness).
     */
    @Test
    public void testGetEntryByTitle_NullInput() {
        assertDoesNotThrow(() -> {
            Entry result = dataModel.getEntryByTitle(null);
            assertNull(result);
        }, "Should handle null input gracefully without throwing NPE");
    }

    /**
     * Test Goal: Verify that the clear() method resets ALL fields, especially the modified flag.
     * Technique: Error Guessing (State consistency).
     */
    @Test
    public void testClear_FullReset() {
        dataModel.setFileName("test.jpass");
        dataModel.setModified(true);
        dataModel.setPassword("pass".toCharArray());

        dataModel.clear();

        assertNull(dataModel.getFileName(), "FileName should be null after clear");
        assertFalse(dataModel.isModified(), "Modified should be false after clear");
        assertNull(dataModel.getPassword(), "Password should be null after clear");
        assertTrue(dataModel.getEntries().getEntry().isEmpty(), "Entries should be empty after clear");
    }

    /**
     * Test Goal: Verify case sensitivity for titles.
     * Technique: Error Guessing.
     */
    @Test
    public void testGetEntryByTitle_CaseSensitivity() {
        Entry entry = new Entry();
        entry.setTitle("Amazon");
        dataModel.getEntries().getEntry().add(entry);

        Entry result = dataModel.getEntryByTitle("amazon");
        assertNull(result, "Should not find 'amazon' if the stored title is 'Amazon'");
    }

    /**
     * Test Goal: Ensure Singleton property - multiple getInstance calls return the same object.
     * Technique: Error Guessing.
     */
    @Test
    public void testSingletonInstanceIdentity() {
        DataModel instance1 = DataModel.getInstance();
        DataModel instance2 = DataModel.getInstance();
        assertSame(instance1, instance2, "Both instances must refer to the same memory address");
    }
}
