import BackgroundTasks
import Foundation
import PFVRCore

/// Android requests a 30-minute WorkManager cycle, enabled by default after activation.
/// iOS only accepts an earliest start hint; delivery depends on system conditions and app usage.
enum BackgroundRefreshPolicy {
    static let identifier = "ch.pfvr.app.test.public-refresh"
    static let preferenceKey = "background_refresh"
    static let activationKey = "access_unlocked_v1"
    static let minimumDelay: TimeInterval = 30 * 60
    static func permits(unlocked: Bool, enabled: Bool, testing: Bool) -> Bool { unlocked && enabled && !testing }
    static func enabled(in defaults: UserDefaults) -> Bool {
        defaults.object(forKey: preferenceKey) as? Bool ?? true
    }
    static func earliestBeginDate(now: Date) -> Date { now.addingTimeInterval(minimumDelay) }
}

@MainActor
protocol BackgroundRefreshScheduling: AnyObject {
    func submit(earliestBeginDate: Date) throws
    func cancel()
}

@MainActor
private final class AppleRefreshScheduler: BackgroundRefreshScheduling {
    func submit(earliestBeginDate: Date) throws {
        let request = BGAppRefreshTaskRequest(identifier: BackgroundRefreshPolicy.identifier)
        request.earliestBeginDate = earliestBeginDate
        try BGTaskScheduler.shared.submit(request)
    }
    func cancel() { BGTaskScheduler.shared.cancel(taskRequestWithIdentifier: BackgroundRefreshPolicy.identifier) }
}

@MainActor
protocol BackgroundRefreshTaskHandle: AnyObject {
    var expirationHandler: (() -> Void)? { get set }
    func setTaskCompleted(success: Bool)
}

@MainActor
private final class AppleRefreshTaskHandle: BackgroundRefreshTaskHandle {
    private let task: BGAppRefreshTask
    init(_ task: BGAppRefreshTask) { self.task = task }
    var expirationHandler: (() -> Void)? {
        get { task.expirationHandler }
        set { task.expirationHandler = newValue }
    }
    func setTaskCompleted(success: Bool) { task.setTaskCompleted(success: success) }
}

/// Owns one OS delivery. Completion is exactly once, including expiration racing with a normal return.
@MainActor
final class BackgroundRefreshRun {
    private let handle: any BackgroundRefreshTaskHandle
    private let work: @Sendable () async -> Bool
    private let onFinish: @MainActor () -> Void
    private var operation: Task<Void, Never>?
    private(set) var isFinished = false
    init(handle: any BackgroundRefreshTaskHandle, work: @escaping @Sendable () async -> Bool, onFinish: @escaping @MainActor () -> Void = {}) {
        self.handle = handle
        self.work = work
        self.onFinish = onFinish
    }
    func start() {
        guard operation == nil, !isFinished else { return }
        handle.expirationHandler = { [weak self] in
            Task { @MainActor in self?.expire() }
        }
        operation = Task { [weak self] in
            guard let self else { return }
            let success: Bool
            if Task.isCancelled { success = false }
            else { success = await self.work() }
            self.finish(success: success && !Task.isCancelled)
            // Keep the run retained until all public cache writers have drained after cancellation.
            self.operation = nil
            self.onFinish()
        }
    }
    func expire() {
        guard !isFinished else { return }
        operation?.cancel()
        finish(success: false)
    }
    func cancelAndWait() async {
        let pending = operation
        expire()
        await pending?.value
    }
    private func finish(success: Bool) {
        guard !isFinished else { return }
        isFinished = true
        handle.expirationHandler = nil
        handle.setTaskCompleted(success: success)
    }
}

@MainActor
final class BackgroundRefresh {
    static let shared = BackgroundRefresh()
    private let defaults: UserDefaults
    private let scheduler: any BackgroundRefreshScheduling
    private let clock: () -> Date
    private let testing: () -> Bool
    private let work: @Sendable () async -> Bool
    private var registered = false
    private var active: BackgroundRefreshRun?
    private(set) var schedulingAvailable = true

    init(defaults: UserDefaults = .standard, scheduler: (any BackgroundRefreshScheduling)? = nil,
         clock: @escaping () -> Date = { Date() }, testing: @escaping () -> Bool = {
             #if DEBUG
             return ProcessInfo.processInfo.arguments.contains("-ui-testing")
             #else
             return false
             #endif
         }, work: @escaping @Sendable () async -> Bool = { await BackgroundPublicRefresh.perform() }) {
        self.defaults = defaults
        self.scheduler = scheduler ?? AppleRefreshScheduler()
        self.clock = clock
        self.testing = testing
        self.work = work
    }

