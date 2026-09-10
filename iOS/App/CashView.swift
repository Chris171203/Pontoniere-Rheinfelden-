import SwiftUI
import PFVRCore

struct CashView: View {
    @EnvironmentObject private var state: AppState
    @Environment(\.openURL) private var openURL
    @State private var amountText = ""
    @State private var payment: PaymentPresentation?
    @State private var invalidAmount = false
    @State private var confirmClear = false
    var body: some View {
        PageScroll {
            ForEach(state.visibleTiles(.cash), id: \.id) { tile in
                switch tile.id {
                case "cash_cart": cartTile
                case "cash_drinks", "cash_food", "cash_celebrations":
                    if let category = state.catalog?.categories.first(where: { "cash_" + $0.id == tile.id }) {
                        CashCategoryTile(category: category)
                    }
                case "cash_free_amount": freeAmountTile
                case "cash_twint": twintTile
                case "cash_payment_details": paymentDetailsTile
                default: EmptyView()
                }
            }
            if state.catalog == nil { Text(state.ui("Die Preisliste konnte nicht geladen werden.")).foregroundStyle(.secondary) }
        }
        .accessibilityIdentifier("screen.cash")
        .sheet(item: $payment, onDismiss: { state.checkPaymentConfirmation() }) { PaymentView(presentation: $0) }
        .alert(state.ui("Ungültiger Betrag"), isPresented: $invalidAmount) {
            Button("OK", role: .cancel) {}
        } message: { Text(state.ui("Bitte einen gültigen CHF-Betrag eingeben oder Feld leer/0 für offenen Betrag lassen.")) }
        .confirmationDialog(state.ui("Warenkorb leeren?"), isPresented: $confirmClear, titleVisibility: .visible) {
            Button(state.ui("Warenkorb leeren"), role: .destructive) { state.clearCart() }.accessibilityIdentifier("cart.clear.confirm")
            Button(state.ui("Abbrechen"), role: .cancel) {}
        }
    }
    private var cartTile: some View {
        PFVRCard(state.ui("Warenkorb")) {
            if state.cart.isEmpty {
                Text(state.ui("Der Warenkorb ist leer.")).foregroundStyle(.secondary).font(.subheadline)
            } else {
                ForEach(state.catalog?.categories.flatMap(\.items).filter { state.quantity($0) > 0 } ?? []) { item in
                    HStack {
                        Text("\(state.quantity(item)) × " + item.displayName).font(.subheadline)
                        Spacer()
                        Text(CashMoney(cents: item.price.cents * Int64(state.quantity(item))).formatted).font(.subheadline).monospacedDigit()
                    }
                }
                Divider()
            }
            HStack {
                Text(state.ui("Total")).font(.headline)
                Spacer()
                Text(state.total.formatted).font(.title2.bold()).monospacedDigit()
            }
            .accessibilityElement(children: .ignore)
            .accessibilityLabel(state.ui("Total"))
            .accessibilityValue(state.total.formatted)
            .accessibilityIdentifier("cart.total")
            HStack {
                Button { showCartQR() } label: {
                    Label(state.ui("Swiss QR"), systemImage: "qrcode").frame(maxWidth: .infinity, minHeight: 34)
                }.buttonStyle(.borderedProminent).disabled(state.cart.isEmpty).accessibilityIdentifier("payment.qr")
                Button { openTwint(fromCart: true) } label: {
                    Text("TWINT").frame(maxWidth: .infinity, minHeight: 34)
                }.buttonStyle(.bordered).disabled(state.cart.isEmpty).accessibilityIdentifier("payment.twint")
            }
            Button(state.ui("Warenkorb leeren"), role: .destructive) { confirmClear = true }
                .font(.caption).disabled(state.cart.isEmpty).accessibilityIdentifier("cart.clear")
        }
    }
    private var freeAmountTile: some View {
        PFVRCard(state.ui("Freier Betrag")) {
            TextField(state.ui("Betrag in CHF"), text: $amountText)
                .keyboardType(.decimalPad).textFieldStyle(.roundedBorder).accessibilityIdentifier("payment.free.amount")
            HStack {
                Button(state.ui("Swiss QR")) {
                    do { payment = PaymentPresentation(amount: try PaymentAmount(raw: amountText), fromCart: false) }
                    catch { invalidAmount = true }
                }.buttonStyle(.borderedProminent)
                Button("TWINT") { openTwint(fromCart: false) }.buttonStyle(.bordered)
            }
        }
    }
    private var twintTile: some View {
        PFVRCard("TWINT") {
            Text(state.ui("Für Zahlung auf demselben Handy: Betrag übernehmen und auf der PFVR-Seite den fünfstelligen TWINT-Code erzeugen."))
                .font(.subheadline).foregroundStyle(.secondary)
            Button(state.ui("TWINT-Code erzeugen")) { openTwint(fromCart: false) }
            Link(state.ui("Vereins-TWINT-QR öffnen"), destination: PaymentDetails.twintQRURL)
        }
    }
    private var paymentDetailsTile: some View {
        PFVRCard(state.ui("Zahlungsdaten")) {
            Text(PaymentDetails.payee).font(.subheadline)
            Text(PaymentDetails.iban).font(.headline).textSelection(.enabled)
            Text(PaymentDetails.note).font(.caption).foregroundStyle(.secondary)
            Button(state.ui("IBAN kopieren")) { UIPasteboard.general.string = PaymentDetails.iban.replacingOccurrences(of: " ", with: "") }
            if let date = state.catalog?.validFrom { Text(state.ui("Preisliste Vereinsbeiz · Stand") + " " + date).font(.caption2).foregroundStyle(.secondary) }
        }
    }
    private func showCartQR() {
        guard !state.cart.isEmpty else { return }
        do { payment = PaymentPresentation(amount: try PaymentAmount(money: state.total), fromCart: true) }
        catch { invalidAmount = true }
    }
    private func openTwint(fromCart: Bool) {
        do {
            let amount: PaymentAmount
            if fromCart { amount = try PaymentAmount(money: state.total) }
            else { amount = try PaymentAmount(raw: amountText) }
            if let money = amount.money { UIPasteboard.general.string = money.decimalString }
            openURL(PaymentDetails.twintURL) { accepted in state.paymentHandoff(fromCart: fromCart, launched: accepted) }
        } catch { invalidAmount = true }
    }
}

