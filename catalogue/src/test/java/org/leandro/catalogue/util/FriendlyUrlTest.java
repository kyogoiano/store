package org.leandro.catalogue.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FriendlyUrlTest {

    @Test
    public void testSanitizeWithDashes() {
        expect:
        assertEquals("harry-potter", new FriendlyUrl().sanitizeWithDashes("Harry Potter"));
        assertEquals("harry", new FriendlyUrl().sanitizeWithDashes("Harry "));
    }
}
