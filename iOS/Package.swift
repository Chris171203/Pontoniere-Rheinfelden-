// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "PFVRCore",
    platforms: [.iOS(.v17), .macOS(.v13)],
    products: [.library(name: "PFVRCore", targets: ["PFVRCore"])],
    dependencies: [.package(url: "https://github.com/scinfu/SwiftSoup.git", exact: "2.8.8")],
    targets: [
        .target(name: "PFVRCore", dependencies: [.product(name: "SwiftSoup", package: "SwiftSoup")], path: "Sources/PFVRCore", resources: [.process("Resources")]),
        .testTarget(name: "PFVRCoreTests", dependencies: ["PFVRCore"], path: "Tests/PFVRCoreTests")
    ],
    swiftLanguageVersions: [.v5]
)
