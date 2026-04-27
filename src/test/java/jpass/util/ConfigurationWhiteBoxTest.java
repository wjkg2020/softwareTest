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

        Field instanceField = Configuration.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);

     
        System.getProperties().keySet().removeIf(key -> key.toString().startsWith("jpass."));
    }

    // ---   (Branch Coverage) ---

    @Test
    public void testSingletonBranch() {
        Configuration c1 = Configuration.getInstance();
        Configuration c2 = Configuration.getInstance();
        assertSame(c1, c2, "Should always return the same instance.");
    }

    @Test
    public void testGetValueExceptionBranch() {
        
        Configuration config = Configuration.getInstance();
        System.setProperty("jpass.invalid.int", "this_is_not_a_number");

        
        Integer result = config.getInteger("invalid.int", 100);

        
        assertEquals(100, result, "Should return default value when parsing fails.");
    }

    @Test
    public void testGetArrayBranch() {
        Configuration config = Configuration.getInstance();

       
        System.setProperty("jpass.array.test", "1,2,3");
        assertArrayEquals(new String[]{"1", "2", "3"}, config.getArray("array.test", new String[]{}));
        assertArrayEquals(new String[]{"def"}, config.getArray("nonexistent.array", new String[]{"def"}));
    }

    // --- 2.  (Loop-based Systematic Testing) ---

    @Test
    public void testAllDataTypesSystematically() {
        Configuration config = Configuration.getInstance();

     
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

   

    @Test
    public void testConfigurationIntegration() throws Exception {
    
        Configuration config = Configuration.getInstance();
        Field propField = Configuration.class.getDeclaredField("properties");
        propField.setAccessible(true);
        Properties internalProps = (Properties) propField.get(config);

        internalProps.setProperty("integration.test", "file_value");
        internalProps.setProperty("override.test", "file_value");

        System.setProperty("jpass.override.test", "system_value");

        
        assertEquals("system_value", config.get("override.test", "default"));

        
        assertEquals("file_value", config.get("integration.test", "default"));

        assertEquals("default", config.get("missing.test", "default"));
    }

    @Test
    public void testGetConfigurationFolderPathCatchBranch() throws Exception {
     
        Configuration config = Configuration.getInstance();
    
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

        // ：invoke(configInstance)
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

        // ：invoke(configInstance)
        File result = (File) pathMethod.invoke(configInstance);
        assertNotNull(result, "Path must not be null to kill mutation on Line 79");
    }

    @Test
    public void testConstructor_WhenFileDoesNotExist() throws Exception {
        Configuration configInstance = Configuration.getInstance();
        Method pathMethod = Configuration.class.getDeclaredMethod("getConfigurationFolderPath");
        pathMethod.setAccessible(true);

        // ：invoke(configInstance)
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

        Files.write(propFile.toPath(), "test=value".getBytes());
        resetSingleton();
        Configuration.getInstance();

        boolean deleted = propFile.delete();

        assertTrue(deleted, "File should be closable and deletable");
    }
}
