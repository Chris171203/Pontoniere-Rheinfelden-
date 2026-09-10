import Foundation

public struct PaymentAmount: Equatable, Sendable {
    public enum AmountError: Error, Equatable { case invalidAmount }
    public let money: CashMoney?
    public var qrField: String { money?.decimalString ?? "" }
    public var fileName: String { "PFVR_\(money?.decimalString ?? "offen")CHF.png" }

    public init(raw: String) throws {
        let text = raw.trimmingCharacters(in: .whitespacesAndNewlines).replacingOccurrences(of: ",", with: ".")
        if text.isEmpty { money = nil; return }
        // Decimal(string:) alone accepts a valid prefix followed by garbage. Match the complete input first.
        guard text.range(of: "^(?:[0-9]+(?:\\.[0-9]*)?|\\.[0-9]+)$", options: .regularExpression) != nil,
              let value = Decimal(string: text, locale: Locale(identifier: "en_US_POSIX")),
              value >= 0, value <= 100_000 else { throw AmountError.invalidAmount }
        var scaled = value * 100
        var rounded = Decimal()
        NSDecimalRound(&rounded, &scaled, 0, .plain)
        let cents = NSDecimalNumber(decimal: rounded).int64Value
        if value > 0 && cents == 0 { throw AmountError.invalidAmount }
        money = cents == 0 ? nil : CashMoney(cents: cents)
    }

    public init(money: CashMoney?) throws {
        guard let money else { self.money = nil; return }
        guard (0...10_000_000).contains(money.cents) else { throw AmountError.invalidAmount }
        self.money = money.cents == 0 ? nil : money
    }
}

public enum PaymentDetails {
    // Existing Android 0.12.6 public payment configuration; remote administration is a separately planned feature.
    public static let iban = "CH58 0076 9440 9013 1200 1"
    public static let payee = "Pontonierfahrverein Rheinfelden"
    public static let note = "Konsumation Vereinsbeiz"
    public static let twintURL = URL(string: "https://www.pfvr.ch/vereinsbeiz-zahlung/")!
    public static let twintQRURL = URL(string: "https://www.pfvr.ch/wp-content/uploads/Seiten/vereinsbeiz_zahlung/Twint_QR.pdf")!
    public static func shareText(amount: PaymentAmount) -> String {
        [payee, iban, note, amount.money.map { "CHF \($0.decimalString)" }].compactMap { $0 }.joined(separator: "\n")
    }
}

public enum PaymentQR {
    /// Swiss QR payload matches Android, including CRLF, reserved blank fields and trailing terminators.
    public static func payload(amount: PaymentAmount) -> String {
        ["SPC", "0200", "1", PaymentDetails.iban.replacingOccurrences(of: " ", with: ""),
         "S", PaymentDetails.payee, "Rheinweg", "", "4310", "Rheinfelden", "CH",
         "", "", "", "", "", "", "",
         amount.qrField, "CHF",
         "", "", "", "", "", "", "",
         "NON", "", PaymentDetails.note, "EPD", "", "", ""].joined(separator: "\r\n")
    }
}