    /// Call during app initialization. Registering creates no data service and performs no network request.
    func register() {
        guard !registered, !testing() else { return }
        registered = BGTaskScheduler.shared.register(forTaskWithIdentifier: BackgroundRefreshPolicy.identifier, using: .main) { [weak self] task in
            guard let task = task as? BGAppRefreshTask else { task.setTaskCompleted(success: false); return }
            Task { @MainActor in
                guard let self else { task.setTaskCompleted(success: false); return }
                self.receive(AppleRefreshTaskHandle(task))
            }
        }
        schedulingAvailable = registered
    }

    func schedule(unlocked: Bool, enabled: Bool, testing: Bool = false) {
        scheduler.cancel()
        guard BackgroundRefreshPolicy.permits(unlocked: unlocked, enabled: enabled, testing: testing || self.testing()) else {
            active?.expire()
            return
        }
        do {
            try scheduler.submit(earliestBeginDate: BackgroundRefreshPolicy.earliestBeginDate(now: clock()))
            schedulingAvailable = true
        } catch {
            // iOS can refuse scheduling in a simulator or when background refresh is disabled.
            // Foreground refresh and the last cache remain usable; no interval is promised.
            schedulingAvailable = false
        }
    }

    /// Cache deletion waits for any old background requests/parsing to finish before removing files.
    func cancelAndWait() async {
        let pending = active
        await pending?.cancelAndWait()
    }

    /// Gate and preference are re-read at actual delivery, including a cold background launch.
    func receive(_ handle: any BackgroundRefreshTaskHandle) {
        let unlocked = defaults.bool(forKey: BackgroundRefreshPolicy.activationKey)
        let enabled = BackgroundRefreshPolicy.enabled(in: defaults)
        guard BackgroundRefreshPolicy.permits(unlocked: unlocked, enabled: enabled, testing: testing()) else {
            scheduler.cancel()
            handle.setTaskCompleted(success: false)
            return
        }
        guard active == nil else { handle.setTaskCompleted(success: false); return }
        schedule(unlocked: unlocked, enabled: enabled)
        let run = BackgroundRefreshRun(handle: handle, work: work, onFinish: { [weak self] in self?.active = nil })
        active = run
        run.start()
    }
}

enum BackgroundPublicRefresh {
    /// Uses the same persistent *public* cache as the foreground app. No Keychain, personal link or WebView access.
    static func perform() async -> Bool {
        guard !Task.isCancelled else { return false }
        let configuration = URLSessionConfiguration.ephemeral
        configuration.urlCache = nil
        configuration.httpCookieStorage = nil
        configuration.waitsForConnectivity = false
        configuration.timeoutIntervalForRequest = 15
        configuration.timeoutIntervalForResource = 25
        let session = URLSession(configuration: configuration)
        defer { session.finishTasksAndInvalidate() }
        let directory = FileManager.default.urls(for: .applicationSupportDirectory, in: .userDomainMask)[0]
            .appendingPathComponent("PFVR/PublicCache", isDirectory: true)
        let service = PFVRDataService(cacheDirectory: directory, transport: URLSessionTransport(session: session))
        return await withTaskCancellationHandler {
            await withTaskGroup(of: Bool.self) { group in
                group.addTask {
                    guard !Task.isCancelled, let value = try? await service.weather() else { return false }
                    return value.metadata.failure == nil && !value.metadata.isStale
                }
                group.addTask {
                    guard !Task.isCancelled, let value = try? await service.events() else { return false }
                    return value.metadata.failure == nil && !value.metadata.isStale
                }
                group.addTask {
                    guard !Task.isCancelled, let value = try? await service.news() else { return false }
                    return value.metadata.failure == nil && !value.metadata.isStale
                }
                for station in HydroStation.allCases {
                    group.addTask {
                        guard !Task.isCancelled else { return false }
                        let value = await service.hydro(station: station)
                        return value.failures.isEmpty && value.live != nil && value.fine != nil && value.history != nil
                    }
                }
                var success = true
                for await result in group { success = success && result }
                return success && !Task.isCancelled
            }
        } onCancel: {
            // PFVRDataService coalesces work with unstructured Tasks. Invalidating this dedicated
            // session also stops their in-flight requests and prevents fallback requests after expiration.
            session.invalidateAndCancel()
        }
    }
}
