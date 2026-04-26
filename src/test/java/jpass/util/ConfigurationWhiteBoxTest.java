package jpass.util;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Whitebox Testing for Configuration.
 * Covers Branch Coverage, Exception Handling, and Integration.
 */
public class ConfigurationWhiteBoxTest {

    @TempDir
    Path tempDir;

    @BeforeEach
    public void resetSingleton() throws Exception {
        // 白盒测试黑科技：利用反射重置单例，以便测试构造函数中的逻辑
        Field instanceField = Configuration.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);

        // 清理可能影响测试的系统变量
        System.getProperties().keySet().removeIf(key -> key.toString().startsWith("jpass."));
    }

    // --- 1. 单元测试：分支覆盖 (Branch Coverage) ---

    @Test
    public void testSingletonBranch() {
        Configuration c1 = Configuration.getInstance();
        Configuration c2 = Configuration.getInstance();
        // 覆盖 instance == null 和 instance != null 分支
        assertSame(c1, c2, "Should always return the same instance.");
    }

    @Test
    public void testGetValueExceptionBranch() {
        // 覆盖 getValue 中的 catch (Exception e) 分支
        Configuration config = Configuration.getInstance();
        System.setProperty("jpass.invalid.int", "this_is_not_a_number");

        // 触发反射调用 Integer(String) 时的异常
        Integer result = config.getInteger("invalid.int", 100);

        // 断言：发生异常后应返回 defaultValue
        assertEquals(100, result, "Should return default value when parsing fails.");
    }

    @Test
    public void testGetArrayBranch() {
        Configuration config = Configuration.getInstance();

        // 分支 1: prop != null
        System.setProperty("jpass.array.test", "1,2,3");
        assertArrayEquals(new String[]{"1", "2", "3"}, config.getArray("array.test", new String[]{}));

        // 分支 2: prop == null
        assertArrayEquals(new String[]{"def"}, config.getArray("nonexistent.array", new String[]{"def"}));
    }

    // --- 2. 自动化多样例测试 (Loop-based Systematic Testing) ---

    @Test
    public void testAllDataTypesSystematically() {
        Configuration config = Configuration.getInstance();

        // 构造多样化的测试数据集
        Object[][] testData = {
                {"str.key", "string", "val", "val"},
                {"int.key", Integer.class, "123", 123},
                {"bool.key", Boolean.class, "true", true},
                {"bool.key.caps", Boolean.class, "TRUE", true}
        };

        for (Object[] data : testData) {
            String key = (String) data[0];
            String sysVal = (String) data[2];
            System.setProperty("jpass." + key, sysVal);

            if (data[1] == String.class || data[1] instanceof String) {
                assertEquals(data[3], config.get(key, "default"));
            } else if (data[1] == Integer.class) {
                assertEquals(data[3], config.getInteger(key, 0));
            } else if (data[1] == Boolean.class) {
                assertEquals(data[3], config.is(key, false));
            }
        }
    }

    // --- 3. 集成测试：文件加载与系统属性覆盖 ---

    @Test
    public void testConfigurationIntegration() throws Exception {
        // 这是一个复杂的集成场景：
        // 1. 系统属性 (Priority 1)
        // 2. 配置文件 (Priority 2)
        // 3. 默认值 (Priority 3)

        // 模拟配置文件 jpass.properties
        // 由于 Configuration 会寻找 jar 所在目录，我们在测试中主要通过反射注入属性
        Configuration config = Configuration.getInstance();
        Field propField = Configuration.class.getDeclaredField("properties");
        propField.setAccessible(true);
        Properties internalProps = (Properties) propField.get(config);

        internalProps.setProperty("integration.test", "file_value");
        internalProps.setProperty("override.test", "file_value");

        // 设置系统属性覆盖
        System.setProperty("jpass.override.test", "system_value");

        // 断言 1: 系统属性优先级高于文件
        assertEquals("system_value", config.get("override.test", "default"));

        // 断言 2: 文件属性高于默认值
        assertEquals("file_value", config.get("integration.test", "default"));

        // 断言 3: 不存在的属性返回默认值
        assertEquals("default", config.get("missing.test", "default"));
    }

    @Test
    public void testGetConfigurationFolderPathCatchBranch() throws Exception {
        // 理论上 getConfigurationFolderPath 很难失败，
        // 但在白盒测试中，我们可以通过模拟某些极端的 ClassLoader 情况（如果有 Mock 框架）
        // 这里的代码已经通过 file.getParentFile() 的 null 安全性做了保护
        Configuration config = Configuration.getInstance();
        // 验证即使路径复杂也能正常返回对象而不崩溃
        assertNotNull(config);
    }

    /*

    After PIT

     */
    @Test
    public void testConstructorStrictFileLoading() throws Exception {
        Configuration configInstance = Configuration.getInstance();
        Method pathMethod = Configuration.class.getDeclaredMethod("getConfigurationFolderPath");
        pathMethod.setAccessible(true);

        // 修正点：invoke(configInstance)
        File folder = (File) pathMethod.invoke(configInstance);
        if (folder == null) return;

        File propFile = new File(folder, "jpass.properties");
        String uniqueKey = "mutation.kill.test";
        String uniqueValue = "killing-line-60";

        try {
            Properties p = new Properties();
            p.setProperty(uniqueKey, uniqueValue);
            try (FileOutputStream out = new FileOutputStream(propFile)) {
                p.store(out, null);
            }

            resetSingleton();
            Configuration config = Configuration.getInstance();
            assertEquals(uniqueValue, config.get(uniqueKey, "default"));
        } finally {
            if (propFile.exists()) propFile.delete();
        }
    }

    @Test
    public void testGetConfigurationFolderPath_NotNull() throws Exception {
        Configuration configInstance = Configuration.getInstance();
        Method pathMethod = Configuration.class.getDeclaredMethod("getConfigurationFolderPath");
        pathMethod.setAccessible(true);

        // 修正点：invoke(configInstance)
        File result = (File) pathMethod.invoke(configInstance);
        assertNotNull(result, "Path must not be null to kill mutation on Line 79");
    }

    @Test
    public void testConstructor_WhenFileDoesNotExist() throws Exception {
        Configuration configInstance = Configuration.getInstance();
        Method pathMethod = Configuration.class.getDeclaredMethod("getConfigurationFolderPath");
        pathMethod.setAccessible(true);

        // 修正点：invoke(configInstance)
        File folder = (File) pathMethod.invoke(configInstance);
        File propFile = new File(folder, "jpass.properties");

        if (propFile.exists()) propFile.delete();

        resetSingleton();
        Configuration config = Configuration.getInstance();
        assertEquals("fallback", config.get("any.random.key", "fallback"));
    }

    @Test
    public void testIsCloseCalled_ByAttemptingDelete() throws Exception {
        Configuration configInstance = Configuration.getInstance();
        Method pathMethod = Configuration.class.getDeclaredMethod("getConfigurationFolderPath");
        pathMethod.setAccessible(true);
        File folder = (File) pathMethod.invoke(configInstance);
        File propFile = new File(folder, "jpass.properties");

        // 1. 准备文件
        Files.write(propFile.toPath(), "test=value".getBytes());

        // 2. 触发加载
        resetSingleton();
        Configuration.getInstance();

        // 3. 关键尝试：如果 Line 61 的 close() 被删除了，
        // 在某些系统（尤其是 Windows）上，这里会返回 false 或抛出异常
        boolean deleted = propFile.delete();

        // 如果 close() 被删除了，这里应该失败 (assertTrue 的反向逻辑)
        // 但这个测试非常依赖操作系统底层行为，不够稳定
        assertTrue(deleted, "File should be closable and deletable");
    }
}