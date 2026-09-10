import XCTest
@testable import PFVR

@MainActor
final class BackgroundRefreshTests: XCTestCase {
    private final class Scheduler: BackgroundRefreshScheduling {
        var dates: [Date] = []
        var cancellations = 0
        var reject = false
        func submit(earliestBeginDate: Date) throws {
            if reject { throw NSError(domain: "BackgroundTest", code: 1) }
            dates.append(earliestBeginDate)
        }
        func cancel() { cancellations += 1 }
    }
    private final class Handle: BackgroundRefreshTaskHandle {
        var expirationHandler: (() -> Void)?
        var completions: [Bool] = []
        var completed: (() -> Void)?
        func setTaskCompleted(success: Bool) { completions.append(success); completed?() }
    }
    private actor DrainGate {
        private var continuation: CheckedContinuation<Void, Never>?
        private var opened = false
        private(set) var waiterReturned = false
        func wait() async {
            if opened { return }
            await withCheckedContinuation { continuation = $0 }
        }
        func open() { opened = true; continuation?.resume(); continuation = nil }
        func markReturned() { waiterReturned = true }
    }

    func testPolicyRequiresActivationAndDefaultsToEnabled() throws {
        let suite = "PFVR.background.policy.\(UUID().uuidString)"
        let defaults = try XCTUnwrap(UserDefaults(suiteName: suite))
        defer { defaults.removePersistentDomain(forName: suite) }
        XCTAssertTrue(BackgroundRefreshPolicy.enabled(in: defaults))
        defaults.set(false, forKey: BackgroundRefreshPolicy.preferenceKey)
        XCTAssertFalse(BackgroundRefreshPolicy.enabled(in: defaults))
        XCTAssertFalse(BackgroundRefreshPolicy.permits(unlocked: false, enabled: true, testing: false))
        XCTAssertFalse(BackgroundRefreshPolicy.permits(unlocked: true, enabled: false, testing: false))
        XCTAssertFalse(BackgroundRefreshPolicy.permits(unlocked: true, enabled: true, testing: true))
        XCTAssertTrue(BackgroundRefreshPolicy.permits(unlocked: true, enabled: true, testing: false))
    }

    func testScheduleCancelsDisabledWorkAndRequestsOnlyAnEarliestStartHint() {
        let scheduler = Scheduler()
        let now = Date(timeIntervalSince1970: 2_000_000_000)
        let refresh = BackgroundRefresh(scheduler: scheduler, clock: { now }, testing: { false }, work: { true })
        refresh.schedule(unlocked: false, enabled: true)
        refresh.schedule(unlocked: true, enabled: false)
        refresh.schedule(unlocked: true, enabled: true, testing: true)
        XCTAssertTrue(scheduler.dates.isEmpty)
        XCTAssertEqual(scheduler.cancellations, 3)
        refresh.schedule(unlocked: true, enabled: true)
        XCTAssertEqual(scheduler.dates, [now.addingTimeInterval(1800)])
        scheduler.reject = true
        refresh.schedule(unlocked: true, enabled: true)
        XCTAssertFalse(refresh.schedulingAvailable)
    }

    func testColdDeliveryRechecksActivationAndPreferenceWithoutStartingWork() async throws {
        let suite = "PFVR.background.delivery.\(UUID().uuidString)"
        let defaults = try XCTUnwrap(UserDefaults(suiteName: suite))
        defer { defaults.removePersistentDomain(forName: suite) }
        let scheduler = Scheduler()
        let notCalled = expectation(description: "Locked work must not start")
        notCalled.isInverted = true
        let refresh = BackgroundRefresh(defaults: defaults, scheduler: scheduler, testing: { false }, work: { notCalled.fulfill(); return true })
        let locked = Handle()
        refresh.receive(locked)
        XCTAssertEqual(locked.completions, [false])
        defaults.set(true, forKey: BackgroundRefreshPolicy.activationKey)
        defaults.set(false, forKey: BackgroundRefreshPolicy.preferenceKey)
        let disabled = Handle()
        refresh.receive(disabled)
        XCTAssertEqual(disabled.completions, [false])
        XCTAssertTrue(scheduler.dates.isEmpty)
        await fulfillment(of: [notCalled], timeout: 0.05)
    }

