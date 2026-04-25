package jpass.crypt;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DecryptionTest {

    @Test
    public void testExceptionInstantiation() {
        // White-box: Instantiate the exception to achieve 100% class/line coverage
        DecryptException exception = new DecryptException();

        // Verify the hardcoded message defined in the default constructor
        assertEquals("Decryption failed.", exception.getMessage(),
                "The exception message should match the hardcoded default string.");
    }
}