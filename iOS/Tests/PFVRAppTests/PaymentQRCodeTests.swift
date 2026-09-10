import XCTest
import UIKit
import Vision
import PFVRCore
@testable import PFVR

/// Decodes the actual generated and PNG-serialized image using Apple's independent Vision reader.
/// This proves machine readability and payload preservation, not acceptance by a particular bank.
/// Vision barcode API: https://developer.apple.com/videos/play/wwdc2021/10041/
@MainActor
final class PaymentQRCodeTests: XCTestCase {
    func testCancelledAndLocalShareActionsDoNotArmPaymentConfirmation() {
        let extensionType = UIActivity.ActivityType(rawValue: "ch.pfvr.fixture.bank-import")
        XCTAssertFalse(PaymentSharePolicy.shouldConfirm(activityType: extensionType, completed: false))
        XCTAssertFalse(PaymentSharePolicy.shouldConfirm(activityType: nil, completed: true))
        for action in [UIActivity.ActivityType.copyToPasteboard, .saveToCameraRoll, .print, .assignToContact,
                       UIActivity.ActivityType(rawValue: "com.apple.CloudDocsUI.AddToiCloudDrive"),
                       UIActivity.ActivityType(rawValue: "com.apple.DocumentManagerUICore.SaveToFiles")] {
            XCTAssertFalse(PaymentSharePolicy.shouldConfirm(activityType: action, completed: true), action.rawValue)
        }
        // An accepted external import may ask the user; it still does not mean the payment succeeded.
        XCTAssertTrue(PaymentSharePolicy.shouldConfirm(activityType: extensionType, completed: true))
        var state = CashCartState(quantities: ["soft_5dl": 1])
        state.markExternalHandoff(fromCart: true, launched: true)
        XCTAssertTrue(state.paymentConfirmationPending)
        XCTAssertFalse(state.isEmpty)
    }

    func testRenderedSwissQRRoundTripsThroughPNGAndVision() throws {
        for raw in ["", "0.01", "12,50", "99999.99"] {
            let amount = try PaymentAmount(raw: raw)
            let image = try PaymentQRCode.image(amount: amount)
            let png = try XCTUnwrap(image.pngData())
            let decodedImage = try XCTUnwrap(UIImage(data: png)?.cgImage)
            let request = VNDetectBarcodesRequest()
            request.symbologies = [.qr]
            try VNImageRequestHandler(cgImage: decodedImage, options: [:]).perform([request])
            let barcode = try XCTUnwrap(request.results?.first, "Unreadable rendered Swiss QR for amount \(raw)")
            let payload = try XCTUnwrap(barcode.payloadStringValue)
            XCTAssertEqual(payload, PaymentQR.payload(amount: amount), "PNG must preserve the complete Swiss payment payload")
            let fields = payload.components(separatedBy: "\r\n")
            XCTAssertEqual(fields.count, 34)
            guard fields.count > 19 else { continue }
            XCTAssertEqual(fields[3], "CH5800769440901312001")
            XCTAssertEqual(fields[5], "Pontonierfahrverein Rheinfelden")
            XCTAssertEqual(fields[18], amount.qrField)
            XCTAssertEqual(fields[19], "CHF")
            if raw == "12,50" {
                let attachment = XCTAttachment(data: png, uniformTypeIdentifier: "public.png")
                attachment.name = "Swiss-QR-12.50-roundtrip"
                attachment.lifetime = .keepAlways
                add(attachment)
            }
        }
    }
}
