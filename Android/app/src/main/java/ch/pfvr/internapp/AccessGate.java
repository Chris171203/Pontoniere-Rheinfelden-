package ch.pfvr.internapp;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Locale;

/**
 * Verifies the shared first-use activation code without storing the code itself.
 *
 * <p>This is an offline app gate, not a replacement for server-side authentication.
 * The personal PFVR internal URL remains device-local and is never embedded here.</p>
 */
final class AccessGate {
    private static final int CODE_LENGTH = 16;
    private static final String EXPECTED_SHA256 =
            "a2d3d2081df9bc8f1fc60a63afe0402b2917840adf4beada017709c77568fb63";

    private AccessGate() {}

    static boolean matches(String candidate) {
        String normalized = normalize(candidate);
        return normalized.length() == CODE_LENGTH && matchesDigest(normalized, EXPECTED_SHA256);
    }

    static String normalize(String candidate) {
        if (candidate == null) return "";
        String upper = candidate.toUpperCase(Locale.ROOT);
        StringBuilder out = new StringBuilder(upper.length());
        for (int i = 0; i < upper.length(); i++) {
            char c = upper.charAt(i);
            if ((c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')) out.append(c);
        }
        return out.toString();
    }

    static boolean matchesDigest(String normalizedCandidate, String expectedSha256) {
        if (normalizedCandidate == null || expectedSha256 == null) return false;
        byte[] actual = sha256(normalizedCandidate);
        byte[] expected = hexToBytes(expectedSha256);
        return expected.length == actual.length && MessageDigest.isEqual(actual, expected);
    }

    static String sha256Hex(String value) {
        byte[] digest = sha256(value == null ? "" : value);
        StringBuilder out = new StringBuilder(digest.length * 2);
        for (byte b : digest) out.append(String.format(Locale.ROOT, "%02x", b & 0xff));
        return out.toString();
    }

    private static byte[] sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(value.getBytes(StandardCharsets.UTF_8));
        } catch (Exception error) {
            throw new IllegalStateException("SHA-256 unavailable", error);
        }
    }

    private static byte[] hexToBytes(String hex) {
        if ((hex.length() & 1) != 0) return new byte[0];
        byte[] out = new byte[hex.length() / 2];
        for (int i = 0; i < out.length; i++) {
            int hi = Character.digit(hex.charAt(i * 2), 16);
            int lo = Character.digit(hex.charAt(i * 2 + 1), 16);
            if (hi < 0 || lo < 0) return new byte[0];
            out[i] = (byte) ((hi << 4) | lo);
        }
        return out;
    }
}
