package jpass.data;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;
import java.util.List;

import jpass.xml.bind.Entries;
import jpass.xml.bind.Entry;

/**
 * Whitebox Test Suite for DataModel.
 * Focus: Branch Coverage, Path Coverage, and Internal Logic Integrity.
 * Goal: Achieve 100% branch coverage and prepare for high mutation scores.
 */
public class DataModelWhiteBoxTest {

    private DataModel dataModel;

    @BeforeEach
    public void resetSingleton() throws Exception {
        // 白盒测试特权：利用反射重置单例状态，确保测试环境绝对干净
        Field instance = DataModel.class.getDeclaredField("instance");
        instance.setAccessible(true);
        instance.set(null, null);

        dataModel = DataModel.getInstance();
        dataModel.clear();
    }

    // --- 1. 覆盖 getInstance() 的同步分支 ---

    /**
     * Test Goal: Cover the 'if (instance == null)' branch in getInstance().
     * Logic: Ensure that the first call creates an instance and the second call returns the same one.
     */
    @Test
    public void testGetInstance_BranchCoverage() {
        DataModel firstCall = DataModel.getInstance();
        assertNotNull(firstCall);

        DataModel secondCall = DataModel.getInstance();
        assertSame(firstCall, secondCall, "Logic Branch: Should return the existing instance on second call.");
    }

    // --- 2. 覆盖 getEntryByTitle() 的所有逻辑分支 ---

    /**
     * Test Goal: Cover the 'if (entryIndex != -1)' TRUE branch.
     * Logic: The title exists in the internal list.
     */
    @Test
    public void testGetEntryByTitle_BranchTrue() {
        Entry e = new Entry();
        e.setTitle("Target");
        dataModel.getEntries().getEntry().add(e);

        // 这里触发了 getEntryIndexByTitle -> indexOf -> 找到索引 (>=0) -> 进入 if
        Entry result = dataModel.getEntryByTitle("Target");
        assertNotNull(result);
        assertEquals("Target", result.getTitle());
    }

    /**
     * Test Goal: Cover the 'if (entryIndex != -1)' FALSE branch.
     * Logic: The title does NOT exist, leading to a return null path.
     */
    @Test
    public void testGetEntryByTitle_BranchFalse() {
        // 这里触发了 getEntryIndexByTitle -> indexOf -> 返回 -1 -> 跳过 if
        Entry result = dataModel.getEntryByTitle("NonExistent");
        assertNull(result, "Logic Branch: Should return null when index is -1");
    }

    // --- 3. 覆盖 getTitles() 的 Stream 内部逻辑 ---

    /**
     * Test Goal: Verify the mapping logic in Stream API.
     * Logic: Ensure the map(Entry::getTitle) handles various entry states.
     */
    @Test
    public void testGetTitles_StreamLogic() {
        Entry e1 = new Entry(); e1.setTitle("A");
        Entry e2 = new Entry(); e2.setTitle("B");
        dataModel.getEntries().getEntry().add(e1);
        dataModel.getEntries().getEntry().add(e2);

        List<String> titles = dataModel.getTitles();

        assertEquals(2, titles.size());
        assertTrue(titles.contains("A"));
        assertTrue(titles.contains("B"));
    }

    /**
     * Test Goal: Potential Bug Hunting (Null Titles).
     * Logic: What if an entry exists but its title is null?
     * Whitebox Insight: The map function will process it.
     */
    @Test
    public void testGetTitles_WithNullTitleEntry() {
        Entry e = new Entry();
        e.setTitle(null); // 这是一个细小的边界情况
        dataModel.getEntries().getEntry().add(e);

        List<String> titles = dataModel.getTitles();
        assertEquals(1, titles.size());
        assertNull(titles.get(0), "Stream should correctly map a null title if it exists in the model");
    }

    // --- 4. 覆盖 clear() 的深度重置 ---

    /**
     * Test Goal: Ensure the internal entries list is actually cleared, not just the reference.
     * Logic: Verify that this.entries.getEntry().clear() is executed.
     */
    @Test
    public void testClear_InternalListClearing() {
        dataModel.getEntries().getEntry().add(new Entry());
        assertEquals(1, dataModel.getEntries().getEntry().size());

        dataModel.clear();

        // 白盒关注点：不仅看 dataModel 状态，还要看 entries 内部列表的状态
        assertEquals(0, dataModel.getEntries().getEntry().size(), "The internal XML entry list must be purged.");
    }

    // --- 5. 状态一致性白盒测试 (Mutation Killer) ---

    /**
     * Test Goal: Kill mutants related to 'modified' flag assignment.
     * Logic: If a mutation changes 'this.modified = false' to 'true' in clear(), this test should fail.
     */
    @Test
    public void testModifiedFlag_Consistency() {
        dataModel.setModified(true);
        dataModel.clear();
        assertFalse(dataModel.isModified(), "Clear must reset modified flag to false.");
    }
}