struct CashCategoryTile: View {
    @EnvironmentObject private var state: AppState
    let category: CashCategory
    var body: some View {
        PFVRCard(state.ui(category.label)) {
            ForEach(category.items) { item in
                HStack(spacing: 8) {
                    VStack(alignment: .leading, spacing: 4) {
                        Text(item.displayName).font(.subheadline)
                        Text(item.price.formatted).font(.caption).foregroundStyle(.secondary)
                    }.frame(maxWidth: .infinity, alignment: .leading)
                    Button { state.setQuantity(state.quantity(item) - 1, for: item) } label: {
                        Image(systemName: "minus.circle.fill").font(.title2).frame(width: 44, height: 44)
                    }.disabled(state.quantity(item) == 0).accessibilityLabel(state.ui("Entfernen") + " " + item.displayName).accessibilityIdentifier("cart.remove.\(item.id)")
                    Text("\(state.quantity(item))").font(.subheadline.monospacedDigit()).frame(minWidth: 20)
                    Button { state.setQuantity(state.quantity(item) + 1, for: item) } label: {
                        Image(systemName: "plus.circle.fill").font(.title2).frame(width: 44, height: 44)
                    }.disabled(state.quantity(item) >= 99).accessibilityLabel(state.ui("Hinzufügen") + " " + item.displayName).accessibilityIdentifier("cart.add.\(item.id)")
                }
                if item.id != category.items.last?.id { Divider() }
            }
        }
    }
}
