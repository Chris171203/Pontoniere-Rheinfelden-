import AppKit
import Foundation

// Bounded preview of named, synthetic test screenshots. Full-resolution originals stay in xcresult/artifacts.
let args = CommandLine.arguments
guard args.count == 4 else { fatalError("Usage: ios-visual-evidence.swift screenshots-directory output.jpg profile") }
let root = URL(fileURLWithPath: args[1], isDirectory: true).standardizedFileURL
let output = URL(fileURLWithPath: args[2])
let profile = args[3]
let fm = FileManager.default
let files = (fm.enumerator(at: root, includingPropertiesForKeys: nil)?.allObjects as? [URL] ?? []).sorted { $0.path < $1.path }
var metadata: [String: String] = [:]
func collect(_ value: Any, context: String = "") {
    if let object = value as? [String: Any] {
        let strings = object.values.compactMap { $0 as? String }
        let combined = context + " " + strings.joined(separator: " ")
        for file in strings where ["png", "jpg", "jpeg"].contains(URL(fileURLWithPath: file).pathExtension.lowercased()) {
            metadata[URL(fileURLWithPath: file).lastPathComponent] = combined
        }
        for child in object.values where !(child is String) { collect(child, context: combined) }
    } else if let array = value as? [Any] {
        for child in array { collect(child, context: context) }
    }
}
for file in files where file.pathExtension == "json" {
    if let data = try? Data(contentsOf: file), let object = try? JSONSerialization.jsonObject(with: data) { collect(object) }
}
let desired = profile == "tablet"
    ? ["system-share-ui", "system-calendar-ui"]
    : ["screen-home", "screen-river", "screen-events", "screen-internal", "screen-cash", "screen-club", "payment-swiss-qr", "river-graph-2091", "river-graph-2289"]
var chosen: [(String, NSImage)] = []
for name in desired {
    guard let file = files.first(where: { candidate in
        let label = metadata[candidate.lastPathComponent] ?? candidate.lastPathComponent
        return ["png", "jpg", "jpeg"].contains(candidate.pathExtension.lowercased()) && label.contains(name)
    }), let image = NSImage(contentsOf: file) else { continue }
    chosen.append((name, image))
    if chosen.count == 9 { break }
}
guard !chosen.isEmpty else {
    print("PFVR_VISUAL_EVIDENCE_UNAVAILABLE: no named synthetic screenshot mappings in export manifest")
    for file in files where file.lastPathComponent == "manifest.json" {
        if let contents = try? String(contentsOf: file, encoding: .utf8) { print(String(contents.prefix(4000))) }
    }
    exit(0)
}
let columns = 3
let cellWidth = 300
let cellHeight = 680
let rows = (chosen.count + columns - 1) / columns
let width = columns * cellWidth
let height = rows * cellHeight
guard let bitmap = NSBitmapImageRep(bitmapDataPlanes: nil, pixelsWide: width, pixelsHigh: height, bitsPerSample: 8, samplesPerPixel: 4, hasAlpha: true, isPlanar: false, colorSpaceName: .deviceRGB, bytesPerRow: 0, bitsPerPixel: 0), let context = NSGraphicsContext(bitmapImageRep: bitmap) else { fatalError("Preview bitmap unavailable") }
NSGraphicsContext.saveGraphicsState()
NSGraphicsContext.current = context
NSColor(calibratedWhite: 0.91, alpha: 1).setFill()
NSRect(x: 0, y: 0, width: CGFloat(width), height: CGFloat(height)).fill()
for (index, item) in chosen.enumerated() {
    let left = CGFloat((index % columns) * cellWidth)
    let bottom = CGFloat(height - ((index / columns) + 1) * cellHeight)
    let title = profile + " · " + item.0
    (title as NSString).draw(in: NSRect(x: left + 8, y: bottom + CGFloat(cellHeight - 27), width: CGFloat(cellWidth - 16), height: 20), withAttributes: [.font: NSFont.systemFont(ofSize: 12), .foregroundColor: NSColor.black])
    let available = NSSize(width: CGFloat(cellWidth - 16), height: CGFloat(cellHeight - 42))
    let factor = min(available.width / item.1.size.width, available.height / item.1.size.height)
    let size = NSSize(width: item.1.size.width * factor, height: item.1.size.height * factor)
    let rect = NSRect(x: left + (CGFloat(cellWidth) - size.width) / 2, y: bottom + 8 + available.height - size.height, width: size.width, height: size.height)
    item.1.draw(in: rect, from: .zero, operation: .sourceOver, fraction: 1)
}
context.flushGraphics()
NSGraphicsContext.restoreGraphicsState()
var jpeg: Data?
for quality in [0.78, 0.65, 0.5, 0.35, 0.2] {
    guard let data = bitmap.representation(using: .jpeg, properties: [.compressionFactor: quality]) else { continue }
    if data.count <= 180_000 { jpeg = data; break }
}
guard let jpeg else { print("PFVR_VISUAL_EVIDENCE_UNAVAILABLE: preview exceeds 180 KB limit"); exit(0) }
try jpeg.write(to: output, options: .atomic)
print("PFVR_VISUAL_EVIDENCE: profile=\(profile), images=\(chosen.count), bytes=\(jpeg.count), names=\(chosen.map { $0.0 }.joined(separator: ","))")
print("PFVR_VISUAL_EVIDENCE_BASE64_BEGIN")
print(jpeg.base64EncodedString())
print("PFVR_VISUAL_EVIDENCE_BASE64_END")
