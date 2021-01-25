package org.leandro.catalogue.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FriendlyUrlTest {

    @Test
    public void testSanitizeWithDashes() {
        assertEquals("harry-potter", FriendlyUrl.sanitizeWithDashes("Harry Potter"));
        assertEquals("harry", FriendlyUrl.sanitizeWithDashes("Harry "));
    }
}
