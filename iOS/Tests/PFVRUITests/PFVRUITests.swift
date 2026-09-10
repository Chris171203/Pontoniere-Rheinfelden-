import XCTest

/// Runs against a real simulator app. Public fixtures are deterministic and all
/// launch overrides are compiled out of Release. No personal access link is used.
@MainActor
final class PFVRUITests: XCTestCase {
    private var app: XCUIApplication!

    override func setUpWithError() throws {
        continueAfterFailure = false
        XCUIDevice.shared.orientation = .portrait
        app = XCUIApplication()
    }

    override func tearDownWithError() throws {
        if let run = testRun, run.hasSucceeded == false, app.state == .runningForeground {
            capture("failure")
        }
        app.terminate()
    }

    private func launch(reset: Bool = true, unlocked: Bool = true, pending: Bool = false) {
        app.launchArguments = ["-ui-testing", "-AppleLanguages", "(de)", "-AppleLocale", "de_CH"]
        if reset { app.launchArguments.append("-ui-test-reset") }
        if unlocked { app.launchArguments.append("-ui-test-unlocked") }
        if pending { app.launchArguments.append("-ui-test-pending-payment") }
        app.launch()
        if unlocked && !pending {
            XCTAssertTrue(app.buttons["tab.home"].waitForExistence(timeout: 10))
        }
    }

    private func element(_ id: String) -> XCUIElement {
        app.descendants(matching: .any).matching(identifier: id).firstMatch
    }

    private func reveal(_ item: XCUIElement, file: StaticString = #filePath, line: UInt = #line) {
        for _ in 0..<6 {
            if item.exists && item.isHittable { return }
            let scroll = app.scrollViews.firstMatch
            if scroll.exists { scroll.swipeUp() } else { app.swipeUp() }
        }
        XCTAssertTrue(item.exists && item.isHittable, "Missing/unreachable element: \(item)", file: file, line: line)
    }

    private func tap(_ id: String, file: StaticString = #filePath, line: UInt = #line) {
        let button = app.buttons[id]
        reveal(button, file: file, line: line)
        button.tap()
    }

    private func capture(_ name: String) {
        let attachment = XCTAttachment(screenshot: app.screenshot())
        attachment.name = name
        attachment.lifetime = .keepAlways
        add(attachment)
    }

    private func cashTotal() -> String {
        let total = element("cart.total")
        XCTAssertTrue(total.waitForExistence(timeout: 5))
        return (total.value as? String) ?? total.label
    }

    private func addDrink() {
        tap("tab.cash")
        tap("cart.add.soft_5dl")
        // The cart is pinned before the categories, so return to its visible position.
        app.scrollViews.firstMatch.swipeDown()
        XCTAssertTrue(cashTotal().contains("3.00"), "One soft drink must total CHF 3.00")
    }

    func testFirstLaunchRemainsLockedAfterInvalidCode() {
        launch(unlocked: false)
        let code = app.secureTextFields["gate.code"]
        reveal(code)
        code.tap()
        code.typeText("WRONG-CODE\n")
        XCTAssertTrue(element("gate.error").waitForExistence(timeout: 5))
        XCTAssertFalse(app.buttons["tab.home"].exists)
        capture("landing-invalid-code")
        app.terminate()
        launch(reset: false, unlocked: false)
        reveal(app.secureTextFields["gate.code"])
        XCTAssertTrue(app.secureTextFields["gate.code"].exists)
        XCTAssertFalse(app.buttons["tab.home"].exists)
    }

    func testEveryTabIsReachableAndCapturesScreenshots() {
        launch()
        XCTAssertTrue(app.staticTexts["Vereinstraining"].firstMatch.waitForExistence(timeout: 5))
        for name in ["home", "river", "events", "internal", "cash", "club"] {
            let tab = app.buttons["tab.\(name)"]
            XCTAssertTrue(tab.isHittable, "The \(name) tab must be directly reachable")
            XCTAssertGreaterThanOrEqual(tab.frame.width, 44)
            XCTAssertGreaterThanOrEqual(tab.frame.height, 44)
            tab.tap()
            XCTAssertTrue(element("screen.\(name)").waitForExistence(timeout: 5))
            capture("screen-\(name)")
        }
    }

    func testRiverRendersActualFixtureReadingsAndBothGraphs() {
        launch()
        tap("tab.river")
        let rheinfelden = element("river.station.2091")
        let basel = element("river.station.2289")
        XCTAssertTrue(rheinfelden.waitForExistence(timeout: 5))
        XCTAssertTrue(rheinfelden.label.contains("268."), "Rheinfelden must show its real fixture level, not an empty-state dash")
        XCTAssertTrue(basel.label.contains("245."), "Basel must show metres above sea level")
        for station in ["2091", "2289"] {
            let graph = element("river.graph.\(station)")
            reveal(graph)
            capture("river-graph-\(station)")
        }
    }

