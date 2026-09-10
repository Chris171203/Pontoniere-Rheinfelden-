import XCTest
@testable import PFVRCore

final class CashPaymentTests: XCTestCase {
    func testBundledAndroidCatalogUsesExactCentsAndDeposit() throws {
        let catalog = try CashCatalog.loadBundled()
        XCTAssertEqual(catalog.currency, "CHF")
        XCTAssertEqual(catalog.validFrom, "2026")
        XCTAssertEqual(catalog.categories.map(\.id), ["drinks", "food", "celebrations"])
        XCTAssertEqual(catalog.categories.flatMap(\.items).count, 14)
        XCTAssertEqual(catalog.item(id: "beer_5dl")?.price.cents, 450)
        XCTAssertEqual(catalog.item(id: "keg_deposit")?.deposit, true)
        // Two 4.50 beers + an 8.00 meal + a 50.00 returnable deposit.
        XCTAssertEqual(catalog.total(quantities: ["beer_5dl": 2, "food_other": 1, "keg_deposit": 1]).cents, 6_700)
        XCTAssertEqual(catalog.total(quantities: ["beer_5dl": -1, "removed-item": 20]).cents, 0)
        XCTAssertEqual(catalog.total(quantities: ["beer_5dl": Int.max]).cents, 44_550)
    }

    func testCatalogRejectsUnsupportedSchemaDuplicateIDsAndFractionalCentPrices() {
        func source(price: String = "3.00", itemTail: String = "", schema: Int = 1) -> Data {
            Data("""
            {"schemaVersion":\(schema),"currency":"CHF","title":"Test","validFrom":"2026","categories":[{"id":"drinks","label":"Trinken","items":[{"id":"one","name":"One","price":\(price)}\(itemTail)]}]}
            """.utf8)
        }
        XCTAssertThrowsError(try CashCatalog(data: source(schema: 99)))
        XCTAssertThrowsError(try CashCatalog(data: source(price: "0.001")))
        XCTAssertThrowsError(try CashCatalog(data: source(price: "-1")))
        XCTAssertThrowsError(try CashCatalog(data: source(itemTail: ",{\"id\":\"one\",\"name\":\"Duplicate\",\"price\":4}")))
    }

    func testAmountParsingAndDecimalRounding() throws {
        XCTAssertNil(try PaymentAmount(raw: " ").money)
        XCTAssertNil(try PaymentAmount(raw: "0,00").money)
        XCTAssertEqual(try PaymentAmount(raw: " 12,50 ").qrField, "12.50")
        XCTAssertEqual(try PaymentAmount(raw: "1.005").money?.cents, 101)
        XCTAssertEqual(try PaymentAmount(raw: ".50").money?.cents, 50)
        XCTAssertEqual(try PaymentAmount(raw: "100000").qrField, "100000.00")
        XCTAssertEqual(try PaymentAmount(raw: "12.50").fileName, "PFVR_12.50CHF.png")
        XCTAssertEqual(try PaymentAmount(raw: "").fileName, "PFVR_offenCHF.png")
        for bad in ["NaN", "Infinity", "-1", "100000.01", "12.50CHF", "12/50", "1e2", "0.001", "1,000.00", "1\n2"] {
            XCTAssertThrowsError(try PaymentAmount(raw: bad), "Must reject \(bad)")
        }
        XCTAssertThrowsError(try PaymentAmount(money: CashMoney(cents: -1)))
        XCTAssertThrowsError(try PaymentAmount(money: CashMoney(cents: 10_000_001)))
    }

    func testMoneyFormattingHandlesNegativeAndSmallAmounts() {
        XCTAssertEqual(CashMoney(cents: 1).decimalString, "0.01")
        XCTAssertEqual(CashMoney(cents: 105).formatted, "CHF 1.05")
        XCTAssertEqual(CashMoney(cents: -101).decimalString, "-1.01")
        XCTAssertEqual(CashMoney(cents: Int64.min).decimalString, "-92233720368547758.08")
    }

