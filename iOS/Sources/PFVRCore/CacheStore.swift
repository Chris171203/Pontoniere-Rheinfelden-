import Foundation

public struct CacheMetadata: Codable, Equatable, Sendable {
    public let source: String
    public let updatedAt: Date
    public var isStale: Bool
    public var failure: String?
    public init(source: String, updatedAt: Date, isStale: Bool = false, failure: String? = nil) {
        self.source = source; self.updatedAt = updatedAt; self.isStale = isStale; self.failure = failure
    }
    public func age(now: Date = Date()) -> TimeInterval { max(0, now.timeIntervalSince(updatedAt)) }
}
public struct Loaded<Value: Codable>: Codable {
    public let value: Value
    public var metadata: CacheMetadata
    public init(value: Value, metadata: CacheMetadata) { self.value = value; self.metadata = metadata }
}
extension Loaded: Sendable where Value: Sendable {}
extension Loaded: Equatable where Value: Equatable {}

/// Atomic local snapshots. Only public source data belongs here; personal access links do not.
public struct CacheStore: Sendable {
    public let directory: URL
    public init(directory: URL) { self.directory = directory }
    private func file(_ key: String) -> URL { directory.appendingPathComponent(key.replacingOccurrences(of: "/", with: "_") + ".json") }
    public func read<Value: Codable>(_ key: String, as: Value.Type = Value.self) -> Loaded<Value>? {
        guard let data = try? Data(contentsOf: file(key)) else { return nil }
        return try? JSONDecoder().decode(Loaded<Value>.self, from: data)
    }
    public func write<Value: Codable>(_ item: Loaded<Value>, key: String) throws {
        try FileManager.default.createDirectory(at: directory, withIntermediateDirectories: true)
        try JSONEncoder().encode(item).write(to: file(key), options: .atomic)
    }
    public func clear() throws {
        guard FileManager.default.fileExists(atPath: directory.path) else { return }
        for file in try FileManager.default.contentsOfDirectory(at: directory, includingPropertiesForKeys: nil) where file.pathExtension == "json" {
            try FileManager.default.removeItem(at: file)
        }
    }
}
