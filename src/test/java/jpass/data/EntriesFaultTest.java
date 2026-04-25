package jpass.data;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import jpass.xml.bind.Entries;
import java.nio.file.Paths; // 必须导入这个类
/**
 * FAULT DEMONSTRATION: EntriesRepository 缺陷验证集合
 * * 本测试类专门用于记录和证明在测试过程中发现的 SUT 缺陷。
 * 包含：
 * 1. 资源泄露 (Resource Leak): 异常路径下未关闭文件句柄。
 * 2. 异常包装不一致 (Exception Wrapping Inconsistency): 内部解析细节泄露。
 * * 注意：由于这些测试旨在证明 Bug 的存在，它们在未修复的源码上运行【必然失败】（报红）。
 * 请在运行 PITest 时将此文件排除。
 */
public class EntriesFaultTest {

    @TempDir
    Path tempDir;

    /**
     * 【漏洞证明 1：资源泄露】
     * 证明点：当读取过程由于密码错误等原因中断时，底层的 FileInputStream 没被释放。
     * 预期：在 Windows 上，由于文件被锁定，删除操作会失败。
     */
    @Test
    public void demonstrateResourceLeakFault() throws Exception {
        String fileName = tempDir.resolve("leak_demo.jpass").toString();
        File file = new File(fileName);
        char[] correctKey = "secure_pass".toCharArray();
        char[] wrongKey = "wrong_pass".toCharArray();

        // 1. 准备工作：创建一个合法的加密文件
        EntriesRepository.newInstance(fileName, correctKey).writeDocument(new Entries());

        // 2. 触发 Bug：使用错误的密码读取，触发 GZIP 流初始化异常
        EntriesRepository wrongRepo = EntriesRepository.newInstance(fileName, wrongKey);
        try {
            wrongRepo.readDocument();
        } catch (Exception e) {
            System.out.println("Caught expected reading exception.");
        }

        // 3. 证明 Fault：尝试删除文件。如果 JPass 没关流，Windows 会抛出 FileSystemException
        boolean deleted = false;
        try {
            deleted = Files.deleteIfExists(file.toPath());
        } catch (java.nio.file.FileSystemException e) {
            System.err.println("FAULT PROVEN: File system locked the file! Resource leaked.");
            deleted = false;
        }

        assertTrue(deleted, "BUG FOUND: File should be deletable if the stream was closed properly.");
    }

    /**
     * 【漏洞证明 2：异常包装不一致/抽象泄露】
     * 证明点：源码声明了会抛出业务异常 DocumentProcessException，但在遇到解析错误时，
     * 却直接泄露了底层的 IOException (JsonParseException)。
     * 预期：assertThrows(DocumentProcessException.class) 会失败，因为实际抛出的是 IOException。
     */
    @Test
    public void demonstrateExceptionWrappingFault() throws Exception {
        String fileName = tempDir.resolve("wrapping_fault.jpass").toString();
        // 写入一段完全非法的 XML 内容
        // Java 8 兼容写法
        Files.write(Paths.get(fileName), "INVALID_XML_DATA_NOT_XML".getBytes());
        EntriesRepository repo = EntriesRepository.newInstance(fileName);

        // 按照良好的封装原则，持久化层应该将底层的解析错误包装为业务异常 DocumentProcessException。
        // 但 JPass 源码中第一个 catch(IOException) 抢先捕获了 JsonParseException 并直接重抛。
        assertThrows(DocumentProcessException.class, () -> {
            repo.readDocument();
        }, "FAULT PROVEN: SUT leaked internal IOException instead of wrapping it into DocumentProcessException.");
    }
}