package jpass.util;

import org.junit.jupiter.api.Test;
import java.time.format.DateTimeFormatter;
import static org.junit.jupiter.api.Assertions.*;

public class DateUtilsBlackBoxTest {

    @Test
    public void testCreateFormatterEquivalence() {
        
        DateTimeFormatter formatter = DateUtils.createFormatter("yyyy-MM-dd");
        assertNotNull(formatter, "Valid pattern should successfully return a formatter");
    }

    @Test
    public void testFormatIsoDateTimeEquivalence() {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String isoResult = DateUtils.formatIsoDateTime("2026-04-24T12:00:00", formatter);
        assertEquals("2026-04-24 12:00:00", isoResult, "Should correctly format standard ISO strings");
        String epochResult = DateUtils.formatIsoDateTime("1700000000000", formatter);
        assertNotNull(epochResult, "Should correctly parse and format Epoch timestamp strings");
        assertFalse(epochResult.isEmpty());
    }

    @Test
    public void testErrorGuessingAndCombinatorial() {
        
        DateTimeFormatter validFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter invalidFormatter = DateUtils.createFormatter("invalid_format_xyz");
        String fallbackYear = java.util.TimeZone.getDefault().getRawOffset() < 0 ? "1969" : "1970";
        String garbageResult = DateUtils.formatIsoDateTime("not_a_date_at_all", validFormatter);
        assertTrue(garbageResult.contains(fallbackYear), "Garbage data should fallback to local Epoch 0");
        String nullResult = DateUtils.formatIsoDateTime(null, validFormatter);
        assertTrue(nullResult.contains(fallbackYear), "Null data should safely fallback to local Epoch 0");
        String comboResult = DateUtils.formatIsoDateTime("2021-03-02T20:11:58", invalidFormatter);
        assertEquals("2021-03-02", comboResult, "Should format correctly using the fallback formatter");
    }
}