    func testCartSurvivesProcessRestartAndCanBeClearedExplicitly() {
        launch()
        addDrink()
        let savedTotal = cashTotal()
        app.terminate()
        launch(reset: false, unlocked: false)
        tap("tab.cash")
        XCTAssertEqual(cashTotal(), savedTotal)
        capture("cart-restored")
        tap("cart.clear")
        tap("cart.clear.confirm")
        XCTAssertTrue(cashTotal().contains("0.00"))
        app.terminate()
        launch(reset: false, unlocked: false)
        tap("tab.cash")
        XCTAssertTrue(cashTotal().contains("0.00"))
    }

    func testReturnedPaymentRequiresExplicitConfirmation() {
        launch()
        addDrink()
        let savedTotal = cashTotal()
        app.terminate()
        launch(reset: false, pending: true)
        XCTAssertTrue(app.buttons["payment.confirm.no"].waitForExistence(timeout: 10))
        capture("payment-confirmation")
        tap("payment.confirm.no")
        tap("tab.cash")
        XCTAssertEqual(cashTotal(), savedTotal, "An unsuccessful/unconfirmed payment must preserve the cart")
        app.terminate()
        launch(reset: false, unlocked: false)
        XCTAssertFalse(app.buttons["payment.confirm.yes"].exists, "No must consume the pending question")
        tap("tab.cash")
        XCTAssertEqual(cashTotal(), savedTotal)
        app.terminate()
        launch(reset: false, pending: true)
        XCTAssertTrue(app.buttons["payment.confirm.yes"].waitForExistence(timeout: 10))
        tap("payment.confirm.yes")
        tap("tab.cash")
        XCTAssertTrue(cashTotal().contains("0.00"), "Only explicit successful confirmation clears the cart")
    }

    func testViewingPaymentQRCodePreservesCart() {
        launch()
        addDrink()
        let savedTotal = cashTotal()
        tap("payment.qr")
        XCTAssertTrue(element("payment.qr.image").waitForExistence(timeout: 5))
        capture("payment-swiss-qr")
        tap("payment.done")
        XCTAssertEqual(cashTotal(), savedTotal)
        app.terminate()
        launch(reset: false, unlocked: false)
        XCTAssertFalse(app.buttons["payment.confirm.yes"].exists)
        tap("tab.cash")
        XCTAssertEqual(cashTotal(), savedTotal)
    }


    /// Opens Apple's real controllers and cancels them without sharing or saving.
    /// This also runs alone on iPad to exercise native modal/popover presentation.
    func testSystemShareAndCalendarEditorsCanBeCancelled() {
        executionTimeAllowance = 90 // Includes first simulator launch and two native controller transitions.
        launch()
        addDrink()
        let savedTotal = cashTotal()
        tap("payment.qr")
        XCTAssertTrue(element("payment.qr.image").waitForExistence(timeout: 5))
        tap("payment.share")
        // Observed in the actual iPad AX tree: UIKit presents these native
        // controls in a remote activity view and omits our container's ID.
        let activities = app.collectionViews["activityCollectionView"]
        let shareAppeared = activities.waitForExistence(timeout: 10)
        let copyAction = activities.cells.matching(NSPredicate(format: "label IN %@", ["Kopieren", "Copy"])).firstMatch
        let copyAppeared = copyAction.waitForExistence(timeout: 5)
        capture("system-share-ui")
        if !shareAppeared || !copyAppeared {
            print("PFVR_SYSTEM_SHARE_AX_BEGIN\n" + app.debugDescription + "\nPFVR_SYSTEM_SHARE_AX_END")
        }
        XCTAssertTrue(shareAppeared && copyAppeared, "The real native share actions must be presented")
        XCTAssertTrue(copyAction.isHittable)
        let closeShare = app.buttons["header.closeButton"]
        XCTAssertTrue(closeShare.waitForExistence(timeout: 5))
        XCTAssertTrue(closeShare.isHittable)
        closeShare.tap()
        waitUntilGone(activities)
        XCTAssertTrue(element("payment.qr.image").waitForExistence(timeout: 5))
        tap("payment.done")
        XCTAssertEqual(cashTotal(), savedTotal)
        XCTAssertFalse(app.buttons["payment.confirm.yes"].exists, "Cancelling share must not arm a payment question")

        tap("tab.events")
        tap("event.ui-training")
        tap("event.calendar")
        // The source title must be editable in Apple's real editor; the
        // underlying event detail has only static text and no title field.
        let titleField = app.textFields.matching(NSPredicate(format: "value == %@", "Vereinstraining")).firstMatch
        let calendarAppeared = titleField.waitForExistence(timeout: 10)
        capture("system-calendar-ui")
        if !calendarAppeared {
            print("PFVR_SYSTEM_CALENDAR_AX_BEGIN\n" + app.debugDescription + "\nPFVR_SYSTEM_CALENDAR_AX_END")
        }
        XCTAssertTrue(calendarAppeared, "The native calendar editor must contain the source title")
        let cancelCalendar = app.navigationBars.buttons.matching(NSPredicate(format: "label IN %@", ["Abbrechen", "Cancel"])).firstMatch
        XCTAssertTrue(cancelCalendar.waitForExistence(timeout: 5))
        XCTAssertTrue(cancelCalendar.isHittable)
        cancelCalendar.tap()
        waitUntilGone(titleField)
        XCTAssertTrue(element("event.detail").waitForExistence(timeout: 5))
        app.terminate()
        launch(reset: false, unlocked: false)
        tap("tab.cash")
        XCTAssertEqual(cashTotal(), savedTotal)
        XCTAssertFalse(app.buttons["payment.confirm.yes"].exists)
    }

