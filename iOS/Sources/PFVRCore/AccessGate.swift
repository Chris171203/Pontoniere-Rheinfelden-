import Foundation
#if canImport(CryptoKit)
import CryptoKit
#endif

/// An offline first-use gate, not server authentication. The original code is never stored in the repository.
public enum AccessGate {
    private static let expectedSHA256 = "a2d3d2081df9bc8f1fc60a63afe0402b2917840adf4beada017709c77568fb63"
    public static func normalize(_ candidate: String) -> String {
        String(candidate.uppercased(with: Locale(identifier: "en_US_POSIX")).unicodeScalars.filter {
            (65...90).contains($0.value) || (48...57).contains($0.value)
        })
    }
    public static func matches(_ candidate: String) -> Bool {
        let normalized = normalize(candidate)
        return normalized.count == 16 && matchesDigest(normalized, expectedSHA256: expectedSHA256)
    }
    public static func matchesDigest(_ normalizedCandidate: String, expectedSHA256: String) -> Bool {
        guard expectedSHA256.utf8.count == 64 else { return false }
        let characters = Array(expectedSHA256.utf8)
        func nibble(_ c: UInt8) -> UInt8? {
            switch c {
            case 48...57: return c - 48
            case 65...70: return c - 55
            case 97...102: return c - 87
            default: return nil
            }
        }
        var expected: [UInt8] = []
        for index in stride(from: 0, to: 64, by: 2) {
            guard let high = nibble(characters[index]), let low = nibble(characters[index + 1]) else { return false }
            expected.append((high << 4) | low)
        }
        let actual = digest(normalizedCandidate)
        guard actual.count == expected.count else { return false }
        var difference: UInt8 = 0
        for index in actual.indices { difference |= actual[index] ^ expected[index] }
        return difference == 0
    }
    public static func sha256Hex(_ value: String) -> String { digest(value).map { String(format: "%02x", $0) }.joined() }
    private static func digest(_ value: String) -> [UInt8] {
        #if canImport(CryptoKit)
        return Array(SHA256.hash(data: Data(value.utf8)))
        #else
        // Non-Apple build hosts may lack CryptoKit. Keep the gate closed; never substitute an unverified hash.
        return []
        #endif
    }
}