    func testSwissQRHasStructuredCreditorAndAndroidFieldPositions() throws {
        let payload = PaymentQR.payload(amount: try PaymentAmount(raw: "12.50"))
        let fields = payload.components(separatedBy: "\r\n")
        XCTAssertEqual(fields.count, 34)
        XCTAssertEqual(Array(fields[0...10]), ["SPC", "0200", "1", "CH5800769440901312001", "S", "Pontonierfahrverein Rheinfelden", "Rheinweg", "", "4310", "Rheinfelden", "CH"])
        XCTAssertTrue(fields[11...17].allSatisfy(\.isEmpty))
        XCTAssertEqual(fields[18], "12.50")
        XCTAssertEqual(fields[19], "CHF")
        XCTAssertTrue(fields[20...26].allSatisfy(\.isEmpty))
        XCTAssertEqual(Array(fields[27...33]), ["NON", "", "Konsumation Vereinsbeiz", "EPD", "", "", ""])
        XCTAssertEqual(PaymentQR.payload(amount: try PaymentAmount(raw: "")).components(separatedBy: "\r\n")[18], "")
        XCTAssertTrue(payload.hasSuffix("EPD\r\n\r\n\r\n"))
    }

    func testCartAndPendingConfirmationSurviveStoreRecreation() throws {
        let suite = "PFVR.tests.cash.\(UUID().uuidString)"
        let defaults = try XCTUnwrap(UserDefaults(suiteName: suite))
        defer { defaults.removePersistentDomain(forName: suite) }
        let first = CashCartStore(defaults: defaults)
        first.setQuantity(2, for: "beer_5dl")
        first.setQuantity(3, for: "food_other")
        first.markExternalHandoff(fromCart: true, launched: true)
        let reopened = CashCartStore(defaults: defaults)
        XCTAssertEqual(reopened.state.quantities, ["beer_5dl": 2, "food_other": 3])
        XCTAssertTrue(reopened.state.paymentConfirmationPending)
        reopened.confirmPayment(successful: false)
        XCTAssertEqual(reopened.state.itemCount, 5)
        XCTAssertFalse(CashCartStore(defaults: defaults).state.paymentConfirmationPending)
        reopened.markExternalHandoff(fromCart: true, launched: true)
        reopened.confirmPayment(successful: true)
        XCTAssertTrue(CashCartStore(defaults: defaults).state.isEmpty)
        XCTAssertFalse(CashCartStore(defaults: defaults).state.paymentConfirmationPending)
    }

    func testOnlyAcceptedCartHandoffArmsConfirmationAndManualClearRemovesIt() {
        var state = CashCartState(quantities: ["beer_5dl": 1])
        state.markExternalHandoff(fromCart: false, launched: true)
        XCTAssertFalse(state.paymentConfirmationPending)
        state.markExternalHandoff(fromCart: true, launched: false)
        XCTAssertFalse(state.paymentConfirmationPending)
        state.confirmPayment(successful: true)
        XCTAssertEqual(state.itemCount, 1)
        state.markExternalHandoff(fromCart: true, launched: true)
        XCTAssertTrue(state.paymentConfirmationPending)
        state.clear()
        XCTAssertFalse(state.paymentConfirmationPending)
        state.markExternalHandoff(fromCart: true, launched: true)
        XCTAssertFalse(state.paymentConfirmationPending)
    }

    func testDecodedStateSanitizesCorruptQuantitiesAndOrphanPendingFlag() throws {
        let raw = Data("""
        {"quantities":{"beer_5dl":999,"food_other":-1,"bad=id":3,"":4},"paymentConfirmationPending":true}
        """.utf8)
        let decoded = try JSONDecoder().decode(CashCartState.self, from: raw)
        XCTAssertEqual(decoded.quantities, ["beer_5dl": 99])
        XCTAssertTrue(decoded.paymentConfirmationPending)
        let empty = try JSONDecoder().decode(CashCartState.self, from: Data("{\"paymentConfirmationPending\":true}".utf8))
        XCTAssertTrue(empty.isEmpty)
        XCTAssertFalse(empty.paymentConfirmationPending)
    }
}
