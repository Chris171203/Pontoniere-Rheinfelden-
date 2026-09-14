import Foundation
import CryptoKit
import ImageIO
import UniformTypeIdentifiers
import PFVRCore

/// Public photos only; bounded disk cache with a clear-generation barrier.
actor ClubImageStore {
    static let shared = ClubImageStore()
    private let directory: URL
    private let transport: any HTTPTransport
    private var generation = 0
    private var tasks: [URL: (UUID, Task<Data, Error>)] = [:]
    init(directory: URL? = nil, transport: (any HTTPTransport)? = nil) {
        self.directory = directory ?? FileManager.default.urls(for: .cachesDirectory, in: .userDomainMask)[0].appendingPathComponent("PFVR/ClubImages")
        if let transport { self.transport = transport }
        else {
            let configuration = URLSessionConfiguration.ephemeral
            configuration.httpMaximumConnectionsPerHost = 2
            self.transport = URLSessionTransport(session: URLSession(configuration: configuration))
        }
    }
    func image(_ url: URL) async throws -> Data {
        guard ClubPhoto.allowed(url) else { throw PFVRDataError.insecureURL }
        if let existing = tasks[url] { return try await existing.1.value }
        let file = directory.appendingPathComponent(SHA256.hash(data: Data(url.absoluteString.utf8)).map { String(format: "%02x", $0) }.joined() + ".jpg")
        let old = try? Data(contentsOf: file)
        let modified = (try? file.resourceValues(forKeys: [.contentModificationDateKey]))?.contentModificationDate
        if let old, let modified, Date().timeIntervalSince(modified) >= 0, Date().timeIntervalSince(modified) < 86400 { return old }
        let epoch = generation, id = UUID()
        let task = Task { try await self.fetch(url, file: file, old: old, epoch: epoch) }
        tasks[url] = (id, task)
        defer { if tasks[url]?.0 == id { tasks[url] = nil } }
        return try await task.value
    }
    func clear() throws {
        generation += 1; tasks.values.forEach { $0.1.cancel() }; tasks.removeAll()
        if FileManager.default.fileExists(atPath: directory.path) { try FileManager.default.removeItem(at: directory) }
    }
    private func fetch(_ url: URL, file: URL, old: Data?, epoch: Int) async throws -> Data {
        do {
            let request = URLRequest(url: url, cachePolicy: .reloadIgnoringLocalCacheData, timeoutInterval: 15)
            let (data, response) = try await transport.data(for: request)
            guard (200..<300).contains(response.statusCode), let finalURL = response.url, ClubPhoto.allowed(finalURL),
                  data.count <= 4 * 1024 * 1024, let source = CGImageSourceCreateWithData(data as CFData, nil),
                  let image = CGImageSourceCreateThumbnailAtIndex(source, 0, [
                    kCGImageSourceCreateThumbnailFromImageAlways: true,
                    kCGImageSourceThumbnailMaxPixelSize: 1400,
                    kCGImageSourceCreateThumbnailWithTransform: true
                  ] as CFDictionary) else { throw PFVRDataError.invalidPayload("Bild nicht verfügbar") }
            let result = NSMutableData()
            guard let destination = CGImageDestinationCreateWithData(result as CFMutableData, UTType.jpeg.identifier as CFString, 1, nil) else {
                throw PFVRDataError.invalidPayload("Bild nicht verfügbar")
            }
            CGImageDestinationAddImage(destination, image, [kCGImageDestinationLossyCompressionQuality: 0.85] as CFDictionary)
            guard CGImageDestinationFinalize(destination) else { throw PFVRDataError.invalidPayload("Bild nicht verfügbar") }
            try Task.checkCancellation()
            guard epoch == generation else { throw CancellationError() }
            let jpeg = result as Data
            try FileManager.default.createDirectory(at: directory, withIntermediateDirectories: true)
            try jpeg.write(to: file, options: .atomic)
            try trim()
            return jpeg
        } catch {
            guard epoch == generation, !Task.isCancelled else { throw CancellationError() }
            if let old { return old }
            throw error
        }
    }
    private func trim() throws {
        let keys: Set<URLResourceKey> = [.fileSizeKey, .contentModificationDateKey]
        let files = try FileManager.default.contentsOfDirectory(at: directory, includingPropertiesForKeys: Array(keys))
            .filter { $0.pathExtension == "jpg" }.sorted {
                ((try? $0.resourceValues(forKeys: keys).contentModificationDate) ?? .distantPast)
                < ((try? $1.resourceValues(forKeys: keys).contentModificationDate) ?? .distantPast)
            }
        var bytes = files.reduce(0) { $0 + ((try? $1.resourceValues(forKeys: keys).fileSize) ?? 0) }
        var count = files.count
        for file in files {
            guard bytes > 16 * 1024 * 1024 || count > 64 else { break }
            let size = (try? file.resourceValues(forKeys: keys).fileSize) ?? 0
            try FileManager.default.removeItem(at: file); bytes -= size; count -= 1
        }
    }
}
