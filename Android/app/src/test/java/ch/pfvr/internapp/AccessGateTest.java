package ch.pfvr.internapp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class AccessGateTest {
    @Test public void normalizeAcceptsGroupedHumanInput() {
        assertEquals("ABCD2345EFGH6789", AccessGate.normalize(" abcd-2345 efgh-6789 "));
    }

    @Test public void digestComparisonWorksWithKnownTestVector() {
        String normalized = "ABCD2345EFGH6789";
        assertTrue(AccessGate.matchesDigest(normalized, AccessGate.sha256Hex(normalized)));
        assertFalse(AccessGate.matchesDigest(normalized + "X", AccessGate.sha256Hex(normalized)));
    }

    @Test public void productionGateRejectsObviousInvalidValues() {
        assertFalse(AccessGate.matches(null));
        assertFalse(AccessGate.matches(""));
        assertFalse(AccessGate.matches("PFVR"));
        assertFalse(AccessGate.matches("0000-0000-0000-0000"));
    }
}
