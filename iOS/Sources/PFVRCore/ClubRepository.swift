import Foundation
#if canImport(FoundationNetworking)
import FoundationNetworking
#endif

/// One persisted snapshot and coalesced request per public page. No personal source URLs.
public actor ClubRepository {
    private let cache: CacheStore
    private let transport: any HTTPTransport
    private let clock: @Sendable () -> Date
    private var tasks: [ClubPage: (UUID, Task<Loaded<ClubContent>, Error>)] = [:]
    private var generation = 0
    public init(cacheDirectory: URL, transport: any HTTPTransport = URLSessionTransport(), clock: @escaping @Sendable () -> Date = { Date() }) {
        cache = CacheStore(directory: cacheDirectory); self.transport = transport; self.clock = clock
    }
    public func cached(_ page: ClubPage) -> Loaded<ClubContent>? {
        guard var saved: Loaded<ClubContent> = cache.read(page.rawValue) else { return nil }
        let age = clock().timeIntervalSince(saved.metadata.updatedAt)
        saved.metadata.isStale = age < 0 || age >= 86400
        return saved
    }
    public func load(_ page: ClubPage, force: Bool = false) async throws -> Loaded<ClubContent> {
        if let existing = tasks[page] { return try await existing.1.value }
        if !force, let saved = cached(page), !saved.metadata.isStale { return saved }
        let id = UUID(), epoch = generation
        let task = Task { try await self.fetch(page, epoch: epoch) }
        tasks[page] = (id, task)
        defer { if tasks[page]?.0 == id { tasks[page] = nil } }
        return try await task.value
    }
    public func clear() throws {
        generation += 1
        tasks.values.forEach { $0.1.cancel() }; tasks.removeAll()
        try cache.clear()
    }
    private func fetch(_ page: ClubPage, epoch: Int) async throws -> Loaded<ClubContent> {
        let saved = cached(page)
        do {
            var request = URLRequest(url: page.endpoint, cachePolicy: .reloadIgnoringLocalCacheData, timeoutInterval: 15)
            request.setValue("application/json", forHTTPHeaderField: "Accept")
            request.setValue("PFVR-iOS/0.15.0", forHTTPHeaderField: "User-Agent")
            let (data, response) = try await transport.data(for: request)
            guard response.url?.scheme == "https", response.url?.host == "www.pfvr.ch" else { throw PFVRDataError.insecureURL }
            guard (200..<300).contains(response.statusCode) else { throw PFVRDataError.httpStatus(response.statusCode) }
            let content = try ClubContentParser.parse(page: page, data: data)
            try Task.checkCancellation()
            guard epoch == generation else { throw CancellationError() }
            var result = Loaded(value: content, metadata: CacheMetadata(source: "pfvr.ch", updatedAt: clock()))
            do { try cache.write(result, key: page.rawValue) }
            catch { result.metadata.failure = "Datenstand konnte lokal nicht gespeichert werden." }
            return result
        } catch {
            guard epoch == generation, !Task.isCancelled else { throw CancellationError() }
            guard var saved else { throw error }
            saved.metadata.isStale = true
            saved.metadata.failure = "Keine Verbindung – gespeicherte Vereinsinfos bleiben sichtbar."
            return saved
        }
    }
}
