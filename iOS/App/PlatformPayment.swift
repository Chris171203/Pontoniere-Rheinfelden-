import SwiftUI
import CoreImage
import UIKit
import PFVRCore

enum PaymentQRCodeError: Error { case unavailable, writeFailed }

enum PaymentSharePolicy {
    static func shouldConfirm(activityType: UIActivity.ActivityType?, completed: Bool) -> Bool {
        guard completed, let activityType else { return false }
        let utilities: Set<String> = [UIActivity.ActivityType.copyToPasteboard.rawValue, UIActivity.ActivityType.saveToCameraRoll.rawValue, UIActivity.ActivityType.print.rawValue, UIActivity.ActivityType.assignToContact.rawValue, UIActivity.ActivityType.addToReadingList.rawValue, "com.apple.CloudDocsUI.AddToiCloudDrive", "com.apple.DocumentManagerUICore.SaveToFiles", "com.apple.UIKit.activity.MarkupAsPDF"]
        return !utilities.contains(activityType.rawValue)
    }
}

@MainActor
enum PaymentQRCode {
    /// Swiss QR with an integer pixel scale, four-module quiet zone and centred Swiss cross.
    static func image(amount: PaymentAmount) throws -> UIImage {
        guard let filter = CIFilter(name: "CIQRCodeGenerator") else { throw PaymentQRCodeError.unavailable }
        filter.setValue(Data(PaymentQR.payload(amount: amount).utf8), forKey: "inputMessage")
        filter.setValue("M", forKey: "inputCorrectionLevel")
        guard let qr = filter.outputImage else { throw PaymentQRCodeError.unavailable }
        let extent = qr.extent.integral
        guard let bitmap = CIContext().createCGImage(qr, from: extent) else { throw PaymentQRCodeError.unavailable }
        let scale = max(1, floor(1000 / (extent.width + 8)))
        let border = 4 * scale
        let codeSize = extent.width * scale
        let size = codeSize + 2 * border
        let format = UIGraphicsImageRendererFormat(); format.scale = 1; format.opaque = true
        return UIGraphicsImageRenderer(size: CGSize(width: size, height: size), format: format).image { renderer in
            let context = renderer.cgContext
            UIColor.white.setFill(); context.fill(CGRect(x: 0, y: 0, width: size, height: size))
            context.interpolationQuality = .none
            UIImage(cgImage: bitmap).draw(in: CGRect(x: border, y: border, width: codeSize, height: codeSize))
            let mark = codeSize * 7 / 46
            let center = size / 2
            UIColor.black.setFill(); context.fill(CGRect(x: center - mark / 2, y: center - mark / 2, width: mark, height: mark))
            UIColor.white.setFill()
            context.fill(CGRect(x: center - mark * 0.11, y: center - mark * 0.32, width: mark * 0.22, height: mark * 0.64))
            context.fill(CGRect(x: center - mark * 0.32, y: center - mark * 0.11, width: mark * 0.64, height: mark * 0.22))
        }
    }
    static func file(image: UIImage, amount: PaymentAmount) throws -> URL {
        guard let data = image.pngData() else { throw PaymentQRCodeError.writeFailed }
        let file = FileManager.default.temporaryDirectory.appendingPathComponent(amount.fileName)
        try data.write(to: file, options: [.atomic, .completeFileProtectionUntilFirstUserAuthentication])
        return file
    }
}

struct ActivitySheet: UIViewControllerRepresentable {
    let items: [Any]
    var completed: ((UIActivity.ActivityType?, Bool) -> Void)?
    func makeUIViewController(context: Context) -> UIActivityViewController {
        let controller = UIActivityViewController(activityItems: items, applicationActivities: nil)
        controller.view.accessibilityIdentifier = "system.share"
        controller.completionWithItemsHandler = { activity, success, _, _ in completed?(activity, success) }
        return controller
    }
    func updateUIViewController(_ controller: UIActivityViewController, context: Context) {}
}

struct PaymentPresentation: Identifiable {
    let id = UUID()
    let amount: PaymentAmount
    let fromCart: Bool
}

struct PaymentView: View {
    @EnvironmentObject private var state: AppState
    @Environment(\.dismiss) private var dismiss
    let presentation: PaymentPresentation
    @State private var qr: UIImage?
    @State private var exportFile: URL?
    @State private var sharing = false
    @State private var bankShare = false
    @State private var error = false
    var body: some View {
        NavigationStack {
            PageScroll {
                PFVRCard {
                    if let qr {
                        Image(uiImage: qr).resizable().interpolation(.none).scaledToFit()
                            .padding(4).background(.white).clipShape(RoundedRectangle(cornerRadius: 8))
                            .accessibilityLabel("Swiss QR").accessibilityIdentifier("payment.qr.image")
                    } else if error {
                        Text(state.ui("Swiss QR konnte nicht erzeugt werden.")).foregroundStyle(.red)
                    } else { ProgressView() }
                    Text(presentation.amount.money?.formatted ?? state.ui("Betrag offen · in Banking-App eingeben")).font(.title2.bold())
                    Text(PaymentDetails.payee).font(.subheadline)
                    Text(PaymentDetails.iban).font(.subheadline).textSelection(.enabled)
                    Text(PaymentDetails.note).font(.caption).foregroundStyle(.secondary)
                    Button { share(toBank: true) } label: {
                        Label(state.ui("An Banking-App teilen"), systemImage: "square.and.arrow.up")
                            .frame(maxWidth: .infinity, minHeight: 36)
                    }.buttonStyle(.borderedProminent).disabled(qr == nil).accessibilityIdentifier("payment.share")
                    Button { share(toBank: false) } label: {
                        Label(state.ui("QR speichern / exportieren"), systemImage: "square.and.arrow.down")
                    }.buttonStyle(.bordered).disabled(qr == nil)
                    Text(state.ui("Die Banking-App muss den Swiss QR aus einem Bild importieren können. Eine erfolgte Übergabe bestätigt keine Zahlung."))
                        .font(.caption).foregroundStyle(.secondary)
                }
            }
            .navigationTitle(state.ui("Bankzahlung · Swiss QR"))
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .confirmationAction) {
                    Button(state.ui("Schliessen")) { dismiss() }.accessibilityIdentifier("payment.done")
                }
            }
        }
        .task {
            do { qr = try PaymentQRCode.image(amount: presentation.amount) }
            catch { self.error = true }
        }
        .sheet(isPresented: $sharing) {
            if let exportFile {
                ActivitySheet(items: bankShare ? [exportFile, PaymentDetails.shareText(amount: presentation.amount)] : [exportFile]) { activity, success in
                    // Local save/copy/print targets do not constitute an external banking handoff.
                    let handedOff = bankShare && PaymentSharePolicy.shouldConfirm(activityType: activity, completed: success)
                    state.paymentHandoff(fromCart: presentation.fromCart, launched: handedOff)
                    if handedOff { dismiss() }
                }
            }
        }
    }
    private func share(toBank: Bool) {
        guard let qr else { return }
        do {
            exportFile = try PaymentQRCode.file(image: qr, amount: presentation.amount)
            bankShare = toBank
            sharing = true
        } catch { self.error = true }
    }
}