    func testSuccessfulDeliveryReschedulesAndCompletesExactlyOnce() async throws {
        let suite = "PFVR.background.success.\(UUID().uuidString)"
        let defaults = try XCTUnwrap(UserDefaults(suiteName: suite))
        defer { defaults.removePersistentDomain(forName: suite) }
        defaults.set(true, forKey: BackgroundRefreshPolicy.activationKey)
        let scheduler = Scheduler()
        let handle = Handle()
        let completed = expectation(description: "Refresh completed")
        handle.completed = { completed.fulfill() }
        let refresh = BackgroundRefresh(defaults: defaults, scheduler: scheduler, testing: { false }, work: { true })
        refresh.receive(handle)
        await fulfillment(of: [completed], timeout: 2)
        XCTAssertEqual(handle.completions, [true])
        XCTAssertNil(handle.expirationHandler)
        XCTAssertEqual(scheduler.dates.count, 1)
    }

    func testExpirationCancelsOperationAndPreventsLateSuccess() async {
        let started = expectation(description: "Operation started")
        let cancelled = expectation(description: "Operation observed cancellation")
        let handle = Handle()
        let run = BackgroundRefreshRun(handle: handle, work: {
            started.fulfill()
            do { try await Task.sleep(for: .seconds(60)) }
            catch { cancelled.fulfill() }
            return true // Even a late success cannot override expired completion.
        })
        run.start()
        await fulfillment(of: [started], timeout: 2)
        XCTAssertNotNil(handle.expirationHandler)
        handle.expirationHandler?()
        await fulfillment(of: [cancelled], timeout: 2)
        run.expire()
        XCTAssertEqual(handle.completions, [false])
        XCTAssertTrue(run.isFinished)
        XCTAssertNil(handle.expirationHandler)
    }

    func testDisablingBackgroundRefreshCancelsAnActiveDelivery() async throws {
        let suite = "PFVR.background.disable.\(UUID().uuidString)"
        let defaults = try XCTUnwrap(UserDefaults(suiteName: suite))
        defer { defaults.removePersistentDomain(forName: suite) }
        defaults.set(true, forKey: BackgroundRefreshPolicy.activationKey)
        let started = expectation(description: "Operation started")
        let cancelled = expectation(description: "Disabled operation cancelled")
        let scheduler = Scheduler()
        let handle = Handle()
        let refresh = BackgroundRefresh(defaults: defaults, scheduler: scheduler, testing: { false }, work: {
            started.fulfill()
            do { try await Task.sleep(for: .seconds(60)) }
            catch { cancelled.fulfill() }
            return true
        })
        refresh.receive(handle)
        await fulfillment(of: [started], timeout: 2)
        defaults.set(false, forKey: BackgroundRefreshPolicy.preferenceKey)
        refresh.schedule(unlocked: true, enabled: false)
        await fulfillment(of: [cancelled], timeout: 2)
        XCTAssertEqual(handle.completions, [false])
        XCTAssertEqual(scheduler.dates.count, 1)
        XCTAssertEqual(scheduler.cancellations, 2)
    }

    func testSourceFailureReportsFailedCompletionWithoutDoubleCallback() async {
        let handle = Handle()
        let completed = expectation(description: "Failure completed")
        handle.completed = { completed.fulfill() }
        let run = BackgroundRefreshRun(handle: handle, work: { false })
        run.start()
        await fulfillment(of: [completed], timeout: 2)
        run.expire()
        XCTAssertEqual(handle.completions, [false])
    }

    func testCacheClearCancellationWaitsForExistingWritersToDrain() async {
        let gate = DrainGate()
        let started = expectation(description: "Background writer running")
        let completed = expectation(description: "OS task promptly cancelled")
        let handle = Handle()
        handle.completed = { completed.fulfill() }
        let run = BackgroundRefreshRun(handle: handle, work: {
            started.fulfill()
            await gate.wait() // Models parsing/cache write already in progress when cancellation arrives.
            return true
        })
        run.start()
        await fulfillment(of: [started], timeout: 2)
        let cancellation = Task {
            await run.cancelAndWait()
            await gate.markReturned()
        }
        await fulfillment(of: [completed], timeout: 2)
        let returnedBeforeDrain = await gate.waiterReturned
        XCTAssertFalse(returnedBeforeDrain)
        await gate.open()
        await cancellation.value
        let returnedAfterDrain = await gate.waiterReturned
        XCTAssertTrue(returnedAfterDrain)
        XCTAssertEqual(handle.completions, [false])
    }
}