    private func waitUntilGone(_ nativeElement: XCUIElement, file: StaticString = #filePath, line: UInt = #line) {
        let dismissed = XCTNSPredicateExpectation(predicate: NSPredicate(format: "exists == false"), object: nativeElement)
        XCTAssertEqual(XCTWaiter.wait(for: [dismissed], timeout: 5), .completed, "The native system editor must actually close", file: file, line: line)
    }

    func testEventDetailPreservesSourceTextAndExposesActions() {
        launch()
        tap("tab.events")
        tap("event.ui-training")
        XCTAssertTrue(element("event.detail").waitForExistence(timeout: 5))
        XCTAssertTrue(app.staticTexts["Vereinstraining"].firstMatch.exists)
        for action in ["event.share", "event.calendar", "event.route"] {
            XCTAssertTrue(element(action).exists, "Missing event action: \(action)")
        }
        capture("event-detail")
    }

    func testLanguageChoiceSurvivesRestartAndLeavesEventTitleUnchanged() {
        launch()
        tap("settings.open")
        tap("settings.language.gsw")
        capture("settings-swiss-german")
        tap("settings.done")
        let dialectLabel = app.buttons["tab.events"].label
        XCTAssertTrue(dialectLabel.contains("Termin"))
        XCTAssertFalse(dialectLabel.contains("Termine"), "The app-owned tab label must actually switch language")
        app.terminate()
        launch(reset: false, unlocked: false)
        XCTAssertEqual(app.buttons["tab.events"].label, dialectLabel)
        tap("tab.events")
        XCTAssertTrue(app.buttons["event.ui-training"].label.contains("Vereinstraining"))
        let sourceTitle = app.buttons["event.ui-source-text"]
        reveal(sourceTitle)
        XCTAssertTrue(sourceTitle.label.contains("Warenkorb"), "A source title matching the app dictionary must remain unchanged")
        XCTAssertFalse(sourceTitle.label.contains("Warenchorb"))
        capture("events-swiss-german")
        tap("settings.open")
        let language = app.buttons["settings.language.gsw"]
        XCTAssertTrue(language.isSelected, "The saved Swiss German choice must remain selected")
    }

    func testTileVisibilityAndOrderPersistWhileCartRemainsPinned() {
        launch()
        tap("settings.open")
        tap("settings.tiles")
        let threeDays = app.switches["tiles.toggle.home_weather_3day"]
        XCTAssertTrue(threeDays.waitForExistence(timeout: 5))
        reveal(threeDays)
        XCTAssertEqual(threeDays.value as? String, "1")
        threeDays.tap()
        XCTAssertEqual(threeDays.value as? String, "0")
        tap("tiles.up.home_weather_3day")
        XCTAssertLessThan(threeDays.frame.minY, app.switches["tiles.toggle.home_weather"].frame.minY)
        capture("tiles-customized")
        app.terminate()
        launch(reset: false, unlocked: false)
        tap("settings.open")
        tap("settings.tiles")
        XCTAssertTrue(threeDays.waitForExistence(timeout: 5))
        XCTAssertEqual(threeDays.value as? String, "0")
        XCTAssertLessThan(threeDays.frame.minY, app.switches["tiles.toggle.home_weather"].frame.minY)
        tap("tiles.area.cash")
        XCTAssertFalse(app.switches["tiles.toggle.cash_cart"].exists)
        XCTAssertFalse(app.buttons["tiles.up.cash_cart"].exists)
        XCTAssertFalse(app.buttons["tiles.down.cash_cart"].exists)
        capture("tiles-cart-pinned")
    }


    func testClearingPublicCachePreservesCartAndLanguage() {
        launch()
        addDrink()
        let savedTotal = cashTotal()
        tap("settings.open")
        tap("settings.language.gsw")
        tap("settings.cache.clear")
        tap("settings.cache.clear.confirm")
        tap("settings.done")
        XCTAssertEqual(cashTotal(), savedTotal)
        XCTAssertFalse(app.buttons["tab.events"].label.contains("Termine"))
        app.terminate()
        launch(reset: false, unlocked: false)
        tap("tab.cash")
        XCTAssertEqual(cashTotal(), savedTotal)
        XCTAssertFalse(app.buttons["payment.confirm.yes"].exists)
        XCTAssertFalse(app.buttons["tab.events"].label.contains("Termine"))
    }

}
