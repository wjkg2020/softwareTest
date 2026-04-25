package jpass.util;

import org.junit.jupiter.api.Test;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;
import static org.junit.jupiter.api.Assertions.*;

public class DateUtilsWhiteBoxTest {

    @Test
    public void testCreateFormatterBranchCoverage() {
        // Branch 1: Try block succeeds
        DateTimeFormatter f1 = DateUtils.createFormatter("yyyy");
        assertNotEquals(DateTimeFormatter.ISO_DATE, f1, "Should use custom formatter");

        // Branch 2: Catch block (IllegalArgumentException)
        // 【修复】使用真正的非法模式字符串 "foo" 而不是 "q"
        DateTimeFormatter f2 = DateUtils.createFormatter("foo");
        assertEquals(DateTimeFormatter.ISO_DATE, f2, "IllegalArgumentException must fallback to ISO_DATE");

        // Branch 3: Catch block (NullPointerException)
        DateTimeFormatter f3 = DateUtils.createFormatter(null);
        assertEquals(DateTimeFormatter.ISO_DATE, f3, "NullPointerException must fallback to ISO_DATE");
    }

    @Test
    public void testFormatIsoDateTimeBranchCoverage() {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

        String path1 = DateUtils.formatIsoDateTime("2026-01-01T10:00:00", formatter);
        assertTrue(path1.contains("2026-01-01"), "Path 1 failed");

        String path2 = DateUtils.formatIsoDateTime("1616697411000", formatter);
        assertNotNull(path2, "Path 2 failed to parse epoch");

        // 【修复】动态获取当前时区下 Epoch 0 的年份，而不是写死 1970
        String fallbackYear = "1970";
        if (TimeZone.getDefault().getRawOffset() < 0) {
            fallbackYear = "1969"; // 西半球时区退回到了 1969 年
        }

        String path3 = DateUtils.formatIsoDateTime("unparseable_string", formatter);
        assertTrue(path3.contains(fallbackYear), "Deepest catch block should fallback to Epoch 0 in local timezone");
    }
}