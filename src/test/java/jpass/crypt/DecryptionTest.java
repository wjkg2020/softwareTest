package jpass.crypt;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DecryptionTest {

    @Test
    public void testExceptionInstantiation() {
        
        DecryptException exception = new DecryptException();
        assertEquals("Decryption failed.", exception.getMessage(),
                "The exception message should match the hardcoded default string.");
    }
}
