// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "PFVRCore",
    platforms: [.iOS(.v17), .macOS(.v13)],
    products: [.library(name: "PFVRCore", targets: ["PFVRCore"])],
    targets: [
        .target(name: "PFVRCore", path: "Sources/PFVRCore", resources: [.process("Resources")]),
        .testTarget(name: "PFVRCoreTests", dependencies: ["PFVRCore"], path: "Tests/PFVRCoreTests")
    ],
    swiftLanguageVersions: [.v5]
)
